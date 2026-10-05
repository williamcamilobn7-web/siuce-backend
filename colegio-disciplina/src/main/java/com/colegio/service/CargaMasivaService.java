package com.colegio.service;

import com.colegio.entity.Estudiante;
import com.colegio.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Carga masiva de estudiantes desde CSV. Las columnas se buscan por nombre en el encabezado,
 * así el orden de las columnas del archivo no importa.
 */
@Service
public class CargaMasivaService {

    // Se guarda de a 500 filas; con un save() por fila 10.000 estudiantes serían 10.000 viajes a la base
    private static final int LOTE = 500;

    private final EstudianteRepository estudianteRepo;
    private final AcudienteRepository acudienteRepo;
    private final ColegioRepository colegioRepo;

    public CargaMasivaService(EstudianteRepository estudianteRepo, AcudienteRepository acudienteRepo,
                              ColegioRepository colegioRepo) {
        this.estudianteRepo = estudianteRepo;
        this.acudienteRepo = acudienteRepo;
        this.colegioRepo = colegioRepo;
    }

    public Map<String, Object> cargarEstudiantes(MultipartFile archivo) throws Exception {
        int creados = 0, omitidos = 0, errores = 0;
        List<String> mensajesError = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) throw new Exception("El archivo está vacío");
            // Excel a veces agrega un carácter invisible (BOM) al inicio del archivo
            if (headerLine.startsWith("\uFEFF")) headerLine = headerLine.substring(1);

            Map<String, Integer> idx = new HashMap<>();
            String[] headers = headerLine.split(",");
            for (int i = 0; i < headers.length; i++) idx.put(headers[i].trim().toLowerCase(), i);

            List<Estudiante> lote = new ArrayList<>();
            Set<String> vistosEnArchivo = new HashSet<>();
            String linea;
            int fila = 1;

            while ((linea = reader.readLine()) != null) {
                fila++;
                if (linea.trim().isEmpty()) continue;
                try {
                    String[] cols = linea.split(",", -1);
                    String numDoc = get(cols, idx, "numerodocumento");
                    if (numDoc.isEmpty()) { errores++; mensajesError.add("Fila " + fila + ": sin documento"); continue; }
                    // Documento repetido dentro del mismo archivo: se omite
                    if (!vistosEnArchivo.add(numDoc)) { omitidos++; continue; }

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
                    // Los ids de colegio y acudiente solo se asignan si existen en la base
                    if (!colegioId.isEmpty() && colegioRepo.existsById(colegioId)) e.setColegioId(colegioId);
                    String acudienteId = get(cols, idx, "acudienteid");
                    if (!acudienteId.isEmpty() && acudienteRepo.existsById(acudienteId)) e.setAcudienteId(acudienteId);

                    lote.add(e);
                    if (lote.size() >= LOTE) {
                        int guardados = guardarLote(lote);
                        creados += guardados;
                        omitidos += lote.size() - guardados;
                        lote.clear();
                    }
                } catch (Exception ex) {
                    mensajesError.add("Fila " + fila + ": " + ex.getMessage());
                    errores++;
                }
            }
            if (!lote.isEmpty()) {
                int guardados = guardarLote(lote);
                creados += guardados;
                omitidos += lote.size() - guardados;
            }
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("creados", creados);
        resultado.put("omitidos", omitidos);
        resultado.put("errores", errores);
        resultado.put("detalleErrores", mensajesError);
        return resultado;
    }

    /** Guarda el lote omitiendo documentos que ya existen. Devuelve cuántos guardó. */
    /**
     * Guarda el lote saltándose los documentos que ya existen en la base.
     * Devuelve cuántos guardó realmente.
     */
    private int guardarLote(List<Estudiante> lote) {
        Set<String> docs = lote.stream().map(Estudiante::getNumeroDocumento).collect(Collectors.toSet());
        Set<String> existentes = estudianteRepo.findByNumeroDocumentoIn(docs).stream()
                .map(Estudiante::getNumeroDocumento).collect(Collectors.toSet());
        List<Estudiante> nuevos = lote.stream()
                .filter(e -> !existentes.contains(e.getNumeroDocumento())).toList();
        estudianteRepo.saveAll(nuevos);
        return nuevos.size();
    }

    private String get(String[] cols, Map<String, Integer> idx, String key) {
        Integer i = idx.get(key);
        if (i == null || i >= cols.length) return "";
        return cols[i].trim();
    }
}
