package com.coding.parkingmanagementservice.shared.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@Tag(
        name = "Sistema",
        description = """
                Endpoints utilitarios para verificar el estado operativo del servicio.
                No requieren autenticación y son utilizados por plataformas de despliegue
                (Render, balanceadores de carga, monitores externos) para health checks automáticos.
                """
)
public class HealthController {

    private static final ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy -- HH:mm:ss");

    @GetMapping("/")
    @Operation(
            summary = "Redirección al health check",
            description = "Redirige automáticamente al endpoint `/health`. Punto de entrada raíz del servicio."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirección a /health.")
    })
    public void redirectToHealth(HttpServletResponse response) throws IOException {
        response.sendRedirect("/health");
    }

    @GetMapping("/health")
    @Operation(
            summary = "Verificar estado del servicio",
            description = """
                    Retorna el estado operativo actual del servicio junto con el timestamp del servidor
                    en zona horaria de Bogotá (America/Bogota).
                    Utilizado por Render y otros orquestadores para determinar si la instancia está lista
                    para recibir tráfico. Una respuesta `200 OK` con `status: UP` indica disponibilidad total.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio operativo. Retorna status, nombre del servicio y timestamp.",
                    content = @Content(schema = @Schema(
                            example = """
                                    {
                                      "status": "UP",
                                      "service": "parking-management-service",
                                      "timestamp": "19-03-2026 -- 14:35:00"
                                    }
                                    """
                    ))
            )
    })
    public ResponseEntity<Map<String, Object>> health() {
        String timestamp = ZonedDateTime.now(BOGOTA_ZONE).format(FORMATTER);
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "parking-management-service",
                "timestamp", timestamp
        ));
    }
}