package com.colegio.repository;

import com.colegio.entity.Rector;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface RectorRepository extends MongoRepository<Rector, String> {
    Optional<Rector> findByColegioId(String colegioId);
}
