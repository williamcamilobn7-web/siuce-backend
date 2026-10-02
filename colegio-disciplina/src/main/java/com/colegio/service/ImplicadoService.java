package com.colegio.service;

import com.colegio.entity.*;
import com.colegio.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ImplicadoService {

    private final ImplicadoRepository  implicadoRepo;
    private final ReporteRepository    reporteRepo;
    private final AcudienteRepository  acudienteRepo;
    private final EstudianteRepository estudianteRepo;

    public ImplicadoService(ImplicadoRepository implicadoRepo,
                            ReporteRepository reporteRepo,
                            AcudienteRepository acudienteRepo,
                            EstudianteRepository estudianteRepo) {
        this.implicadoRepo  = implicadoRepo;
        this.reporteRepo    = reporteRepo;
        this.acudienteRepo  = acudienteRepo;
        this.estudianteRepo = estudianteRepo;
    }

    public List<Implicado> listarPorReporte(String reporteId) {
        return implicadoRepo.findByReporteId(reporteId);
    }

    public Optional<Implicado> buscarPorId(String id) {
        return implicadoRepo.findById(id);
    }

    public Implicado agregar(String reporteId, Map<String, Object> body) {
        // Verifica que el reporte existe
        reporteRepo.findById(reporteId)
            .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reporteId));

        Implicado imp = new Implicado();
        imp.setRol((String) body.get("rol"));
        imp.setReporteId(reporteId);

        if (body.get("estudianteId") != null)
            imp.setEstudianteId(body.get("estudianteId").toString());

        if (body.get("acudienteId") != null)
            imp.setAcudienteId(body.get("acudienteId").toString());

        return implicadoRepo.save(imp);
    }

    public void eliminar(String id) {
        if (!implicadoRepo.existsById(id))
            throw new IllegalArgumentException("Implicado no encontrado: " + id);
        implicadoRepo.deleteById(id);
    }
}