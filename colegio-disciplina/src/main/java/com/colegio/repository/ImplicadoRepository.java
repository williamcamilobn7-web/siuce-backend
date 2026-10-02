package com.colegio.repository;

import com.colegio.entity.Implicado;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface ImplicadoRepository extends MongoRepository<Implicado, String> {
    List<Implicado> findByReporteId(String reporteId);
    List<Implicado> findByEstudianteId(String estudianteId);
    List<Implicado> findByAcudienteId(String acudienteId);
}
