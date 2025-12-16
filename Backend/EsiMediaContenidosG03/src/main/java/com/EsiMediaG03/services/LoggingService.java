package com.EsiMediaG03.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.EsiMediaG03.dao.LogEntryRepository;
import com.EsiMediaG03.model.LogEntry;



@Service
public class LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);

    @Autowired
    private LogEntryRepository logEntryRepository;

    /**
     * Guarda un log en la base de datos de forma asíncrona.
     * No bloqueará el hilo de la petición HTTP principal.
     */
    @Async
    public void saveLog(String method, String path, String ipAddress, int statusCode, long durationMs) {
        try {
            LogEntry logEntry = new LogEntry(method, path, ipAddress, statusCode, durationMs);
            logEntryRepository.save(logEntry);
        } catch (Exception e) {
            // Si falla el logging, solo lo mostramos en la consola de error,
            // pero no lanzamos una excepción que rompa la aplicación.
            logger.error("Error al guardar la entrada de log: {}", e.getMessage());
        }
    }
}
