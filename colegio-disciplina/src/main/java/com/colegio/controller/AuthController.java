package com.colegio.controller;

import com.colegio.security.JwtCookieService;
import com.colegio.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Autenticación y manejo de sesión. Base URL: /api/auth
 * El token ya no viaja en el cuerpo de la respuesta: el servidor lo entrega en una cookie HttpOnly.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtCookieService cookies;

    public AuthController(AuthService authService, JwtCookieService cookies) {
        this.authService = authService;
        this.cookies = cookies;
    }

    // El token viene en la cookie (o en el header Authorization si se prueba con Postman)
    private String token(HttpServletRequest req) {
        return JwtCookieService.leerToken(req);
    }

    // Saca el token del mapa de respuesta, lo pone en la cookie y deja en el cuerpo solo los datos del usuario
    private ResponseEntity<?> conCookie(Map<String, Object> resp) {
        String token = (String) resp.remove("token");
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.crear(token).toString())
                .body(resp);
    }

    /** POST /api/auth/login: personal institucional. 401 si falla y 429 si la cuenta está bloqueada. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest req) {
        try {
            return conCookie(authService.login(body.get("username"), body.get("password"), req.getRemoteAddr()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/auth/login-estudiante: el estudiante entra con su número de documento. */
    @PostMapping("/login-estudiante")
    public ResponseEntity<?> loginEstudiante(@RequestBody Map<String, String> body, HttpServletRequest req) {
        try {
            return conCookie(authService.loginEstudiante(
                    body.get("numeroDocumento"), body.get("password"), req.getRemoteAddr()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/auth/registro: crea un usuario. Solo el rol DISCIPLINA. */
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/auth/logout: cancela el token y le pide al navegador que borre la cookie. */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest req) {
        authService.logout(token(req));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.borrar().toString())
                .body(Map.of("mensaje", "Sesión cerrada"));
    }

    /** POST /api/auth/renovar: cambia el token por uno nuevo y actualiza la cookie. */
    @PostMapping("/renovar")
    public ResponseEntity<?> renovar(HttpServletRequest req) {
        try {
            return conCookie(authService.renovar(token(req)));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/auth/cambiar-password: cierra todas las sesiones, así que también se borra la cookie. */
    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> body, Authentication auth) {
        try {
            authService.cambiarPassword(auth.getName(), body.get("actual"), body.get("nueva"));
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookies.borrar().toString())
                    .body(Map.of("mensaje", "Contraseña actualizada. Inicia sesión de nuevo"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/auth/logout-todas: cierra la sesión en todos los dispositivos y borra la cookie. */
    @PostMapping("/logout-todas")
    public ResponseEntity<?> logoutTodas(Authentication auth) {
        authService.cerrarTodasLasSesiones(auth.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.borrar().toString())
                .body(Map.of("mensaje", "Se cerraron todas tus sesiones"));
    }

    /** POST /api/auth/usuarios/{username}/cerrar-sesiones: DISCIPLINA saca a otra persona de inmediato. */
    @PostMapping("/usuarios/{username}/cerrar-sesiones")
    public ResponseEntity<?> cerrarSesionesDe(@PathVariable String username) {
        try {
            authService.cerrarTodasLasSesiones(username);
            return ResponseEntity.ok(Map.of("mensaje", "Sesiones de " + username + " cerradas"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** GET /api/auth/me: nombre, rol y reportes del usuario con sesión iniciada. */
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        return ResponseEntity.ok(authService.perfil(auth.getName()));
    }

    /** PUT /api/auth/usuarios/{username}/email: DISCIPLINA vincula el correo que puede entrar con Google. */
    @PutMapping("/usuarios/{username}/email")
    public ResponseEntity<?> vincularEmail(@PathVariable String username, @RequestBody Map<String, Object> body) {
        try {
            authService.vincularEmail(username, body.get("email"));
            return ResponseEntity.ok(Map.of("mensaje", "Correo actualizado"));
        } catch (IllegalArgumentException e) {
            if ("Usuario no encontrado".equals(e.getMessage())) return ResponseEntity.notFound().build();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}