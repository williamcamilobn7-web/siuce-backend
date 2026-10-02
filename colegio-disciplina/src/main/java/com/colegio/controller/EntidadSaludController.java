package com.colegio.controller;

import com.colegio.entity.EntidadSalud;
import com.colegio.repository.EntidadSaludRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.http.HttpStatus;
import java.util.List;

/**
 * Controller que gestiona las Entidades Promotoras de Salud (EPS).
 * Las EPS se asignan automáticamente a los reportes Tipo III
 * según cuál esté más cerca del lugar del incidente (usando coordenadas GPS).
 * Base URL: /api/entidades-salud
 */
@RestController
@RequestMapping("/api/entidades-salud")
@CrossOrigin(origins = "*")
public class EntidadSaludController {

    // Repositorio que se comunica con la tabla entidades_salud en la BD
    private final EntidadSaludRepository repo;

    public EntidadSaludController(EntidadSaludRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /api/entidades-salud
     * Devuelve la lista de todas las EPS registradas con sus coordenadas.
     */
    @GetMapping
    public ResponseEntity<List<EntidadSalud>> listar() {
        return ResponseEntity.ok(repo.findAll());
    }

    /**
     * GET /api/entidades-salud/{id}
     * Busca una EPS específica por su ID.
     * Devuelve 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/entidades-salud/{id}
     * Actualiza los datos de una EPS existente.
     * Se usa principalmente para agregar o actualizar las coordenadas GPS
     * que permiten calcular la distancia al lugar del incidente.
     * Body: { "nombre": "", "telefono": "", "direccion": "",
     *          "latitud": 10.39, "longitud": -75.51 }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(e -> {
            if (body.get("nombre") != null) e.setNombre(body.get("nombre").toString());
            if (body.get("telefono") != null) e.setTelefono(body.get("telefono").toString());
            if (body.get("direccion") != null) e.setDireccion(body.get("direccion").toString());
            if (body.get("latitud") != null)
                e.setLatitud(Double.valueOf(body.get("latitud").toString()));
            if (body.get("longitud") != null)
                e.setLongitud(Double.valueOf(body.get("longitud").toString()));
            return (ResponseEntity<?>) ResponseEntity.ok(repo.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/entidades-salud
     * Registra una nueva EPS en el sistema.
     * Es importante incluir latitud y longitud para que el sistema pueda
     * asignarla automáticamente a reportes Tipo III según proximidad.
     * Body: { "nombre": "", "telefono": "", "direccion": "",
     *          "latitud": 10.39, "longitud": -75.51 }
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            EntidadSalud e = new EntidadSalud();
            if (body.get("nombre") != null) e.setNombre(body.get("nombre").toString());
            if (body.get("telefono") != null) e.setTelefono(body.get("telefono").toString());
            if (body.get("direccion") != null) e.setDireccion(body.get("direccion").toString());
            if (body.get("latitud") != null)
                e.setLatitud(Double.valueOf(body.get("latitud").toString()));
            if (body.get("longitud") != null)
                e.setLongitud(Double.valueOf(body.get("longitud").toString()));
            return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(e));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
