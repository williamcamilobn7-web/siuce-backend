package com.colegio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "policia")
public class Policia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "estacion", length = 150)
    private String estacion;

    @Column(name = "telefono_emergencia", length = 20)
    private String telefonoEmergencia;

    public Policia() {}

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEstacion() { return estacion; }
    public void setEstacion(String estacion) { this.estacion = estacion; }
    public String getTelefonoEmergencia() { return telefonoEmergencia; }
    public void setTelefonoEmergencia(String t) { this.telefonoEmergencia = t; }
}
