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
    private final ReporteRepository reporteRepo;
    private final ImplicadoRepository implicadoRepo;
    private final UsuarioRepository usuarioRepo;
    private final MongoTemplate mongoTemplate;

    public MigracionController(EstudianteRepository estudianteRepo,
                                AcudienteRepository acudienteRepo,
                                ReporteRepository reporteRepo,
                                ImplicadoRepository implicadoRepo,
                                UsuarioRepository usuarioRepo,
                                MongoTemplate mongoTemplate) {
        this.estudianteRepo = estudianteRepo;
        this.acudienteRepo  = acudienteRepo;
        this.reporteRepo    = reporteRepo;
        this.implicadoRepo  = implicadoRepo;
        this.usuarioRepo    = usuarioRepo;
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

    /** POST /api/migracion/preparar-acudiente?clave=XXX&email=XXX — Enlaza acudiente con reportes y asigna el email */
    @PostMapping("/preparar-acudiente")
    public ResponseEntity<?> prepararAcudiente(@RequestParam String clave,
                                               @RequestParam String email) {
        ResponseEntity<?> err = validarClave(clave);
        if (err != null) return err;
        try {
            String correoNorm = email.trim().toLowerCase();

            // 1. Limpiar el email de cualquier usuario que NO sea acudiente (ej: disciplina) para evitar choques
            usuarioRepo.findByEmailIgnoreCase(correoNorm).ifPresent(u -> {
                if (!"ACUDIENTE".equals(u.getRol())) {
                    u.setEmail(null);
                    usuarioRepo.save(u);
                }
            });

            // 2. Obtener un reporte
            List<Reporte> reportes = reporteRepo.findAll();
            if (reportes.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No hay reportes en la base de datos"));
            }
            Reporte rep = reportes.get(0);

            // 3. Obtener un estudiante y un acudiente
            List<Estudiante> estudiantes = estudianteRepo.findAll();
            if (estudiantes.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No hay estudiantes en la base de datos"));
            }
            Estudiante est = estudiantes.get(0);

            List<Acudiente> acudientes = acudienteRepo.findAll();
            if (acudientes.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No hay acudientes en la base de datos"));
            }
            Acudiente acu = acudientes.get(0);

            // 4. Asignar el email al acudiente y guardar
            acu.setEmail(correoNorm);
            acudienteRepo.save(acu);

            // 5. Vincular estudiante con este acudiente
            est.setAcudienteId(acu.getId());
            estudianteRepo.save(est);

            // 6. Asegurar un Implicado que conecte el reporte con el estudiante y el acudiente
            List<Implicado> imps = implicadoRepo.findByReporteId(rep.getId());
            Implicado imp;
            if (imps.isEmpty()) {
                imp = new Implicado();
                imp.setReporteId(rep.getId());
                imp.setEstudianteId(est.getId());
                imp.setAcudienteId(acu.getId());
                imp.setRol("VICTIMA");
            } else {
                imp = imps.get(0);
                imp.setEstudianteId(est.getId());
                imp.setAcudienteId(acu.getId());
            }
            implicadoRepo.save(imp);

            return ResponseEntity.ok(Map.of(
                "mensaje", "Acudiente preparado exitosamente con reportes vinculados",
                "acudienteId", acu.getId(),
                "acudienteNombre", acu.getNombreCompleto(),
                "acudienteEmail", acu.getEmail(),
                "estudianteNombre", est.getNombres() + " " + est.getApellidos(),
                "reporteId", rep.getId(),
                "tipoFalta", rep.getTipoFalta() != null ? rep.getTipoFalta() : "TIPO_I",
                "estadoReporte", rep.getEstado() != null ? rep.getEstado() : "PENDIENTE"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
