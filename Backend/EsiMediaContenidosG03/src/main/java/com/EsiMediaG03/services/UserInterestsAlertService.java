package com.EsiMediaG03.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.mongodb.client.result.UpdateResult;
import org.springframework.stereotype.Component;

import com.EsiMediaG03.model.Contenido;

/**
 * Servicio que genera alertas de contenido que coincide con los gustos de cada usuario.
 * Compara los tags del contenido con los misGustos de cada usuario (ambos normalizados a minúsculas).
 * Respeta restricciones de edad y VIP.
 */
@Component
public class UserInterestsAlertService {

    private final MongoTemplate mongoTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserInterestsAlertService.class);

    public UserInterestsAlertService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Genera alertas de contenido por gustos/intereses para usuarios elegibles.
     * Solo genera alertas para usuarios USUARIO cuyo misGustos intersecte con los tags del contenido.
     *
     * @param contenido el contenido recién creado
     * @return número de alertas creadas
     */
    public int generateInterestBasedAlert(Contenido contenido) {
        List<String> normalizedTags = extractAndNormalizeTags(contenido);
        if (normalizedTags.isEmpty()) {
            return 0;
        }

        List<Document> users = findEligibleUsers();
        log.debug("Se evaluarán {} usuarios para alertas de interés del contenido {}",
                users.size(), contenido.getId());

        int alertsCreated = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Document userDoc : users) {
            if (processUserAlert(userDoc, contenido, normalizedTags, now)) {
                alertsCreated++;
            }
        }

        log.info("Contenido {}: {} alertas de interés creadas", contenido.getId(), alertsCreated);
        return alertsCreated;
    }

    /**
     * Extrae y normaliza los tags del contenido.
     */
    private List<String> extractAndNormalizeTags(Contenido contenido) {
        if (contenido == null || contenido.getTags() == null || contenido.getTags().isEmpty()) {
            log.debug("Contenido {} sin tags, no se generan alertas de interés", 
                    contenido != null ? contenido.getId() : "null");
            return List.of();
        }

        List<String> normalized = contenido.getTags().stream()
                .map(tag -> tag != null ? tag.toLowerCase() : "")
                .filter(tag -> !tag.isEmpty())
                .toList();

        if (normalized.isEmpty()) {
            log.debug("Contenido {} sin tags válidos, no se generan alertas de interés", contenido.getId());
        }

        return normalized;
    }

    /**
     * Obtiene todos los usuarios USUARIO no bloqueados.
     */
    private List<Document> findEligibleUsers() {
        Query usersQ = new Query();
        usersQ.addCriteria(Criteria.where("role").is("USUARIO"));
        usersQ.addCriteria(Criteria.where("blocked").ne(true));
        return mongoTemplate.find(usersQ, Document.class, "users");
    }

    /**
     * Procesa una alerta de interés para un usuario específico.
     * Retorna true si la alerta fue creada exitosamente.
     */
    private boolean processUserAlert(Document userDoc, Contenido contenido, 
                                     List<String> normalizedTags, LocalDateTime now) {
        try {
            if (!meetsVipRestriction(userDoc, contenido)) {
                return false;
            }

            if (!meetsAgeRestriction(userDoc, contenido)) {
                return false;
            }

            List<String> userGustos = extractUserGustos(userDoc);
            if (!hasInterestIntersection(userGustos, normalizedTags)) {
                return false;
            }

            return createAndPushAlert(userDoc, contenido, now);
        } catch (Exception ex) {
            log.error("Error procesando usuario para alerta de interés: {}", ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * Verifica si el usuario cumple la restricción VIP.
     */
    private boolean meetsVipRestriction(Document userDoc, Contenido contenido) {
        if (!contenido.isVip()) {
            return true;
        }
        Boolean isVip = userDoc.getBoolean("vip");
        return isVip != null && isVip;
    }

    /**
     * Verifica si el usuario cumple la restricción de edad.
     */
    private boolean meetsAgeRestriction(Document userDoc, Contenido contenido) {
        Integer minEdad = contenido.getRestringidoEdad();
        if (minEdad == null || minEdad <= 0) {
            return true;
        }

        Object fechaNacObj = userDoc.get("fechaNac");
        if (fechaNacObj == null) {
            return false;
        }

        LocalDate fechaNac = parseFechaNac(fechaNacObj);
        if (fechaNac == null) {
            return false;
        }

        int userAge = calculateAge(fechaNac);
        return userAge >= minEdad;
    }

    /**
     * Convierte un objeto de fecha (puede ser String, java.util.Date, o LocalDate) a LocalDate.
     */
    private LocalDate parseFechaNac(Object fechaNacObj) {
        try {
            if (fechaNacObj instanceof String fechaNacStr) {
                return LocalDate.parse(fechaNacStr);
            } else if (fechaNacObj instanceof java.util.Date utilDate) {
                return utilDate.toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
            } else if (fechaNacObj instanceof LocalDate localDate) {
                return localDate;
            }
        } catch (Exception ex) {
            log.error("Error parseando fecha de nacimiento: {}", ex.getMessage());
        }
        return null;
    }

    /**
     * Extrae y normaliza los gustos del usuario.
     */
    private List<String> extractUserGustos(Document userDoc) {
        @SuppressWarnings("unchecked")
        List<String> misGustos = (List<String>) userDoc.get("misGustos");
        if (misGustos == null || misGustos.isEmpty()) {
            return List.of();
        }

        return misGustos.stream()
                .map(gusto -> gusto != null ? gusto.toLowerCase() : "")
                .filter(gusto -> !gusto.isEmpty())
                .toList();
    }

    /**
     * Comprueba si hay intersección entre gustos del usuario y tags del contenido.
     */
    private boolean hasInterestIntersection(List<String> userGustos, List<String> contentTags) {
        return userGustos.stream().anyMatch(contentTags::contains);
    }

    /**
     * Crea y añade una alerta al usuario.
     * Retorna true si la operación fue exitosa.
     */
    private boolean createAndPushAlert(Document userDoc, Contenido contenido, LocalDateTime now) {
        Document alertDoc = buildAlertDocument(contenido, now);
        String userId = userDoc.getString("_id");

        Query userUpdateQ = new Query(Criteria.where("_id").is(userId));
        Update u = new Update().push("alertInbox", alertDoc);
        UpdateResult res = mongoTemplate.updateFirst(userUpdateQ, u, "users");

        if (res.getModifiedCount() > 0) {
            log.debug("Alerta de interés añadida a usuario {}", userId);
            return true;
        }
        return false;
    }

    /**
     * Construye un documento de alerta de tipo CONTENT_MATCHES_INTERESTS.
     */
    private Document buildAlertDocument(Contenido c, LocalDateTime now) {
        Document d = new Document();
        d.put("id", UUID.randomUUID().toString());
        d.put("type", "CONTENT_MATCHES_INTERESTS");
        d.put("contenidoId", c.getId());
        d.put("tituloContenido", c.getTitulo());
        String mensaje = String.format("Nuevo contenido que coincide con tus gustos: %s", c.getTitulo());
        d.put("mensaje", mensaje);
        d.put("vipOnly", c.isVip());
        d.put("minEdad", c.getRestringidoEdad());
        d.put("creadaEn", now);
        d.put("disponibleHasta", c.getDisponibleHasta());
        return d;
    }

    /**
     * Calcula la edad de una persona a partir de su fecha de nacimiento.
     */
    private int calculateAge(LocalDate birthDate) {
        return (int) ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }
}

