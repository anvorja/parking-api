package com.coding.parkingmanagementservice.ingreso.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * HU-012 — Request para crear una nueva ubicación.
 * Solo ADMINISTRADOR.
 */
public record CrearUbicacionRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
        String nombre,

        @NotNull(message = "El tipo de vehículo nativo es obligatorio")
        Long idTipoVehiculoNativo,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad mínima es 1")
        Integer capacidad
) {}