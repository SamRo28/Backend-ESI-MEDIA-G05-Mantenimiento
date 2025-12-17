package com.EsiMediaG03.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento para almacenar un log de una petición HTTP al servidor.
 */
@Document(collection = "request_logs") // Se guardará en una nueva colección "request_logs"
public class LogEntry {

    @Id
    private String id;

    private LocalDateTime timestamp;
    private String method; // GET, POST, etc.
    private String path; // /api/visualizador/registro
    private String ipAddress;
    private int statusCode; // 200, 404, 500
    private Long durationMs; // Tiempo que tardó en procesar (muy útil)

    // Constructor vacío para MongoDB
    public LogEntry() {
    }

    // Constructor principal
    public LogEntry(String method, String path, String ipAddress, int statusCode, Long durationMs) {
        this.timestamp = LocalDateTime.now();
        this.method = method;
        this.path = path;
        this.ipAddress = ipAddress;
        this.statusCode = statusCode;
        this.durationMs = durationMs;
    }

    // --- Getters y Setters ---
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
}