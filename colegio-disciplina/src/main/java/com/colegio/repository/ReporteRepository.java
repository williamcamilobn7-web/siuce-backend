package com.colegio.repository;

import com.colegio.entity.Reporte;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface ReporteRepository extends MongoRepository<Reporte, String> {
    List<Reporte> findByTipoFalta(String tipoFalta);
    List<Reporte> findByEstado(String estado);
}
