package com.colegio.controller;

import com.colegio.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Autenticación y manejo de sesión.
 * Solo /login y /login-estudiante son públicos; el resto exige estar autenticado
 * (las reglas están en SecurityConfig). Base URL: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    // El token viene en el header "Authorization: Bearer xxx"
    private String token(HttpServletRequest req) {
        return req.getHeader("Authorization").substring(7);
    }

    /**
     * POST /api/auth/login
     * Personal institucional. Body: { "username": "", "password": "" }
     * 401 si las credenciales son incorrectas y 429 si la cuenta está bloqueada por intentos fallidos.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpServletRequest req) {
        try {
            return ResponseEntity.ok(authService.login(body.get("username"), body.get("password"), req.getRemoteAddr()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/login-estudiante
     * El estudiante entra con su número de documento. La primera vez la contraseña es el
     * mismo documento (o su fecha de nacimiento) y ahí se le crea la cuenta.
     */
    @PostMapping("/login-estudiante")
    public ResponseEntity<?> loginEstudiante(@RequestBody Map<String, String> body, HttpServletRequest req) {
        try {
            return ResponseEntity.ok(authService.loginEstudiante(
                    body.get("numeroDocumento"), body.get("password"), req.getRemoteAddr()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/registro
     * Crea un usuario. Solo lo puede usar el rol DISCIPLINA (si fuera público, cualquiera
     * podría crearse una cuenta de administrador).
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/logout
     * Cierra la sesión actual: el token queda revocado y ya no sirve aunque no haya vencido.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest req) {
        authService.logout(token(req));
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada"));
    }

    /**
     * POST /api/auth/renovar
     * Cambia el token vigente por uno nuevo, para que el usuario no sea sacado a mitad de trabajo.
     * El frontend puede llamarlo poco antes de que se cumplan los 30 minutos.
     */
    @PostMapping("/renovar")
    public ResponseEntity<?> renovar(HttpServletRequest req) {
        try {
            return ResponseEntity.ok(authService.renovar(token(req)));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/cambiar-password
     * Body: { "actual": "", "nueva": "" }
     * Al cambiarla se invalidan todas las sesiones abiertas, así que hay que volver a iniciar sesión.
     */
    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> body, Authentication auth) {
        try {
            authService.cambiarPassword(auth.getName(), body.get("actual"), body.get("nueva"));
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada. Inicia sesión de nuevo"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/logout-todas
     * Cierra la sesión del usuario en todos los dispositivos donde la tenga abierta.
     */
    @PostMapping("/logout-todas")
    public ResponseEntity<?> logoutTodas(Authentication auth) {
        authService.cerrarTodasLasSesiones(auth.getName());
        return ResponseEntity.ok(Map.of("mensaje", "Se cerraron todas tus sesiones"));
    }

    /**
     * POST /api/auth/usuarios/{username}/cerrar-sesiones
     * Lo usa DISCIPLINA cuando hay que sacar a alguien de inmediato (cuenta comprometida,
     * persona que ya no trabaja en el colegio, etc.).
     */
    @PostMapping("/usuarios/{username}/cerrar-sesiones")
    public ResponseEntity<?> cerrarSesionesDe(@PathVariable String username) {
        try {
            authService.cerrarTodasLasSesiones(username);
            return ResponseEntity.ok(Map.of("mensaje", "Sesiones de " + username + " cerradas"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}