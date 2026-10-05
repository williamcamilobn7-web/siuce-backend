package com.colegio.repository;

import com.colegio.entity.Implicado;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ImplicadoRepository extends MongoRepository<Implicado, String> {
    List<Implicado> findByReporteId(String reporteId);
    List<Implicado> findByEstudianteId(String estudianteId);
    List<Implicado> findByAcudienteId(String acudienteId);
}