package com.coding.parkingmanagementservice.tarifa.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * HU-013 — Request para crear una nueva tarifa.
 * Al crear, el service desactiva automáticamente la tarifa activa previa
 * del mismo tipo de vehículo + unidad de tiempo.
 */
public record CrearTarifaRequest(

        @NotNull(message = "El tipo de vehículo es obligatorio")
        Long idTipoVehiculo,

        @NotNull(message = "La unidad de tarifa es obligatoria")
        Long idUnidadTarifa,

        @NotNull(message = "El valor es obligatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "El valor debe ser mayor a cero")
        BigDecimal valor
) {}