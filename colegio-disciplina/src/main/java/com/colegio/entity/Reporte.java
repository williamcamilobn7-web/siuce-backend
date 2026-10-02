package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "reportes")
public class Reporte {
    @Id
    private String id;

    private LocalDateTime fechaHora;
    private String lugar;
    private String descripcionHecho;
    private String tipoFalta;
    private String estado;
    private String firmadoPor;
    private Double latitud;
    private Double longitud;
    private String entidadSaludId;
    private String policiaId;

    public Reporte() {}

    public String getId() { return id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime f) { this.fechaHora = f; }
    public String getLugar() { return lugar; }
    public void setLugar(String l) { this.lugar = l; }
    public String getDescripcionHecho() { return descripcionHecho; }
    public void setDescripcionHecho(String d) { this.descripcionHecho = d; }
    public String getTipoFalta() { return tipoFalta; }
    public void setTipoFalta(String t) { this.tipoFalta = t; }
    public String getEstado() { return estado; }
    public void setEstado(String e) { this.estado = e; }
    public String getFirmadoPor() { return firmadoPor; }
    public void setFirmadoPor(String f) { this.firmadoPor = f; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double l) { this.latitud = l; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double l) { this.longitud = l; }
    public String getEntidadSaludId() { return entidadSaludId; }
    public void setEntidadSaludId(String e) { this.entidadSaludId = e; }
    public String getPoliciaId() { return policiaId; }
    public void setPoliciaId(String p) { this.policiaId = p; }
}