package com.coding.parkingmanagementservice.ingreso.dto;

import java.time.OffsetDateTime;

/**
 *
 * Campos permitidos según rol:
 *   ADMINISTRADOR → todos los campos
 *   AUXILIAR      → solo placa e idUbicacion
 *
 * La validación de permisos se realiza en IngresoVehiculoServiceImpl.
 * La validación de coherencia de fechas (salida >= ingreso) también en el service.
 */
public record EditarIngresoRequest(

        /** Placa del vehículo (editable por AUXILIAR y ADMINISTRADOR) */
        String placa,

        /** Solo ADMINISTRADOR */
        Long idTipoVehiculo,

        /** Editable por AUXILIAR y ADMINISTRADOR */
        Long idUbicacion,

        /** Solo ADMINISTRADOR */
        Long idEstadoIngreso,

        /** Solo ADMINISTRADOR */
        OffsetDateTime fechaHoraIngreso,

        /** Solo ADMINISTRADOR. Debe ser >= fechaHoraIngreso si se proporciona */
        OffsetDateTime fechaHoraSalida
) {}