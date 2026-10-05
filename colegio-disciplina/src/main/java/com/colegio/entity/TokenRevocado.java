package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

/**
 * Token JWT que se invalidó antes de vencer (por ejemplo, al cerrar sesión).
 * Como un JWT es "sin estado", el servidor no puede olvidarlo por sí solo;
 * por eso guardamos aquí su identificador (jti) y el filtro lo consulta en cada petición.
 */
@Document(collection = "tokens_revocados")
public class TokenRevocado {

    // El id de este documento es el jti del token
    @Id
    private String id;

    // Mongo borra el documento solo cuando llega esta fecha (índice TTL),
    // o sea cuando el token ya habría vencido de todas formas. Así la colección no crece sin parar.
    @Indexed(expireAfterSeconds = 0)
    private Date expira;

    public TokenRevocado() {}

    public TokenRevocado(String jti, Date expira) {
        this.id = jti;
        this.expira = expira;
    }

    public String getId() { return id; }
    public Date getExpira() { return expira; }
}