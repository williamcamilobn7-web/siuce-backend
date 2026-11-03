package com.colegio.repository;

import com.colegio.entity.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByUsername(String username);

    // Para vincular la cuenta de Google con el usuario del sistema (se compara sin importar mayúsculas)
    Optional<Usuario> findByEmailIgnoreCase(String email);
}