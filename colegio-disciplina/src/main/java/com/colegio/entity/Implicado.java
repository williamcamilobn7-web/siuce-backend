package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Estudiante que participó en un reporte, con un rol: VICTIMA, AGRESOR o TESTIGO.
 */
@Document(collection = "implicados")
public class Implicado {
    @Id
    private String id;

    private String rol;
    private String reporteId;
    private String estudianteId;
    private String acudienteId;
    // Los dos objetos de abajo NO se guardan en Mongo (@Transient).
    // Los llena el servicio antes de responder para que el frontend
    // reciba el estudiante y el acudiente completos.
    @Transient
    private Estudiante estudiante;
    @Transient
    private Acudiente acudiente;

    public Implicado() {}

    public String getId() { return id; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getReporteId() { return reporteId; }
    public void setReporteId(String reporteId) { this.reporteId = reporteId; }
    public String getEstudianteId() { return estudianteId; }
    public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }
    public String getAcudienteId() { return acudienteId; }
    public void setAcudienteId(String acudienteId) { this.acudienteId = acudienteId; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }
    public Acudiente getAcudiente() { return acudiente; }
    public void setAcudiente(Acudiente acudiente) { this.acudiente = acudiente; }
}