package com.colegio.entity;

import jakarta.persistence.*;

@Document(collection = "acudientes")
public class Acudiente {
    @Id
    private String id;

    private String tipoDocumento;
    private String numeroDocumento;
    private String nombreCompleto;
    private String telefono;
    private String email;
    private Boolean notificado = false;


    public Acudiente() {}

    public Long getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getNotificado() { return notificado; }
    public void setNotificado(Boolean notificado) { this.notificado = notificado; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String t) { this.tipoDocumento = t; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String n) { this.numeroDocumento = n; }
}
