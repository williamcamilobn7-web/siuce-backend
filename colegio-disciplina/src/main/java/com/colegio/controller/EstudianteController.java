package com.colegio.controller;

import com.colegio.entity.Estudiante;
import com.colegio.service.EstudianteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Controller que gestiona el registro completo de estudiantes.
 * Incluye paginación para manejar eficientemente los 10.000 registros.
 * Base URL: /api/estudiantes
 */
@RestController
@RequestMapping("/api/estudiantes")
@CrossOrigin(origins = "*")
public class EstudianteController {

    // Servicio que contiene la lógica de negocio de estudiantes
    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    /**
     * GET /api/estudiantes?page=0&size=50
     * Devuelve los estudiantes de forma paginada para no sobrecargar el sistema.
     * Por defecto devuelve la primera página con 50 estudiantes.
     * Respuesta: { contenido: [], totalElementos, totalPaginas, paginaActual }
     */
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Estudiante> resultado = estudianteService.listarPaginado(
                PageRequest.of(page, size));
        return ResponseEntity.ok(Map.of(
                "contenido",      resultado.getContent(),
                "totalElementos", resultado.getTotalElements(),
                "totalPaginas",   resultado.getTotalPages(),
                "paginaActual",   resultado.getNumber()
        ));
    }

    /**
     * GET /api/estudiantes/{id}
     * Busca un estudiante específico por su ID de base de datos.
     * Devuelve 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return estudianteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/estudiantes/documento/{doc}
     * Busca un estudiante por su número de documento de identidad.
     * Útil para el login de estudiantes y para verificar si ya existe.
     */
    @GetMapping("/documento/{doc}")
    public ResponseEntity<?> buscarPorDocumento(@PathVariable String doc) {
        return estudianteService.buscarPorDocumento(doc)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/estudiantes/buscar?q=texto
     * Busca estudiantes en tiempo real por nombre, apellido o documento.
     * Solo funciona con mínimo 2 caracteres y devuelve máximo 20 resultados.
     * Se usa en el buscador del modal de agregar implicados.
     */
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

    /**
     * GET /api/estudiantes/estadisticas
     * Devuelve estadísticas agrupadas por grado:
     * total de estudiantes, promedio académico y total de inasistencias.
     * Se usa en el dashboard para mostrar métricas institucionales.
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(estudianteService.estadisticas());
    }

    /**
     * POST /api/estudiantes
     * Crea un nuevo estudiante en el sistema.
     * Campos obligatorios: numeroDocumento, nombres, apellidos.
     * Campos opcionales: fechaNacimiento, genero, grado, jornada, sede,
     *                    promedioAcademico, numeroInasistencias, colegioId, acudienteId.
     * Devuelve 400 si faltan campos obligatorios.
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            if (body.get("numeroDocumento") == null ||
                    body.get("nombres") == null ||
                    body.get("apellidos") == null)
                return ResponseEntity.badRequest().body(
                        Map.of("error", "numeroDocumento, nombres y apellidos son obligatorios"));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(estudianteService.crear(body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/estudiantes/{id}
     * Actualiza los datos de un estudiante existente.
     * Solo actualiza los campos que se envíen en el body.
     * Devuelve 404 si el estudiante no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(estudianteService.actualizar(id, body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/estudiantes/{id}
     * Elimina un estudiante del sistema por su ID.
     * Solo puede ejecutarlo un usuario con rol DISCIPLINA.
     * Devuelve 404 si el estudiante no existe.
     */
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