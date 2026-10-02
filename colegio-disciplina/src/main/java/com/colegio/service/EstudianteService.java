package com.colegio.service;

import com.colegio.repository.AcudienteRepository;
import com.colegio.entity.*;
import com.colegio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepo;
    private final ColegioRepository    colegioRepo;
    private final AcudienteRepository  acudienteRepo;

    public EstudianteService(EstudianteRepository estudianteRepo,
                             ColegioRepository colegioRepo,
                             AcudienteRepository acudienteRepo) {
        this.estudianteRepo = estudianteRepo;
        this.colegioRepo    = colegioRepo;
        this.acudienteRepo  = acudienteRepo;
    }
    public List<Estudiante> listarTodos() { return estudianteRepo.findAll();}
    public org.springframework.data.domain.Page<Estudiante> listarPaginado(
            org.springframework.data.domain.Pageable pageable) {
        return estudianteRepo.findAll(pageable);
    }

    public Optional<Estudiante> buscarPorId(String id) { return estudianteRepo.findById(id); }

    public Optional<Estudiante> buscarPorDocumento(String doc) { return estudianteRepo.findByNumeroDocumento(doc); }

    @Transactional
    public Estudiante crear(Map<String, Object> body) {
        Estudiante e = new Estudiante();
        mapearCampos(e, body);
        return estudianteRepo.save(e);
    }

    @Transactional
    public Estudiante actualizar(String id, Map<String, Object> body) {
        Estudiante e = estudianteRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + id));
        mapearCampos(e, body);
        return estudianteRepo.save(e);
    }

    @Transactional
    public void eliminar(String id) {
        if (!estudianteRepo.existsById(id))
            throw new IllegalArgumentException("Estudiante no encontrado: " + id);
        estudianteRepo.deleteById(id);
    }

    public Map<String, Object> estadisticas() {
        List<Object[]> rows = estudianteRepo.estadisticasPorGrado();
        List<Map<String, Object>> porGrado = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("grado", row[0]);
            m.put("total", row[1]);
            m.put("promedioAcademico", row[2]);
            m.put("totalInasistencias", row[3]);
            porGrado.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalEstudiantes", estudianteRepo.count());
        result.put("porGrado", porGrado);
        return result;
    }

    private void mapearCampos(Estudiante e, Map<String, Object> body) {
        if (body.get("acudienteId") != null)
            acudienteRepo.findById(Long.valueOf(body.get("acudienteId").toString()))
                    .ifPresent(e::setAcudiente);
        if (body.get("tipoDocumento") != null) e.setTipoDocumento((String) body.get("tipoDocumento"));
        if (body.get("numeroDocumento") != null) e.setNumeroDocumento((String) body.get("numeroDocumento"));
        if (body.get("nombres") != null) e.setNombres((String) body.get("nombres"));
        if (body.get("apellidos") != null) e.setApellidos((String) body.get("apellidos"));
        if (body.get("fechaNacimiento") != null) e.setFechaNacimiento(LocalDate.parse((String) body.get("fechaNacimiento")));
        if (body.get("genero") != null) e.setGenero((String) body.get("genero"));
        if (body.get("paisOrigen") != null) e.setPaisOrigen((String) body.get("paisOrigen"));
        if (body.get("departamentoExpedicion") != null) e.setDepartamentoExpedicion((String) body.get("departamentoExpedicion"));
        if (body.get("municipioExpedicion") != null) e.setMunicipioExpedicion((String) body.get("municipioExpedicion"));
        if (body.get("direccionResidencia") != null) e.setDireccionResidencia((String) body.get("direccionResidencia"));
        if (body.get("telefono") != null) e.setTelefono((String) body.get("telefono"));
        if (body.get("email") != null) e.setEmail((String) body.get("email"));
        if (body.get("grupoEtnico") != null) e.setGrupoEtnico((String) body.get("grupoEtnico"));
        if (body.get("tipoDiscapacidad") != null) e.setTipoDiscapacidad((String) body.get("tipoDiscapacidad"));
        if (body.get("victimaConflicto") != null) e.setVictimaConflicto((Boolean) body.get("victimaConflicto"));
        if (body.get("sede") != null) e.setSede((String) body.get("sede"));
        if (body.get("jornada") != null) e.setJornada((String) body.get("jornada"));
        if (body.get("grado") != null) e.setGrado((String) body.get("grado"));
        if (body.get("promedioAcademico") != null) e.setPromedioAcademico(Double.valueOf(body.get("promedioAcademico").toString()));
        if (body.get("numeroInasistencias") != null) e.setNumeroInasistencias(Integer.valueOf(body.get("numeroInasistencias").toString()));
        if (body.get("colegioId") != null)
            colegioRepo.findById(Long.valueOf(body.get("colegioId").toString())).ifPresent(e::setColegio);
    }
}