package com.colegio.repository;

import com.colegio.entity.Rector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RectorRepository extends JpaRepository<Rector, Long> {
    Optional<Rector> findByColegioId(Long colegioId);
}
