package com.colegio.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "estudiantes")
public class Estudiante {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identificación ──
    @Column(name = "tipo_documento", nullable = false, length = 30)
    private String tipoDocumento; // RC, TI, CC, PPT, etc.

    @Column(name = "numero_documento", nullable = false, unique = true, length = 30)
    private String numeroDocumento;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "acudiente_id")
    private Acudiente acudiente;

    // ── Demográficos ──
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "genero", length = 20)
    private String genero;

    @Column(name = "pais_origen", length = 60)
    private String paisOrigen;

    @Column(name = "departamento_expedicion", length = 60)
    private String departamentoExpedicion;

    @Column(name = "municipio_expedicion", length = 60)
    private String municipioExpedicion;

    // ── Contacto ──
    @Column(name = "direccion_residencia", length = 200)
    private String direccionResidencia;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    // ── Caracterización poblacional ──
    @Column(name = "grupo_etnico", length = 50)
    private String grupoEtnico; // INDIGENA, AFRODESCENDIENTE, RAIZAL, ROM, NINGUNO

    @Column(name = "tipo_discapacidad", length = 80)
    private String tipoDiscapacidad;

    @Column(name = "victima_conflicto")
    private Boolean victimaConflicto = false;

    // ── Información académica ──
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "colegio_id")
    private Colegio colegio;

    @Column(name = "sede", length = 100)
    private String sede;

    @Column(name = "jornada", length = 30)
    private String jornada; // MANANA, TARDE, NOCHE, UNICA

    @Column(name = "grado", length = 20)
    private String grado;

    // ── Atributos numéricos
    @Column(name = "promedio_academico")
    private Double promedioAcademico; // 0.0 - 5.0

    @Column(name = "numero_inasistencias")
    private Integer numeroInasistencias = 0;

    // ── Getters y Setters ──
    public Long getId() { return id; }
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