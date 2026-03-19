package com.coding.parkingmanagementservice.tarifa.service;

import com.coding.parkingmanagementservice.tarifa.dto.CrearTarifaRequest;
import com.coding.parkingmanagementservice.tarifa.dto.EditarTarifaRequest;
import com.coding.parkingmanagementservice.tarifa.dto.TarifaResponse;

import java.util.List;

public interface TarifaService {

    /** Retorna todas las tarifas activas */
    List<TarifaResponse> listarActivas();

    /**
     * Crea una nueva tarifa.
     * Desactiva automáticamente la tarifa activa previa del mismo tipo+unidad.
     * Solo ADMINISTRADOR (validado en controller con @PreAuthorize).
     */
    TarifaResponse crear(CrearTarifaRequest request);

    /**
     * Edita el valor de una tarifa existente.
     * Solo ADMINISTRADOR.
     */
    TarifaResponse editar(Long idTarifa, EditarTarifaRequest request);

    /**
     * Desactiva (soft delete) una tarifa.
     * Solo ADMINISTRADOR.
     */
    void desactivar(Long idTarifa);
}