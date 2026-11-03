package com.colegio.service;

import com.colegio.entity.*;
import com.colegio.repository.*;
import com.colegio.security.JwtUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Lógica de autenticación y de manejo de sesiones.
 * Un JWT por sí solo no se puede "cancelar", así que aquí está todo lo que
 * hace falta para cerrar sesión, renovar el token y revocar sesiones.
 */
@Service
public class AuthService {

    // Tras 5 intentos fallidos en 15 minutos la cuenta se bloquea temporalmente
    private static final int MAX_FALLIDOS = 5;
    private static final int VENTANA_MINUTOS = 15;
    // Aunque el usuario siga renovando el token, la sesión no puede pasar de 8 horas
    private static final int MAX_SESION_HORAS = 8;
    private static final Set<String> ROLES = Set.of("DISCIPLINA", "DOCENTE", "RECTOR", "ESTUDIANTE", "ACUDIENTE");
    // Mismo mensaje para "usuario no existe" y "contraseña mala", para no revelar qué usuarios existen
    private static final String CRED_INVALIDAS = "Credenciales incorrectas";

    private final UsuarioRepository usuarioRepo;
    private final EstudianteRepository estudianteRepo;
    private final AcudienteRepository acudienteRepo;
    private final ImplicadoRepository implicadoRepo;
    private final ReporteRepository reporteRepo;
    private final IntentoLoginRepository intentoRepo;
    private final TokenRevocadoRepository revocadoRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepo, EstudianteRepository estudianteRepo,
                       AcudienteRepository acudienteRepo, ImplicadoRepository implicadoRepo,
                       ReporteRepository reporteRepo, IntentoLoginRepository intentoRepo,
                       TokenRevocadoRepository revocadoRepo,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepo = usuarioRepo;
        this.estudianteRepo = estudianteRepo;
        this.acudienteRepo = acudienteRepo;
        this.implicadoRepo = implicadoRepo;
        this.reporteRepo = reporteRepo;
        this.intentoRepo = intentoRepo;
        this.revocadoRepo = revocadoRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ───────────── Login ─────────────

    public Map<String, Object> login(String username, String password, String ip) {
        if (username == null || password == null) throw new IllegalArgumentException(CRED_INVALIDAS);
        verificarBloqueo(username);

        Optional<Usuario> opt = usuarioRepo.findByUsername(username);
        if (opt.isEmpty() || !passwordEncoder.matches(password, opt.get().getPassword())) {
            registrarIntento(username, false, ip);
            throw new IllegalArgumentException(CRED_INVALIDAS);
        }
        registrarIntento(username, true, ip);
        return construirRespuesta(opt.get());
    }

    public Map<String, Object> loginEstudiante(String numeroDocumento, String password, String ip) {
        if (numeroDocumento == null || password == null) throw new IllegalArgumentException(CRED_INVALIDAS);
        verificarBloqueo(numeroDocumento);

        Optional<Estudiante> optEst = estudianteRepo.findByNumeroDocumento(numeroDocumento);
        if (optEst.isEmpty()) {
            registrarIntento(numeroDocumento, false, ip);
            throw new IllegalArgumentException(CRED_INVALIDAS);
        }
        Estudiante est = optEst.get();

        Optional<Usuario> existente = usuarioRepo.findByUsername(numeroDocumento);
        Usuario usuario;
        if (existente.isPresent()) {
            // Ya tiene cuenta: se valida contra el hash guardado (así, si cambió su contraseña, la inicial deja de servir)
            if (!passwordEncoder.matches(password, existente.get().getPassword())) {
                registrarIntento(numeroDocumento, false, ip);
                throw new IllegalArgumentException(CRED_INVALIDAS);
            }
            usuario = existente.get();
        } else {
            // Primer ingreso: la contraseña inicial es el documento o la fecha de nacimiento (yyyyMMdd)
            boolean ok = numeroDocumento.equals(password) ||
                    (est.getFechaNacimiento() != null &&
                     est.getFechaNacimiento().toString().replace("-", "").equals(password));
            if (!ok) {
                registrarIntento(numeroDocumento, false, ip);
                throw new IllegalArgumentException(CRED_INVALIDAS);
            }
            Usuario u = new Usuario();
            u.setUsername(numeroDocumento);
            u.setPassword(passwordEncoder.encode(numeroDocumento));
            u.setRol("ESTUDIANTE");
            u.setNombreCompleto(est.getNombres() + " " + est.getApellidos());
            u.setEstudianteId(est.getId());
            usuario = usuarioRepo.save(u);
        }
        registrarIntento(numeroDocumento, true, ip);
        return construirRespuesta(usuario);
    }

      public Map<String, Object> registrar(Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String rol = body.get("rol") == null ? null : body.get("rol").toString().toUpperCase();
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username es obligatorio");
        if (password == null || password.length() < 8) throw new IllegalArgumentException("La contraseña debe tener mínimo 8 caracteres");
        if (rol == null || !ROLES.contains(rol)) throw new IllegalArgumentException("Rol inválido. Use: " + ROLES);
        if (usuarioRepo.findByUsername(username).isPresent()) throw new IllegalArgumentException("El username ya existe");
        String email = normalizarEmail(body.get("email"));
        if (email != null && usuarioRepo.findByEmailIgnoreCase(email).isPresent())
            throw new IllegalArgumentException("Ese correo ya está vinculado a otro usuario");

        Usuario u = new Usuario();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setRol(rol);
        u.setNombreCompleto((String) body.get("nombreCompleto"));
        if (body.get("estudianteId") != null)
            estudianteRepo.findById(body.get("estudianteId").toString()).ifPresent(e -> u.setEstudianteId(e.getId()));
        if (body.get("acudienteId") != null)
            acudienteRepo.findById(body.get("acudienteId").toString()).ifPresent(a -> u.setAcudienteId(a.getId()));
        usuarioRepo.save(u);
        return Map.of("mensaje", "Usuario registrado correctamente", "rol", rol);
    }

    // ───────────── Manejo de sesión ─────────────

    /** Cierra la sesión: el token queda en la lista de revocados hasta que venza. */
    public void logout(String token) {
        revocadoRepo.save(new TokenRevocado(jwtUtil.extractJti(token), jwtUtil.extractExpiration(token)));
    }

    /**
     * Cambia el token por uno nuevo sin pedir la contraseña otra vez.
     * El token viejo se revoca y la sesión no puede durar más de MAX_SESION_HORAS.
     */
    public Map<String, Object> renovar(String token) {
        Usuario u = usuarioRepo.findByUsername(jwtUtil.extractUsername(token))
                .orElseThrow(() -> new IllegalStateException("Sesión inválida"));
        Instant inicio = jwtUtil.extractInicio(token);
        if (Duration.between(inicio, Instant.now()).toHours() >= MAX_SESION_HORAS)
            throw new IllegalStateException("La sesión alcanzó su duración máxima. Inicia sesión de nuevo");

        logout(token);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("token", jwtUtil.generateToken(u.getUsername(), u.getRol(), inicio));
        resp.put("rol", u.getRol());
        return resp;
    }

    /** Cambio de contraseña. Al cambiarla se invalidan TODOS los tokens anteriores del usuario. */
    public void cambiarPassword(String username, String actual, String nueva) {
        Usuario u = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (actual == null || !passwordEncoder.matches(actual, u.getPassword()))
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        if (nueva == null || nueva.length() < 8)
            throw new IllegalArgumentException("La nueva contraseña debe tener mínimo 8 caracteres");
        if (nueva.equals(actual))
            throw new IllegalArgumentException("La nueva contraseña debe ser distinta a la actual");

        u.setPassword(passwordEncoder.encode(nueva));
        u.setTokensValidosDesde(Instant.now());
        usuarioRepo.save(u);
    }

    /**
     * Invalida todos los tokens emitidos hasta este momento para ese usuario.
     * Sirve para "cerrar sesión en todos los dispositivos" o si se sospecha que robaron un token.
     */
    public void cerrarTodasLasSesiones(String username) {
        Usuario u = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        u.setTokensValidosDesde(Instant.now());
        usuarioRepo.save(u);
    }
        // ───────────── Inicio de sesión con Google / GitHub (OAuth2) ─────────────

    /**
     * Se llama cuando Google o GitHub ya confirmaron quién es la persona.
     * Regla de seguridad: NO se crean cuentas nuevas. Solo entra quien ya tiene un usuario
     * en el sistema con ese mismo correo (y el proveedor debe haberlo verificado).
     * Si no, cualquier persona con una cuenta de Google podría entrar al colegio.
     * Devuelve nuestro propio token JWT; el rol sale de la base de datos.
     */
    public String loginConOAuth(String proveedor, String email, boolean emailVerificado, String ip) {
        String etiqueta = proveedor + ":" + (email == null ? "sin-correo" : email);
        if (email == null || !emailVerificado) {
            registrarIntento(etiqueta, false, ip);
            throw new IllegalArgumentException("email_no_verificado");
        }
        Optional<Usuario> opt = usuarioRepo.findByEmailIgnoreCase(email);
        if (opt.isEmpty()) {
            registrarIntento(etiqueta, false, ip);
            throw new IllegalArgumentException("cuenta_no_vinculada");
        }
        Usuario u = opt.get();
        registrarIntento(u.getUsername(), true, ip);
        return jwtUtil.generateToken(u.getUsername(), u.getRol());
    }

    /** Datos del usuario que inició sesión (lo mismo que devuelve el login, pero sin token). */
    public Map<String, Object> perfil(String username) {
        Usuario u = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Map<String, Object> resp = construirRespuesta(u);
        resp.remove("token");
        return resp;
    }

    /** El coordinador vincula (o desvincula, con correo vacío) el correo de un usuario. */
    public void vincularEmail(String username, Object emailCrudo) {
        Usuario u = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        String email = normalizarEmail(emailCrudo);
        if (email != null) {
            Optional<Usuario> otro = usuarioRepo.findByEmailIgnoreCase(email);
            if (otro.isPresent() && !otro.get().getUsername().equals(username))
                throw new IllegalArgumentException("Ese correo ya está vinculado a otro usuario");
        }
        u.setEmail(email);
        usuarioRepo.save(u);
    }

    // Deja el correo en minúsculas y sin espacios; vacío = null; si no parece un correo, error
    private String normalizarEmail(Object crudo) {
        if (crudo == null) return null;
        String e = crudo.toString().trim().toLowerCase();
        if (e.isEmpty()) return null;
        if (!e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) throw new IllegalArgumentException("Correo inválido");
        return e;
    }

    // ───────────── Helpers ─────────────

    private void verificarBloqueo(String username) {
        long fallidos = intentoRepo.countByUsernameAndExitosoFalseAndFechaHoraAfter(
                username, LocalDateTime.now().minusMinutes(VENTANA_MINUTOS));
        if (fallidos >= MAX_FALLIDOS)
            throw new IllegalStateException("Demasiados intentos fallidos. Intenta de nuevo en " + VENTANA_MINUTOS + " minutos");
    }

    private void registrarIntento(String username, boolean exitoso, String ip) {
        IntentoLogin i = new IntentoLogin();
        i.setUsername(username);
        i.setExitoso(exitoso);
        i.setFechaHora(LocalDateTime.now());
        i.setIp(ip);
        intentoRepo.save(i);
    }

    // Arma la respuesta del login. Si es estudiante o acudiente, adjunta sus reportes para el dashboard
    private Map<String, Object> construirRespuesta(Usuario usuario) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("token", jwtUtil.generateToken(usuario.getUsername(), usuario.getRol()));
        resp.put("rol", usuario.getRol());
        resp.put("username", usuario.getUsername());
        resp.put("nombreCompleto", usuario.getNombreCompleto());

        if ("ESTUDIANTE".equals(usuario.getRol()) && usuario.getEstudianteId() != null)
            resp.put("misReportes", filas(implicadoRepo.findByEstudianteId(usuario.getEstudianteId()), false));
        if ("ACUDIENTE".equals(usuario.getRol()) && usuario.getAcudienteId() != null)
            resp.put("reportesAcudidos", filas(implicadoRepo.findByAcudienteId(usuario.getAcudienteId()), true));
        return resp;
    }

    private List<Map<String, Object>> filas(List<Implicado> imps, boolean conEstudiante) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Implicado i : imps) {
            if (i.getReporteId() == null) continue;
            Optional<Reporte> r = reporteRepo.findById(i.getReporteId());
            if (r.isEmpty()) continue; // el reporte pudo haberse eliminado
            Map<String, Object> m = new LinkedHashMap<>();
            if (conEstudiante) {
                String nombre = "Desconocido";
                if (i.getEstudianteId() != null)
                    nombre = estudianteRepo.findById(i.getEstudianteId())
                            .map(e -> e.getNombres() + " " + e.getApellidos()).orElse("Desconocido");
                m.put("estudiante", nombre);
            }
            m.put("reporteId", r.get().getId());
            m.put("rol", i.getRol());
            m.put("tipoFalta", r.get().getTipoFalta());
            m.put("estado", r.get().getEstado());
            m.put("fecha", r.get().getFechaHora() == null ? null : r.get().getFechaHora().toString());
            out.add(m);
        }
        return out;
    }
   

}
