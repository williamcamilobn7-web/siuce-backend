package com.colegio.controller;

import com.colegio.entity.Policia;
import com.colegio.repository.PoliciaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
