package com.colegio.controller;

import com.colegio.entity.Policia;
import com.colegio.repository.PoliciaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.http.HttpStatus;
import java.util.List;

/**
 * Controller que gestiona los cuadrantes de policía.
 * Los cuadrantes se asignan automáticamente a los reportes Tipo III
 * según cuál esté más cerca del lugar del incidente (usando coordenadas GPS).
 * Base URL: /api/policia
 */
@RestController
@RequestMapping("/api/policia")
@CrossOrigin(origins = "*")
public class PoliciaController {

    // Repositorio que se comunica con la tabla policia en la BD
    private final PoliciaRepository repo;

    public PoliciaController(PoliciaRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /api/policia
     * Devuelve la lista de todos los cuadrantes de policía registrados
     * con sus coordenadas GPS y datos de contacto.
     */
    @GetMapping
    public ResponseEntity<List<Policia>> listar() {
        return ResponseEntity.ok(repo.findAll());
    }

    /**
     * GET /api/policia/{id}
     * Busca un cuadrante específico por su ID.
     * Devuelve 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/policia/{id}
     * Actualiza los datos de un cuadrante existente.
     * Se usa principalmente para agregar o actualizar las coordenadas GPS
     * que permiten calcular la distancia al lugar del incidente.
     * Body: { "nombre": "", "estacion": "", "telefonoEmergencia": "",
     *          "latitud": 10.39, "longitud": -75.51 }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(p -> {
            if (body.get("nombre") != null) p.setNombre(body.get("nombre").toString());
            if (body.get("estacion") != null) p.setEstacion(body.get("estacion").toString());
            if (body.get("telefonoEmergencia") != null)
                p.setTelefonoEmergencia(body.get("telefonoEmergencia").toString());
            if (body.get("latitud") != null)
                p.setLatitud(Double.valueOf(body.get("latitud").toString()));
            if (body.get("longitud") != null)
                p.setLongitud(Double.valueOf(body.get("longitud").toString()));
            return (ResponseEntity<?>) ResponseEntity.ok(repo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/policia
     * Registra un nuevo cuadrante de policía en el sistema.
     * Es importante incluir latitud y longitud para que el sistema pueda
     * asignarlo automáticamente a reportes Tipo III según proximidad.
     * Body: { "nombre": "", "estacion": "", "telefonoEmergencia": "",
     *          "latitud": 10.39, "longitud": -75.51 }
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            Policia p = new Policia();
            if (body.get("nombre") != null) p.setNombre(body.get("nombre").toString());
            if (body.get("estacion") != null) p.setEstacion(body.get("estacion").toString());
            if (body.get("telefonoEmergencia") != null)
                p.setTelefonoEmergencia(body.get("telefonoEmergencia").toString());
            if (body.get("latitud") != null)
                p.setLatitud(Double.valueOf(body.get("latitud").toString()));
            if (body.get("longitud") != null)
                p.setLongitud(Double.valueOf(body.get("longitud").toString()));
            return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(p));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}