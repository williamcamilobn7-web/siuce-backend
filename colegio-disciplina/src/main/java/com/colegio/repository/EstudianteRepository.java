package com.colegio.repository;

import com.colegio.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    Optional<Estudiante> findByNumeroDocumento(String numeroDocumento);
    List<Estudiante> findByGrado(String grado);
    List<Estudiante> findByColegioId(Long colegioId);

    @Query("SELECT e.grado, COUNT(e), AVG(e.promedioAcademico), SUM(e.numeroInasistencias) FROM Estudiante e GROUP BY e.grado")
    List<Object[]> estadisticasPorGrado();
}