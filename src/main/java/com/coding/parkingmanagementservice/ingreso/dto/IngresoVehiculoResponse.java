package com.coding.parkingmanagementservice.ingreso.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record IngresoVehiculoResponse(
        Long idIngreso,
        String placa,
        Long idTipoVehiculo,
        String tipoVehiculo,
        Long idUbicacion,
        String ubicacion,
        Long idEstadoIngreso,
        String estadoIngreso,
        OffsetDateTime fechaHoraIngreso,
        OffsetDateTime fechaCreacion,
        Long idUsuarioRegistro,
        String usuarioRegistro,
        BigDecimal valorCobrado
) {
}