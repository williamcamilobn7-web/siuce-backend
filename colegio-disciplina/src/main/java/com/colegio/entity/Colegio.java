package com.colegio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "colegios")
public class Colegio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "sede", length = 100)
    private String sede;

    @Column(name = "nit", nullable = false, unique = true, length = 20)
    private String nit;

    public Colegio() {}

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
}
