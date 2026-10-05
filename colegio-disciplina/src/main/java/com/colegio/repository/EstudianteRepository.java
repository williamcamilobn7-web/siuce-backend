package com.colegio.repository;

import com.colegio.entity.Estudiante;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EstudianteRepository extends MongoRepository<Estudiante, String> {
    Optional<Estudiante> findByNumeroDocumento(String numeroDocumento);
    List<Estudiante> findByGrado(String grado);
    List<Estudiante> findByColegioId(String colegioId);
    List<Estudiante> findByNumeroDocumentoIn(Collection<String> documentos);

    // Busca por nombre, apellido (sin importar mayúsculas) o documento. El parámetro llega como regex
    @Query("{ $or: [ { nombres: { $regex: ?0, $options: 'i' } }, { apellidos: { $regex: ?0, $options: 'i' } }, { numeroDocumento: { $regex: ?0 } } ] }")
    List<Estudiante> buscar(String regex, Pageable pageable);
}
