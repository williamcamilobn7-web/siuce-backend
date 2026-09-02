package com.colegio.service;

import com.colegio.entity.*;
import com.colegio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ImplicadoService {

    private final ImplicadoRepository  implicadoRepo;
    private final ReporteRepository    reporteRepo;
    private final AcudienteRepository  acudienteRepo;
    private final EstudianteRepository estudianteRepo;

    public ImplicadoService(ImplicadoRepository implicadoRepo, ReporteRepository reporteRepo,
                            AcudienteRepository acudienteRepo, EstudianteRepository estudianteRepo) {
        this.implicadoRepo  = implicadoRepo;
        this.reporteRepo    = reporteRepo;
        this.acudienteRepo  = acudienteRepo;
        this.estudianteRepo = estudianteRepo;
    }

    public List<Implicado> listarPorReporte(Long reporteId) {
        return implicadoRepo.findByReporteId(reporteId);
    }

    public Optional<Implicado> buscarPorId(Long id) {
        return implicadoRepo.findById(id);
    }

    @Transactional
    public Implicado agregar(Long reporteId, Map<String, Object> body) {
        Reporte reporte = reporteRepo.findById(reporteId)
                .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado: " + reporteId));
        Implicado imp = new Implicado();
        imp.setRol((String) body.get("rol"));
        imp.setReporte(reporte);
        if (body.get("estudianteId") != null)
            estudianteRepo.findById(Long.valueOf(body.get("estudianteId").toString()))
                    .ifPresent(imp::setEstudiante);
        if (body.get("acudienteId") != null)
            acudienteRepo.findById(Long.valueOf(body.get("acudienteId").toString()))
                    .ifPresent(imp::setAcudiente);
        return implicadoRepo.save(imp);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!implicadoRepo.existsById(id))
            throw new IllegalArgumentException("Implicado no encontrado: " + id);
        implicadoRepo.deleteById(id);
    }
}