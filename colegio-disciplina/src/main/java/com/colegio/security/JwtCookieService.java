package com.colegio.security;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Crea, borra y lee la cookie que guarda el JWT en el navegador.
 * La cookie es HttpOnly: el navegador la guarda y la reenvía solo, pero JavaScript no puede leerla.
 */
@Component
public class JwtCookieService {

    public static final String NOMBRE = "SIUCE_TOKEN";

    private final boolean secure;
    private final String sameSite;
    private final long duracionMs;

    public JwtCookieService(@Value("${app.cookie.secure:false}") boolean secure,
                            @Value("${app.cookie.same-site:Lax}") String sameSite,
                            @Value("${jwt.expiration-ms:1800000}") long duracionMs) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.duracionMs = duracionMs;
    }

    /**
     * Cookie con el token. Dura lo mismo que el token (30 minutos).
     */
    public ResponseCookie crear(String token) {
        return ResponseCookie.from(NOMBRE, token)
                .httpOnly(true)          // JavaScript no puede leerla
                .secure(secure)          // true en producción: solo viaja por HTTPS
                .sameSite(sameSite)      // el navegador no la envía desde otros sitios
                .path("/")
                .maxAge(Duration.ofMillis(duracionMs))
                .build();
    }

    /**
     * Cookie vacía y ya vencida: le dice al navegador que borre la que tiene.
     */
    public ResponseCookie borrar() {
        return ResponseCookie.from(NOMBRE, "")
                .httpOnly(true).secure(secure).sameSite(sameSite).path("/")
                .maxAge(0)
                .build();
    }

    /*** Busca el token: primero en la cookie HttpOnly y, si no hay, en el header Authorization.*/
    public static String leerToken(HttpServletRequest req) {
        if (req == null) return null;
        // 1. Buscar en las cookies (para el Frontend / Navegador)
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (NOMBRE.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    return c.getValue();
                }
            }
        }
        // 2. Si no hay cookie, buscar en el Header Authorization (para Postman / Swagger)
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (!token.isBlank()) {
                return token;
            }
        }
        return null;
    }
}
