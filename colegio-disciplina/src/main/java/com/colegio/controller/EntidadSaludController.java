package com.colegio.controller;

import com.colegio.entity.EntidadSalud;
import com.colegio.repository.EntidadSaludRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Gestiona las EPS. A los reportes Tipo III se les asigna automáticamente la más cercana
 * al lugar del incidente usando las coordenadas GPS. Base URL: /api/entidades-salud
 */
@RestController
@RequestMapping("/api/entidades-salud")
public class EntidadSaludController {

    private final EntidadSaludRepository repo;

    public EntidadSaludController(EntidadSaludRepository repo) { this.repo = repo; }

    /**
     * GET /api/entidades-salud - todas las EPS con sus coordenadas.
     */
    @GetMapping
    public ResponseEntity<List<EntidadSalud>> listar() { return ResponseEntity.ok(repo.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/entidades-salud/{id}
     * Actualiza los datos de una EPS. Se usa sobre todo para agregar o corregir las coordenadas.
     * Solo cambia los campos que vengan en el body.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            return repo.findById(id).map(x -> {
                aplicar(x, body);
                return ResponseEntity.ok((Object) repo.save(x));
            }).orElse(ResponseEntity.notFound().build());
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "latitud/longitud inválidas"));
        }
    }

    /**
     * POST /api/entidades-salud
     * Registra una EPS. Conviene enviar latitud y longitud; sin ellas no se puede calcular la cercanía.
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            EntidadSalud x = new EntidadSalud();
            aplicar(x, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(x));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", String.valueOf(ex.getMessage())));
        }
    }

    // Copia al objeto solo los campos que llegaron en el body
    private void aplicar(EntidadSalud x, Map<String, Object> body) {
            if (body.get("nombre") != null) x.setNombre(body.get("nombre").toString());
            if (body.get("telefono") != null) x.setTelefono(body.get("telefono").toString());
            if (body.get("direccion") != null) x.setDireccion(body.get("direccion").toString());
        if (body.get("latitud") != null) x.setLatitud(Double.valueOf(body.get("latitud").toString()));
        if (body.get("longitud") != null) x.setLongitud(Double.valueOf(body.get("longitud").toString()));
    }
}
