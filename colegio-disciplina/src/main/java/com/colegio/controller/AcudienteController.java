package com.colegio.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.colegio.entity.Acudiente;
import com.colegio.repository.AcudienteRepository;

/**
 * Todo lo relacionado con los acudientes (padre, madre o tutor de un estudiante).
 * Base URL: /api/acudientes
 */
@RestController
@RequestMapping("/api/acudientes")
public class AcudienteController {

    private final AcudienteRepository acudienteRepo;

    public AcudienteController(AcudienteRepository acudienteRepo) { this.acudienteRepo = acudienteRepo; }

    /**
     * GET /api/acudientes
     * Devuelve todos los acudientes registrados.
     */
    @GetMapping
    public ResponseEntity<List<Acudiente>> listar() { return ResponseEntity.ok(acudienteRepo.findAll()); }

    /**
     * GET /api/acudientes/{id}
     * Busca un acudiente por su id. Devuelve 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable String id) {
        return acudienteRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/acudientes
     * Body: { "nombreCompleto": "", "telefono": "", "email": "" }
     * Solo el nombre es obligatorio; el resto es opcional.
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, String> body) {
        try {
            if (body.get("nombreCompleto") == null || body.get("nombreCompleto").isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "nombreCompleto es obligatorio"));
            Acudiente a = new Acudiente();
            a.setTipoDocumento(body.get("tipoDocumento"));
            a.setNumeroDocumento(body.get("numeroDocumento"));
            a.setNombreCompleto(body.get("nombreCompleto"));
            a.setTelefono(body.get("telefono"));
            a.setEmail(body.get("email"));
            a.setNotificado(false);
            return ResponseEntity.status(HttpStatus.CREATED).body(acudienteRepo.save(a));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/acudientes/{id}/notificar
     * Marca al acudiente como notificado del incidente (campo notificado = true).
     */
    @PutMapping("/{id}/notificar")
    public ResponseEntity<?> notificar(@PathVariable String id) {
        return acudienteRepo.findById(id).map(a -> {
            a.setNotificado(true);
            return ResponseEntity.ok((Object) acudienteRepo.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/acudientes/carga-masiva
     * Carga acudientes desde un CSV con las columnas:
     * tipoDocumento, numeroDocumento, nombreCompleto, telefono, email
     * Se guarda de a 500 para no hacer una consulta por cada fila.
     */
    @PostMapping("/carga-masiva")
    public ResponseEntity<?> cargaMasiva(@RequestParam("archivo") MultipartFile archivo) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            int errores = 0;
            List<Acudiente> lote = new ArrayList<>();
            reader.readLine(); // encabezado
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] cols = linea.split(",", -1);
                if (cols.length < 5) { errores++; continue; }
                Acudiente a = new Acudiente();
                a.setTipoDocumento(cols[0].trim());
                a.setNumeroDocumento(cols[1].trim());
                a.setNombreCompleto(cols[2].trim());
                a.setTelefono(cols[3].trim());
                a.setEmail(cols[4].trim());
                a.setNotificado(false);
                lote.add(a);
                if (lote.size() >= 500) { acudienteRepo.saveAll(lote); lote.clear(); }
            }
            int restantes = lote.size();
            acudienteRepo.saveAll(lote);
            return ResponseEntity.ok(Map.of("mensaje", "Carga completada", "errores", errores,
                    "ultimoLote", restantes));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
