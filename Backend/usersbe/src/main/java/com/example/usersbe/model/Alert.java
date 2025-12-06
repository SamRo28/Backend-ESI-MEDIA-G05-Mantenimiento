package com.example.usersbe.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa una alerta/notificacion almacenada en el buzon del usuario.
 * No se usan flags de leido: el usuario las elimina manualmente.
 */
public class Alert {

    public enum AlertType {
        NEW_CONTENT,
        CONTENT_EXPIRING
    }

    private String id = UUID.randomUUID().toString();
    private AlertType type = AlertType.NEW_CONTENT;
    private String contenidoId;
    private String tituloContenido;
    private String mensaje;
    private boolean vipOnly;
    private Integer minEdad;
    private LocalDateTime creadaEn = LocalDateTime.now();
    private LocalDateTime disponibleHasta;

    public Alert() {
    }

    public Alert(AlertType type, String contenidoId, String tituloContenido, String mensaje,
                 boolean vipOnly, Integer minEdad, LocalDateTime disponibleHasta) {
        this.type = type;
        this.contenidoId = contenidoId;
        this.tituloContenido = tituloContenido;
        this.mensaje = mensaje;
        this.vipOnly = vipOnly;
        this.minEdad = minEdad;
        this.disponibleHasta = disponibleHasta;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public String getContenidoId() {
        return contenidoId;
    }

    public void setContenidoId(String contenidoId) {
        this.contenidoId = contenidoId;
    }

    public String getTituloContenido() {
        return tituloContenido;
    }

    public void setTituloContenido(String tituloContenido) {
        this.tituloContenido = tituloContenido;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isVipOnly() {
        return vipOnly;
    }

    public void setVipOnly(boolean vipOnly) {
        this.vipOnly = vipOnly;
    }

    public Integer getMinEdad() {
        return minEdad;
    }

    public void setMinEdad(Integer minEdad) {
        this.minEdad = minEdad;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(LocalDateTime creadaEn) {
        this.creadaEn = creadaEn;
    }

    public LocalDateTime getDisponibleHasta() {
        return disponibleHasta;
    }

    public void setDisponibleHasta(LocalDateTime disponibleHasta) {
        this.disponibleHasta = disponibleHasta;
    }
}
