package com.colegio.repository;

import com.colegio.entity.Policia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoliciaRepository extends JpaRepository<Policia, Long> {}
