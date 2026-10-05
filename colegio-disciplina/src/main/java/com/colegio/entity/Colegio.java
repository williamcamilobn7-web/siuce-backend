package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Institución educativa a la que pertenecen los estudiantes y el rector.
 */
@Document(collection = "colegios")
public class Colegio {
    @Id
    private String id;

    private String nombre;
    private String sede;
    private String nit;

    public Colegio() {}

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
}