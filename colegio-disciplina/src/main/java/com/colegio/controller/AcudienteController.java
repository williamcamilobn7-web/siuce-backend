package com.colegio.controller;

import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    @PostMapping("/carga-masiva")
    public ResponseEntity<?> cargaMasiva(@RequestParam("archivo") MultipartFile archivo) {
        try {
            int creados = 0, errores = 0;
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8));
            reader.readLine();
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                try {
                    String[] cols = linea.split(",", -1);
                    if (cols.length < 5) continue;
                    Acudiente a = new Acudiente();
                    a.setTipoDocumento(cols[0].trim());
                    a.setNumeroDocumento(cols[1].trim());
                    a.setNombreCompleto(cols[2].trim());
                    a.setTelefono(cols[3].trim());
                    a.setEmail(cols[4].trim());
                    acudienteRepo.save(a);
                    creados++;
                } catch (Exception ex) { errores++; }
            }
            reader.close();
            return ResponseEntity.ok(Map.of("creados", creados, "errores", errores));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
