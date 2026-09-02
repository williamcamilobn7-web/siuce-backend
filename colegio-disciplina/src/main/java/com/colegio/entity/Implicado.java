package com.colegio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "implicados")
public class Implicado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rol", nullable = false, length = 20)
    private String rol; // VICTIMA, AGRESOR, TESTIGO

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "acudiente_id")
    private Acudiente acudiente;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporte_id", nullable = false)
    private Reporte reporte;

    public Long getId() { return id; }
    public String getRol() { return rol; }
    public void setRol(String r) { this.rol = r; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante e) { this.estudiante = e; }
    public Acudiente getAcudiente() { return acudiente; }
    public void setAcudiente(Acudiente a) { this.acudiente = a; }
    public Reporte getReporte() { return reporte; }
    public void setReporte(Reporte r) { this.reporte = r; }
}