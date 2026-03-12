package com.univalle.parkingmanagementservice.shared.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
public class HealthController {

    private static final ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy -- HH:mm:ss");

    @GetMapping("/")
    public void redirectToHealth(HttpServletResponse response) throws IOException {
        response.sendRedirect("/health");
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {

        String timestamp = ZonedDateTime.now(BOGOTA_ZONE).format(FORMATTER);

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "parking-management-service",
                "timestamp", timestamp
        ));
    }
}