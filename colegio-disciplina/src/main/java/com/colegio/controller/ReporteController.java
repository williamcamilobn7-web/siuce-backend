package com.colegio.controller;

import com.colegio.entity.Reporte;
import com.colegio.service.ReporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Reportes disciplinarios. Base URL: /api/reportes
 * Los permisos de cada operación están definidos en SecurityConfig.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) { this.reporteService = reporteService; }

    // Saca el rol (sin el prefijo ROLE_) del usuario autenticado
    private String rolDe(Authentication auth) {
        return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
    }

    /**
     * GET /api/reportes - lista completa (solo personal del colegio).
     */
    @GetMapping
    public ResponseEntity<List<Reporte>> listarTodos() { return ResponseEntity.ok(reporteService.listarTodos()); }

    /**
     * GET /api/reportes/estadisticas - totales por tipo de falta y por grado, para el dashboard.
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() { return ResponseEntity.ok(reporteService.estadisticas()); }

    /**
     * GET /api/reportes/{id}
     * El personal ve cualquier reporte; un estudiante o acudiente solo ve los reportes
     * donde está implicado (si no, 403).
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id, Authentication auth) {
        if (!reporteService.puedeVer(auth.getName(), rolDe(auth), id))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "No tienes acceso a este reporte"));
        return reporteService.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/reportes
     * Body: { "lugar": "...", "descripcionHecho": "...", "latitud": 10.4, "longitud": -75.5 }
     * El tipo de falta (I, II o III) lo calcula el sistema a partir de la descripción;
     * las coordenadas son opcionales pero ayudan a asignar la EPS y la policía más cercanas.
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            if (body.get("lugar") == null || body.get("descripcionHecho") == null)
                return ResponseEntity.badRequest().body(Map.of("error", "lugar y descripcionHecho son obligatorios"));
            return ResponseEntity.status(HttpStatus.CREATED).body(reporteService.crear(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    /**
     * PUT /api/reportes/{id}
     * Solo funciona mientras el reporte esté PENDIENTE; si ya se firmó no se puede modificar.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(reporteService.actualizar(id, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/reportes/{id}
     * Solo DISCIPLINA, y únicamente reportes Tipo I o II. Los Tipo III nunca se eliminan
     * porque son evidencia de casos graves.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
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
     * Genera el formato oficial del SIUCE (Ley 1620 de 2013) de un reporte Tipo III.
     * Por ahora devuelve el formato listo para imprimir o reportar; no lo envía al Ministerio.
     */
    @PostMapping("/{id}/enviar-siuce")
    public ResponseEntity<?> enviarSiuce(@PathVariable String id) {
        try {
            return ResponseEntity.ok(reporteService.generarFormatoSiuce(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/reportes/{id}/firmar
     * Firma del rector (solo ROLE_RECTOR) sobre un reporte Tipo III. El estado pasa a FIRMADO
     * y se guarda el nombre de quien firmó.
     */
    @PostMapping("/{id}/firmar")
    public ResponseEntity<?> firmar(@PathVariable String id, Authentication auth) {
        try {
            return ResponseEntity.ok(reporteService.firmar(id, auth.getName()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
