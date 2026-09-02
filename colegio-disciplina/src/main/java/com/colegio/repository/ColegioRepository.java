package com.colegio.repository;

import com.colegio.entity.Colegio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ColegioRepository extends JpaRepository<Colegio, Long> {
    Optional<Colegio> findByNit(String nit);
}
