package com.colegio.service;

import com.colegio.entity.*;
import com.colegio.repository.*;
import com.colegio.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final EstudianteRepository estudianteRepo;
    private final AcudienteRepository acudienteRepo;
    private final ImplicadoRepository implicadoRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepo, EstudianteRepository estudianteRepo,
                       AcudienteRepository acudienteRepo, ImplicadoRepository implicadoRepo,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepo    = usuarioRepo;
        this.estudianteRepo = estudianteRepo;
        this.acudienteRepo  = acudienteRepo;
        this.implicadoRepo  = implicadoRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil        = jwtUtil;
    }

    public Map<String, Object> login(String username, String password) {
        Usuario usuario = usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!passwordEncoder.matches(password, usuario.getPassword()))
            throw new IllegalArgumentException("Contraseña incorrecta");

        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol());
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("token", token);
        resp.put("rol", usuario.getRol());
        resp.put("username", usuario.getUsername());
        resp.put("nombreCompleto", usuario.getNombreCompleto());

        // Si es estudiante adjuntar sus reportes
        if ("ESTUDIANTE".equals(usuario.getRol()) && usuario.getEstudiante() != null) {
            Long estudianteId = usuario.getEstudiante().getId();
            List<Implicado> participaciones = implicadoRepo.findByEstudianteId(estudianteId);
            resp.put("misReportes", participaciones.stream()
                    .map(i -> Map.of(
                            "reporteId", i.getReporte().getId(),
                            "rol", i.getRol(),
                            "tipoFalta", i.getReporte().getTipoFalta(),
                            "estado", i.getReporte().getEstado(),
                            "fecha", i.getReporte().getFechaHora().toString()
                    )).toList());
        }

        // Si es acudiente adjuntar reportes de sus acudidos
        if ("ACUDIENTE".equals(usuario.getRol()) && usuario.getAcudiente() != null) {
            Long acudienteId = usuario.getAcudiente().getId();
            List<Implicado> participaciones = implicadoRepo.findByAcudienteId(acudienteId);
            resp.put("reportesAcudidos", participaciones.stream()
                    .map(i -> Map.of(
                            "estudiante", i.getEstudiante() != null
                                    ? i.getEstudiante().getNombres() + " " + i.getEstudiante().getApellidos()
                                    : "Desconocido",
                            "reporteId", i.getReporte().getId(),
                            "rol", i.getRol(),
                            "tipoFalta", i.getReporte().getTipoFalta(),
                            "estado", i.getReporte().getEstado(),
                            "fecha", i.getReporte().getFechaHora().toString()
                    )).toList());
        }

        return resp;
    }

    @Transactional
    public Map<String, Object> registrar(Map<String, Object> body) {
        if (usuarioRepo.findByUsername((String) body.get("username")).isPresent())
            throw new IllegalArgumentException("El username ya existe");

        Usuario u = new Usuario();
        u.setUsername((String) body.get("username"));
        u.setPassword(passwordEncoder.encode((String) body.get("password")));
        u.setRol((String) body.get("rol"));
        u.setNombreCompleto((String) body.get("nombreCompleto"));

        if (body.get("estudianteId") != null)
            estudianteRepo.findById(Long.valueOf(body.get("estudianteId").toString()))
                    .ifPresent(u::setEstudiante);
        if (body.get("acudienteId") != null)
            acudienteRepo.findById(Long.valueOf(body.get("acudienteId").toString()))
                    .ifPresent(u::setAcudiente);

        usuarioRepo.save(u);
        return Map.of("mensaje", "Usuario registrado correctamente", "rol", u.getRol());
    }

    @Transactional
    public Map<String, Object> loginEstudiante(String numeroDocumento, String password) {
        // Buscar estudiante por documento
        Estudiante estudiante = estudianteRepo.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado"));

        // Verificar que la contraseña coincide con el documento (o fecha de nacimiento)
        if (!numeroDocumento.equals(password) &&
                (estudiante.getFechaNacimiento() == null ||
                        !estudiante.getFechaNacimiento().toString().replace("-","").equals(password))) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        // Crear o recuperar usuario automáticamente
        Usuario usuario = usuarioRepo.findByUsername(numeroDocumento).orElseGet(() -> {
            Usuario u = new Usuario();
            u.setUsername(numeroDocumento);
            u.setPassword(passwordEncoder.encode(numeroDocumento));
            u.setRol("ESTUDIANTE");
            u.setNombreCompleto(estudiante.getNombres() + " " + estudiante.getApellidos());
            u.setEstudiante(estudiante);
            return usuarioRepo.save(u);
        });

        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol());
        List<Implicado> participaciones = implicadoRepo.findByEstudianteId(estudiante.getId());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("token", token);
        resp.put("rol", "ESTUDIANTE");
        resp.put("username", numeroDocumento);
        resp.put("nombreCompleto", usuario.getNombreCompleto());
        resp.put("misReportes", participaciones.stream()
                .map(i -> Map.of(
                        "reporteId", i.getReporte().getId(),
                        "rol", i.getRol(),
                        "tipoFalta", i.getReporte().getTipoFalta(),
                        "estado", i.getReporte().getEstado(),
                        "fecha", i.getReporte().getFechaHora().toString()
                )).toList());
        return resp;
    }

}