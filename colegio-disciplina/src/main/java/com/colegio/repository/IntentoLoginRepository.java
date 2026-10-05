package com.colegio.repository;

import com.colegio.entity.IntentoLogin;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;

public interface IntentoLoginRepository extends MongoRepository<IntentoLogin, String> {
    // Cuenta los intentos fallidos de un usuario desde cierta hora; sirve para bloquear la cuenta
    long countByUsernameAndExitosoFalseAndFechaHoraAfter(String username, LocalDateTime desde);
}