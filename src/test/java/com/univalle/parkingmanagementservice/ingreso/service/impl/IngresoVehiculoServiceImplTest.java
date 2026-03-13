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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngresoVehiculoServiceImplTest {

    @Mock
    private IngresoVehiculoRepository ingresoVehiculoRepository;
    @Mock
    private TipoVehiculoRepository tipoVehiculoRepository;
    @Mock
    private UbicacionRepository ubicacionRepository;
    @Mock
    private EstadoIngresoRepository estadoIngresoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private IngresoVehiculoServiceImpl service;

    private TipoVehiculo tipoVehiculo;
    private Ubicacion ubicacion;
    private EstadoUbicacion estadoUbicacion;
    private EstadoIngreso estadoIngreso;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        tipoVehiculo = new TipoVehiculo();
        tipoVehiculo.setId(1L);
        tipoVehiculo.setNombre("CARRO");

        estadoUbicacion = new EstadoUbicacion();
        estadoUbicacion.setId(1L);
        estadoUbicacion.setNombre("DISPONIBLE");

        ubicacion = new Ubicacion();
        ubicacion.setId(10L);
        ubicacion.setNombre("A-01");
        ubicacion.setEstadoUbicacion(estadoUbicacion);

        estadoIngreso = new EstadoIngreso();
        estadoIngreso.setId(1L);
        estadoIngreso.setNombre("INGRESADO");

        usuario = new Usuario();
        usuario.setId(99L);
        usuario.setNombreUsuario("admin");
    }

    @Test
    void shouldRegisterIngresoSuccessfully() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest(
                "abc123",
                1L,
                10L,
                OffsetDateTime.parse("2026-03-13T14:00:00-05:00")
        );

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("ABC123")).thenReturn(false);
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipoVehiculo));
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacion));
        when(ingresoVehiculoRepository.existsByUbicacion_IdAndFechaHoraSalidaIsNull(10L)).thenReturn(false);
        when(estadoIngresoRepository.findByNombre("INGRESADO")).thenReturn(Optional.of(estadoIngreso));
        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(usuario));
        when(ingresoVehiculoRepository.save(any(IngresoVehiculo.class))).thenAnswer(invocation -> {
            IngresoVehiculo ingreso = invocation.getArgument(0);
            ingreso.setId(123L);
            ingreso.setFechaCreacion(OffsetDateTime.now());
            return ingreso;
        });

        IngresoVehiculoResponse response = service.registrarIngreso(request, "admin");

        assertNotNull(response);
        assertEquals(123L, response.idIngreso());
        assertEquals("ABC123", response.placa());
        assertEquals("CARRO", response.tipoVehiculo());
        assertEquals("A-01", response.ubicacion());

        verify(ingresoVehiculoRepository).save(any(IngresoVehiculo.class));
    }

    @Test
    void shouldFailWhenPlacaAlreadyActive() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("abc123", 1L, 10L, null);

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("ABC123")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarIngreso(request, "admin")
        );

        assertEquals("La placa ya se encuentra registrada con un ingreso activo", ex.getMessage());
        verify(ingresoVehiculoRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenUbicacionNotAvailable() {
        RegistrarIngresoRequest request = new RegistrarIngresoRequest("abc123", 1L, 10L, null);

        estadoUbicacion.setNombre("OCUPADA");

        when(ingresoVehiculoRepository.existsByPlacaIgnoreCaseAndFechaHoraSalidaIsNull("ABC123")).thenReturn(false);
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipoVehiculo));
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacion));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarIngreso(request, "admin")
        );

        assertEquals("La ubicación seleccionada no está disponible", ex.getMessage());
        verify(ingresoVehiculoRepository, never()).save(any());
    }
}