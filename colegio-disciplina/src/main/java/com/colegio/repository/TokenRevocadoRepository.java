package com.colegio.repository;

import com.colegio.entity.TokenRevocado;
import org.springframework.data.mongodb.repository.MongoRepository;

// Se consulta con existsById(jti) en cada petición para saber si el token fue cerrado
public interface TokenRevocadoRepository extends MongoRepository<TokenRevocado, String> {
}