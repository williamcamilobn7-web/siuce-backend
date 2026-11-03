package com.colegio.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * El codificador de contraseñas vive en su propia clase para no formar un ciclo
 * entre AuthService, SecurityConfig y OAuth2SuccessHandler.
 */
@Configuration
public class PasswordConfig {

    // BCrypt: hash de una sola vía con sal, resistente a fuerza bruta
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}