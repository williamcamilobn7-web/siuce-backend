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
    List<Implicado> findByEstudianteId(Long estudianteId);
    List<Implicado> findByAcudienteId(Long acudienteId);

    @Query("SELECT i.estudiante.grado, COUNT(i) FROM Implicado i WHERE i.estudiante IS NOT NULL GROUP BY i.estudiante.grado")
    List<Object[]> contarPorGrado();
}