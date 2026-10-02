package com.colegio.repository;

import com.colegio.entity.Policia;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

public interface PoliciaRepository extends MongoRepository<Policia, String> {
}