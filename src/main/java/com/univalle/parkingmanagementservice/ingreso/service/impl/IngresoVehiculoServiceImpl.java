package com.univalle.parkingmanagementservice.ingreso.service.impl;

import com.univalle.parkingmanagementservice.auth.entities.Usuario;
import com.univalle.parkingmanagementservice.auth.repositories.UsuarioRepository;
import com.univalle.parkingmanagementservice.ingreso.dto.IngresoVehiculoResponse;
import com.univalle.parkingmanagementservice.ingreso.dto.RegistrarIngresoRequest;
import com.univalle.parkingmanagementservice.ingreso.entities.*;
import com.univalle.parkingmanagementservice.ingreso.repository.EstadoIngresoRepository;
import com.univalle.parkingmanagementservice.ingreso.repository.IngresoVehiculoRepository;
import com.univalle.parkingmanagementservice.ingreso.repository.TipoVehiculoRepository;
import com.univalle.parkingmanagementservice.ingreso.repository.UbicacionRepository;
import com.univalle.parkingmanagementservice.ingreso.service.IngresoVehiculoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class IngresoVehiculoServiceImpl implements IngresoVehiculoService {

    private static final String ESTADO_INGRESADO = "INGRESADO";
    private static final String ESTADO_UBICACION_DISPONIBLE = "DISPONIBLE";

    private final IngresoVehiculoRepository ingresoVehiculoRepository;
    private final TipoVehiculoRepository tipoVehiculoRepository;
    private final UbicacionRepository ubicacionRepository;
    private final EstadoIngresoRepository estadoIngresoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public IngresoVehiculoResponse registrarIngreso(RegistrarIngresoRequest request, String nombreUsuarioAutenticado) {

        String placaNormalizada = normalizarPlaca(request.placa());

        validarPlacaNoActiva(placaNormalizada);
        TipoVehiculo tipoVehiculo = obtenerTipoVehiculo(request.idTipoVehiculo());
        Ubicacion ubicacion = obtenerUbicacionDisponible(request.idUbicacion());
        validarUbicacionSinIngresoActivo(ubicacion.getId());

        EstadoIngreso estadoIngresado = estadoIngresoRepository.findByNombre(ESTADO_INGRESADO)
                .orElseThrow(() -> new IllegalArgumentException("No existe el estado de ingreso INGRESADO"));

        Usuario usuarioRegistro = usuarioRepository.findByNombreUsuario(nombreUsuarioAutenticado)
                .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));

        OffsetDateTime fechaIngreso = request.fechaHoraIngreso() != null
                ? request.fechaHoraIngreso()
                : OffsetDateTime.now();

        IngresoVehiculo ingreso = new IngresoVehiculo();
        ingreso.setPlaca(placaNormalizada);
        ingreso.setTipoVehiculo(tipoVehiculo);
        ingreso.setUbicacion(ubicacion);
        ingreso.setEstadoIngreso(estadoIngresado);
        ingreso.setFechaHoraIngreso(fechaIngreso);
        ingreso.setUsuarioRegistro(usuarioRegistro);
        ingreso.setFechaCreacion(OffsetDateTime.now());

        IngresoVehiculo saved = ingresoVehiculoRepository.save(ingreso);

        return new IngresoVehiculoResponse(
                saved.getId(),
                saved.getPlaca(),
                saved.getTipoVehiculo().getId(),
                saved.getTipoVehiculo().getNombre(),
                saved.getUbicacion().getId(),
                saved.getUbicacion().getNombre(),
                saved.getEstadoIngreso().getId(),
                saved.getEstadoIngreso().getNombre(),
                saved.getFechaHoraIngreso(),
                saved.getFechaCreacion(),
                saved.getUsuarioRegistro().getId(),
                saved.getUsuarioRegistro().getNombreUsuario(),
                saved.getValorCobrado()
        );
    }

    private String normalizarPlaca(String placa) {
        return placa == null
                ? null
                : placa.trim().toUpperCase(Locale.ROOT).replace(" ", "");
    }

    private void validarPlacaNoActiva(String placa) {
        if (ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull(placa)) {
            throw new IllegalArgumentException("La placa ya se encuentra registrada con un ingreso activo");
        }
    }

    private TipoVehiculo obtenerTipoVehiculo(Long idTipoVehiculo) {
        return tipoVehiculoRepository.findById(idTipoVehiculo)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de vehículo no encontrado"));
    }

    private Ubicacion obtenerUbicacionDisponible(Long idUbicacion) {
        Ubicacion ubicacion = ubicacionRepository.findById(idUbicacion)
                .orElseThrow(() -> new EntityNotFoundException("Ubicación no encontrada"));

        EstadoUbicacion estadoUbicacion = ubicacion.getEstadoUbicacion();
        if (estadoUbicacion == null || !ESTADO_UBICACION_DISPONIBLE.equalsIgnoreCase(estadoUbicacion.getNombre())) {
            throw new IllegalArgumentException("La ubicación seleccionada no está disponible");
        }

        return ubicacion;
    }

    private void validarUbicacionSinIngresoActivo(Long idUbicacion) {
        if (ingresoVehiculoRepository.existsByUbicacion_IdAndFechaHoraSalidaIsNull(idUbicacion)) {
            throw new IllegalArgumentException("La ubicación seleccionada ya está ocupada por un vehículo activo");
        }
    }
}
