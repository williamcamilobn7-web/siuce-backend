package com.colegio.repository;

import com.colegio.entity.Acudiente;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AcudienteRepository extends MongoRepository<Acudiente, String> {
}
