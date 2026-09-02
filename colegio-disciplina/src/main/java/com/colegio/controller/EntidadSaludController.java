package com.colegio.controller;

import com.colegio.entity.EntidadSalud;
import com.colegio.repository.EntidadSaludRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/entidades-salud")
@CrossOrigin(origins = "*")
public class EntidadSaludController {

    private final EntidadSaludRepository repo;

    public EntidadSaludController(EntidadSaludRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<EntidadSalud>> listar() {
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
        return repo.findById(id).map(e -> {
            if (body.get("nombre") != null) e.setNombre(body.get("nombre").toString());
            if (body.get("telefono") != null) e.setTelefono(body.get("telefono").toString());
            if (body.get("direccion") != null) e.setDireccion(body.get("direccion").toString());
            if (body.get("latitud") != null) e.setLatitud(Double.valueOf(body.get("latitud").toString()));
            if (body.get("longitud") != null) e.setLongitud(Double.valueOf(body.get("longitud").toString()));
            return (ResponseEntity<?>) ResponseEntity.ok(repo.save(e));
        }).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            EntidadSalud e = new EntidadSalud();
            if (body.get("nombre") != null) e.setNombre(body.get("nombre").toString());
            if (body.get("telefono") != null) e.setTelefono(body.get("telefono").toString());
            if (body.get("direccion") != null) e.setDireccion(body.get("direccion").toString());
            if (body.get("latitud") != null) e.setLatitud(Double.valueOf(body.get("latitud").toString()));
            if (body.get("longitud") != null) e.setLongitud(Double.valueOf(body.get("longitud").toString()));
            return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(e));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

}
