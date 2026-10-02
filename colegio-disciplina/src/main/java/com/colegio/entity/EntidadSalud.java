package com.colegio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "entidades_salud")
public class EntidadSalud {
@Document(collection = "entidades_salud")
public class EntidadSalud {
    @Id
    private String id;

    private String nombre;
    private String telefono;
    private String direccion;
    private Double latitud;
    private Double longitud;
}

    public EntidadSalud() {}

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double l) { this.latitud = l; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double l) { this.longitud = l; }
}
