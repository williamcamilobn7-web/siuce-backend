package com.colegio.repository;

import com.colegio.entity.Implicado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImplicadoRepository extends JpaRepository<Implicado, Long> {
    List<Implicado> findByReporteId(Long reporteId);
    List<Implicado> findByRol(String rol);

    @Query("SELECT i.grado, COUNT(i) FROM Implicado i GROUP BY i.grado")
    List<Object[]> contarPorGrado();
}
