package com.coding.parkingmanagementservice.ingreso.service.impl;

import com.coding.parkingmanagementservice.ingreso.dto.CrearUbicacionRequest;
import com.coding.parkingmanagementservice.ingreso.dto.EditarUbicacionRequest;
import com.coding.parkingmanagementservice.ingreso.dto.UbicacionResponse;
import com.coding.parkingmanagementservice.ingreso.entities.EstadoUbicacion;
import com.coding.parkingmanagementservice.ingreso.entities.TipoVehiculo;
import com.coding.parkingmanagementservice.ingreso.entities.Ubicacion;
import com.coding.parkingmanagementservice.ingreso.repository.EstadoUbicacionRepository;
import com.coding.parkingmanagementservice.ingreso.repository.TipoVehiculoRepository;
import com.coding.parkingmanagementservice.ingreso.repository.UbicacionRepository;
import com.coding.parkingmanagementservice.ingreso.service.UbicacionService;
import com.coding.parkingmanagementservice.shared.exception.BusinessException;
import com.coding.parkingmanagementservice.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionServiceImpl implements UbicacionService {

    private static final String ESTADO_DISPONIBLE = "DISPONIBLE";
    private static final String ESTADO_INACTIVO   = "INACTIVO";

    private final UbicacionRepository       ubicacionRepository;
    private final TipoVehiculoRepository    tipoVehiculoRepository;
    private final EstadoUbicacionRepository estadoUbicacionRepository;

    // ─── HU-014: Listar activas ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionResponse> listarActivas() {
        return ubicacionRepository.findAllActivas()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionResponse> listarTodas() {
        return ubicacionRepository.findAllUbicaciones()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── HU-012: Crear ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UbicacionResponse crear(CrearUbicacionRequest request) {
        // Validar nombre único (sin excluir ningún id)
        if (ubicacionRepository.existeNombre(request.nombre().trim(), null)) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Ya existe una ubicación con el nombre '" + request.nombre().trim() + "'",
                    HttpStatus.CONFLICT);
        }

        TipoVehiculo tipoNativo = tipoVehiculoRepository.findById(request.idTipoVehiculoNativo())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TIPO_VEHICULO_NO_ENCONTRADO,
                        "Tipo de vehículo no encontrado: " + request.idTipoVehiculoNativo(),
                        HttpStatus.BAD_REQUEST));

        EstadoUbicacion estadoDisponible = estadoUbicacionRepository
                .findByNombre(ESTADO_DISPONIBLE)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UBICACION_NO_ENCONTRADA,
                        "No existe el estado de ubicación DISPONIBLE",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        Ubicacion nueva = new Ubicacion();
        nueva.setNombre(request.nombre().trim().toUpperCase());
        nueva.setTipoVehiculoNativo(tipoNativo);
        nueva.setCapacidad(request.capacidad());
        nueva.setEstadoUbicacion(estadoDisponible);
        nueva.setFechaCreacion(OffsetDateTime.now());

        return toResponse(ubicacionRepository.save(nueva));
    }

    // ─── HU-015: Editar ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public UbicacionResponse editar(Long idUbicacion, EditarUbicacionRequest request) {
        Ubicacion ubicacion = ubicacionRepository.findById(idUbicacion)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UBICACION_NO_ENCONTRADA,
                        "No existe una ubicación con id " + idUbicacion,
                        HttpStatus.NOT_FOUND));

        if (ESTADO_INACTIVO.equalsIgnoreCase(ubicacion.getEstadoUbicacion().getNombre())) {
            throw new BusinessException(
                    ErrorCode.ACCION_NO_PERMITIDA,
                    "No se puede editar una ubicación inactiva",
                    HttpStatus.BAD_REQUEST);
        }

        // Editar nombre
        if (request.nombre() != null && !request.nombre().isBlank()) {
            String nuevoNombre = request.nombre().trim().toUpperCase();
            if (!nuevoNombre.equals(ubicacion.getNombre())
                    && ubicacionRepository.existeNombre(nuevoNombre, idUbicacion)) {
                throw new BusinessException(
                        ErrorCode.VALIDATION_ERROR,
                        "Ya existe una ubicación con el nombre '" + nuevoNombre + "'",
                        HttpStatus.CONFLICT);
            }
            ubicacion.setNombre(nuevoNombre);
        }

        // Editar tipo nativo — solo si NO tiene ingresos activos
        if (request.idTipoVehiculoNativo() != null
                && !request.idTipoVehiculoNativo().equals(ubicacion.getTipoVehiculoNativo().getId())) {

            if (ubicacionRepository.tieneIngresosActivos(idUbicacion)) {
                throw new BusinessException(
                        ErrorCode.UBICACION_CON_INGRESOS_ACTIVOS,
                        "No se puede cambiar el tipo de una ubicación con vehículos activos",
                        HttpStatus.CONFLICT);
            }

            TipoVehiculo nuevoTipo = tipoVehiculoRepository.findById(request.idTipoVehiculoNativo())
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.TIPO_VEHICULO_NO_ENCONTRADO,
                            "Tipo de vehículo no encontrado: " + request.idTipoVehiculoNativo(),
                            HttpStatus.BAD_REQUEST));
            ubicacion.setTipoVehiculoNativo(nuevoTipo);
        }

        // Editar capacidad
        if (request.capacidad() != null) {
            ubicacion.setCapacidad(request.capacidad());
        }

        return toResponse(ubicacionRepository.save(ubicacion));
    }

    // ─── HU-016: Desactivar (soft delete) ────────────────────────────────────

    @Override
    @Transactional
    public void desactivar(Long idUbicacion) {
        Ubicacion ubicacion = ubicacionRepository.findById(idUbicacion)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UBICACION_NO_ENCONTRADA,
                        "No existe una ubicación con id " + idUbicacion,
                        HttpStatus.NOT_FOUND));

        if (ESTADO_INACTIVO.equalsIgnoreCase(ubicacion.getEstadoUbicacion().getNombre())) {
            throw new BusinessException(
                    ErrorCode.ACCION_NO_PERMITIDA,
                    "La ubicación ya está inactiva",
                    HttpStatus.BAD_REQUEST);
        }

        // Restricción: no eliminar si tiene ingresos activos
        if (ubicacionRepository.tieneIngresosActivos(idUbicacion)) {
            throw new BusinessException(
                    ErrorCode.UBICACION_CON_INGRESOS_ACTIVOS,
                    "No se puede eliminar una ubicación con vehículos activos. "
                            + "Espera a que todos los vehículos registren su salida.",
                    HttpStatus.CONFLICT);
        }

        EstadoUbicacion estadoInactivo = estadoUbicacionRepository
                .findByNombre(ESTADO_INACTIVO)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UBICACION_NO_ENCONTRADA,
                        "No existe el estado de ubicación INACTIVO",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        ubicacion.setEstadoUbicacion(estadoInactivo);
        ubicacionRepository.save(ubicacion);
    }

    // ─── Reactivar ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void reactivar(Long idUbicacion) {
        Ubicacion ubicacion = ubicacionRepository.findById(idUbicacion)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UBICACION_NO_ENCONTRADA,
                        "No existe una ubicación con id " + idUbicacion,
                        HttpStatus.NOT_FOUND));

        if (!ESTADO_INACTIVO.equalsIgnoreCase(ubicacion.getEstadoUbicacion().getNombre())) {
            throw new BusinessException(
                    ErrorCode.ACCION_NO_PERMITIDA,
                    "La ubicación no está inactiva",
                    HttpStatus.BAD_REQUEST);
        }

        EstadoUbicacion estadoDisponible = estadoUbicacionRepository
                .findByNombre(ESTADO_DISPONIBLE)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UBICACION_NO_ENCONTRADA,
                        "No existe el estado de ubicación DISPONIBLE",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        ubicacion.setEstadoUbicacion(estadoDisponible);
        ubicacionRepository.save(ubicacion);
    }


    // ─── Mapper ───────────────────────────────────────────────────────────────

    private UbicacionResponse toResponse(Ubicacion u) {
        String estado = u.getEstadoUbicacion().getNombre();
        return new UbicacionResponse(
                u.getId(),
                u.getNombre(),
                u.getTipoVehiculoNativo().getId(),
                u.getTipoVehiculoNativo().getNombre(),
                u.getCapacidad(),
                estado,
                ESTADO_DISPONIBLE.equalsIgnoreCase(estado)
        );
    }
}