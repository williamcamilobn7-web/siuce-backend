package com.colegio.repository;

import com.colegio.entity.EntidadSalud;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface EntidadSaludRepository extends MongoRepository<EntidadSalud, String> {
}