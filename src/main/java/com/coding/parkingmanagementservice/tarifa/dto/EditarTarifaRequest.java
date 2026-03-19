package com.coding.parkingmanagementservice.tarifa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * HU-013 — Request para editar el valor de una tarifa existente.
 * Solo se puede cambiar el valor — tipo y unidad son inmutables una vez creados.
 */
public record EditarTarifaRequest(

        @NotNull(message = "El valor es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El valor debe ser mayor a cero")
        BigDecimal valor
) {}