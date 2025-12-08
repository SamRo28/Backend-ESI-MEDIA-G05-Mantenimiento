package com.EsiMediaG03.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import com.mongodb.client.result.UpdateResult;
import org.springframework.stereotype.Component;

import com.EsiMediaG03.model.Contenido;

@Component
public class ExpiringContentAlertService {

    private final MongoTemplate mongoTemplate;
    private static final Logger log = LoggerFactory.getLogger(ExpiringContentAlertService.class);

    public ExpiringContentAlertService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Scheduled(cron = "0 0 9 * * *") // cada día a las 09:00
    public void scheduledRun() {
        try {
            generateExpiringContentAlerts();
        } catch (Exception ex) {
            log.error("Error en tarea programada de alertas: {}", ex.getMessage(), ex);
        }
    }

    public int generateExpiringContentAlerts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusDays(7);

        Query q = new Query();
        q.addCriteria(Criteria.where("disponibleHasta").gt(now).lte(limit));
        q.addCriteria(Criteria.where("alertCaducidadSentAt").is(null));
        q.addCriteria(Criteria.where("visible").is(true));

        List<Contenido> contenidos = mongoTemplate.find(q, Contenido.class);
        log.info("Encontrados {} contenidos próximos a expirar (7d)", contenidos.size());

        int alertsCreated = 0;
        for (Contenido c : contenidos) {
            try {
                Document alertDoc = buildAlertDocument(c, now);

                Query usersQ = new Query();
                usersQ.addCriteria(Criteria.where("role").is("USUARIO"));

                if (c.isVip()) {
                    usersQ.addCriteria(Criteria.where("vip").is(true));
                }

                if (c.getRestringidoEdad() > 0) {
                    LocalDate cutoff = LocalDate.now().minusYears(c.getRestringidoEdad());
                    usersQ.addCriteria(Criteria.where("fechaNac").lte(cutoff));
                }

                Update u = new Update().push("alertInbox", alertDoc);
                UpdateResult res = mongoTemplate.updateMulti(usersQ, u, "users");
                long modified = (res != null) ? res.getModifiedCount() : 0L;
                log.info("Contenido {}: alertas añadidas a {} usuarios", c.getId(), modified);
                alertsCreated += modified;

                // marcar el contenido como notificado para no repetir
                Query cq = new Query(Criteria.where("_id").is(c.getId()));
                Update cu = new Update().set("alertCaducidadSentAt", now);
                mongoTemplate.updateFirst(cq, cu, Contenido.class);
            } catch (Exception ex) {
                log.error("Error creando alertas para contenido {}: {}", c.getId(), ex.getMessage(), ex);
            }
        }

        return alertsCreated;
    }

    private Document buildAlertDocument(Contenido c, LocalDateTime now) {
        Document d = new Document();
        d.put("id", UUID.randomUUID().toString());
        d.put("type", "CONTENT_EXPIRING");
        d.put("contenidoId", c.getId());
        d.put("tituloContenido", c.getTitulo());
        String mensaje = String.format("El contenido '%s' caduca el %s.", c.getTitulo(),
                c.getDisponibleHasta() != null ? c.getDisponibleHasta().toLocalDate().toString() : "próximamente");
        d.put("mensaje", mensaje);
        d.put("vipOnly", c.isVip());
        d.put("minEdad", c.getRestringidoEdad());
        d.put("creadaEn", now);
        d.put("disponibleHasta", c.getDisponibleHasta());
        return d;
    }

    /**
     * Genera alertas de tipo NEW_CONTENT para un contenido recién creado.
     * Se envía a todos los usuarios elegibles (respetando VIP y restricción de edad).
     */
    public int generateNewContentAlert(Contenido c) {
        if (c == null || c.getId() == null) {
            log.warn("Contenido nulo o sin ID, no se generan alertas");
            return 0;
        }

        // Solo notificar contenido visible
        if (!c.isVisible()) {
            log.info("Contenido {} no es visible, no se generan alertas NEW_CONTENT", c.getId());
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        int alertsCreated = 0;

        try {
            Document alertDoc = buildNewContentAlertDocument(c, now);

            Query usersQ = new Query();
            usersQ.addCriteria(Criteria.where("role").is("USUARIO"));

            // Si el contenido es VIP, solo notificar a usuarios VIP
            if (c.isVip()) {
                usersQ.addCriteria(Criteria.where("vip").is(true));
            }

            // Si tiene restricción de edad, filtrar por fecha de nacimiento
            if (c.getRestringidoEdad() > 0) {
                LocalDate cutoff = LocalDate.now().minusYears(c.getRestringidoEdad());
                usersQ.addCriteria(Criteria.where("fechaNac").lte(cutoff));
            }

            Update u = new Update().push("alertInbox", alertDoc);
            UpdateResult res = mongoTemplate.updateMulti(usersQ, u, "users");
            long modified = (res != null) ? res.getModifiedCount() : 0L;
            log.info("Nuevo contenido {}: alertas NEW_CONTENT añadidas a {} usuarios", c.getId(), modified);
            alertsCreated = (int) modified;

            // Marcar el contenido como notificado para no repetir
            Query cq = new Query(Criteria.where("_id").is(c.getId()));
            Update cu = new Update().set("alertNuevoContenidoSentAt", now);
            mongoTemplate.updateFirst(cq, cu, Contenido.class);

        } catch (Exception ex) {
            log.error("Error creando alertas NEW_CONTENT para contenido {}: {}", c.getId(), ex.getMessage(), ex);
        }

        return alertsCreated;
    }

    private Document buildNewContentAlertDocument(Contenido c, LocalDateTime now) {
        Document d = new Document();
        d.put("id", UUID.randomUUID().toString());
        d.put("type", "NEW_CONTENT");
        d.put("contenidoId", c.getId());
        d.put("tituloContenido", c.getTitulo());
        String tipoStr = c.getTipo() != null ? c.getTipo().toString().toLowerCase() : "contenido";
        String mensaje = String.format("¡Nuevo %s disponible: '%s'!", tipoStr, c.getTitulo());
        d.put("mensaje", mensaje);
        d.put("vipOnly", c.isVip());
        d.put("minEdad", c.getRestringidoEdad());
        d.put("creadaEn", now);
        d.put("disponibleHasta", c.getDisponibleHasta());
        return d;
    }
}
