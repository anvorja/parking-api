package com.coding.parkingmanagementservice.ingreso.service.impl;

import com.coding.parkingmanagementservice.auth.entities.Usuario;
import com.coding.parkingmanagementservice.auth.repositories.UsuarioRepository;
import com.coding.parkingmanagementservice.ingreso.dto.*;
import com.coding.parkingmanagementservice.ingreso.entities.*;
import com.coding.parkingmanagementservice.ingreso.repository.*;
import com.coding.parkingmanagementservice.ingreso.service.IngresoVehiculoService;
import com.coding.parkingmanagementservice.shared.exception.BusinessException;
import com.coding.parkingmanagementservice.shared.exception.ErrorCode;
import com.coding.parkingmanagementservice.tarifa.entities.Tarifa;
import com.coding.parkingmanagementservice.tarifa.repository.TarifaRepository;
import com.coding.parkingmanagementservice.tarifa.repository.UnidadTarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class IngresoVehiculoServiceImpl implements IngresoVehiculoService {

    private static final String ESTADO_INGRESADO                 = "INGRESADO";
    private static final String ESTADO_ENTREGADO                 = "ENTREGADO";
    private static final String ESTADO_DISPONIBLE                = "DISPONIBLE";
    private static final String UNIDAD_HORA                      = "HORA";
    private static final String TIPO_CARRO                       = "CARRO";
    private static final String TIPO_MOTO                        = "MOTO";
    private static final int    CAPACIDAD_MOTOS_EN_ESPACIO_CARRO = 4;

    private final IngresoVehiculoRepository  ingresoVehiculoRepository;
    private final TipoVehiculoRepository     tipoVehiculoRepository;
    private final UbicacionRepository        ubicacionRepository;
    private final EstadoIngresoRepository    estadoIngresoRepository;
    private final EstadoUbicacionRepository  estadoUbicacionRepository;
    private final UsuarioRepository          usuarioRepository;
    private final TarifaRepository           tarifaRepository;
    private final UnidadTarifaRepository     unidadTarifaRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // HU-006: Registrar ingreso
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public IngresoVehiculoResponse registrarIngreso(
            RegistrarIngresoRequest request,
            String nombreUsuarioAutenticado
    ) {
        String placaNormalizada = normalizarPlaca(request.placa());

        validarPlacaNoActiva(placaNormalizada);

        TipoVehiculo tipoVehiculo = obtenerTipoVehiculo(request.idTipoVehiculo());
        Ubicacion    ubicacion    = obtenerUbicacion(request.idUbicacion());

        validarCompatibilidadYCapacidad(ubicacion, tipoVehiculo);

        EstadoIngreso estadoIngresado = estadoIngresoRepository
                .findByNombre(ESTADO_INGRESADO)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ESTADO_INGRESO_NO_ENCONTRADO,
                        "No existe el estado de ingreso INGRESADO",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        Usuario usuarioRegistro = usuarioRepository
                .findByNombreUsuario(nombreUsuarioAutenticado)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USUARIO_AUTENTICADO_NO_ENCONTRADO,
                        "Usuario autenticado no encontrado",
                        HttpStatus.INTERNAL_SERVER_ERROR));

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

        return toResponse(ingresoVehiculoRepository.save(ingreso));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-018: Listar ingresos
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public IngresoVehiculoPageResponse listarIngresos(String placa, String estado, int page, int size) {
        PageRequest pageable     = PageRequest.of(page, size);
        String      placaFiltro  = (placa  != null && !placa.isBlank())  ? placa.trim()  : null;
        String      estadoFiltro = (estado != null && !estado.isBlank()) ? estado.trim() : null;

        Page<IngresoVehiculo> resultado = ingresoVehiculoRepository
                .listarConFiltros(placaFiltro, estadoFiltro, pageable);

        List<IngresoVehiculoResponse> content = resultado.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new IngresoVehiculoPageResponse(
                content,
                resultado.getNumber(),
                resultado.getSize(),
                resultado.getTotalElements(),
                resultado.getTotalPages()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-019: Eliminar ingreso
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void eliminarIngreso(Long idIngreso) {
        IngresoVehiculo ingreso = ingresoVehiculoRepository.findById(idIngreso)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INGRESO_NO_ENCONTRADO,
                        "No existe un registro de ingreso con id " + idIngreso,
                        HttpStatus.NOT_FOUND));
        ingresoVehiculoRepository.delete(ingreso);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-020: Editar ingreso
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public IngresoVehiculoResponse editarIngreso(
            Long idIngreso,
            EditarIngresoRequest request,
            boolean esAdministrador
    ) {
        IngresoVehiculo ingreso = ingresoVehiculoRepository.findById(idIngreso)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INGRESO_NO_ENCONTRADO,
                        "No existe un registro de ingreso con id " + idIngreso,
                        HttpStatus.NOT_FOUND));

        if (request.placa() != null && !request.placa().isBlank()) {
            String placaNueva = normalizarPlaca(request.placa());
            if (!placaNueva.equalsIgnoreCase(ingreso.getPlaca())) {
                validarPlacaNoActiva(placaNueva);
            }
            ingreso.setPlaca(placaNueva);
        }

        if (request.idUbicacion() != null) {
            Ubicacion nuevaUbicacion = obtenerUbicacion(request.idUbicacion());
            if (!nuevaUbicacion.getId().equals(ingreso.getUbicacion().getId())) {
                validarCompatibilidadYCapacidad(nuevaUbicacion, ingreso.getTipoVehiculo());
            }
            ingreso.setUbicacion(nuevaUbicacion);
        }

        if (esAdministrador) {
            if (request.idTipoVehiculo() != null) {
                TipoVehiculo nuevoTipo = obtenerTipoVehiculo(request.idTipoVehiculo());
                if (!nuevoTipo.getId().equals(ingreso.getTipoVehiculo().getId())) {
                    validarCompatibilidadYCapacidad(ingreso.getUbicacion(), nuevoTipo);
                }
                ingreso.setTipoVehiculo(nuevoTipo);
            }
            if (request.idEstadoIngreso() != null) {
                EstadoIngreso nuevoEstado = estadoIngresoRepository.findById(request.idEstadoIngreso())
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.ESTADO_INGRESO_NO_ENCONTRADO,
                                "Estado de ingreso no encontrado: " + request.idEstadoIngreso(),
                                HttpStatus.BAD_REQUEST));
                ingreso.setEstadoIngreso(nuevoEstado);
            }
            if (request.fechaHoraIngreso() != null) {
                ingreso.setFechaHoraIngreso(request.fechaHoraIngreso());
            }
            if (request.fechaHoraSalida() != null) {
                OffsetDateTime fechaIngreso = request.fechaHoraIngreso() != null
                        ? request.fechaHoraIngreso()
                        : ingreso.getFechaHoraIngreso();
                if (request.fechaHoraSalida().isBefore(fechaIngreso)) {
                    throw new BusinessException(
                            ErrorCode.VALIDATION_ERROR,
                            "La fecha de salida no puede ser anterior a la fecha de ingreso",
                            HttpStatus.BAD_REQUEST);
                }
                ingreso.setFechaHoraSalida(request.fechaHoraSalida());
            }
        }

        return toResponse(ingresoVehiculoRepository.save(ingreso));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-009: Obtener detalle por id (preview QR)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public IngresoVehiculoResponse obtenerPorId(Long idIngreso) {
        IngresoVehiculo ingreso = ingresoVehiculoRepository.findByIdFetchAll(idIngreso)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INGRESO_NO_ENCONTRADO,
                        "No existe un registro de ingreso con id " + idIngreso,
                        HttpStatus.NOT_FOUND));
        return toResponse(ingreso);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-010: Buscar ingreso activo por placa (salida manual)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public IngresoVehiculoResponse buscarActivoPorPlaca(String placa) {
        String placaNormalizada = normalizarPlaca(placa);
        IngresoVehiculo ingreso = ingresoVehiculoRepository
                .findActivoByPlaca(placaNormalizada)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INGRESO_NO_ENCONTRADO,
                        "No se encontró un ingreso activo para la placa " + placaNormalizada,
                        HttpStatus.NOT_FOUND));
        return toResponse(ingreso);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HU-011: Registrar salida + cálculo de costo
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SalidaResponse registrarSalida(
            Long idIngreso,
            RegistrarSalidaRequest request,
            String nombreUsuarioEntrega
    ) {
        // 1 — Obtener el ingreso y validar que esté activo
        IngresoVehiculo ingreso = ingresoVehiculoRepository.findByIdFetchAll(idIngreso)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INGRESO_NO_ENCONTRADO,
                        "No existe un registro de ingreso con id " + idIngreso,
                        HttpStatus.NOT_FOUND));

        if (ingreso.getFechaHoraSalida() != null) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "El vehículo con id " + idIngreso + " ya tiene registrada una salida",
                    HttpStatus.CONFLICT);
        }

        // 2 — Determinar la hora de salida
        OffsetDateTime fechaSalida = request.fechaHoraSalida() != null
                ? request.fechaHoraSalida()
                : OffsetDateTime.now();

        if (fechaSalida.isBefore(ingreso.getFechaHoraIngreso())) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "La fecha de salida no puede ser anterior a la fecha de ingreso",
                    HttpStatus.BAD_REQUEST);
        }

        // 3 — Calcular costo: ceil(minutos / 60) × tarifa_hora_tipo
        // HU-011: siempre se cobra la hora iniciada (techo)
        long minutos = Duration.between(ingreso.getFechaHoraIngreso(), fechaSalida).toMinutes()
                + (Duration.between(ingreso.getFechaHoraIngreso(), fechaSalida).toSecondsPart() > 0 ? 1 : 0);
        // Mínimo 1 minuto → 1 hora cobrada (evita costo 0 si se registra la salida
        // exactamente en el mismo minuto del ingreso)
        if (minutos < 1) minutos = 1;

        int horasCobradas = (int) Math.ceil(minutos / 60.0);

        // Buscar tarifa activa por tipo de vehículo + unidad HORA
        String nombreUnidad = UNIDAD_HORA;
        Long   idUnidadHora = unidadTarifaRepository
                .findByNombre(nombreUnidad)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TARIFA_UNIDAD_NO_ENCONTRADA,
                        "No existe la unidad de tarifa HORA",
                        HttpStatus.INTERNAL_SERVER_ERROR))
                .getId();

        Tarifa tarifa = tarifaRepository
                .findByTipoVehiculo_IdAndUnidadTarifa_IdAndActivaTrue(
                        ingreso.getTipoVehiculo().getId(), idUnidadHora)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TARIFA_NO_ENCONTRADA,
                        "No existe tarifa activa para " + ingreso.getTipoVehiculo().getNombre() + "/HORA",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        BigDecimal valorCobrado = tarifa.getValor()
                .multiply(BigDecimal.valueOf(horasCobradas))
                .setScale(2, RoundingMode.HALF_UP);

        // 4 — Obtener estado ENTREGADO
        EstadoIngreso estadoEntregado = estadoIngresoRepository
                .findByNombre(ESTADO_ENTREGADO)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.ESTADO_INGRESO_NO_ENCONTRADO,
                        "No existe el estado de ingreso ENTREGADO",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        // 5 — Obtener usuario entrega
        Usuario usuarioEntrega = usuarioRepository
                .findByNombreUsuario(nombreUsuarioEntrega)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USUARIO_AUTENTICADO_NO_ENCONTRADO,
                        "Usuario autenticado no encontrado",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        // 6 — Obtener estado DISPONIBLE para la ubicación
        EstadoUbicacion estadoDisponible = estadoUbicacionRepository
                .findByNombre(ESTADO_DISPONIBLE)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UBICACION_NO_ENCONTRADA,
                        "No existe el estado de ubicación DISPONIBLE",
                        HttpStatus.INTERNAL_SERVER_ERROR));

        // 7 — Actualizar el ingreso
        ingreso.setFechaHoraSalida(fechaSalida);
        ingreso.setValorCobrado(valorCobrado);
        ingreso.setEstadoIngreso(estadoEntregado);
        ingreso.setUsuarioEntrega(usuarioEntrega);

        // 8 — Liberar la ubicación
        Ubicacion ubicacion = ingreso.getUbicacion();
        // Solo se libera si ya no quedan ingresos activos en esa ubicación
        int activosRestantes = ingresoVehiculoRepository.contarActivosPorUbicacion(ubicacion.getId());
        // contarActivosPorUbicacion usa fechaHoraSalida IS NULL, pero ya seteamos la salida en memoria.
        // Flush implícito antes del count no ocurre hasta el commit, así que ajustamos manualmente:
        // el ingreso actual ya tiene salida → activosRestantes - 1 no aplica porque el save no ocurrió aún.
        // Por eso usamos activosRestantes <= 1 (este es el que acabamos de cerrar + posibles otros)
        if (activosRestantes <= 1) {
            ubicacion.setEstadoUbicacion(estadoDisponible);
            ubicacionRepository.save(ubicacion);
        }

        ingresoVehiculoRepository.save(ingreso);

        return new SalidaResponse(
                ingreso.getId(),
                ingreso.getPlaca(),
                ingreso.getTipoVehiculo().getNombre(),
                ubicacion.getNombre(),
                ingreso.getFechaHoraIngreso(),
                fechaSalida,
                horasCobradas,
                tarifa.getValor(),
                valorCobrado,
                nombreUsuarioEntrega
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Validaciones compartidas
    // ─────────────────────────────────────────────────────────────────────────

    private void validarPlacaNoActiva(String placa) {
        if (ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull(placa)) {
            throw new BusinessException(
                    ErrorCode.PLACA_CON_INGRESO_ACTIVO,
                    "La placa ya tiene un ingreso activo en el parqueadero",
                    HttpStatus.CONFLICT);
        }
    }

    private void validarCompatibilidadYCapacidad(Ubicacion ubicacion, TipoVehiculo tipoVehiculo) {
        String tipoNativo  = ubicacion.getTipoVehiculoNativo().getNombre().toUpperCase(Locale.ROOT);
        String tipoIngreso = tipoVehiculo.getNombre().toUpperCase(Locale.ROOT);

        if (TIPO_MOTO.equals(tipoNativo)) {
            if (!TIPO_MOTO.equals(tipoIngreso)) {
                throw new BusinessException(
                        ErrorCode.TIPO_VEHICULO_INCOMPATIBLE_CON_UBICACION,
                        "El espacio " + ubicacion.getNombre() + " es exclusivo para motos",
                        HttpStatus.CONFLICT);
            }
            if (ingresoVehiculoRepository.contarActivosPorUbicacion(ubicacion.getId()) >= 1) {
                throw new BusinessException(
                        ErrorCode.UBICACION_NO_DISPONIBLE,
                        "El espacio " + ubicacion.getNombre() + " ya está ocupado",
                        HttpStatus.CONFLICT);
            }
            return;
        }

        if (TIPO_CARRO.equals(tipoNativo)) {
            int activosActuales = ingresoVehiculoRepository.contarActivosPorUbicacion(ubicacion.getId());
            if (TIPO_CARRO.equals(tipoIngreso)) {
                if (activosActuales > 0) {
                    throw new BusinessException(
                            ErrorCode.UBICACION_NO_DISPONIBLE,
                            "El espacio " + ubicacion.getNombre() + " ya está ocupado",
                            HttpStatus.CONFLICT);
                }
            } else if (TIPO_MOTO.equals(tipoIngreso)) {
                if (activosActuales > 0) {
                    Long idTipoNativo = ubicacion.getTipoVehiculoNativo().getId();
                    boolean hayCarroActivo = ingresoVehiculoRepository
                            .existeActivoConTipoVehiculo(ubicacion.getId(), idTipoNativo);
                    if (hayCarroActivo) {
                        throw new BusinessException(
                                ErrorCode.TIPO_VEHICULO_INCOMPATIBLE_CON_UBICACION,
                                "El espacio " + ubicacion.getNombre()
                                        + " está ocupado por un carro — no puede recibir motos",
                                HttpStatus.CONFLICT);
                    }
                    if (activosActuales >= CAPACIDAD_MOTOS_EN_ESPACIO_CARRO) {
                        throw new BusinessException(
                                ErrorCode.UBICACION_NO_DISPONIBLE,
                                "El espacio " + ubicacion.getNombre()
                                        + " ya tiene " + CAPACIDAD_MOTOS_EN_ESPACIO_CARRO
                                        + " motos (capacidad máxima en modo adaptado)",
                                HttpStatus.CONFLICT);
                    }
                }
            }
            return;
        }

        throw new BusinessException(
                ErrorCode.TIPO_VEHICULO_INCOMPATIBLE_CON_UBICACION,
                "Tipo de espacio no reconocido: " + tipoNativo,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private IngresoVehiculoResponse toResponse(IngresoVehiculo i) {
        return new IngresoVehiculoResponse(
                i.getId(),
                i.getPlaca(),
                i.getTipoVehiculo().getId(),
                i.getTipoVehiculo().getNombre(),
                i.getUbicacion().getId(),
                i.getUbicacion().getNombre(),
                i.getEstadoIngreso().getId(),
                i.getEstadoIngreso().getNombre(),
                i.getFechaHoraIngreso(),
                i.getFechaCreacion(),
                i.getUsuarioRegistro().getId(),
                i.getUsuarioRegistro().getNombreUsuario(),
                i.getValorCobrado()
        );
    }

    private String normalizarPlaca(String placa) {
        return placa == null ? null
                : placa.trim().toUpperCase(Locale.ROOT).replace(" ", "");
    }

    private TipoVehiculo obtenerTipoVehiculo(Long id) {
        return tipoVehiculoRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.TIPO_VEHICULO_NO_ENCONTRADO,
                        "Tipo de vehículo no encontrado",
                        HttpStatus.BAD_REQUEST));
    }

    private Ubicacion obtenerUbicacion(Long id) {
        return ubicacionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.UBICACION_NO_ENCONTRADA,
                        "Ubicación no encontrada",
                        HttpStatus.BAD_REQUEST));
    }
}