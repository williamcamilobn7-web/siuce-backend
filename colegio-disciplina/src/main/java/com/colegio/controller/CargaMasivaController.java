package com.colegio.controller;

import com.colegio.service.CargaMasivaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/carga-masiva")
@CrossOrigin(origins = "*")
public class CargaMasivaController {

    private final CargaMasivaService cargaMasivaService;

    public CargaMasivaController(CargaMasivaService cargaMasivaService) {
        this.cargaMasivaService = cargaMasivaService;
    }

    @PostMapping("/estudiantes")
    public ResponseEntity<?> cargarEstudiantes(@RequestParam("archivo") MultipartFile archivo) {
        try {
            Map<String, Object> resultado = cargaMasivaService.cargarEstudiantes(archivo);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}