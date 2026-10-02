package com.colegio.controller;

import com.colegio.entity.Implicado;
import com.colegio.service.ImplicadoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Controller que gestiona los implicados de cada reporte disciplinario.
 * Un implicado es un estudiante que participó en un incidente con un rol específico.
 * Las URLs están anidadas bajo los reportes: /api/reportes/{reporteId}/implicados
 */
@RestController
@RequestMapping("/api/reportes/{reporteId}/implicados")
@CrossOrigin(origins = "*")
public class ImplicadoController {

    // Servicio con la lógica de negocio de implicados
    private final ImplicadoService implicadoService;

    public ImplicadoController(ImplicadoService implicadoService) {
        this.implicadoService = implicadoService;
    }

    /**
     * GET /api/reportes/{reporteId}/implicados
     * Devuelve todos los implicados de un reporte específico.
     * Incluye la información del estudiante y su acudiente.
     */
    @GetMapping
    public ResponseEntity<List<Implicado>> listar(@PathVariable Long reporteId) {
        return ResponseEntity.ok(implicadoService.listarPorReporte(reporteId));
    }

    /**
     * GET /api/reportes/{reporteId}/implicados/{id}
     * Busca un implicado específico por su ID.
     * Devuelve 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long reporteId,
                                    @PathVariable Long id) {
        return implicadoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/reportes/{reporteId}/implicados
     * Agrega un estudiante como implicado en un reporte con un rol específico.
     * Body esperado: {
     *   "estudianteId": 1,
     *   "rol": "VICTIMA",        ← puede ser VICTIMA, AGRESOR o TESTIGO
     *   "acudienteId": 1         ← opcional, se asigna automáticamente del estudiante
     * }
     * Devuelve 400 si el rol no es válido o si el reporte no existe.
     */
    @PostMapping
    public ResponseEntity<?> agregar(@PathVariable Long reporteId,
                                     @RequestBody Map<String, Object> body) {
        try {
            // Valida que el rol sea uno de los tres valores permitidos
            String rol = (String) body.get("rol");
            if (rol == null || !List.of("VICTIMA", "AGRESOR", "TESTIGO")
                    .contains(rol.toUpperCase())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "rol debe ser VICTIMA, AGRESOR o TESTIGO"));
            }
            Implicado nuevo = implicadoService.agregar(reporteId, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/reportes/{reporteId}/implicados/{id}
     * Elimina un implicado de un reporte.
     * Solo puede ejecutarlo un usuario con rol DISCIPLINA.
     * Devuelve 404 si el implicado no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long reporteId,
                                      @PathVariable Long id) {
        try {
            implicadoService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Implicado eliminado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}