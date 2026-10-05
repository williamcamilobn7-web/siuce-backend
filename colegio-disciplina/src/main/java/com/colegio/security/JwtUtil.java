package com.colegio.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Crea y lee los tokens JWT. Cada token lleva:
 *  - jti: un id único, para poder revocarlo si el usuario cierra sesión
 *  - sub: el username
 *  - rol: el rol con el que se logueó
 *  - inicio: cuándo empezó la sesión original (se conserva al renovar el token)
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiration;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms:1800000}") long expiration) {
        // El secreto viene de la configuración, no del código. Debe tener mínimo 32 caracteres para HS256
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /** Token para un login nuevo: la sesión empieza ahora. */
    public String generateToken(String username, String rol) {
        return generateToken(username, rol, Instant.now());
    }

    /** Token para una renovación: se mantiene la hora de inicio de la sesión original. */
    public String generateToken(String username, String rol, Instant inicioSesion) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("rol", rol)
                .claim("inicio", inicioSesion.getEpochSecond())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) { return getClaims(token).getSubject(); }
    public String extractRol(String token) { return (String) getClaims(token).get("rol"); }
    public String extractJti(String token) { return getClaims(token).getId(); }
    public Date extractExpiration(String token) { return getClaims(token).getExpiration(); }
    public Instant extractIssuedAt(String token) { return getClaims(token).getIssuedAt().toInstant(); }
    public Instant extractInicio(String token) {
        return Instant.ofEpochSecond(((Number) getClaims(token).get("inicio")).longValue());
    }

    /** Válido = firma correcta y que no haya vencido. Si alguien altera el token, la firma deja de coincidir. */
    public boolean validateToken(String token) {
        try { getClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    private Claims getClaims(String token) {
        // verifyWith(key) también rechaza tokens con algoritmo "none" o firmados con otra llave
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
