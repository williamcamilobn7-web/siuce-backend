package com.colegio.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Reporte disciplinario. El tipo de falta (TIPO_I, TIPO_II o TIPO_III) se calcula
 * automáticamente a partir de la descripción del hecho.
 */
@Document(collection = "reportes")
public class Reporte {
    @Id
    private String id;

    private LocalDateTime fechaHora;
    private String lugar;
    private String descripcionHecho;
    private String tipoFalta;
    // PENDIENTE al crearse y FIRMADO cuando el rector lo firma
    private String estado;
    // Nombre de quien firmó
    private String firmadoPor;
    private Double latitud;
    private Double longitud;
    // Solo se asignan en los reportes Tipo III
    private String entidadSaludId;
    private String policiaId;
    // Objetos completos para el frontend; no se guardan en la base
    @Transient
    private EntidadSalud entidadSalud;
    @Transient
    private Policia policia;

    public Reporte() {}

    public String getId() { return id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public String getDescripcionHecho() { return descripcionHecho; }
    public void setDescripcionHecho(String descripcionHecho) { this.descripcionHecho = descripcionHecho; }
    public String getTipoFalta() { return tipoFalta; }
    public void setTipoFalta(String tipoFalta) { this.tipoFalta = tipoFalta; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFirmadoPor() { return firmadoPor; }
    public void setFirmadoPor(String firmadoPor) { this.firmadoPor = firmadoPor; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public String getEntidadSaludId() { return entidadSaludId; }
    public void setEntidadSaludId(String entidadSaludId) { this.entidadSaludId = entidadSaludId; }
    public String getPoliciaId() { return policiaId; }
    public void setPoliciaId(String policiaId) { this.policiaId = policiaId; }
    public EntidadSalud getEntidadSalud() { return entidadSalud; }
    public void setEntidadSalud(EntidadSalud entidadSalud) { this.entidadSalud = entidadSalud; }
    public Policia getPolicia() { return policia; }
    public void setPolicia(Policia policia) { this.policia = policia; }
}