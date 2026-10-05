package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Rector de un colegio. Es quien firma los reportes Tipo III.
 */
@Document(collection = "rectores")
public class Rector {
    @Id
    private String id;

    private String nombreCompleto;
    private String email;
    // Referencia al colegio (en Mongo guardamos el id, no el objeto completo)
    private String colegioId;

    public Rector() {}

    public String getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getColegioId() { return colegioId; }
    public void setColegioId(String colegioId) { this.colegioId = colegioId; }
}
