package com.colegio.controller;

import com.colegio.entity.EntidadSalud;
import com.colegio.repository.EntidadSaludRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
