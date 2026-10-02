package com.colegio.controller;

import com.colegio.service.AuthService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Controller que maneja la autenticación del sistema.
 * Es el único controller completamente público — no requiere token JWT.
 * Base URL: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    // Servicio que contiene la lógica de verificación de credenciales
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Punto de entrada para personal institucional (DISCIPLINA, DOCENTE).
     * Body esperado: { "username": "admin", "password": "123456" }
     * Si las credenciales son correctas devuelve el token JWT y los datos del usuario.
     * Si son incorrectas devuelve 401 Unauthorized.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.login(
                    body.get("username"), body.get("password")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/registro
     * Registra un nuevo usuario en el sistema.
     * Body esperado: { "username": "", "password": "", "rol": "",
     *                  "nombreCompleto": "", "estudianteId": null, "acudienteId": null }
     * El rol puede ser: DISCIPLINA, DOCENTE, ESTUDIANTE o ACUDIENTE.
     * Devuelve 201 Created si se registró correctamente o 400 si el usuario ya existe.
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(authService.registrar(body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/login-estudiante
     * Los estudiantes se autentican con su número de documento como usuario y contraseña.
     * Si el estudiante no tiene usuario creado, el sistema lo crea automáticamente.
     * Devuelve el token JWT y la lista de reportes donde el estudiante aparece como implicado.
     */
    @PostMapping("/login-estudiante")
    public ResponseEntity<?> loginEstudiante(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.loginEstudiante(
                    body.get("numeroDocumento"), body.get("password")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}