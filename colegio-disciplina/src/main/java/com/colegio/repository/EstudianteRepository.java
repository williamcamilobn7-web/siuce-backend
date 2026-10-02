package com.colegio.repository;

import com.colegio.entity.Estudiante;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
public interface EstudianteRepository extends MongoRepository<Estudiante, String> {
    Optional<Estudiante> findByNumeroDocumento(String numeroDocumento);
    List<Estudiante> findByGrado(String grado);
    List<Estudiante> findByColegioId(String colegioId);
}