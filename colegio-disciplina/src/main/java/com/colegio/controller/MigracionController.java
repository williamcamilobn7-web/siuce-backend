package com.colegio.controller;

import com.colegio.entity.*;
import com.colegio.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoint TEMPORAL para migrar datos locales a Atlas a través de Render.
 * IMPORTANTE: eliminar este controller (o el env MIGRACION_CLAVE) después de usarlo.
 */
@RestController
@RequestMapping("/api/migracion")
public class MigracionController {

    @Value("${migracion.clave:}")
    private String claveEsperada;

    private final EstudianteRepository estudianteRepo;
    private final AcudienteRepository acudienteRepo;
    private final MongoTemplate mongoTemplate;

    public MigracionController(EstudianteRepository estudianteRepo,
                                AcudienteRepository acudienteRepo,
                                MongoTemplate mongoTemplate) {
        this.estudianteRepo = estudianteRepo;
        this.acudienteRepo  = acudienteRepo;
        this.mongoTemplate  = mongoTemplate;
    }

    // Verifica la clave antes de cualquier operación
    private ResponseEntity<?> validarClave(String clave) {
        if (claveEsperada == null || claveEsperada.isBlank())
            return ResponseEntity.status(503).body(Map.of("error", "Migración no habilitada en este entorno"));
        if (!claveEsperada.equals(clave))
            return ResponseEntity.status(403).body(Map.of("error", "Clave incorrecta"));
        return null;
    }

    /** GET /api/migracion/estado?clave=XXX — cuántos documentos hay actualmente en Atlas */
    @GetMapping("/estado")
    public ResponseEntity<?> estado(@RequestParam String clave) {
        ResponseEntity<?> err = validarClave(clave);
        if (err != null) return err;
        return ResponseEntity.ok(Map.of(
            "estudiantes", estudianteRepo.count(),
            "acudientes",  acudienteRepo.count()
        ));
    }

    /** POST /api/migracion/limpiar?clave=XXX — borra TODAS las colecciones (úsalo antes de importar) */
    @PostMapping("/limpiar")
    public ResponseEntity<?> limpiar(@RequestParam String clave) {
        ResponseEntity<?> err = validarClave(clave);
        if (err != null) return err;
        mongoTemplate.remove(new Query(), "estudiantes");
        mongoTemplate.remove(new Query(), "acudientes");
        mongoTemplate.remove(new Query(), "usuarios");
        mongoTemplate.remove(new Query(), "reportes");
        mongoTemplate.remove(new Query(), "implicados");
        mongoTemplate.remove(new Query(), "intentos_login");
        mongoTemplate.remove(new Query(), "tokens_revocados");
        mongoTemplate.remove(new Query(), "colegios");
        mongoTemplate.remove(new Query(), "rectores");
        mongoTemplate.remove(new Query(), "entidades_salud");
        mongoTemplate.remove(new Query(), "policia");
        return ResponseEntity.ok(Map.of("mensaje", "Base de datos limpiada"));
    }

    /** POST /api/migracion/estudiantes?clave=XXX — importa un lote de estudiantes */
    @PostMapping("/estudiantes")
    public ResponseEntity<?> importarEstudiantes(@RequestParam String clave,
                                                  @RequestBody List<Estudiante> lista) {
        ResponseEntity<?> err = validarClave(clave);
        if (err != null) return err;
        try {
            List<Estudiante> guardados = estudianteRepo.saveAll(lista);
            return ResponseEntity.ok(Map.of("insertados", guardados.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/migracion/acudientes?clave=XXX — importa un lote de acudientes */
    @PostMapping("/acudientes")
    public ResponseEntity<?> importarAcudientes(@RequestParam String clave,
                                                 @RequestBody List<Acudiente> lista) {
        ResponseEntity<?> err = validarClave(clave);
        if (err != null) return err;
        try {
            List<Acudiente> guardados = acudienteRepo.saveAll(lista);
            return ResponseEntity.ok(Map.of("insertados", guardados.size()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** POST /api/migracion/raw?clave=XXX&coleccion=nombre — importa JSON crudo a cualquier colección */
    @PostMapping("/raw")
    public ResponseEntity<?> importarRaw(@RequestParam String clave,
                                          @RequestParam String coleccion,
                                          @RequestBody List<Map<String, Object>> lista) {
        ResponseEntity<?> err = validarClave(clave);
        if (err != null) return err;
        try {
            for (Map<String, Object> doc : lista) {
                mongoTemplate.save(doc, coleccion);
            }
            return ResponseEntity.ok(Map.of("insertados", lista.size(), "coleccion", coleccion));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/migracion/actualizar?clave=XXX&coleccion=nombre&id=XXX — actualiza un campo de un documento */
    @PutMapping("/actualizar")
    public ResponseEntity<?> actualizar(@RequestParam String clave,
                                         @RequestParam String coleccion,
                                         @RequestParam String id,
                                         @RequestBody Map<String, Object> campos) {
        ResponseEntity<?> err = validarClave(clave);
        if (err != null) return err;
        try {
            org.springframework.data.mongodb.core.query.Query query =
                new org.springframework.data.mongodb.core.query.Query(
                    org.springframework.data.mongodb.core.query.Criteria.where("_id").is(id));
            org.bson.Document updateDoc = new org.bson.Document("$set", new org.bson.Document(campos));
            com.mongodb.client.result.UpdateResult result =
                mongoTemplate.getCollection(coleccion).updateOne(query.getQueryObject(), updateDoc);
            return ResponseEntity.ok(Map.of(
                "matched", result.getMatchedCount(),
                "modified", result.getModifiedCount()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
