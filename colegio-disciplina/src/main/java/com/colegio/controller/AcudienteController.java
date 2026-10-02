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

/**
 * Controller que maneja todo lo relacionado con los acudiantes.
 * Un acudiente es el padre, madre o tutor responsable de un estudiante.
 * Base URL: /api/acudientes
 */
@RestController
@RequestMapping("/api/acudientes")
@CrossOrigin(origins = "*")
public class AcudienteController {

    // Repositorio que se comunica directamente con la tabla acudientes en la BD
    private final AcudienteRepository acudienteRepo;

    // Spring inyecta automáticamente el repositorio al crear el controller
    public AcudienteController(AcudienteRepository acudienteRepo) {
        this.acudienteRepo = acudienteRepo;
    }

    /**
     * GET /api/acudientes
     * Devuelve la lista completa de todos los acudientes registrados.
     * Respuesta: lista de objetos Acudiente en formato JSON
     */
    @GetMapping
    public ResponseEntity<List<Acudiente>> listar() {
        return ResponseEntity.ok(acudienteRepo.findAll());
    }

    /**
     * GET /api/acudientes/{id}
     * Busca un acudiente específico por su ID.
     * Si existe devuelve el acudiente, si no devuelve 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return acudienteRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/acudientes
     * Crea un nuevo acudiente en la base de datos.
     * Body esperado: { "nombreCompleto": "", "telefono": "", "email": "" }
     * Devuelve el acudiente creado con su ID asignado o error 400 si algo falla.
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, String> body) {
        try {
            Acudiente a = new Acudiente();
            a.setNombreCompleto(body.get("nombreCompleto"));
            a.setTelefono(body.get("telefono"));
            a.setEmail(body.get("email"));
            // Guarda en BD y devuelve 201 Created con el objeto guardado
            return ResponseEntity.status(HttpStatus.CREATED).body(acudienteRepo.save(a));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * PUT /api/acudientes/{id}/notificar
     * Marca a un acudiente como notificado de un incidente disciplinario.
     * Cambia el campo notificado a true y guarda el cambio en la BD.
     * Devuelve el acudiente actualizado o 404 si no existe.
     */
    @PutMapping("/{id}/notificar")
    public ResponseEntity<?> notificar(@PathVariable Long id) {
        return acudienteRepo.findById(id).map(a -> {
            a.setNotificado(true);
            return ResponseEntity.ok(acudienteRepo.save(a));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/acudientes/carga-masiva
     * Carga miles de acudientes desde un archivo CSV.
     * El archivo debe tener columnas: tipoDocumento, numeroDocumento,
     * nombreCompleto, telefono, email
     * Devuelve cuántos registros fueron creados y cuántos tuvieron error.
     */
    @PostMapping("/carga-masiva")
    public ResponseEntity<?> cargaMasiva(@RequestParam("archivo") MultipartFile archivo) {
        try {
            int creados = 0, errores = 0;
            // Lee el archivo línea por línea
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8));
            reader.readLine(); // Salta la primera línea que es el encabezado
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.trim().isEmpty()) continue; // Ignora líneas vacías
                try {
                    String[] cols = linea.split(",", -1);
                    if (cols.length < 5) continue; // Ignora líneas con columnas insuficientes
                    Acudiente a = new Acudiente();
                    a.setTipoDocumento(cols[0].trim());
                    a.setNumeroDocumento(cols[1].trim());
                    a.setNombreCompleto(cols[2].trim());
                    a.setTelefono(cols[3].trim());
                    a.setEmail(cols[4].trim());
                    acudienteRepo.save(a);
                    creados++;
                } catch (Exception ex) { errores++; } // Cuenta errores por fila
            }
            reader.close();
            return ResponseEntity.ok(Map.of("creados", creados, "errores", errores));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}