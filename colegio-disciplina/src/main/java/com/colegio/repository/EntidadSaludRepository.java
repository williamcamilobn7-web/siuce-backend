package com.colegio.repository;

import com.colegio.entity.EntidadSalud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntidadSaludRepository extends JpaRepository<EntidadSalud, Long> {}
