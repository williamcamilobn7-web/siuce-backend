package com.colegio.controller;

import com.colegio.entity.Estudiante;
import com.colegio.service.EstudianteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Registro completo de estudiantes. El listado es paginado para poder manejar los 10.000 registros.
 * Base URL: /api/estudiantes
 */
@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) { this.estudianteService = estudianteService; }

    /**
     * GET /api/estudiantes?page=0&size=50
     * Devuelve una página de estudiantes: { contenido, totalElementos, totalPaginas, paginaActual }
     */
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "50") int size) {
        // Se limita el tamaño de página para que nadie pida los 10.000 de una sola vez
        size = Math.min(Math.max(size, 1), 200);
        Page<Estudiante> r = estudianteService.listarPaginado(PageRequest.of(Math.max(page, 0), size));
        return ResponseEntity.ok(Map.of(
                "contenido", r.getContent(),
                "totalElementos", r.getTotalElements(),
                "totalPaginas", r.getTotalPages(),
                "paginaActual", r.getNumber()));
    }

    /**
     * GET /api/estudiantes/buscar?q=texto
     * Busca por nombre, apellido o documento (mínimo 2 caracteres, máximo 20 resultados).
     * Lo usa el buscador del modal de agregar implicados.
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Estudiante>> buscar(@RequestParam String q) {
        return ResponseEntity.ok(estudianteService.buscar(q));
    }

    /**
     * GET /api/estudiantes/estadisticas
     * Totales por grado: cantidad de estudiantes, promedio académico e inasistencias.
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(estudianteService.estadisticas());
    }

    /**
     * GET /api/estudiantes/documento/{doc}
     * Busca por número de documento.
     */
    @GetMapping("/documento/{doc}")
    public ResponseEntity<?> buscarPorDocumento(@PathVariable String doc) {
        return estudianteService.buscarPorDocumento(doc).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/estudiantes/{id}
     * Busca por id. 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        return estudianteService.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/estudiantes
     * Obligatorios: numeroDocumento, nombres y apellidos. El resto es opcional.
     * Responde 400 si faltan o si el documento ya existe.
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            if (body.get("numeroDocumento") == null || body.get("nombres") == null || body.get("apellidos") == null)
                return ResponseEntity.badRequest().body(Map.of("error", "numeroDocumento, nombres y apellidos son obligatorios"));
            return ResponseEntity.status(HttpStatus.CREATED).body(estudianteService.crear(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    /**
     * PUT /api/estudiantes/{id}
     * Actualiza solo los campos que lleguen en el body. 404 si el estudiante no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(estudianteService.actualizar(id, body));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Estudiante no encontrado"))
                return ResponseEntity.notFound().build();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", String.valueOf(e.getMessage())));
        }
    }

    /**
     * DELETE /api/estudiantes/{id}
     * Solo el rol DISCIPLINA. 404 si no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            estudianteService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Estudiante eliminado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}