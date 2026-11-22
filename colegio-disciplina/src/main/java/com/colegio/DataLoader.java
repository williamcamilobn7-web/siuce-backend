package com.colegio;

import com.colegio.entity.*;
import com.colegio.repository.*;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Datos iniciales para poder probar el sistema. Se ejecuta al arrancar y carga
 * las colecciones completas desde los archivos JSON empaquetados si están vacías.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final ColegioRepository colegioRepo;
    private final RectorRepository rectorRepo;
    private final AcudienteRepository acudienteRepo;
    private final EntidadSaludRepository entidadSaludRepo;
    private final PoliciaRepository policiaRepo;
    private final UsuarioRepository usuarioRepo;
    private final EstudianteRepository estudianteRepo;
    private final ReporteRepository reporteRepo;
    private final PasswordEncoder encoder;
    private final MongoTemplate mongoTemplate;

    public DataLoader(ColegioRepository colegioRepo, RectorRepository rectorRepo, AcudienteRepository acudienteRepo,
                      EntidadSaludRepository entidadSaludRepo, PoliciaRepository policiaRepo,
                      UsuarioRepository usuarioRepo, EstudianteRepository estudianteRepo,
                      ReporteRepository reporteRepo, PasswordEncoder encoder, MongoTemplate mongoTemplate) {
        this.colegioRepo = colegioRepo;
        this.rectorRepo = rectorRepo;
        this.acudienteRepo = acudienteRepo;
        this.entidadSaludRepo = entidadSaludRepo;
        this.policiaRepo = policiaRepo;
        this.usuarioRepo = usuarioRepo;
        this.estudianteRepo = estudianteRepo;
        this.reporteRepo = reporteRepo;
        this.encoder = encoder;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        if (colegioRepo.count() == 0) {
            cargarColeccionDesdeJson("colegios", "/data/colegios.json");
            cargarColeccionDesdeJson("rectores", "/data/rectores.json");
            cargarColeccionDesdeJson("entidades_salud", "/data/entidades_salud.json");
            cargarColeccionDesdeJson("policia", "/data/policia.json");
        }

        // Si solo están los 2 acudientes demo, cargar los 9.902 reales
        if (acudienteRepo.count() <= 2) {
            mongoTemplate.getCollection("acudientes").deleteMany(new Document());
            cargarColeccionDesdeJson("acudientes", "/data/acudientes.json");
        }

        // Cargar los 10.000 estudiantes si la colección está vacía
        if (estudianteRepo.count() == 0) {
            cargarColeccionDesdeJson("estudiantes", "/data/estudiantes.json");
        }

        // Cargar reportes e implicados reales si no hay reportes
        if (reporteRepo.count() == 0) {
            cargarColeccionDesdeJson("reportes", "/data/reportes.json");
            cargarColeccionDesdeJson("implicados", "/data/implicados.json");
        }

        if (usuarioRepo.count() == 0) {
            crearUsuariosIniciales();
        }
    }

    private void cargarColeccionDesdeJson(String collectionName, String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.out.println(">>> Archivo no encontrado en classpath: " + resourcePath);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                List<Document> batch = new ArrayList<>(1000);
                String line;
                int total = 0;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        batch.add(Document.parse(line));
                        if (batch.size() >= 1000) {
                            mongoTemplate.getCollection(collectionName).insertMany(batch);
                            total += batch.size();
                            batch.clear();
                        }
                    }
                }
                if (!batch.isEmpty()) {
                    mongoTemplate.getCollection(collectionName).insertMany(batch);
                    total += batch.size();
                }
                System.out.println(">>> Coleccion '" + collectionName + "' poblada con " + total + " documentos.");
            }
        } catch (Exception e) {
            System.err.println("Error cargando colección " + collectionName + ": " + e.getMessage());
        }
    }

    private void crearUsuariosIniciales() {
        crear("disciplina", "Disciplina2026*", "DISCIPLINA", "Coordinador de Disciplina");
        crear("docente", "Docente2026*", "DOCENTE", "Docente Demo");
        crear("rector", "Rector2026*", "RECTOR", "Ana Maria Rojas");
        System.out.println(">>> Usuarios iniciales creados (disciplina / docente / rector).");
    }

    private void crear(String user, String pass, String rol, String nombre) {
        Usuario u = new Usuario();
        u.setUsername(user);
        u.setPassword(encoder.encode(pass));
        u.setRol(rol);
        u.setNombreCompleto(nombre);
        usuarioRepo.save(u);
    }
}
