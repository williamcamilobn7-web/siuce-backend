package com.colegio.service;

import com.colegio.entity.Estudiante;
import com.colegio.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Lógica de negocio de los estudiantes.
 */
@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepo;
    private final ColegioRepository colegioRepo;
    private final AcudienteRepository acudienteRepo;
    private final MongoTemplate mongo;

    public EstudianteService(EstudianteRepository estudianteRepo, ColegioRepository colegioRepo,
                             AcudienteRepository acudienteRepo, MongoTemplate mongo) {
        this.estudianteRepo = estudianteRepo;
        this.colegioRepo = colegioRepo;
        this.acudienteRepo = acudienteRepo;
        this.mongo = mongo;
    }

    // Pedimos solo una página a la base y a cada estudiante le llenamos su acudiente y colegio
    public Page<Estudiante> listarPaginado(Pageable pageable) {
        Page<Estudiante> p = estudianteRepo.findAll(pageable);
        p.getContent().forEach(this::enriquecer);
        return p;
    }

    public Optional<Estudiante> buscarPorId(String id) {
        return estudianteRepo.findById(id).map(this::enriquecer);
    }

    public Optional<Estudiante> buscarPorDocumento(String doc) {
        return estudianteRepo.findByNumeroDocumento(doc).map(this::enriquecer);
    }

    public List<Estudiante> buscar(String q) {
        if (q == null || q.trim().length() < 2) return List.of();
        // Pattern.quote hace que lo que escribe el usuario se trate como texto literal y no como una
        // expresión regular; así un "." o un "*" en la búsqueda no rompen la consulta
        return estudianteRepo.buscar(Pattern.quote(q.trim()), PageRequest.of(0, 20));
    }

    // El documento es único: se revisa antes de guardar para dar un mensaje claro
    public Estudiante crear(Map<String, Object> body) {
        String doc = String.valueOf(body.get("numeroDocumento"));
        if (estudianteRepo.findByNumeroDocumento(doc).isPresent())
            throw new IllegalArgumentException("Ya existe un estudiante con documento " + doc);
        Estudiante e = new Estudiante();
        mapearCampos(e, body);
        return estudianteRepo.save(e);
    }

    public Estudiante actualizar(String id, Map<String, Object> body) {
        Estudiante e = estudianteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + id));
        mapearCampos(e, body);
        return estudianteRepo.save(e);
    }

    public void eliminar(String id) {
        if (!estudianteRepo.existsById(id))
            throw new IllegalArgumentException("Estudiante no encontrado: " + id);
        estudianteRepo.deleteById(id);
    }

    // Agrupa en la propia base de datos (por grado) en vez de traer los 10.000 estudiantes a memoria
    @SuppressWarnings("rawtypes")
    public Map<String, Object> estadisticas() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.group("grado")
                        .count().as("total")
                        .avg("promedioAcademico").as("promedio")
                        .sum("numeroInasistencias").as("inasistencias"),
                Aggregation.sort(Sort.Direction.ASC, "_id"));
        List<Map<String, Object>> porGrado = new ArrayList<>();
        for (Map row : mongo.aggregate(agg, "estudiantes", Map.class).getMappedResults()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("grado", row.get("_id"));
            m.put("total", row.get("total"));
            m.put("promedioAcademico", row.get("promedio"));
            m.put("totalInasistencias", row.get("inasistencias"));
            porGrado.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEstudiantes", estudianteRepo.count());
        result.put("porGrado", porGrado);
        return result;
    }

    // En Mongo solo se guardan los ids; aquí se cargan los objetos para que el frontend los reciba completos
    private Estudiante enriquecer(Estudiante e) {
        if (e.getAcudienteId() != null) acudienteRepo.findById(e.getAcudienteId()).ifPresent(e::setAcudiente);
        if (e.getColegioId() != null) colegioRepo.findById(e.getColegioId()).ifPresent(e::setColegio);
        return e;
    }

    private String str(Map<String, Object> b, String k) {
        Object v = b.get(k);
        return v == null ? null : v.toString();
    }

    // Solo se tocan los campos que vienen en el body, así un PUT parcial no borra el resto
    private void mapearCampos(Estudiante e, Map<String, Object> b) {
        if (b.get("acudienteId") != null && !str(b, "acudienteId").isBlank()) {
            String aid = str(b, "acudienteId");
            if (!acudienteRepo.existsById(aid)) throw new IllegalArgumentException("Acudiente no existe: " + aid);
            e.setAcudienteId(aid);
        }
        if (b.get("colegioId") != null && !str(b, "colegioId").isBlank()) {
            String cid = str(b, "colegioId");
            if (!colegioRepo.existsById(cid)) throw new IllegalArgumentException("Colegio no existe: " + cid);
            e.setColegioId(cid);
        }
        if (b.get("tipoDocumento") != null) e.setTipoDocumento(str(b, "tipoDocumento"));
        if (b.get("numeroDocumento") != null) e.setNumeroDocumento(str(b, "numeroDocumento"));
        if (b.get("nombres") != null) e.setNombres(str(b, "nombres"));
        if (b.get("apellidos") != null) e.setApellidos(str(b, "apellidos"));
        if (b.get("fechaNacimiento") != null && !str(b, "fechaNacimiento").isBlank())
            e.setFechaNacimiento(LocalDate.parse(str(b, "fechaNacimiento")));
        if (b.get("genero") != null) e.setGenero(str(b, "genero"));
        if (b.get("paisOrigen") != null) e.setPaisOrigen(str(b, "paisOrigen"));
        if (b.get("departamentoExpedicion") != null) e.setDepartamentoExpedicion(str(b, "departamentoExpedicion"));
        if (b.get("municipioExpedicion") != null) e.setMunicipioExpedicion(str(b, "municipioExpedicion"));
        if (b.get("direccionResidencia") != null) e.setDireccionResidencia(str(b, "direccionResidencia"));
        if (b.get("telefono") != null) e.setTelefono(str(b, "telefono"));
        if (b.get("email") != null) e.setEmail(str(b, "email"));
        if (b.get("grupoEtnico") != null) e.setGrupoEtnico(str(b, "grupoEtnico"));
        if (b.get("tipoDiscapacidad") != null) e.setTipoDiscapacidad(str(b, "tipoDiscapacidad"));
        if (b.get("victimaConflicto") != null) e.setVictimaConflicto(Boolean.valueOf(str(b, "victimaConflicto")));
        if (b.get("sede") != null) e.setSede(str(b, "sede"));
        if (b.get("jornada") != null) e.setJornada(str(b, "jornada"));
        if (b.get("grado") != null) e.setGrado(str(b, "grado"));
        if (b.get("promedioAcademico") != null && !str(b, "promedioAcademico").isBlank())
            e.setPromedioAcademico(Double.valueOf(str(b, "promedioAcademico")));
        if (b.get("numeroInasistencias") != null && !str(b, "numeroInasistencias").isBlank())
            e.setNumeroInasistencias(Integer.valueOf(str(b, "numeroInasistencias")));
        if (b.get("latitud") != null) e.setLatitud(Double.valueOf(str(b, "latitud")));
        if (b.get("longitud") != null) e.setLongitud(Double.valueOf(str(b, "longitud")));
    }
}
