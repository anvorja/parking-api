package com.coding.parkingmanagementservice.ingreso.service;

import com.coding.parkingmanagementservice.ingreso.dto.CrearUbicacionRequest;
import com.coding.parkingmanagementservice.ingreso.dto.EditarUbicacionRequest;
import com.coding.parkingmanagementservice.ingreso.dto.UbicacionResponse;

import java.util.List;

public interface UbicacionService {

    /** HU-014 — Lista todas las ubicaciones activas (excluye INACTIVO) */
    List<UbicacionResponse> listarActivas();

    /** HU-012 — Crea una nueva ubicación. Solo ADMINISTRADOR */
    UbicacionResponse crear(CrearUbicacionRequest request);

    /** HU-015 — Edita nombre, tipo o capacidad. Solo ADMINISTRADOR */
    UbicacionResponse editar(Long idUbicacion, EditarUbicacionRequest request);

    /** HU-016 — Desactiva (soft delete) una ubicación.
     * Solo ADMINISTRADOR. Falla si tiene ingresos activos.
     */
    void desactivar(Long idUbicacion);

    /** Lista todas las ubicaciones, incluyendo INACTIVOS */
    List<UbicacionResponse> listarTodas();

    /** Reactiva una ubicación inactiva. Solo ADMINISTRADOR. */
    void reactivar(Long idUbicacion);
}