package com.colegio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Cuenta de acceso al sistema. Roles: DISCIPLINA, DOCENTE, RECTOR, ESTUDIANTE y ACUDIENTE.
 */
@Document(collection = "usuarios")
public class Usuario {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    // Aquí va el hash BCrypt, nunca la contraseña en texto plano.
    // @JsonIgnore evita que se devuelva en alguna respuesta.
    private String password;
    private String rol;
    private String nombreCompleto;
    private String estudianteId;
    private String acudienteId;
    // Todo token emitido ANTES de esta fecha se rechaza. Se actualiza al cambiar
    // la contraseña o al cerrar todas las sesiones, y así se puede invalidar
    // tokens que todavía no vencen.
    private Instant tokensValidosDesde;

    public Usuario() {}

    public String getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    @JsonIgnore
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEstudianteId() { return estudianteId; }
    public void setEstudianteId(String estudianteId) { this.estudianteId = estudianteId; }
    public String getAcudienteId() { return acudienteId; }
    public void setAcudienteId(String acudienteId) { this.acudienteId = acudienteId; }
    public Instant getTokensValidosDesde() { return tokensValidosDesde; }
    public void setTokensValidosDesde(Instant tokensValidosDesde) { this.tokensValidosDesde = tokensValidosDesde; }
}