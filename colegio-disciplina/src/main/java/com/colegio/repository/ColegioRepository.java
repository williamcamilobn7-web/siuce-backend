package com.colegio.repository;

import com.colegio.entity.Colegio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

public interface ColegioRepository extends MongoRepository<Colegio, String> {
}
