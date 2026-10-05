package com.colegio.controller;

import com.colegio.service.CargaMasivaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * Carga de estudiantes desde un archivo CSV 
 * Solo el rol DISCIPLINA puede usarlo. Base URL: /api/carga-masiva
 */
@RestController
@RequestMapping("/api/carga-masiva")
public class CargaMasivaController {

    private final CargaMasivaService cargaMasivaService;

    public CargaMasivaController(CargaMasivaService cargaMasivaService) { this.cargaMasivaService = cargaMasivaService; }

    /**
     * POST /api/carga-masiva/estudiantes
     * Recibe el CSV en el campo "archivo" y devuelve cuántos se crearon, cuántos se omitieron
     * (documento repetido) y cuántos filas tuvieron error.
     */
    @PostMapping("/estudiantes")
    public ResponseEntity<?> cargarEstudiantes(@RequestParam("archivo") MultipartFile archivo) {
        try {
            return ResponseEntity.ok(cargaMasivaService.cargarEstudiantes(archivo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
