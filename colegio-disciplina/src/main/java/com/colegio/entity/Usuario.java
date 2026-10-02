package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    private String password;
    private String rol; // ESTUDIANTE, ACUDIENTE, DOCENTE, DISCIPLINA
    private String nombreCompleto;

    @Id
    private Estudiante estudiante;
    @Id
    private Acudiente acudiente;

    public String getId() { return id; }
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