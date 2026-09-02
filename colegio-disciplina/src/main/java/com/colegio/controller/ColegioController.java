package com.colegio.controller;

import com.colegio.entity.Colegio;
import com.colegio.repository.ColegioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/colegios")
@CrossOrigin(origins = "*")
public class ColegioController {

    private final ColegioRepository colegioRepo;

    public ColegioController(ColegioRepository colegioRepo) {
        this.colegioRepo = colegioRepo;
    }

    @GetMapping
    public ResponseEntity<List<Colegio>> listar() {
        return ResponseEntity.ok(colegioRepo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return colegioRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, String> body) {
        try {
            Colegio c = new Colegio();
            c.setNombre(body.get("nombre"));
            c.setSede(body.get("sede"));
            c.setNit(body.get("nit"));
            return ResponseEntity.status(HttpStatus.CREATED).body(colegioRepo.save(c));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
