package com.coding.parkingmanagementservice.ingreso.service;

import com.coding.parkingmanagementservice.ingreso.dto.*;
import java.util.UUID;

public interface IngresoVehiculoService {

    IngresoVehiculoResponse registrarIngreso(RegistrarIngresoRequest request, String nombreUsuarioAutenticado);

    IngresoVehiculoPageResponse listarIngresos(String placa, String estado, String fecha, int page, int size);

    void eliminarIngreso(Long idIngreso);

    IngresoVehiculoResponse editarIngreso(Long idIngreso, EditarIngresoRequest request, boolean esAdministrador);

    /** HU-009/HU-010 — Obtiene el detalle de un ingreso activo por id (preview antes de confirmar salida) */
    IngresoVehiculoResponse obtenerPorId(Long idIngreso);

    /** HU-009 — Obtiene el detalle de un ingreso por su UUID público (leído del QR del tiquete) */
    IngresoVehiculoResponse obtenerPorUuid(UUID uuid);

    /** HU-010 — Busca el ingreso activo por placa (salida manual) */
    IngresoVehiculoResponse buscarActivoPorPlaca(String placa);

    /**
     * HU-009/HU-010/HU-011 — Confirma la salida del vehículo.
     * Calcula el costo: ceil(minutos/60) × tarifa_hora_tipo.
     * Libera la ubicación (estado → DISPONIBLE).
     * Cambia el estado del ingreso a ENTREGADO.
     *
     * @param idIngreso           id del registro de ingreso
     * @param request             contiene la fechaHoraSalida (null = ahora)
     * @param nombreUsuarioEntrega username del operador que registra la salida
     */
    SalidaResponse registrarSalida(Long idIngreso, RegistrarSalidaRequest request, String nombreUsuarioEntrega);
}