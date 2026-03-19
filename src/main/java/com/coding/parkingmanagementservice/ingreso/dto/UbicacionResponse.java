package com.coding.parkingmanagementservice.ingreso.dto;

/**
 * DTO de respuesta para ubicaciones.
 * estadoNombre: DISPONIBLE | OCUPADO | INACTIVO
 * disponible: true si estadoNombre == DISPONIBLE (conveniente para el frontend)
 */
public record UbicacionResponse(
        Long    id,
        String  nombre,
        Long    idTipoVehiculoNativo,
        String  tipoVehiculoNativo,
        Integer capacidad,
        String  estadoNombre,
        boolean disponible
) {}