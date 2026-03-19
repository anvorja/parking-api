package com.coding.parkingmanagementservice.tarifa.service.impl;

import com.coding.parkingmanagementservice.ingreso.entities.TipoVehiculo;
import com.coding.parkingmanagementservice.ingreso.repository.TipoVehiculoRepository;
import com.coding.parkingmanagementservice.shared.exception.BusinessException;
import com.coding.parkingmanagementservice.shared.exception.ErrorCode;
import com.coding.parkingmanagementservice.tarifa.dto.CrearTarifaRequest;
import com.coding.parkingmanagementservice.tarifa.dto.EditarTarifaRequest;
import com.coding.parkingmanagementservice.tarifa.dto.TarifaResponse;
import com.coding.parkingmanagementservice.tarifa.entities.Tarifa;
import com.coding.parkingmanagementservice.tarifa.entities.UnidadTarifa;
import com.coding.parkingmanagementservice.tarifa.repository.TarifaRepository;
import com.coding.parkingmanagementservice.tarifa.repository.UnidadTarifaRepository;
import com.coding.parkingmanagementservice.tarifa.service.TarifaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarifaServiceImpl implements TarifaService {

    private final TarifaRepository       tarifaRepository;
    private final TipoVehiculoRepository  tipoVehiculoRepository;
    private final UnidadTarifaRepository  unidadTarifaRepository;

    // ─── Listar activas ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TarifaResponse> listarActivas() {
        return tarifaRepository.findByActivaTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Crear ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TarifaResponse crear(CrearTarifaRequest request) {
        TipoVehiculo tipoVehiculo = tipoVehiculoRepository
                .findById(request.idTipoVehiculo())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TIPO_VEHICULO_NO_ENCONTRADO,
                        "Tipo de vehículo no encontrado: " + request.idTipoVehiculo(),
                        HttpStatus.BAD_REQUEST));

        UnidadTarifa unidadTarifa = unidadTarifaRepository
                .findById(request.idUnidadTarifa())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TARIFA_UNIDAD_NO_ENCONTRADA,
                        "Unidad de tarifa no encontrada: " + request.idUnidadTarifa(),
                        HttpStatus.BAD_REQUEST));

        // Regla de negocio: desactivar la tarifa activa previa del mismo tipo+unidad
        // Esto garantiza exactamente UNA tarifa activa por tipo+unidad en todo momento
        tarifaRepository.desactivarActivasPorTipoYUnidad(
                request.idTipoVehiculo(), request.idUnidadTarifa());

        Tarifa nueva = new Tarifa();
        nueva.setTipoVehiculo(tipoVehiculo);
        nueva.setUnidadTarifa(unidadTarifa);
        nueva.setValor(request.valor());
        nueva.setActiva(true);
        nueva.setFechaCreacion(OffsetDateTime.now());

        return toResponse(tarifaRepository.save(nueva));
    }

    // ─── Editar ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TarifaResponse editar(Long idTarifa, EditarTarifaRequest request) {
        Tarifa tarifa = tarifaRepository.findById(idTarifa)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TARIFA_NO_ENCONTRADA,
                        "No existe una tarifa con id " + idTarifa,
                        HttpStatus.NOT_FOUND));

        if (!tarifa.isActiva()) {
            throw new BusinessException(
                    ErrorCode.TARIFA_NO_ENCONTRADA,
                    "No se puede editar una tarifa inactiva. Crea una nueva tarifa en su lugar.",
                    HttpStatus.BAD_REQUEST);
        }

        tarifa.setValor(request.valor());
        return toResponse(tarifaRepository.save(tarifa));
    }

    // ─── Desactivar (soft delete) ─────────────────────────────────────────────

    @Override
    @Transactional
    public void desactivar(Long idTarifa) {
        Tarifa tarifa = tarifaRepository.findById(idTarifa)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TARIFA_NO_ENCONTRADA,
                        "No existe una tarifa con id " + idTarifa,
                        HttpStatus.NOT_FOUND));

        tarifa.setActiva(false);
        tarifaRepository.save(tarifa);
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private TarifaResponse toResponse(Tarifa t) {
        return new TarifaResponse(
                t.getId(),
                t.getTipoVehiculo().getId(),
                t.getTipoVehiculo().getNombre(),
                t.getUnidadTarifa().getId(),
                t.getUnidadTarifa().getNombre(),
                t.getValor(),
                t.isActiva(),
                t.getFechaCreacion()
        );
    }
}