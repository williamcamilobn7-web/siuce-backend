package com.colegio.controller;

import com.colegio.entity.Acudiente;
import com.colegio.repository.AcudienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/acudientes")
@CrossOrigin(origins = "*")
public class AcudienteController {

    private final AcudienteRepository acudienteRepo;

    public AcudienteController(AcudienteRepository acudienteRepo) {
        this.acudienteRepo = acudienteRepo;
    }

    @GetMapping
    public ResponseEntity<List<Acudiente>> listar() {
        return ResponseEntity.ok(acudienteRepo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return acudienteRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, String> body) {
        try {
            Acudiente a = new Acudiente();
            a.setNombreCompleto(body.get("nombreCompleto"));
            a.setTelefono(body.get("telefono"));
            a.setEmail(body.get("email"));
            return ResponseEntity.status(HttpStatus.CREATED).body(acudienteRepo.save(a));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/acudientes/{id}/notificar
    @PutMapping("/{id}/notificar")
    public ResponseEntity<?> notificar(@PathVariable Long id) {
        return acudienteRepo.findById(id).map(a -> {
            a.setNotificado(true);
            return ResponseEntity.ok(acudienteRepo.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }
}
