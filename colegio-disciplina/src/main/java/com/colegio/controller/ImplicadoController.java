package com.colegio.controller;

import com.colegio.entity.Implicado;
import com.colegio.service.ImplicadoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Implicados de cada reporte: los estudiantes que participaron en el hecho y con qué rol.
 * Las rutas cuelgan del reporte: /api/reportes/{reporteId}/implicados
 */
@RestController
@RequestMapping("/api/reportes/{reporteId}/implicados")
public class ImplicadoController {

    private final ImplicadoService implicadoService;

    public ImplicadoController(ImplicadoService implicadoService) { this.implicadoService = implicadoService; }

    /**
     * GET /api/reportes/{reporteId}/implicados
     * Todos los implicados del reporte, con el estudiante y el acudiente ya incluidos.
     */
    @GetMapping
    public ResponseEntity<List<Implicado>> listar(@PathVariable String reporteId) {
        return ResponseEntity.ok(implicadoService.listarPorReporte(reporteId));
    }

    /**
     * GET /api/reportes/{reporteId}/implicados/{id}
     * Un implicado puntual. Se verifica que realmente pertenezca a ese reporte.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable String reporteId, @PathVariable String id) {
        return implicadoService.buscarPorId(id)
                .filter(i -> reporteId.equals(i.getReporteId()))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/reportes/{reporteId}/implicados
     * Body: { "estudianteId": "...", "rol": "VICTIMA|AGRESOR|TESTIGO", "acudienteId": "..." }
     * El acudiente es opcional: si no se envía se usa el que tenga el estudiante.
     */
    @PostMapping
    public ResponseEntity<?> agregar(@PathVariable String reporteId, @RequestBody Map<String, Object> body) {
        try {
            Object rolObj = body.get("rol");
            String rol = rolObj == null ? null : rolObj.toString();
            if (rol == null || !List.of("VICTIMA", "AGRESOR", "TESTIGO").contains(rol.toUpperCase()))
                return ResponseEntity.badRequest().body(Map.of("error", "rol debe ser VICTIMA, AGRESOR o TESTIGO"));
            return ResponseEntity.status(HttpStatus.CREATED).body(implicadoService.agregar(reporteId, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/reportes/{reporteId}/implicados/{id}
     * Solo el rol DISCIPLINA.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String reporteId, @PathVariable String id) {
        try {
            implicadoService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Implicado eliminado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
