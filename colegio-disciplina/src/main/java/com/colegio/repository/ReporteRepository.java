package com.colegio.repository;

import com.colegio.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findByTipoFalta(String tipoFalta);
    List<Reporte> findByEstado(String estado);

    @Query("SELECT r.tipoFalta, COUNT(r) FROM Reporte r GROUP BY r.tipoFalta")
    List<Object[]> contarPorTipo();
}
