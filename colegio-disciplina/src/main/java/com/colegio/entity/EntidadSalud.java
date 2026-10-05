package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * EPS o centro de salud al que se remite un caso grave.
 * La latitud y la longitud se usan para saber cuál queda más cerca del incidente.
 */
@Document(collection = "entidades_salud")
public class EntidadSalud {
    @Id
    private String id;

    private String nombre;
    private String telefono;
    private String direccion;
    private Double latitud;
    private Double longitud;

    public EntidadSalud() {}

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}
