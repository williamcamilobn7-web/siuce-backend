package com.colegio.repository;

import com.colegio.entity.Acudiente;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AcudienteRepository extends MongoRepository<Acudiente, String> {

    // Busca un acudiente por su número de documento (el primero, por si el documento se repitiera)
    Optional<Acudiente> findFirstByNumeroDocumento(String numeroDocumento);

    // Busca un acudiente por correo electrónico (para login con Google OAuth)
    Optional<Acudiente> findByEmailIgnoreCase(String email);

    // Busca muchos acudientes de una sola vez; se usa al enlazar estudiantes por lotes
    List<Acudiente> findByNumeroDocumentoIn(Collection<String> documentos);
}