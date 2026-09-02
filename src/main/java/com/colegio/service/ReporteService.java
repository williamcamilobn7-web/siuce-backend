package com.colegio.service;

import com.colegio.entity.*;
import com.colegio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReporteService {

    private final ReporteRepository    reporteRepo;
    private final ImplicadoRepository  implicadoRepo;
    private final RectorRepository     rectorRepo;
    private final EntidadSaludRepository entidadSaludRepo;
    private final PoliciaRepository    policiaRepo;

    public ReporteService(ReporteRepository reporteRepo,
                          ImplicadoRepository implicadoRepo,
                          RectorRepository rectorRepo,
                          EntidadSaludRepository entidadSaludRepo,
                          PoliciaRepository policiaRepo) {
        this.reporteRepo      = reporteRepo;
        this.implicadoRepo    = implicadoRepo;
        this.rectorRepo       = rectorRepo;
        this.entidadSaludRepo = entidadSaludRepo;
        this.policiaRepo      = policiaRepo;
    }

    // ── CRUD básico ──────────────────────────────────────────────────────────

    public List<Reporte> listarTodos() {
        return reporteRepo.findAll();
    }

    public Optional<Reporte> buscarPorId(Long id) {
        return reporteRepo.findById(id);
    }

    @Transactional
    public Reporte crear(Map<String, Object> body) {
        Reporte reporte = new Reporte();
        reporte.setFechaHora(LocalDateTime.now());
        reporte.setLugar((String) body.get("lugar"));
        reporte.setDescripcionHecho((String) body.get("descripcionHecho"));

        // Clasificación automática según la descripción del hecho
        String tipo = clasificarTipo((String) body.get("descripcionHecho"));
        reporte.setTipoFalta(tipo);
        reporte.setEstado("PENDIENTE");

        // Si Tipo III → asignar automáticamente entidad salud y policía
        if ("TIPO_III".equals(tipo)) {
            entidadSaludRepo.findAll().stream().findFirst()
                .ifPresent(reporte::setEntidadSalud);
            policiaRepo.findAll().stream().findFirst()
                .ifPresent(reporte::setPolicia);
        }

        return reporteRepo.save(reporte);
    }

    @Transactional
    public Reporte actualizar(Long id, Map<String, Object> body) {
        Reporte reporte = reporteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + id));

        if (!"PENDIENTE".equals(reporte.getEstado())) {
            throw new IllegalStateException("Solo se pueden actualizar reportes en estado PENDIENTE");
        }

        if (body.containsKey("lugar"))
            reporte.setLugar((String) body.get("lugar"));

        if (body.containsKey("descripcionHecho")) {
            String desc = (String) body.get("descripcionHecho");
            reporte.setDescripcionHecho(desc);
            reporte.setTipoFalta(clasificarTipo(desc));
        }

        return reporteRepo.save(reporte);
    }

    @Transactional
    public void eliminar(Long id) {
        Reporte reporte = reporteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + id));

        if ("TIPO_III".equals(reporte.getTipoFalta())) {
            throw new IllegalStateException("No se pueden eliminar reportes de Tipo III");
        }

        reporteRepo.deleteById(id);
    }

    // ── Lógica de negocio ────────────────────────────────────────────────────

    /**
     * Clasifica automáticamente el tipo de falta según palabras clave
     * en la descripción del hecho.
     *
     * TIPO_III: presuntos delitos graves (agresión física, armas, violencia sexual)
     * TIPO_II:  situaciones repetitivas que afectan la convivencia (bullying, ciberacoso)
     * TIPO_I:   conflictos menores manejados en el aula
     */
    public String clasificarTipo(String descripcion) {
        if (descripcion == null) return "TIPO_I";
        String d = descripcion.toLowerCase();

        List<String> palabrasTipo3 = List.of(
            "arma", "cuchillo", "pistola", "violencia sexual", "abuso sexual",
            "agresion fisica grave", "agresión física grave", "hospitaliz",
            "delito", "presunto delito", "herida", "sangre", "fractura"
        );
        List<String> palabrasTipo2 = List.of(
            "bullying", "acoso", "ciberacoso", "intimidacion", "intimidación",
            "amenaza", "golpe", "pelea repetida", "exclusion", "exclusión",
            "humillacion", "humillación", "matoneo"
        );

        for (String p : palabrasTipo3) {
            if (d.contains(p)) return "TIPO_III";
        }
        for (String p : palabrasTipo2) {
            if (d.contains(p)) return "TIPO_II";
        }
        return "TIPO_I";
    }

    /**
     * Genera el JSON en formato SIUCE para un reporte Tipo III.
     * Solo aplica a reportes Tipo III según la Ley 1620 de 2013.
     */
    public Map<String, Object> generarFormatoSiuce(Long id) {
        Reporte reporte = reporteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + id));

        if (!"TIPO_III".equals(reporte.getTipoFalta())) {
            throw new IllegalStateException("Solo los reportes Tipo III se envían al SIUCE");
        }

        List<Implicado> implicados = implicadoRepo.findByReporteId(id);

        List<Map<String, String>> victimas   = new ArrayList<>();
        List<Map<String, String>> agresores  = new ArrayList<>();
        List<Map<String, String>> testigos   = new ArrayList<>();

        for (Implicado imp : implicados) {
            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("nombre", imp.getNombreCompleto());
            datos.put("grado",  imp.getGrado() != null ? imp.getGrado() : "N/A");

            switch (imp.getRol().toUpperCase()) {
                case "VICTIMA"  -> victimas.add(datos);
                case "AGRESOR"  -> agresores.add(datos);
                case "TESTIGO"  -> testigos.add(datos);
            }
        }

        List<String> medidas = new ArrayList<>();
        medidas.add("Citacion a acudientes");
        if (reporte.getEntidadSalud() != null)
            medidas.add("Remision a " + reporte.getEntidadSalud().getNombre());
        if (reporte.getPolicia() != null)
            medidas.add("Reporte a " + reporte.getPolicia().getNombre());
        if ("FIRMADO".equals(reporte.getEstado()))
            medidas.add("Reporte firmado por el Rector");

        Map<String, Object> siuce = new LinkedHashMap<>();
        siuce.put("fechaReporte",      reporte.getFechaHora().toString());
        siuce.put("lugar",             reporte.getLugar());
        siuce.put("descripcion",       reporte.getDescripcionHecho());
        siuce.put("tipoFalta",         reporte.getTipoFalta());
        siuce.put("victimas",          victimas);
        siuce.put("agresores",         agresores);
        siuce.put("testigos",          testigos);
        siuce.put("medidasCautelares", medidas);
        siuce.put("firmadoPor",        reporte.getFirmadoPor() != null
                                           ? reporte.getFirmadoPor()
                                           : "Pendiente de firma");
        return siuce;
    }

    /**
     * Firma digital del rector — cambia el estado a FIRMADO.
     * Busca el rector asociado al colegio del primer implicado.
     */
    @Transactional
    public Reporte firmar(Long id) {
        Reporte reporte = reporteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + id));

        if (!"TIPO_III".equals(reporte.getTipoFalta())) {
            throw new IllegalStateException("Solo los reportes Tipo III requieren firma del Rector");
        }

        List<Implicado> implicados = implicadoRepo.findByReporteId(id);
        String nombreRector = "Rector";

        if (!implicados.isEmpty() && implicados.get(0).getColegio() != null) {
            Long colegioId = implicados.get(0).getColegio().getId();
            nombreRector = rectorRepo.findByColegioId(colegioId)
                .map(Rector::getNombreCompleto)
                .orElse("Rector");
        }

        reporte.setEstado("FIRMADO");
        reporte.setFirmadoPor(nombreRector);
        return reporteRepo.save(reporte);
    }

    /**
     * Estadísticas: casos por tipo y por grado.
     */
    public Map<String, Object> estadisticas() {
        List<Object[]> porTipo  = reporteRepo.contarPorTipo();
        List<Object[]> porGrado = implicadoRepo.contarPorGrado();

        Map<String, Long> mapTipo  = new LinkedHashMap<>();
        Map<String, Long> mapGrado = new LinkedHashMap<>();

        for (Object[] row : porTipo)
            mapTipo.put((String) row[0], (Long) row[1]);

        for (Object[] row : porGrado)
            mapGrado.put((String) row[0], (Long) row[1]);

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("totalReportes", reporteRepo.count());
        resultado.put("porTipo",       mapTipo);
        resultado.put("porGrado",      mapGrado);
        return resultado;
    }
}
