package com.colegio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 60)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "rol", nullable = false, length = 20)
    private String rol; // ESTUDIANTE, ACUDIENTE, DOCENTE, DISCIPLINA

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "acudiente_id")
    private Acudiente acudiente;

    @Column(name = "nombre_completo", length = 150)
    private String nombreCompleto;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getPassword() { return password; }
    public void setPassword(String p) { this.password = p; }
    public String getRol() { return rol; }
    public void setRol(String r) { this.rol = r; }
    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante e) { this.estudiante = e; }
    public Acudiente getAcudiente() { return acudiente; }
    public void setAcudiente(Acudiente a) { this.acudiente = a; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String n) { this.nombreCompleto = n; }
}