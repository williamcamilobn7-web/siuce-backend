package com.colegio.entity;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Estudiante de la institución. Con el volumen esperado (unos 10.000 registros)
 * el listado siempre se pide paginado.
 */
@Document(collection = "estudiantes")
public class Estudiante {
    @Id
    private String id;

    // Índice único: no puede haber dos estudiantes con el mismo documento
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
    // Relaciones guardadas como ids
    private String colegioId;
    private String acudienteId;
    // Se rellenan al responder (no se guardan en la base)
    @Transient
    private Colegio colegio;
    @Transient
    private Acudiente acudiente;

    public Estudiante() {}

    public String getId() { return id; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getGrado() { return grado; }
    public void setGrado(String grado) { this.grado = grado; }
    public String getJornada() { return jornada; }
    public void setJornada(String jornada) { this.jornada = jornada; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public String getPaisOrigen() { return paisOrigen; }
    public void setPaisOrigen(String paisOrigen) { this.paisOrigen = paisOrigen; }
    public String getDepartamentoExpedicion() { return departamentoExpedicion; }
    public void setDepartamentoExpedicion(String departamentoExpedicion) { this.departamentoExpedicion = departamentoExpedicion; }
    public String getMunicipioExpedicion() { return municipioExpedicion; }
    public void setMunicipioExpedicion(String municipioExpedicion) { this.municipioExpedicion = municipioExpedicion; }
    public String getGrupoEtnico() { return grupoEtnico; }
    public void setGrupoEtnico(String grupoEtnico) { this.grupoEtnico = grupoEtnico; }
    public String getTipoDiscapacidad() { return tipoDiscapacidad; }
    public void setTipoDiscapacidad(String tipoDiscapacidad) { this.tipoDiscapacidad = tipoDiscapacidad; }
    public Boolean getVictimaConflicto() { return victimaConflicto; }
    public void setVictimaConflicto(Boolean victimaConflicto) { this.victimaConflicto = victimaConflicto; }
    public Double getPromedioAcademico() { return promedioAcademico; }
    public void setPromedioAcademico(Double promedioAcademico) { this.promedioAcademico = promedioAcademico; }
    public Integer getNumeroInasistencias() { return numeroInasistencias; }
    public void setNumeroInasistencias(Integer numeroInasistencias) { this.numeroInasistencias = numeroInasistencias; }
    public String getDireccionResidencia() { return direccionResidencia; }
    public void setDireccionResidencia(String direccionResidencia) { this.direccionResidencia = direccionResidencia; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public String getColegioId() { return colegioId; }
    public void setColegioId(String colegioId) { this.colegioId = colegioId; }
    public String getAcudienteId() { return acudienteId; }
    public void setAcudienteId(String acudienteId) { this.acudienteId = acudienteId; }
    public Colegio getColegio() { return colegio; }
    public void setColegio(Colegio colegio) { this.colegio = colegio; }
    public Acudiente getAcudiente() { return acudiente; }
    public void setAcudiente(Acudiente acudiente) { this.acudiente = acudiente; }
}
