package com.colegio.controller;

import com.colegio.entity.Reporte;
import com.colegio.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    // GET /api/reportes
    @GetMapping
    public ResponseEntity<List<Reporte>> listarTodos() {
        return ResponseEntity.ok(reporteService.listarTodos());
    }

    // GET /api/reportes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        return reporteService.buscarPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/reportes/estadisticas
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(reporteService.estadisticas());
    }

    /**
     * POST /api/reportes
     * Body: { "lugar": "Salon 203", "descripcionHecho": "..." }
     * El sistema clasifica automaticamente el tipo (TIPO_I, TIPO_II, TIPO_III)
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            if (body.get("lugar") == null || body.get("descripcionHecho") == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "lugar y descripcionHecho son obligatorios"));
            }
            Reporte nuevo = reporteService.crear(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/reportes/{id}
     * Solo funciona si el reporte esta en estado PENDIENTE
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        try {
            Reporte actualizado = reporteService.actualizar(id, body);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/reportes/{id}
     * Solo se pueden eliminar reportes Tipo I o Tipo II
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            reporteService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Reporte eliminado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/reportes/{id}/enviar-siuce
     * Genera el JSON en formato SIUCE para reportes Tipo III
     * Segun lo exige el MEN - Ley 1620 de 2013
     */
    @PostMapping("/{id}/enviar-siuce")
    public ResponseEntity<?> enviarSiuce(@PathVariable Long id) {
        try {
            Map<String, Object> formatoSiuce = reporteService.generarFormatoSiuce(id);
            return ResponseEntity.ok(formatoSiuce);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/reportes/{id}/firmar
     * Firma digital del rector — cambia el estado a FIRMADO
     * Solo aplica a reportes Tipo III
     */
    @PostMapping("/{id}/firmar")
    public ResponseEntity<?> firmar(@PathVariable Long id) {
        try {
            Reporte firmado = reporteService.firmar(id);
            return ResponseEntity.ok(firmado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
