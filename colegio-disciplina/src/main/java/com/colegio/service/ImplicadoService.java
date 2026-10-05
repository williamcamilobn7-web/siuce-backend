package com.colegio.service;

import com.colegio.entity.Estudiante;
import com.colegio.entity.Implicado;
import com.colegio.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lógica de los implicados de un reporte.
 */
@Service
public class ImplicadoService {

    private final ImplicadoRepository implicadoRepo;
    private final ReporteRepository reporteRepo;
    private final AcudienteRepository acudienteRepo;
    private final EstudianteRepository estudianteRepo;

    public ImplicadoService(ImplicadoRepository implicadoRepo, ReporteRepository reporteRepo,
                            AcudienteRepository acudienteRepo, EstudianteRepository estudianteRepo) {
        this.implicadoRepo = implicadoRepo;
        this.reporteRepo = reporteRepo;
        this.acudienteRepo = acudienteRepo;
        this.estudianteRepo = estudianteRepo;
    }

    public List<Implicado> listarPorReporte(String reporteId) {
        List<Implicado> lista = implicadoRepo.findByReporteId(reporteId);
        lista.forEach(this::enriquecer);
        return lista;
    }

    public Optional<Implicado> buscarPorId(String id) {
        return implicadoRepo.findById(id).map(this::enriquecer);
    }

    // Se valida que existan el reporte y el estudiante antes de crear el implicado
    public Implicado agregar(String reporteId, Map<String, Object> body) {
        if (!reporteRepo.existsById(reporteId))
            throw new IllegalArgumentException("Reporte no encontrado: " + reporteId);
        if (body.get("estudianteId") == null)
            throw new IllegalArgumentException("estudianteId es obligatorio");

        String estudianteId = body.get("estudianteId").toString();
        Estudiante est = estudianteRepo.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + estudianteId));

        Implicado imp = new Implicado();
        imp.setRol(((String) body.get("rol")).toUpperCase());
        imp.setReporteId(reporteId);
        imp.setEstudianteId(est.getId());

        // Si no se envía acudiente, se usa el del estudiante
        String acudienteId = body.get("acudienteId") == null ? "" : body.get("acudienteId").toString();
        imp.setAcudienteId(acudienteId.isBlank() ? est.getAcudienteId() : acudienteId);

        return enriquecer(implicadoRepo.save(imp));
    }

    public void eliminar(String id) {
        if (!implicadoRepo.existsById(id))
            throw new IllegalArgumentException("Implicado no encontrado: " + id);
        implicadoRepo.deleteById(id);
    }

    // Llena el estudiante y el acudiente (no se guardan en la base, solo se devuelven)
    private Implicado enriquecer(Implicado i) {
        if (i.getEstudianteId() != null) estudianteRepo.findById(i.getEstudianteId()).ifPresent(i::setEstudiante);
        if (i.getAcudienteId() != null) acudienteRepo.findById(i.getAcudienteId()).ifPresent(i::setAcudiente);
        return i;
    }
}
