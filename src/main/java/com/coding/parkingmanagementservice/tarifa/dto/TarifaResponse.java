package com.coding.parkingmanagementservice.tarifa.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TarifaResponse(
        Long            idTarifa,
        Long            idTipoVehiculo,
        String          tipoVehiculo,
        Long            idUnidadTarifa,
        String          unidadTarifa,
        BigDecimal valor,
        boolean         activa,
        OffsetDateTime fechaCreacion
) {}