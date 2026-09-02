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

    private final ImplicadoRepository implicadoRepo;
    private final ReporteRepository   reporteRepo;
    private final ColegioRepository   colegioRepo;
    private final AcudienteRepository acudienteRepo;

    public ImplicadoService(ImplicadoRepository implicadoRepo,
                            ReporteRepository reporteRepo,
                            ColegioRepository colegioRepo,
                            AcudienteRepository acudienteRepo) {
        this.implicadoRepo = implicadoRepo;
        this.reporteRepo   = reporteRepo;
        this.colegioRepo   = colegioRepo;
        this.acudienteRepo = acudienteRepo;
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
        imp.setNombreCompleto((String) body.get("nombreCompleto"));
        imp.setTipoDocumento((String) body.get("tipoDocumento"));
        imp.setNumeroDocumento((String) body.get("numeroDocumento"));
        imp.setGrado((String) body.get("grado"));
        imp.setRol((String) body.get("rol"));
        imp.setReporte(reporte);

        if (body.get("colegioId") != null) {
            Long colegioId = Long.valueOf(body.get("colegioId").toString());
            colegioRepo.findById(colegioId).ifPresent(imp::setColegio);
        }

        if (body.get("acudienteId") != null) {
            Long acudienteId = Long.valueOf(body.get("acudienteId").toString());
            acudienteRepo.findById(acudienteId).ifPresent(imp::setAcudiente);
        }

        return implicadoRepo.save(imp);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!implicadoRepo.existsById(id))
            throw new IllegalArgumentException("Implicado no encontrado: " + id);
        implicadoRepo.deleteById(id);
    }
}
