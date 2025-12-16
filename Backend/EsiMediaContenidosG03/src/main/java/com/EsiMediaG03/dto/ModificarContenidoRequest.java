package com.EsiMediaG03.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ModificarContenidoRequest {
    private String titulo;
    private String descripcion;
    private List<String> tags;
    private Integer duracionMinutos;
    private String resolucion;
    private Boolean vip;
    private Boolean visible;
    private LocalDateTime disponibleHasta;
    private LocalDate disponibilidadContenido;
    private Integer restringidoEdad;
    private String ficheroAudio;
    private String urlAudio;
    private String urlVideo;
    private String imagen;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public Boolean getVip() {
        return vip;
    }

    public void setVip(Boolean vip) {
        this.vip = vip;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public LocalDateTime getDisponibleHasta() {
        return disponibleHasta;
    }

    public void setDisponibleHasta(LocalDateTime disponibleHasta) {
        this.disponibleHasta = disponibleHasta;
    }

    public LocalDate getDisponibilidadContenido() {
        return disponibilidadContenido;
    }

    public void setDisponibilidadContenido(LocalDate disponibilidadContenido) {
        this.disponibilidadContenido = disponibilidadContenido;
    }

    public Integer getRestringidoEdad() {
        return restringidoEdad;
    }

    public void setRestringidoEdad(Integer restringidoEdad) {
        this.restringidoEdad = restringidoEdad;
    }

    public String getFicheroAudio() {
        return ficheroAudio;
    }

    public void setFicheroAudio(String ficheroAudio) {
        this.ficheroAudio = ficheroAudio;
    }

    public String getUrlAudio() {
        return urlAudio;
    }

    public void setUrlAudio(String urlAudio) {
        this.urlAudio = urlAudio;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

}
