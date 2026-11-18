package com.colegio.security;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Manejador global de excepciones para toda la API REST.
 * Centraliza las respuestas de error y garantiza códigos HTTP adecuados
 * (400, 403, 404, 409, 413, 429, 500) según los estándares de la industria.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> respuesta(HttpStatus status, String mensaje) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("timestamp", LocalDateTime.now().toString());
        resp.put("status", status.value());
        resp.put("error", status.getReasonPhrase());
        resp.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(resp);
    }

    // 400 Bad Request: Argumentos inválidos o faltantes
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarIllegalArgument(IllegalArgumentException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 400 Bad Request o 429 Too Many Requests: Reglas de negocio o bloqueo por intentos
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarIllegalState(IllegalStateException e) {
        HttpStatus status = e.getMessage() != null && e.getMessage().contains("Demasiados intentos")
                ? HttpStatus.TOO_MANY_REQUESTS
                : HttpStatus.BAD_REQUEST;
        return respuesta(status, e.getMessage());
    }

    // 403 Forbidden: Sin permisos para realizar la acción
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> manejarAccessDenied(AccessDeniedException e) {
        return respuesta(HttpStatus.FORBIDDEN, "No tienes permisos suficientes para realizar esta acción.");
    }

    // 404 Not Found: Recurso no encontrado
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> manejarNotFound(NoSuchElementException e) {
        return respuesta(HttpStatus.NOT_FOUND, "El registro solicitado no fue encontrado.");
    }

    // 409 Conflict: Duplicados en MongoDB (índices únicos como número de documento o correo)
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, Object>> manejarDuplicado(DuplicateKeyException e) {
        return respuesta(HttpStatus.CONFLICT, "Ya existe un registro con esos mismos datos (número de documento o correo duplicado).");
    }

    // 413 Payload Too Large: Archivos que superan el límite
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> manejarArchivoPesado(MaxUploadSizeExceededException e) {
        return respuesta(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo seleccionado supera el límite máximo permitido (50 MB).");
    }

    // 400 Bad Request: Formato JSON no válido o mal estructurado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición contiene un formato JSON no válido.");
    }

    // 400 Bad Request: Parámetro en URL con tipo incorrecto
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> manejarTipoInvalido(MethodArgumentTypeMismatchException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El parámetro en la URL no tiene el tipo de dato esperado.");
    }

    // 500 Internal Server Error: Cualquier otro error no controlado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarGeneral(Exception e) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor: " + e.getMessage());
    }
}
