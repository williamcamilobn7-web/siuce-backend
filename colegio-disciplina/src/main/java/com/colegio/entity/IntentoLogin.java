package com.colegio.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Registro de cada intento de inicio de sesión (exitoso o fallido).
 * Sirve para auditoría y para bloquear la cuenta tras varios fallos seguidos.
 */
@Document(collection = "intentos_login")
public class IntentoLogin {
    @Id
    private String id;

    private String username;
    private Boolean exitoso;
    private LocalDateTime fechaHora;
    private String ip;

    public IntentoLogin() {}

    public String getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Boolean getExitoso() { return exitoso; }
    public void setExitoso(Boolean exitoso) { this.exitoso = exitoso; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}