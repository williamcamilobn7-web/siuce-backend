package com.colegio.repository;

import com.colegio.entity.Colegio;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ColegioRepository extends MongoRepository<Colegio, String> {
}
