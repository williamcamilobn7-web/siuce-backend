package com.colegio;

import com.colegio.entity.*;
import com.colegio.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final ColegioRepository      colegioRepo;
    private final RectorRepository       rectorRepo;
    private final AcudienteRepository    acudienteRepo;
    private final EntidadSaludRepository entidadSaludRepo;
    private final PoliciaRepository      policiaRepo;
    private final ReporteRepository      reporteRepo;

    public DataLoader(ColegioRepository colegioRepo,
                      RectorRepository rectorRepo,
                      AcudienteRepository acudienteRepo,
                      EntidadSaludRepository entidadSaludRepo,
                      PoliciaRepository policiaRepo,
                      ReporteRepository reporteRepo) {
        this.colegioRepo      = colegioRepo;
        this.rectorRepo       = rectorRepo;
        this.acudienteRepo    = acudienteRepo;
        this.entidadSaludRepo = entidadSaludRepo;
        this.policiaRepo      = policiaRepo;
        this.reporteRepo      = reporteRepo;
    }

    @Override
    public void run(String... args) {
        if (colegioRepo.count() > 0) {
            System.out.println(">>> Datos demo ya existen, omitiendo carga inicial.");
            return;
        }

        System.out.println(">>> Insertando datos demo...");

        Colegio colegio = new Colegio();
        colegio.setNombre("Institucion Educativa San Jose");
        colegio.setSede("Sede Principal");
        colegio.setNit("890123456-1");
        colegioRepo.save(colegio);

        Rector rector = new Rector();
        rector.setNombreCompleto("Ana Maria Rojas");
        rector.setEmail("rectora@sanjose.edu.co");
        rector.setColegio(colegio);
        rectorRepo.save(rector);

        Acudiente a1 = new Acudiente();
        a1.setNombreCompleto("Carlos Perez");
        a1.setTelefono("3001234567");
        a1.setEmail("carlos.perez@gmail.com");
        acudienteRepo.save(a1);

        Acudiente a2 = new Acudiente();
        a2.setNombreCompleto("Maria Lopez");
        a2.setTelefono("3119876543");
        a2.setEmail("maria.lopez@gmail.com");
        acudienteRepo.save(a2);

        EntidadSalud eps = new EntidadSalud();
        eps.setNombre("EPS Sanitas");
        eps.setTelefono("6041234567");
        eps.setDireccion("Calle 30 No 15-20, Cartagena");
        entidadSaludRepo.save(eps);

        Policia pol = new Policia();
        pol.setNombre("Policia de Infancia y Adolescencia");
        pol.setEstacion("Estacion Centro");
        pol.setTelefonoEmergencia("156");
        policiaRepo.save(pol);

        System.out.println(">>> Datos demo insertados correctamente.");
        System.out.println("    - Colegio: " + colegio.getNombre());
        System.out.println("    - Rector:  " + rector.getNombreCompleto());
        System.out.println("    - Acudientes: 2");
        System.out.println("    - EPS: " + eps.getNombre());
        System.out.println("    - Policia: " + pol.getNombre());
    }
}
