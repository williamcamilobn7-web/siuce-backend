package com.colegio.service;

import com.colegio.entity.*;
import com.colegio.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lógica de los reportes disciplinarios: clasificación automática, asignación de EPS y policía,
 * firma del rector y generación del formato SIUCE.
 */
@Service
public class ReporteService {

    // Roles que pueden ver cualquier reporte
    private static final List<String> ROLES_STAFF = List.of("DISCIPLINA", "DOCENTE", "RECTOR");

    private final ReporteRepository reporteRepo;
    private final ImplicadoRepository implicadoRepo;
    private final EstudianteRepository estudianteRepo;
    private final EntidadSaludRepository entidadSaludRepo;
    private final PoliciaRepository policiaRepo;
    private final UsuarioRepository usuarioRepo;

    public ReporteService(ReporteRepository reporteRepo, ImplicadoRepository implicadoRepo,
                          EstudianteRepository estudianteRepo, EntidadSaludRepository entidadSaludRepo,
                          PoliciaRepository policiaRepo, UsuarioRepository usuarioRepo) {
        this.reporteRepo = reporteRepo;
        this.implicadoRepo = implicadoRepo;
        this.estudianteRepo = estudianteRepo;
        this.entidadSaludRepo = entidadSaludRepo;
        this.policiaRepo = policiaRepo;
        this.usuarioRepo = usuarioRepo;
    }

    public List<Reporte> listarTodos() { return reporteRepo.findAll(); }

    // Además del reporte, se cargan la EPS y la policía asignadas para mostrarlas en el detalle
    public Optional<Reporte> buscarPorId(String id) {
        return reporteRepo.findById(id).map(r -> {
            if (r.getEntidadSaludId() != null) entidadSaludRepo.findById(r.getEntidadSaludId()).ifPresent(r::setEntidadSalud);
            if (r.getPoliciaId() != null) policiaRepo.findById(r.getPoliciaId()).ifPresent(r::setPolicia);
            return r;
        });
    }

    /** Estudiantes y acudientes solo pueden ver reportes donde están implicados. */
    /**
     * Un estudiante solo puede ver los reportes donde aparece como implicado, y un acudiente
     * los de sus acudidos. El personal del colegio ve todos.
     */
    public boolean puedeVer(String username, String rol, String reporteId) {
        if (ROLES_STAFF.contains(rol)) return true;
        Usuario u = usuarioRepo.findByUsername(username).orElse(null);
        if (u == null) return false;
        List<Implicado> imps = implicadoRepo.findByReporteId(reporteId);
        if ("ESTUDIANTE".equals(rol))
            return u.getEstudianteId() != null &&
                   imps.stream().anyMatch(i -> u.getEstudianteId().equals(i.getEstudianteId()));
        if ("ACUDIENTE".equals(rol))
            return u.getAcudienteId() != null &&
                   imps.stream().anyMatch(i -> u.getAcudienteId().equals(i.getAcudienteId()));
        return false;
    }

    // El tipo de falta lo decide el sistema, no quien escribe el reporte
    public Reporte crear(Map<String, Object> body) {
        Reporte reporte = new Reporte();
        reporte.setFechaHora(LocalDateTime.now());
        reporte.setLugar((String) body.get("lugar"));
        reporte.setDescripcionHecho((String) body.get("descripcionHecho"));
        if (body.get("latitud") != null) reporte.setLatitud(Double.valueOf(body.get("latitud").toString()));
        if (body.get("longitud") != null) reporte.setLongitud(Double.valueOf(body.get("longitud").toString()));

        String tipo = clasificarTipo((String) body.get("descripcionHecho"));
        reporte.setTipoFalta(tipo);
        reporte.setEstado("PENDIENTE");

        if ("TIPO_III".equals(tipo)) asignarEntidades(reporte);
        return reporteRepo.save(reporte);
    }

    /**
     * A los reportes Tipo III se les asigna la EPS y la policía más cercanas al lugar del hecho.
     * Si el reporte no trae coordenadas se usa la primera disponible.
     */
    private void asignarEntidades(Reporte reporte) {
        Double lat = reporte.getLatitud();
        Double lon = reporte.getLongitud();
        if (lat != null && lon != null) {
            entidadSaludRepo.findAll().stream()
                .filter(e -> e.getLatitud() != null && e.getLongitud() != null)
                .min(Comparator.comparingDouble(e -> distancia(lat, lon, e.getLatitud(), e.getLongitud())))
                .ifPresent(e -> reporte.setEntidadSaludId(e.getId()));
            policiaRepo.findAll().stream()
                .filter(p -> p.getLatitud() != null && p.getLongitud() != null)
                .min(Comparator.comparingDouble(p -> distancia(lat, lon, p.getLatitud(), p.getLongitud())))
                .ifPresent(p -> reporte.setPoliciaId(p.getId()));
        }
        // Sin coordenadas (o sin entidades con coordenadas): se asigna la primera disponible
        if (reporte.getEntidadSaludId() == null)
            entidadSaludRepo.findAll().stream().findFirst().ifPresent(e -> reporte.setEntidadSaludId(e.getId()));
        if (reporte.getPoliciaId() == null)
            policiaRepo.findAll().stream().findFirst().ifPresent(p -> reporte.setPoliciaId(p.getId()));
    }

    // Si al editar la descripción el reporte pasa a Tipo III, también se le asignan EPS y policía
    public Reporte actualizar(String id, Map<String, Object> body) {
        Reporte reporte = reporteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + id));
        if (!"PENDIENTE".equals(reporte.getEstado()))
            throw new IllegalStateException("Solo se pueden actualizar reportes en estado PENDIENTE");
        if (body.containsKey("lugar")) reporte.setLugar((String) body.get("lugar"));
        if (body.containsKey("descripcionHecho")) {
            String desc = (String) body.get("descripcionHecho");
            reporte.setDescripcionHecho(desc);
            String tipo = clasificarTipo(desc);
            reporte.setTipoFalta(tipo);
            if ("TIPO_III".equals(tipo)) asignarEntidades(reporte);
        }
        return reporteRepo.save(reporte);
    }

    // Los Tipo III no se pueden eliminar. Al borrar un reporte también se borran sus implicados
    public void eliminar(String id) {
        Reporte reporte = reporteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + id));
        if ("TIPO_III".equals(reporte.getTipoFalta()))
            throw new IllegalStateException("No se pueden eliminar reportes de Tipo III");
        implicadoRepo.deleteAll(implicadoRepo.findByReporteId(id));
        reporteRepo.deleteById(id);
    }

    // Solo Tipo III, y una sola vez. Se guarda el nombre real del usuario que firmó
    public Reporte firmar(String id, String username) {
        Reporte reporte = reporteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + id));
        if (!"TIPO_III".equals(reporte.getTipoFalta()))
            throw new IllegalStateException("Solo los reportes Tipo III requieren firma del Rector");
        if ("FIRMADO".equals(reporte.getEstado()))
            throw new IllegalStateException("El reporte ya fue firmado");

        String firmante = usuarioRepo.findByUsername(username)
                .map(Usuario::getNombreCompleto).filter(n -> n != null && !n.isBlank()).orElse(username);
        reporte.setEstado("FIRMADO");
        reporte.setFirmadoPor(firmante);
        return reporteRepo.save(reporte);
    }

    // Arma la información en el formato que pide el SIUCE: hechos, víctimas, agresores, testigos y medidas
    public Map<String, Object> generarFormatoSiuce(String id) {
        Reporte reporte = reporteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + id));
        if (!"TIPO_III".equals(reporte.getTipoFalta()))
            throw new IllegalStateException("Solo los reportes Tipo III se envían al SIUCE");

        List<Map<String, String>> victimas = new ArrayList<>();
        List<Map<String, String>> agresores = new ArrayList<>();
        List<Map<String, String>> testigos = new ArrayList<>();

        for (Implicado imp : implicadoRepo.findByReporteId(id)) {
            String nombre = "Desconocido", grado = "N/A";
            if (imp.getEstudianteId() != null) {
                Optional<Estudiante> est = estudianteRepo.findById(imp.getEstudianteId());
                if (est.isPresent()) {
                    nombre = est.get().getNombres() + " " + est.get().getApellidos();
                    grado = est.get().getGrado() != null ? est.get().getGrado() : "N/A";
                }
            }
            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("nombre", nombre);
            datos.put("grado", grado);
            switch (imp.getRol().toUpperCase()) {
                case "VICTIMA" -> victimas.add(datos);
                case "AGRESOR" -> agresores.add(datos);
                case "TESTIGO" -> testigos.add(datos);
                default -> { }
            }
        }

        String nombreEps = reporte.getEntidadSaludId() == null ? null :
            entidadSaludRepo.findById(reporte.getEntidadSaludId()).map(EntidadSalud::getNombre).orElse(null);
        String nombrePolicia = reporte.getPoliciaId() == null ? null :
            policiaRepo.findById(reporte.getPoliciaId()).map(Policia::getNombre).orElse(null);

        List<String> medidas = new ArrayList<>();
        medidas.add("Citacion a acudientes");
        if (nombreEps != null) medidas.add("Remision a " + nombreEps);
        if (nombrePolicia != null) medidas.add("Reporte a " + nombrePolicia);
        if ("FIRMADO".equals(reporte.getEstado())) medidas.add("Reporte firmado por el Rector");

        Map<String, Object> siuce = new LinkedHashMap<>();
        siuce.put("fechaReporte", reporte.getFechaHora().toString());
        siuce.put("lugar", reporte.getLugar());
        siuce.put("descripcion", reporte.getDescripcionHecho());
        siuce.put("tipoFalta", reporte.getTipoFalta());
        siuce.put("victimas", victimas);
        siuce.put("agresores", agresores);
        siuce.put("testigos", testigos);
        siuce.put("medidasCautelares", medidas);
        siuce.put("firmadoPor", reporte.getFirmadoPor() != null ? reporte.getFirmadoPor() : "Pendiente de firma");
        return siuce;
    }

    // Los estudiantes se cargan todos juntos con findAllById para no hacer una consulta por implicado
    public Map<String, Object> estadisticas() {
        Map<String, Long> mapTipo = new LinkedHashMap<>();
        for (Reporte r : reporteRepo.findAll())
            if (r.getTipoFalta() != null) mapTipo.merge(r.getTipoFalta(), 1L, Long::sum);

        List<Implicado> todos = implicadoRepo.findAll();
        Set<String> ids = todos.stream().map(Implicado::getEstudianteId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, Estudiante> porId = new HashMap<>();
        estudianteRepo.findAllById(ids).forEach(e -> porId.put(e.getId(), e));

        Map<String, Long> mapGrado = new LinkedHashMap<>();
        for (Implicado imp : todos) {
            Estudiante est = imp.getEstudianteId() == null ? null : porId.get(imp.getEstudianteId());
            if (est != null && est.getGrado() != null) mapGrado.merge(est.getGrado(), 1L, Long::sum);
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("totalReportes", reporteRepo.count());
        resultado.put("porTipo", mapTipo);
        resultado.put("porGrado", mapGrado);
        return resultado;
    }

    /**
     * Clasifica la falta según palabras clave de la descripción (Ley 1620):
     * Tipo III (grave) tiene prioridad sobre Tipo II, y lo demás queda como Tipo I.
     * Es una clasificación aproximada: el coordinador siempre puede revisarla.
     */
    public String clasificarTipo(String descripcion) {
        if (descripcion == null) return "TIPO_I";
        String d = descripcion.toLowerCase();
        List<String> t3 = List.of("arma", "cuchillo", "pistola", "violencia sexual", "abuso sexual",
            "agresion fisica grave", "agresión física grave", "hospitaliz", "delito", "presunto delito",
            "herida", "sangre", "fractura");
        List<String> t2 = List.of("bullying", "acoso", "ciberacoso", "intimidacion", "intimidación",
            "amenaza", "golpe", "pelea repetida", "exclusion", "exclusión", "humillacion", "humillación", "matoneo");
        for (String p : t3) if (d.contains(p)) return "TIPO_III";
        for (String p : t2) if (d.contains(p)) return "TIPO_II";
        return "TIPO_I";
    }

    // Distancia en km entre dos coordenadas (fórmula de Haversine)
    private double distancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
