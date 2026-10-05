package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Padre, madre o tutor responsable de un estudiante.
 * Cuando el colegio le avisa de un incidente se marca como notificado.
 */
@Document(collection = "acudientes")
public class Acudiente {
    @Id
    private String id;

    private String tipoDocumento;
    private String numeroDocumento;
    private String nombreCompleto;
    private String telefono;
    private String email;
    // Pasa a true cuando se le avisa de un incidente disciplinario
    private Boolean notificado;

    public Acudiente() {}

    public String getId() { return id; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Boolean getNotificado() { return notificado; }
    public void setNotificado(Boolean notificado) { this.notificado = notificado; }
}

