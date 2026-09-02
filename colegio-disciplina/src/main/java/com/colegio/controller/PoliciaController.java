package com.colegio.controller;

import com.colegio.entity.Policia;
import com.colegio.repository.PoliciaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/policia")
@CrossOrigin(origins = "*")
public class PoliciaController {

    private final PoliciaRepository repo;

    public PoliciaController(PoliciaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<Policia>> listar() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return repo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(p -> {
            if (body.get("nombre") != null) p.setNombre(body.get("nombre").toString());
            if (body.get("estacion") != null) p.setEstacion(body.get("estacion").toString());
            if (body.get("telefonoEmergencia") != null) p.setTelefonoEmergencia(body.get("telefonoEmergencia").toString());
            if (body.get("latitud") != null) p.setLatitud(Double.valueOf(body.get("latitud").toString()));
            if (body.get("longitud") != null) p.setLongitud(Double.valueOf(body.get("longitud").toString()));
            return (ResponseEntity<?>) ResponseEntity.ok(repo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            Policia p = new Policia();
            if (body.get("nombre") != null) p.setNombre(body.get("nombre").toString());
            if (body.get("estacion") != null) p.setEstacion(body.get("estacion").toString());
            if (body.get("telefonoEmergencia") != null) p.setTelefonoEmergencia(body.get("telefonoEmergencia").toString());
            if (body.get("latitud") != null) p.setLatitud(Double.valueOf(body.get("latitud").toString()));
            if (body.get("longitud") != null) p.setLongitud(Double.valueOf(body.get("longitud").toString()));
            return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(p));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
