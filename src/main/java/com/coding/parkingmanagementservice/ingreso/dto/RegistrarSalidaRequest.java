package com.coding.parkingmanagementservice.ingreso.dto;

import java.time.OffsetDateTime;

/**
 * HU-009/HU-010 — Request para confirmar la salida de un vehículo.
 *
 * fechaHoraSalida es opcional: si no se envía, el backend usa OffsetDateTime.now().
 * Esto permite que el frontend envíe la hora exacta cuando la operación
 * se procesó offline (vía outbox).
 */
public record RegistrarSalidaRequest(
        /**
         * Momento de la salida. Si es null, el backend usa la hora actual.
         * El frontend offline envía el timestamp del momento en que el operador
         * confirmó la salida, no el momento de la sincronización.
         */
        OffsetDateTime fechaHoraSalida
) {}