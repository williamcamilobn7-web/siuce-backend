package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Cuadrante o estación de policía que se asigna a los reportes Tipo III.
 * Igual que la EPS, se elige por cercanía usando las coordenadas.
 */
@Document(collection = "policia")
public class Policia {
    @Id
    private String id;

    private String nombre;
    private String estacion;
    private String telefonoEmergencia;
    private Double latitud;
    private Double longitud;

    public Policia() {}

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEstacion() { return estacion; }
    public void setEstacion(String estacion) { this.estacion = estacion; }
    public String getTelefonoEmergencia() { return telefonoEmergencia; }
    public void setTelefonoEmergencia(String telefonoEmergencia) { this.telefonoEmergencia = telefonoEmergencia; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}