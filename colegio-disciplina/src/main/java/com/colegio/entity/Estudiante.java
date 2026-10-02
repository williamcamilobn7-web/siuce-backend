package com.colegio.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "estudiantes")
public class Estudiante {
    @Id
    private String id;

    @Indexed(unique = true)
    private String numeroDocumento;

    private String tipoDocumento;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String genero;
    private String grado;
    private String jornada;
    private String sede;
    private String paisOrigen;
    private String departamentoExpedicion;
    private String municipioExpedicion;
    private String grupoEtnico;
    private String tipoDiscapacidad;
    private Boolean victimaConflicto;
    private Double promedioAcademico;
    private Integer numeroInasistencias;

    private String direccionResidencia;
    private String telefono;
    private String email;
    private Double latitud;
    private Double longitud;

    private String colegioId;
    private String acudienteId;

    private Colegio colegio;
    private Acudiente acudiente;

    // ── Getters y Setters ──
    public String getId() { return id; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String t) { this.tipoDocumento = t; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String n) { this.numeroDocumento = n; }
    public String getNombres() { return nombres; }
    public void setNombres(String n) { this.nombres = n; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String a) { this.apellidos = a; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate f) { this.fechaNacimiento = f; }
    public String getGenero() { return genero; }
    public void setGenero(String g) { this.genero = g; }
    public String getPaisOrigen() { return paisOrigen; }
    public void setPaisOrigen(String p) { this.paisOrigen = p; }
    public String getDepartamentoExpedicion() { return departamentoExpedicion; }
    public void setDepartamentoExpedicion(String d) { this.departamentoExpedicion = d; }
    public String getMunicipioExpedicion() { return municipioExpedicion; }
    public void setMunicipioExpedicion(String m) { this.municipioExpedicion = m; }
    public String getDireccionResidencia() { return direccionResidencia; }
    public void setDireccionResidencia(String d) { this.direccionResidencia = d; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String t) { this.telefono = t; }
    public String getEmail() { return email; }
    public void setEmail(String e) { this.email = e; }
    public String getGrupoEtnico() { return grupoEtnico; }
    public void setGrupoEtnico(String g) { this.grupoEtnico = g; }
    public String getTipoDiscapacidad() { return tipoDiscapacidad; }
    public void setTipoDiscapacidad(String t) { this.tipoDiscapacidad = t; }
    public Boolean getVictimaConflicto() { return victimaConflicto; }
    public void setVictimaConflicto(Boolean v) { this.victimaConflicto = v; }
    public Colegio getColegio() { return colegio; }
    public void setColegio(Colegio c) { this.colegio = c; }
    public String getSede() { return sede; }
    public void setSede(String s) { this.sede = s; }
    public String getJornada() { return jornada; }
    public void setJornada(String j) { this.jornada = j; }
    public String getGrado() { return grado; }
    public void setGrado(String g) { this.grado = g; }
    public Double getPromedioAcademico() { return promedioAcademico; }
    public void setPromedioAcademico(Double p) { this.promedioAcademico = p; }
    public Integer getNumeroInasistencias() { return numeroInasistencias; }
    public void setNumeroInasistencias(Integer n) { this.numeroInasistencias = n; }
    public Acudiente getAcudiente() { return acudiente; }
    public void setAcudiente(Acudiente a) { this.acudiente = a; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double l) { this.latitud = l; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double l) { this.longitud = l; }
}