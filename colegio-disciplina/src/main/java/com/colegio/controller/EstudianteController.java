package com.colegio.controller;

import com.colegio.entity.Estudiante;
import com.colegio.service.EstudianteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes")
@CrossOrigin(origins = "*")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Estudiante> resultado = estudianteService.listarPaginado(PageRequest.of(page, size));
        return ResponseEntity.ok(Map.of(
                "contenido",       resultado.getContent(),
                "totalElementos",  resultado.getTotalElements(),
                "totalPaginas",    resultado.getTotalPages(),
                "paginaActual",    resultado.getNumber()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return estudianteService.buscarPorId(id)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/documento/{doc}")
    public ResponseEntity<?> buscarPorDocumento(@PathVariable String doc) {
        return estudianteService.buscarPorDocumento(doc)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Estudiante>> buscar(@RequestParam String q) {
        if (q == null || q.trim().length() < 2) return ResponseEntity.ok(List.of());
        String query = q.toLowerCase().trim();
        List<Estudiante> resultado = estudianteService.listarTodos().stream()
                .filter(e ->
                        e.getNombres().toLowerCase().contains(query) ||
                                e.getApellidos().toLowerCase().contains(query) ||
                                e.getNumeroDocumento().contains(query))
                .limit(20)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(estudianteService.estadisticas());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            if (body.get("numeroDocumento") == null || body.get("nombres") == null || body.get("apellidos") == null)
                return ResponseEntity.badRequest().body(Map.of("error", "numeroDocumento, nombres y apellidos son obligatorios"));
            return ResponseEntity.status(HttpStatus.CREATED).body(estudianteService.crear(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(estudianteService.actualizar(id, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            estudianteService.eliminar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Estudiante eliminado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}