package com.EsiMediaG03.http;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.EsiMediaG03.services.ExpiringContentAlertService;

@RestController
@RequestMapping("/internal/alerts")
public class AlertsAdminController {

    private final ExpiringContentAlertService alertService;

    public AlertsAdminController(ExpiringContentAlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping("/trigger-expiring")
    public String triggerOnce() {
        int created = alertService.generateExpiringContentAlerts();
        return "Alertas creadas: " + created;
    }
}
