package com.coding.parkingmanagementservice.ingreso.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * HU-015 — Request para editar una ubicación.
 * Todos los campos son opcionales (edición parcial).
 * Restricción: idTipoVehiculoNativo no se puede cambiar si hay ingresos activos.
 */
public record EditarUbicacionRequest(

        @Size(max = 50, message = "El nombre no puede superar 50 caracteres")
        String nombre,

        Long idTipoVehiculoNativo,

        @Min(value = 1, message = "La capacidad mínima es 1")
        Integer capacidad
) {}