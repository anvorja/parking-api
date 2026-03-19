package com.coding.parkingmanagementservice.ingreso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record RegistrarIngresoRequest(

        @NotBlank(message = "La placa es obligatoria")
        String placa,

        @NotNull(message = "El tipo de vehículo es obligatorio")
        Long idTipoVehiculo,

        @NotNull(message = "La ubicación es obligatoria")
        Long idUbicacion,

        OffsetDateTime fechaHoraIngreso
) {
}
