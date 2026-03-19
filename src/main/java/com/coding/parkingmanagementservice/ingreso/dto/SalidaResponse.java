package com.coding.parkingmanagementservice.ingreso.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * HU-011 — Respuesta tras confirmar la salida.
 * Incluye el costo calculado y todos los datos del tiquete de salida.
 */
public record SalidaResponse(
        Long           idIngreso,
        String         placa,
        String         tipoVehiculo,
        String         ubicacion,
        OffsetDateTime fechaHoraIngreso,
        OffsetDateTime fechaHoraSalida,
        /** Horas cobradas (siempre entero — se aplica ceil) */
        int            horasCobradas,
        /** Tarifa por hora aplicada */
        BigDecimal     tarifaPorHora,
        /** valorCobrado = horasCobradas × tarifaPorHora */
        BigDecimal     valorCobrado,
        String         usuarioEntrega
) {}