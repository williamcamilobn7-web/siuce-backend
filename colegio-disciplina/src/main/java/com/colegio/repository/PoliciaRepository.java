package com.colegio.repository;

import com.colegio.entity.Policia;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PoliciaRepository extends MongoRepository<Policia, String> {
}