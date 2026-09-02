package com.colegio.service;

import com.colegio.entity.*;
import com.colegio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Service
public class CargaMasivaService {

    private final EstudianteRepository estudianteRepo;
    private final AcudienteRepository  acudienteRepo;
    private final ColegioRepository    colegioRepo;

    public CargaMasivaService(EstudianteRepository estudianteRepo,
                              AcudienteRepository acudienteRepo,
                              ColegioRepository colegioRepo) {
        this.estudianteRepo = estudianteRepo;
        this.acudienteRepo  = acudienteRepo;
        this.colegioRepo    = colegioRepo;
    }

    @Transactional
    public Map<String, Object> cargarEstudiantes(MultipartFile archivo) throws Exception {
        int creados = 0, omitidos = 0, errores = 0;
        List<String> mensajesError = new ArrayList<>();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8));

        String headerLine = reader.readLine(); // saltar encabezado
        if (headerLine == null) throw new Exception("El archivo está vacío");

        String[] headers = headerLine.split(",");
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < headers.length; i++)
            idx.put(headers[i].trim().toLowerCase(), i);

        String linea;
        int fila = 1;

        while ((linea = reader.readLine()) != null) {
            fila++;
            if (linea.trim().isEmpty()) continue;
            try {
                String[] cols = linea.split(",", -1);

                String numDoc = get(cols, idx, "numerodocumento");
                if (numDoc.isEmpty()) { errores++; mensajesError.add("Fila " + fila + ": sin documento"); continue; }
                if (estudianteRepo.findByNumeroDocumento(numDoc).isPresent()) { omitidos++; continue; }

                Estudiante e = new Estudiante();
                e.setTipoDocumento(get(cols, idx, "tipodocumento"));
                e.setNumeroDocumento(numDoc);
                e.setNombres(get(cols, idx, "nombres"));
                e.setApellidos(get(cols, idx, "apellidos"));

                String fecha = get(cols, idx, "fechanacimiento");
                if (!fecha.isEmpty()) e.setFechaNacimiento(LocalDate.parse(fecha));

                e.setGenero(get(cols, idx, "genero"));
                e.setGrado(get(cols, idx, "grado"));
                e.setJornada(get(cols, idx, "jornada"));
                e.setSede(get(cols, idx, "sede"));
                e.setPaisOrigen(get(cols, idx, "paisorigen"));
                e.setDepartamentoExpedicion(get(cols, idx, "departamentoexpedicion"));
                e.setMunicipioExpedicion(get(cols, idx, "municipioexpedicion"));

                String promedio = get(cols, idx, "promedioacademico");
                if (!promedio.isEmpty()) e.setPromedioAcademico(Double.parseDouble(promedio));

                String inas = get(cols, idx, "numeroinasistencias");
                if (!inas.isEmpty()) e.setNumeroInasistencias(Integer.parseInt(inas));

                String colegioId = get(cols, idx, "colegioid");
                if (!colegioId.isEmpty())
                    colegioRepo.findById(Long.valueOf(colegioId)).ifPresent(e::setColegio);

                String acudienteId = get(cols, idx, "acudienteid");
                if (!acudienteId.isEmpty())
                    acudienteRepo.findById(Long.valueOf(acudienteId)).ifPresent(e::setAcudiente);

                estudianteRepo.save(e);
                creados++;

            } catch (Exception ex) {
                mensajesError.add("Fila " + fila + ": " + ex.getMessage());
                errores++;
            }
        }
        reader.close();

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("creados",        creados);
        resultado.put("omitidos",       omitidos);
        resultado.put("errores",        errores);
        resultado.put("detalleErrores", mensajesError);
        return resultado;
    }

    private String get(String[] cols, Map<String, Integer> idx, String key) {
        Integer i = idx.get(key);
        if (i == null || i >= cols.length) return "";
        return cols[i].trim();
    }
}
