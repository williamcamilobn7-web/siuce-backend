package com.colegio.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "lugar", nullable = false, length = 200)
    private String lugar;

    @Column(name = "descripcion_hecho", nullable = false, columnDefinition = "text")
    private String descripcionHecho;

    @Column(name = "tipo_falta", nullable = false, length = 10)
    private String tipoFalta;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "firmado_por", length = 150)
    private String firmadoPor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "entidad_salud_id")
    private EntidadSalud entidadSalud;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "policia_id")
    private Policia policia;

    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Implicado> implicados = new ArrayList<>();

    public Reporte() {}

    public Long getId() { return id; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public String getDescripcionHecho() { return descripcionHecho; }
    public void setDescripcionHecho(String descripcionHecho) { this.descripcionHecho = descripcionHecho; }
    public String getTipoFalta() { return tipoFalta; }
    public void setTipoFalta(String tipoFalta) { this.tipoFalta = tipoFalta; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFirmadoPor() { return firmadoPor; }
    public void setFirmadoPor(String firmadoPor) { this.firmadoPor = firmadoPor; }
    public EntidadSalud getEntidadSalud() { return entidadSalud; }
    public void setEntidadSalud(EntidadSalud entidadSalud) { this.entidadSalud = entidadSalud; }
    public Policia getPolicia() { return policia; }
    public void setPolicia(Policia policia) { this.policia = policia; }
    public List<Implicado> getImplicados() { return implicados; }
    public void setImplicados(List<Implicado> implicados) { this.implicados = implicados; }
}
