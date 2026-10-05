package com.colegio;

import com.colegio.entity.*;
import com.colegio.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Datos iniciales para poder probar el sistema. Se ejecuta al arrancar y solo inserta
 * si las colecciones están vacías, así que se puede reiniciar sin duplicar nada.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final ColegioRepository colegioRepo;
    private final RectorRepository rectorRepo;
    private final AcudienteRepository acudienteRepo;
    private final EntidadSaludRepository entidadSaludRepo;
    private final PoliciaRepository policiaRepo;
    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder encoder;

    public DataLoader(ColegioRepository colegioRepo, RectorRepository rectorRepo, AcudienteRepository acudienteRepo,
                      EntidadSaludRepository entidadSaludRepo, PoliciaRepository policiaRepo,
                      UsuarioRepository usuarioRepo, PasswordEncoder encoder) {
        this.colegioRepo = colegioRepo;
        this.rectorRepo = rectorRepo;
        this.acudienteRepo = acudienteRepo;
        this.entidadSaludRepo = entidadSaludRepo;
        this.policiaRepo = policiaRepo;
        this.usuarioRepo = usuarioRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (colegioRepo.count() == 0) cargarDatosDemo();
        if (usuarioRepo.count() == 0) crearUsuariosIniciales();
    }

    private void cargarDatosDemo() {
        Colegio colegio = new Colegio();
        colegio.setNombre("Institucion Educativa San Jose");
        colegio.setSede("Sede Principal");
        colegio.setNit("890123456-1");
        colegioRepo.save(colegio);

        Rector rector = new Rector();
        rector.setNombreCompleto("Ana Maria Rojas");
        rector.setEmail("rectora@sanjose.edu.co");
        rector.setColegioId(colegio.getId());
        rectorRepo.save(rector);

        for (String[] a : new String[][]{
                {"Carlos Perez", "3001234567", "carlos.perez@gmail.com"},
                {"Maria Lopez", "3119876543", "maria.lopez@gmail.com"}}) {
            Acudiente ac = new Acudiente();
            ac.setNombreCompleto(a[0]); ac.setTelefono(a[1]); ac.setEmail(a[2]); ac.setNotificado(false);
            acudienteRepo.save(ac);
        }

        EntidadSalud eps = new EntidadSalud();
        eps.setNombre("EPS Sanitas");
        eps.setTelefono("6041234567");
        eps.setDireccion("Calle 30 No 15-20, Cartagena");
        eps.setLatitud(10.4000);   // coordenadas aproximadas de demo: ajústalas
        eps.setLongitud(-75.5100);
        entidadSaludRepo.save(eps);

        Policia pol = new Policia();
        pol.setNombre("Policia de Infancia y Adolescencia");
        pol.setEstacion("Estacion Centro");
        pol.setTelefonoEmergencia("156");
        pol.setLatitud(10.4236);
        pol.setLongitud(-75.5470);
        policiaRepo.save(pol);

        System.out.println(">>> Datos demo insertados.");
    }

    // Sin al menos un usuario no habría forma de entrar (el registro exige estar logueado como DISCIPLINA).
    // Estas contraseñas son de demostración: hay que cambiarlas.
    private void crearUsuariosIniciales() {
        crear("disciplina", "Disciplina2026*", "DISCIPLINA", "Coordinador de Disciplina");
        crear("docente", "Docente2026*", "DOCENTE", "Docente Demo");
        crear("rector", "Rector2026*", "RECTOR", "Ana Maria Rojas");
        System.out.println(">>> Usuarios iniciales creados (disciplina / docente / rector). CAMBIA ESTAS CONTRASEÑAS.");
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
