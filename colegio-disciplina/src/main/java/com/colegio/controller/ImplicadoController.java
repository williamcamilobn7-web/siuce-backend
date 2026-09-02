package com.colegio.controller;

import com.colegio.entity.Implicado;
import com.colegio.service.ImplicadoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes/{reporteId}/implicados")
@CrossOrigin(origins = "*")
public class ImplicadoController {

    private final ImplicadoService implicadoService;

    public ImplicadoController(ImplicadoService implicadoService) {
        this.implicadoService = implicadoService;
    }

    // GET /api/reportes/{reporteId}/implicados
    @GetMapping
    public ResponseEntity<List<Implicado>> listar(@PathVariable Long reporteId) {
        return ResponseEntity.ok(implicadoService.listarPorReporte(reporteId));
    }

    // GET /api/reportes/{reporteId}/implicados/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long reporteId,
                                    @PathVariable Long id) {
        return implicadoService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/reportes/{reporteId}/implicados
     * Body: {
     *   "nombreCompleto": "Juan Perez",
     *   "tipoDocumento": "TI",
     *   "numeroDocumento": "1234567890",
     *   "grado": "10A",
     *   "rol": "VICTIMA",
     *   "colegioId": 1,
     *   "acudienteId": 1
     * }
     * rol puede ser: VICTIMA, AGRESOR o TESTIGO
     */
    @PostMapping
    public ResponseEntity<?> agregar(@PathVariable Long reporteId,
                                     @RequestBody Map<String, Object> body) {
        try {
            String rol = (String) body.get("rol");
            if (rol == null || !List.of("VICTIMA", "AGRESOR", "TESTIGO").contains(rol.toUpperCase())) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "rol debe ser VICTIMA, AGRESOR o TESTIGO"));
            }
            Implicado nuevo = implicadoService.agregar(reporteId, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/reportes/{reporteId}/implicados/{id}
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
