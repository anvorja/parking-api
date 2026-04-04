package com.coding.parkingmanagementservice.ingreso.service.impl;

import com.coding.parkingmanagementservice.auth.entities.Usuario;
import com.coding.parkingmanagementservice.auth.repositories.UsuarioRepository;
import com.coding.parkingmanagementservice.ingreso.dto.*;
import com.coding.parkingmanagementservice.ingreso.entities.*;
import com.coding.parkingmanagementservice.ingreso.repository.*;
import com.coding.parkingmanagementservice.shared.exception.BusinessException;
import com.coding.parkingmanagementservice.shared.exception.ErrorCode;
import com.coding.parkingmanagementservice.tarifa.entities.Tarifa;
import com.coding.parkingmanagementservice.tarifa.entities.UnidadTarifa;
import com.coding.parkingmanagementservice.tarifa.repository.TarifaRepository;
import com.coding.parkingmanagementservice.tarifa.repository.UnidadTarifaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngresoVehiculoServiceImplTest {

    @Mock private IngresoVehiculoRepository ingresoVehiculoRepository;
    @Mock private TipoVehiculoRepository    tipoVehiculoRepository;
    @Mock private UbicacionRepository       ubicacionRepository;
    @Mock private EstadoIngresoRepository   estadoIngresoRepository;
    @Mock private UsuarioRepository         usuarioRepository;
    @Mock private EstadoUbicacionRepository  estadoUbicacionRepository;
    @Mock private TarifaRepository tarifaRepository;
    @Mock private UnidadTarifaRepository unidadTarifaRepository;

    @InjectMocks
    private IngresoVehiculoServiceImpl service;

    // ── Catálogo ──────────────────────────────────────────────────────────────
    private TipoVehiculo tipoCarro;     // id=1
    private TipoVehiculo tipoMoto;      // id=2
    private Ubicacion    ubicacionCarro; // A01, nativa CARRO
    private Ubicacion    ubicacionMoto;  // M01, nativa MOTO
    private EstadoIngreso estadoIngresado;
    private EstadoIngreso estadoEntregado;
    private Usuario       usuario;

    @BeforeEach
    void setUp() {
        tipoCarro = new TipoVehiculo();
        tipoCarro.setId(1L);
        tipoCarro.setNombre("CARRO");

        tipoMoto = new TipoVehiculo();
        tipoMoto.setId(2L);
        tipoMoto.setNombre("MOTO");

        EstadoUbicacion estadoDisponible = new EstadoUbicacion();
        estadoDisponible.setId(1L);
        estadoDisponible.setNombre("DISPONIBLE");

        EstadoUbicacion estadoOcupado = new EstadoUbicacion();
        estadoOcupado.setId(2L);
        estadoOcupado.setNombre("OCUPADO");

        ubicacionCarro = new Ubicacion();
        ubicacionCarro.setId(10L);
        ubicacionCarro.setNombre("A01");
        ubicacionCarro.setTipoVehiculoNativo(tipoCarro);
        ubicacionCarro.setCapacidad(1);
        ubicacionCarro.setEstadoUbicacion(estadoDisponible);

        ubicacionMoto = new Ubicacion();
        ubicacionMoto.setId(60L);
        ubicacionMoto.setNombre("M01");
        ubicacionMoto.setTipoVehiculoNativo(tipoMoto);
        ubicacionMoto.setCapacidad(1);
        ubicacionMoto.setEstadoUbicacion(estadoDisponible);

        estadoIngresado = new EstadoIngreso();
        estadoIngresado.setId(1L);
        estadoIngresado.setNombre("INGRESADO");

        estadoEntregado = new EstadoIngreso();
        estadoEntregado.setId(2L);
        estadoEntregado.setNombre("ENTREGADO");

        usuario = new Usuario();
        usuario.setId(99L);
        usuario.setNombreUsuario("admin");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Mock de save que asigna id=123 y fechaCreacion al ingreso persistido. */
    private void mockSaveAsignaId() {
        when(ingresoVehiculoRepository.save(any(IngresoVehiculo.class))).thenAnswer(inv -> {
            IngresoVehiculo iv = inv.getArgument(0);
            iv.setId(123L);
            iv.setFechaCreacion(OffsetDateTime.now());
            return iv;
        });
    }

    /**
     * Construye un IngresoVehiculo ya persistido para usarlo en pruebas de edición.
     * Simula el estado actual de la BD antes de la modificación.
     */
    private IngresoVehiculo buildIngresoExistente(Long id, String placa,
                                                  TipoVehiculo tipo, Ubicacion ubicacion, EstadoIngreso estado) {
        IngresoVehiculo iv = new IngresoVehiculo();
        iv.setId(id);
        iv.setPlaca(placa);
        iv.setTipoVehiculo(tipo);
        iv.setUbicacion(ubicacion);
        iv.setEstadoIngreso(estado);
        iv.setFechaHoraIngreso(OffsetDateTime.parse("2026-03-14T10:00:00-05:00"));
        iv.setFechaCreacion(OffsetDateTime.parse("2026-03-14T10:00:00-05:00"));
        iv.setUsuarioRegistro(usuario);
        return iv;
    }

    // =========================================================================
    // HU-006 — REGISTRAR INGRESO (casos 1-10, sin cambios)
    // =========================================================================

    @Test
    void shouldRegisterCarroInEspacioCarroSuccessfully() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest(
                "abc123", 1L, 10L, OffsetDateTime.parse("2026-03-13T14:00:00-05:00"));

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("ABC123")).thenReturn(false);
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipoCarro));
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionCarro));
        when(ingresoVehiculoRepository.contarActivosPorUbicacion(10L)).thenReturn(0);
        when(estadoIngresoRepository.findByNombre("INGRESADO")).thenReturn(Optional.of(estadoIngresado));
        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(usuario));
        
        EstadoUbicacion estadoOcupado = new EstadoUbicacion();
        estadoOcupado.setId(2L);
        estadoOcupado.setNombre("OCUPADO");
        when(estadoUbicacionRepository.findByNombre("OCUPADO")).thenReturn(Optional.of(estadoOcupado));
        
        mockSaveAsignaId();

        IngresoVehiculoResponse response = service.registrarIngreso(request, "admin");

        assertNotNull(response);
        assertEquals(123L,     response.idIngreso());
        assertEquals("ABC123", response.placa());
        assertEquals("CARRO",  response.tipoVehiculo());
        assertEquals("A01",    response.ubicacion());
        verify(ingresoVehiculoRepository).save(any(IngresoVehiculo.class));
    }

    @Test
    void shouldRegisterMotoInEspacioMotoSuccessfully() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("MTO001", 2L, 60L, null);

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("MTO001")).thenReturn(false);
        when(tipoVehiculoRepository.findById(2L)).thenReturn(Optional.of(tipoMoto));
        when(ubicacionRepository.findById(60L)).thenReturn(Optional.of(ubicacionMoto));
        when(ingresoVehiculoRepository.contarActivosPorUbicacion(60L)).thenReturn(0);
        when(estadoIngresoRepository.findByNombre("INGRESADO")).thenReturn(Optional.of(estadoIngresado));
        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(usuario));
        
        EstadoUbicacion estadoOcupado = new EstadoUbicacion();
        estadoOcupado.setId(2L);
        estadoOcupado.setNombre("OCUPADO");
        when(estadoUbicacionRepository.findByNombre("OCUPADO")).thenReturn(Optional.of(estadoOcupado));
        
        mockSaveAsignaId();

        IngresoVehiculoResponse response = service.registrarIngreso(request, "admin");

        assertNotNull(response);
        assertEquals("MTO001", response.placa());
        assertEquals("MOTO",   response.tipoVehiculo());
        assertEquals("M01",    response.ubicacion());
    }

    @Test
    void shouldRegisterMotoInEspacioCarroVacioSuccessfully() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("MTO002", 2L, 10L, null);

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("MTO002")).thenReturn(false);
        when(tipoVehiculoRepository.findById(2L)).thenReturn(Optional.of(tipoMoto));
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionCarro));
        when(ingresoVehiculoRepository.contarActivosPorUbicacion(10L)).thenReturn(0);
        when(estadoIngresoRepository.findByNombre("INGRESADO")).thenReturn(Optional.of(estadoIngresado));
        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(usuario));
        mockSaveAsignaId();

        IngresoVehiculoResponse response = service.registrarIngreso(request, "admin");

        assertNotNull(response);
        assertEquals("MTO002", response.placa());
    }

    @Test
    void shouldRegisterCuartaMotoInEspacioCarroSuccessfully() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("MTO004", 2L, 10L, null);

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("MTO004")).thenReturn(false);
        when(tipoVehiculoRepository.findById(2L)).thenReturn(Optional.of(tipoMoto));
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionCarro));
        when(ingresoVehiculoRepository.contarActivosPorUbicacion(10L)).thenReturn(3);
        when(ingresoVehiculoRepository.existeActivoConTipoVehiculo(10L, 1L)).thenReturn(false);
        when(estadoIngresoRepository.findByNombre("INGRESADO")).thenReturn(Optional.of(estadoIngresado));
        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(usuario));
        
        EstadoUbicacion estadoOcupado = new EstadoUbicacion();
        estadoOcupado.setId(2L);
        estadoOcupado.setNombre("OCUPADO");
        when(estadoUbicacionRepository.findByNombre("OCUPADO")).thenReturn(Optional.of(estadoOcupado));
        
        mockSaveAsignaId();

        IngresoVehiculoResponse response = service.registrarIngreso(request, "admin");

        assertNotNull(response);
        assertEquals("MTO004", response.placa());
    }

    @Test
    void shouldFailWhenPlacaAlreadyActive() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("abc123", 1L, 10L, null);
        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("ABC123")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarIngreso(request, "admin"));

        assertTrue(ex.getMessage().contains("ya tiene un ingreso activo"));
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenEspacioCarroOcupadoPorCarro() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("XYZ999", 1L, 10L, null);

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("XYZ999")).thenReturn(false);
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipoCarro));
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionCarro));
        when(ingresoVehiculoRepository.contarActivosPorUbicacion(10L)).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarIngreso(request, "admin"));

        assertTrue(ex.getMessage().contains("ya está ocupado"));
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenCarroIntentaEspacioMoto() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("CAR001", 1L, 60L, null);

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("CAR001")).thenReturn(false);
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipoCarro));
        when(ubicacionRepository.findById(60L)).thenReturn(Optional.of(ubicacionMoto));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarIngreso(request, "admin"));

        assertTrue(ex.getMessage().contains("exclusivo para motos"));
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenMotoIntentaEspacioCarroConCarroActivo() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("MTO005", 2L, 10L, null);

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("MTO005")).thenReturn(false);
        when(tipoVehiculoRepository.findById(2L)).thenReturn(Optional.of(tipoMoto));
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionCarro));
        when(ingresoVehiculoRepository.contarActivosPorUbicacion(10L)).thenReturn(1);
        when(ingresoVehiculoRepository.existeActivoConTipoVehiculo(10L, 1L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarIngreso(request, "admin"));

        assertTrue(ex.getMessage().contains("ocupado por un carro"));
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenEspacioCarroAlcanzoCapacidadMaximaMotos() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("MTO005", 2L, 10L, null);

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("MTO005")).thenReturn(false);
        when(tipoVehiculoRepository.findById(2L)).thenReturn(Optional.of(tipoMoto));
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionCarro));
        when(ingresoVehiculoRepository.contarActivosPorUbicacion(10L)).thenReturn(4);
        when(ingresoVehiculoRepository.existeActivoConTipoVehiculo(10L, 1L)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarIngreso(request, "admin"));

        assertTrue(ex.getMessage().contains("capacidad máxima"));
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenEspacioMotoYaOcupado() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("MTO006", 2L, 60L, null);

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("MTO006")).thenReturn(false);
        when(tipoVehiculoRepository.findById(2L)).thenReturn(Optional.of(tipoMoto));
        when(ubicacionRepository.findById(60L)).thenReturn(Optional.of(ubicacionMoto));
        when(ingresoVehiculoRepository.contarActivosPorUbicacion(60L)).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarIngreso(request, "admin"));

        assertTrue(ex.getMessage().contains("ya está ocupado"));
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    // =========================================================================
    // HU-019 — ELIMINAR INGRESO
    // =========================================================================

    @Test
    void shouldEliminarIngresoSuccessfully() {
        IngresoVehiculo existente = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);

        when(ingresoVehiculoRepository.findById(55L)).thenReturn(Optional.of(existente));

        service.eliminarIngreso(55L);

        verify(ingresoVehiculoRepository).delete(existente);
    }

    @Test
    void shouldFailEliminarWhenIngresoNotFound() {
        when(ingresoVehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.eliminarIngreso(99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(ingresoVehiculoRepository, never()).delete(any());
    }

    // =========================================================================
    // HU-020 — EDITAR INGRESO
    // =========================================================================

    // ── CASO 11: Admin edita placa exitosamente ───────────────────────────────
    @Test
    void shouldEditarPlacaAsAdminSuccessfully() {
        IngresoVehiculo existente = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);

        EditarIngresoRequest request = new EditarIngresoRequest(
                "XYZ999", null, null, null, null, null);

        when(ingresoVehiculoRepository.findById(55L)).thenReturn(Optional.of(existente));
        // Nueva placa distinta a la actual → validar que no esté activa
        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("XYZ999"))
                .thenReturn(false);
        when(ingresoVehiculoRepository.save(any(IngresoVehiculo.class))).thenReturn(existente);

        IngresoVehiculoResponse response = service.editarIngreso(55L, request, true);

        assertNotNull(response);
        // El save se llama con la nueva placa seteada
        verify(ingresoVehiculoRepository).save(argThat(iv -> "XYZ999".equals(iv.getPlaca())));
    }

    // ── CASO 12: Admin edita estado exitosamente ──────────────────────────────
    @Test
    void shouldEditarEstadoAsAdminSuccessfully() {
        IngresoVehiculo existente = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);

        EditarIngresoRequest request = new EditarIngresoRequest(
                null, null, null, 2L, null, null);

        when(ingresoVehiculoRepository.findById(55L)).thenReturn(Optional.of(existente));
        when(estadoIngresoRepository.findById(2L)).thenReturn(Optional.of(estadoEntregado));
        when(ingresoVehiculoRepository.save(any(IngresoVehiculo.class))).thenReturn(existente);

        IngresoVehiculoResponse response = service.editarIngreso(55L, request, true);

        assertNotNull(response);
        verify(estadoIngresoRepository).findById(2L);
        verify(ingresoVehiculoRepository).save(argThat(iv ->
                "ENTREGADO".equals(iv.getEstadoIngreso().getNombre())));
    }

    // ── CASO 13: Admin registra fecha de salida válida ────────────────────────
    @Test
    void shouldEditarFechaSalidaValidaAsAdminSuccessfully() {
        IngresoVehiculo existente = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);

        OffsetDateTime salida = OffsetDateTime.parse("2026-03-14T14:00:00-05:00"); // posterior al ingreso

        EditarIngresoRequest request = new EditarIngresoRequest(
                null, null, null, null, null, salida);

        when(ingresoVehiculoRepository.findById(55L)).thenReturn(Optional.of(existente));
        when(ingresoVehiculoRepository.save(any(IngresoVehiculo.class))).thenReturn(existente);

        IngresoVehiculoResponse response = service.editarIngreso(55L, request, true);

        assertNotNull(response);
        verify(ingresoVehiculoRepository).save(argThat(iv ->
                salida.equals(iv.getFechaHoraSalida())));
    }

    // ── CASO 14: Auxiliar edita solo placa y ubicación ────────────────────────
    @Test
    void shouldEditarPlacaYUbicacionAsAuxiliarSuccessfully() {
        // Ubicación destino: A02 (también nativa de carro, disponible)
        Ubicacion ubicacionA02 = new Ubicacion();
        ubicacionA02.setId(11L);
        ubicacionA02.setNombre("A02");
        ubicacionA02.setTipoVehiculoNativo(tipoCarro);
        ubicacionA02.setCapacidad(1);
        EstadoUbicacion disponible = new EstadoUbicacion();
        disponible.setId(1L); disponible.setNombre("DISPONIBLE");
        ubicacionA02.setEstadoUbicacion(disponible);

        IngresoVehiculo existente = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);

        // Auxiliar intenta enviar idEstadoIngreso también (debe ignorarse)
        EditarIngresoRequest request = new EditarIngresoRequest(
                "NEW001", null, 11L, 2L, null, null);

        when(ingresoVehiculoRepository.findById(55L)).thenReturn(Optional.of(existente));
        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("NEW001"))
                .thenReturn(false);
        when(ubicacionRepository.findById(11L)).thenReturn(Optional.of(ubicacionA02));
        // La nueva ubicación es distinta → valida compatibilidad (A02 acepta carros)
        when(ingresoVehiculoRepository.contarActivosPorUbicacion(11L)).thenReturn(0);
        when(ingresoVehiculoRepository.save(any(IngresoVehiculo.class))).thenReturn(existente);

        service.editarIngreso(55L, request, false); // false = AUXILIAR

        // Estado NO debe haber cambiado (campo de admin ignorado)
        verify(estadoIngresoRepository, never()).findById(any());
        // Placa y ubicación sí se aplican
        verify(ingresoVehiculoRepository).save(argThat(iv ->
                "NEW001".equals(iv.getPlaca()) && iv.getUbicacion().getId().equals(11L)));
    }

    // ── CASO 15: Falla — ingreso no encontrado ────────────────────────────────
    @Test
    void shouldFailEditarWhenIngresoNotFound() {
        EditarIngresoRequest request = new EditarIngresoRequest(
                "XYZ", null, null, null, null, null);

        when(ingresoVehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editarIngreso(99L, request, true));

        assertTrue(ex.getMessage().contains("99"));
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    // ── CASO 16: Falla — nueva placa ya tiene ingreso activo ─────────────────
    @Test
    void shouldFailEditarWhenNuevaPLacaYaActiva() {
        IngresoVehiculo existente = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);

        EditarIngresoRequest request = new EditarIngresoRequest(
                "OCUPADA", null, null, null, null, null);

        when(ingresoVehiculoRepository.findById(55L)).thenReturn(Optional.of(existente));
        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("OCUPADA"))
                .thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editarIngreso(55L, request, true));

        assertTrue(ex.getMessage().contains("ya tiene un ingreso activo"));
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    // ── CASO 17: Falla — fecha de salida anterior a la de ingreso ────────────
    @Test
    void shouldFailEditarWhenFechaSalidaAnteriorAIngreso() {
        IngresoVehiculo existente = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);
        // fechaHoraIngreso del existente = 2026-03-14T10:00:00-05:00

        OffsetDateTime salidaAnterior = OffsetDateTime.parse("2026-03-14T08:00:00-05:00"); // ANTES

        EditarIngresoRequest request = new EditarIngresoRequest(
                null, null, null, null, null, salidaAnterior);

        when(ingresoVehiculoRepository.findById(55L)).thenReturn(Optional.of(existente));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editarIngreso(55L, request, true));

        assertTrue(ex.getMessage().contains("no puede ser anterior"));
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    // ── CASO 18: Falla — nueva ubicación incompatible con tipo de vehículo ───
    @Test
    void shouldFailEditarWhenNuevaUbicacionIncompatibleConTipo() {
        // Ingreso original: carro en A01
        IngresoVehiculo existente = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);

        // Auxiliar intenta mover el carro a un espacio de moto
        EditarIngresoRequest request = new EditarIngresoRequest(
                null, null, 60L, null, null, null); // M01 = nativa MOTO

        when(ingresoVehiculoRepository.findById(55L)).thenReturn(Optional.of(existente));
        when(ubicacionRepository.findById(60L)).thenReturn(Optional.of(ubicacionMoto));
        // La nueva ubicación (M01) es distinta a la actual (A01) → valida compatibilidad
        // M01 es MOTO y el vehículo es CARRO → incompatible

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editarIngreso(55L, request, false));

        assertTrue(ex.getMessage().contains("exclusivo para motos"));
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    @Test
    void shouldRegistrarSalidaCalcularCobroYLiberarUbicacionSuccessfully() {
        IngresoVehiculo ingreso = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);
        ingreso.setFechaHoraSalida(null);
        ingreso.setValorCobrado(null);

        RegistrarSalidaRequest request = new RegistrarSalidaRequest(
                OffsetDateTime.parse("2026-03-14T11:20:00-05:00")
        );

        UnidadTarifa unidadHora = new UnidadTarifa();
        unidadHora.setId(1L);
        unidadHora.setNombre("HORA");

        Tarifa tarifa = new Tarifa();
        tarifa.setId(10L);
        tarifa.setValor(new BigDecimal("5000.00"));
        tarifa.setTipoVehiculo(tipoCarro);
        tarifa.setUnidadTarifa(unidadHora);
        tarifa.setActiva(true);

        EstadoUbicacion estadoDisponible = new EstadoUbicacion();
        estadoDisponible.setId(1L);
        estadoDisponible.setNombre("DISPONIBLE");

        when(ingresoVehiculoRepository.findByIdFetchAll(55L))
                .thenReturn(Optional.of(ingreso));
        when(unidadTarifaRepository.findByNombre("HORA"))
                .thenReturn(Optional.of(unidadHora));
        when(tarifaRepository.findByTipoVehiculo_IdAndUnidadTarifa_IdAndActivaTrue(1L, 1L))
                .thenReturn(Optional.of(tarifa));
        when(estadoIngresoRepository.findByNombre("ENTREGADO"))
                .thenReturn(Optional.of(estadoEntregado));
        when(usuarioRepository.findByNombreUsuario("admin"))
                .thenReturn(Optional.of(usuario));
        when(estadoUbicacionRepository.findByNombre("DISPONIBLE"))
                .thenReturn(Optional.of(estadoDisponible));
        when(ingresoVehiculoRepository.contarActivosPorUbicacion(10L))
                .thenReturn(1);

        when(ubicacionRepository.save(any(Ubicacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(ingresoVehiculoRepository.save(any(IngresoVehiculo.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SalidaResponse response = service.registrarSalida(55L, request, "admin");

        assertNotNull(response);
        assertEquals(55L, response.idIngreso());
        assertEquals("ABC123", response.placa());
        assertEquals("CARRO", response.tipoVehiculo());
        assertEquals("A01", response.ubicacion());
        assertEquals(2, response.horasCobradas());
        assertEquals(0, new BigDecimal("5000.00").compareTo(response.tarifaPorHora()));
        assertEquals(0, new BigDecimal("10000.00").compareTo(response.valorCobrado()));
        assertEquals("admin", response.usuarioEntrega());

        verify(ubicacionRepository).save(argThat(ubicacion ->
                "DISPONIBLE".equals(ubicacion.getEstadoUbicacion().getNombre())
        ));

        verify(ingresoVehiculoRepository).save(argThat(iv ->
                request.fechaHoraSalida().equals(iv.getFechaHoraSalida())
                        && 0 == new BigDecimal("10000.00").compareTo(iv.getValorCobrado())
                        && "ENTREGADO".equals(iv.getEstadoIngreso().getNombre())
                        && "admin".equals(iv.getUsuarioEntrega().getNombreUsuario())
        ));
    }

    @Test
    void shouldFailRegistrarSalidaWhenIngresoNotFound() {
        RegistrarSalidaRequest request = new RegistrarSalidaRequest(
                OffsetDateTime.parse("2026-03-14T11:20:00-05:00")
        );

        when(ingresoVehiculoRepository.findByIdFetchAll(99L))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarSalida(99L, request, "admin"));

        assertTrue(ex.getMessage().contains("99"));
        verify(unidadTarifaRepository, never()).findByNombre(anyString());
        verify(tarifaRepository, never())
                .findByTipoVehiculo_IdAndUnidadTarifa_IdAndActivaTrue(anyLong(), anyLong());
        verify(ingresoVehiculoRepository, never()).save(any(IngresoVehiculo.class));
        verify(ubicacionRepository, never()).save(any(Ubicacion.class));
    }

    @Test
    void shouldFailRegistrarSalidaWhenIngresoYaTieneSalida() {
        IngresoVehiculo ingreso = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);
        ingreso.setFechaHoraSalida(OffsetDateTime.parse("2026-03-14T12:00:00-05:00"));

        RegistrarSalidaRequest request = new RegistrarSalidaRequest(
                OffsetDateTime.parse("2026-03-14T13:00:00-05:00")
        );

        when(ingresoVehiculoRepository.findByIdFetchAll(55L))
                .thenReturn(Optional.of(ingreso));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarSalida(55L, request, "admin"));

        assertTrue(ex.getMessage().contains("ya tiene registrada una salida"));
        verify(ingresoVehiculoRepository, never()).save(any(IngresoVehiculo.class));
        verify(ubicacionRepository, never()).save(any(Ubicacion.class));
    }

    @Test
    void shouldFailRegistrarSalidaWhenFechaSalidaEsAnteriorAlIngreso() {
        IngresoVehiculo ingreso = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);
        ingreso.setFechaHoraSalida(null);

        RegistrarSalidaRequest request = new RegistrarSalidaRequest(
                OffsetDateTime.parse("2026-03-14T08:00:00-05:00")
        );

        when(ingresoVehiculoRepository.findByIdFetchAll(55L))
                .thenReturn(Optional.of(ingreso));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarSalida(55L, request, "admin"));

        assertTrue(ex.getMessage().contains("no puede ser anterior"));
        verify(unidadTarifaRepository, never()).findByNombre(anyString());
        verify(tarifaRepository, never())
                .findByTipoVehiculo_IdAndUnidadTarifa_IdAndActivaTrue(anyLong(), anyLong());
        verify(ingresoVehiculoRepository, never()).save(any(IngresoVehiculo.class));
    }

    @Test
    void shouldFailRegistrarSalidaWhenUnidadTarifaHoraNoExiste() {
        IngresoVehiculo ingreso = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);
        ingreso.setFechaHoraSalida(null);

        RegistrarSalidaRequest request = new RegistrarSalidaRequest(
                OffsetDateTime.parse("2026-03-14T11:20:00-05:00")
        );

        when(ingresoVehiculoRepository.findByIdFetchAll(55L))
                .thenReturn(Optional.of(ingreso));
        when(unidadTarifaRepository.findByNombre("HORA"))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarSalida(55L, request, "admin"));

        assertTrue(ex.getMessage().contains("unidad de tarifa HORA"));
        verify(tarifaRepository, never())
                .findByTipoVehiculo_IdAndUnidadTarifa_IdAndActivaTrue(anyLong(), anyLong());
        verify(ingresoVehiculoRepository, never()).save(any(IngresoVehiculo.class));
        verify(ubicacionRepository, never()).save(any(Ubicacion.class));
    }

    @Test
    void shouldFailRegistrarSalidaWhenTarifaActivaNoExiste() {
        IngresoVehiculo ingreso = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);
        ingreso.setFechaHoraSalida(null);

        RegistrarSalidaRequest request = new RegistrarSalidaRequest(
                OffsetDateTime.parse("2026-03-14T11:20:00-05:00")
        );

        UnidadTarifa unidadHora = new UnidadTarifa();
        unidadHora.setId(1L);
        unidadHora.setNombre("HORA");

        when(ingresoVehiculoRepository.findByIdFetchAll(55L))
                .thenReturn(Optional.of(ingreso));
        when(unidadTarifaRepository.findByNombre("HORA"))
                .thenReturn(Optional.of(unidadHora));
        when(tarifaRepository.findByTipoVehiculo_IdAndUnidadTarifa_IdAndActivaTrue(1L, 1L))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.registrarSalida(55L, request, "admin"));

        assertTrue(ex.getMessage().contains("No existe tarifa activa"));
        verify(ingresoVehiculoRepository, never()).save(any(IngresoVehiculo.class));
        verify(ubicacionRepository, never()).save(any(Ubicacion.class));
    }

    @Test
    void shouldListarIngresosConFiltrosSuccessfully() {
        IngresoVehiculo ingreso1 = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);

        IngresoVehiculo ingreso2 = buildIngresoExistente(
                56L, "ABC999", tipoCarro, ubicacionCarro, estadoIngresado);

        PageImpl<IngresoVehiculo> pageResult = new PageImpl<>(
                List.of(ingreso1, ingreso2),
                PageRequest.of(0, 2),
                2
        );

        when(ingresoVehiculoRepository.listarConFiltros(
                "ABC", "INGRESADO", null, null, PageRequest.of(0, 2)))
                .thenReturn(pageResult);

        IngresoVehiculoPageResponse response =
                service.listarIngresos("  ABC ", " INGRESADO ", null, 0, 2);

        assertNotNull(response);
        assertEquals(2, response.content().size());
        assertEquals(0, response.page());
        assertEquals(2, response.size());
        assertEquals(2L, response.totalElements());
        assertEquals(1, response.totalPages());

        assertEquals("ABC123", response.content().get(0).placa());
        assertEquals("CARRO", response.content().get(0).tipoVehiculo());
        assertEquals("A01", response.content().get(0).ubicacion());
        assertEquals("INGRESADO", response.content().get(0).estadoIngreso());

        verify(ingresoVehiculoRepository)
                .listarConFiltros("ABC", "INGRESADO", null, null, PageRequest.of(0, 2));
    }

    @Test
    void shouldListarIngresosEnviarFiltrosNullWhenBlank() {
        PageImpl<IngresoVehiculo> pageResult = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(ingresoVehiculoRepository.listarConFiltros(
                null, null, null, null, PageRequest.of(0, 10)))
                .thenReturn(pageResult);

        IngresoVehiculoPageResponse response =
                service.listarIngresos("   ", "   ", "   ", 0, 10);

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(0L, response.totalElements());
        assertEquals(0, response.totalPages());

        verify(ingresoVehiculoRepository)
                .listarConFiltros(null, null, null, null, PageRequest.of(0, 10));
    }

    @Test
    void shouldListarIngresosRetornarPaginaVaciaConMetadatosCorrectos() {
        PageImpl<IngresoVehiculo> pageResult = new PageImpl<>(
                List.of(),
                PageRequest.of(1, 5),
                0
        );

        when(ingresoVehiculoRepository.listarConFiltros(
                "ZZZ", "ENTREGADO", null, null, PageRequest.of(1, 5)))
                .thenReturn(pageResult);

        IngresoVehiculoPageResponse response =
                service.listarIngresos("ZZZ", "ENTREGADO", null, 1, 5);

        assertNotNull(response);
        assertTrue(response.content().isEmpty());
        assertEquals(1, response.page());
        assertEquals(5, response.size());
        assertEquals(0L, response.totalElements());
        assertEquals(0, response.totalPages());

        verify(ingresoVehiculoRepository)
                .listarConFiltros("ZZZ", "ENTREGADO", null, null, PageRequest.of(1, 5));
    }

    @Test
    void obtenerPorId_deberiaRetornarIngresoVehiculoResponse_cuandoExiste() {
        // Arrange
        Long idIngreso = 1L;
        IngresoVehiculo ingreso = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);

        when(ingresoVehiculoRepository.findByIdFetchAll(idIngreso))
                .thenReturn(Optional.of(ingreso));

        // Act
        IngresoVehiculoResponse response = service.obtenerPorId(idIngreso);

        // Assert
        assertNotNull(response);
        verify(ingresoVehiculoRepository).findByIdFetchAll(idIngreso);
    }

    @Test
    void obtenerPorId_deberiaLanzarBusinessException_cuandoNoExiste() {
        // Arrange
        Long idIngreso = 99L;

        when(ingresoVehiculoRepository.findByIdFetchAll(idIngreso))
                .thenReturn(Optional.empty());

        // Act
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.obtenerPorId(idIngreso)
        );

        // Assert
        assertEquals("No existe un registro de ingreso con id " + idIngreso, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals(ErrorCode.INGRESO_NO_ENCONTRADO, ex.getCode());

        verify(ingresoVehiculoRepository).findByIdFetchAll(idIngreso);
    }

    @Test
    void buscarActivoPorPlaca_deberiaRetornarIngresoVehiculoResponse_cuandoExiste() {
        // Arrange
        String placaEntrada = "abc 123";
        String placaNormalizadaEsperada = "ABC123";
        IngresoVehiculo ingreso = buildIngresoExistente(
                55L, "ABC123", tipoCarro, ubicacionCarro, estadoIngresado);

        when(ingresoVehiculoRepository.findActivoByPlaca(placaNormalizadaEsperada))
                .thenReturn(List.of(ingreso));

        // Act
        IngresoVehiculoResponse response = service.buscarActivoPorPlaca(placaEntrada);

        // Assert
        assertNotNull(response);
        verify(ingresoVehiculoRepository).findActivoByPlaca(placaNormalizadaEsperada);
    }

    @Test
    void buscarActivoPorPlaca_deberiaLanzarBusinessException_cuandoNoExisteIngresoActivo() {
        // Arrange
        String placaEntrada = "abc 123";
        String placaNormalizadaEsperada = "ABC123";

        when(ingresoVehiculoRepository.findActivoByPlaca(placaNormalizadaEsperada))
                .thenReturn(List.of());

        // Act
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.buscarActivoPorPlaca(placaEntrada)
        );

        // Assert
        assertEquals(
                "No se encontró un ingreso activo para la placa " + placaNormalizadaEsperada,
                ex.getMessage()
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals(ErrorCode.INGRESO_NO_ENCONTRADO, ex.getCode());

        verify(ingresoVehiculoRepository).findActivoByPlaca(placaNormalizadaEsperada);
    }

}