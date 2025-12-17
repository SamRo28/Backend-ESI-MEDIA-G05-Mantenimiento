package com.example.usersbe.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.usersbe.services.LoggingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    @Autowired
    private LoggingService loggingService;

    /**
     * Se ejecuta ANTES de que la petición llegue al controlador.
     * Guardamos la hora de inicio en la propia petición.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true; // true = continuar con la petición
    }

    /**
     * Se ejecuta DESPUÉS de que la petición se ha completado y enviado la respuesta.
     * Aquí es donde recogemos todos los datos y los mandamos al servicio de log.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        
        // Recuperar la hora de inicio que guardamos en preHandle
        long startTime = (Long) request.getAttribute("startTime");
        long durationMs = System.currentTimeMillis() - startTime;

        // Obtener los datos requeridos
        String method = request.getMethod();
        String path = request.getRequestURI();
        String ipAddress = getClientIp(request);
        int statusCode = response.getStatus();

        // Guardar el log usando nuestro servicio asíncrono
        loggingService.saveLog(method, path, ipAddress, statusCode, durationMs);
    }

    /**
     * Método de ayuda para obtener la IP real del cliente,
     * teniendo en cuenta proxies (X-FORWARDED-FOR).
     * (Copiado de tu UsuarioController)
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}