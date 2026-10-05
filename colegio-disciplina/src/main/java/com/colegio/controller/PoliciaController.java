package com.colegio.controller;

import com.colegio.entity.Policia;
import com.colegio.repository.PoliciaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Gestiona los cuadrantes de policía. Igual que con las EPS, a los reportes Tipo III
 * se les asigna el más cercano usando las coordenadas. Base URL: /api/policia
 */
@RestController
@RequestMapping("/api/policia")
public class PoliciaController {

    private final PoliciaRepository repo;

    public PoliciaController(PoliciaRepository repo) { this.repo = repo; }

    /**
     * GET /api/policia - todos los cuadrantes con sus coordenadas.
     */
    @GetMapping
    public ResponseEntity<List<Policia>> listar() { return ResponseEntity.ok(repo.findAll()); }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/policia/{id}
     * Actualiza un cuadrante; solo cambia los campos que vengan en el body.
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
     * POST /api/policia
     * Registra un cuadrante. Incluye latitud y longitud para que pueda asignarse por cercanía.
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            Policia x = new Policia();
            aplicar(x, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(x));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", String.valueOf(ex.getMessage())));
        }
    }

    // Copia al objeto solo los campos que llegaron en el body
    private void aplicar(Policia x, Map<String, Object> body) {
            if (body.get("nombre") != null) x.setNombre(body.get("nombre").toString());
            if (body.get("estacion") != null) x.setEstacion(body.get("estacion").toString());
            if (body.get("telefonoEmergencia") != null) x.setTelefonoEmergencia(body.get("telefonoEmergencia").toString());
        if (body.get("latitud") != null) x.setLatitud(Double.valueOf(body.get("latitud").toString()));
        if (body.get("longitud") != null) x.setLongitud(Double.valueOf(body.get("longitud").toString()));
    }
}
