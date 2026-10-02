package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "implicados")
public class Implicado {
    @Id
    private String id;

    private String rol;
    private String reporteId;
    private String estudianteId;
    private String acudienteId;

    public String getId() { return id; }
    
    public String getRol() { return rol; }
    public void setRol(String r) { this.rol = r; }

    public String getReporteId() { return reporteId; }
    public void setReporteId(String r) { this.reporteId = r; }

    public String getEstudianteId() { return estudianteId; }
    public void setEstudianteId(String e) { this.estudianteId = e; }

    public String getAcudienteId() { return acudienteId; }
    public void setAcudienteId(String a) { this.acudienteId = a; }

    public Object getEstudiante() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}