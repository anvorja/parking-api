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
import com.coding.parkingmanagementservice.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UbicacionServiceImplTest {

    @Mock
    private UbicacionRepository ubicacionRepository;
    @Mock
    private TipoVehiculoRepository tipoVehiculoRepository;
    @Mock
    private EstadoUbicacionRepository estadoUbicacionRepository;

    @InjectMocks
    private UbicacionServiceImpl service;

    private TipoVehiculo tipoCarro;
    private TipoVehiculo tipoMoto;
    private EstadoUbicacion estadoDisponible;
    private EstadoUbicacion estadoInactivo;
    private Ubicacion ubicacionActiva;
    private Ubicacion ubicacionInactiva;

    @BeforeEach
    void setUp() {
        tipoCarro = new TipoVehiculo();
        tipoCarro.setId(1L);
        tipoCarro.setNombre("CARRO");

        tipoMoto = new TipoVehiculo();
        tipoMoto.setId(2L);
        tipoMoto.setNombre("MOTO");

        estadoDisponible = new EstadoUbicacion();
        estadoDisponible.setId(1L);
        estadoDisponible.setNombre("DISPONIBLE");

        estadoInactivo = new EstadoUbicacion();
        estadoInactivo.setId(2L);
        estadoInactivo.setNombre("INACTIVO");

        ubicacionActiva = new Ubicacion();
        ubicacionActiva.setId(10L);
        ubicacionActiva.setNombre("A01");
        ubicacionActiva.setTipoVehiculoNativo(tipoCarro);
        ubicacionActiva.setCapacidad(1);
        ubicacionActiva.setEstadoUbicacion(estadoDisponible);

        ubicacionInactiva = new Ubicacion();
        ubicacionInactiva.setId(20L);
        ubicacionInactiva.setNombre("M01");
        ubicacionInactiva.setTipoVehiculoNativo(tipoMoto);
        ubicacionInactiva.setCapacidad(1);
        ubicacionInactiva.setEstadoUbicacion(estadoInactivo);
    }

    private void mockSaveUbicacion() {
        when(ubicacionRepository.save(any(Ubicacion.class))).thenAnswer(inv -> {
            Ubicacion u = inv.getArgument(0);
            if (u.getId() == null) {
                u.setId(100L);
            }
            return u;
        });
    }

    @Test
    void shouldCrearUbicacionSuccessfully() {
        CrearUbicacionRequest request = new CrearUbicacionRequest("A01", 1L, 4);

        when(ubicacionRepository.existeNombre("A01", null)).thenReturn(false);
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipoCarro));
        when(estadoUbicacionRepository.findByNombre("DISPONIBLE"))
                .thenReturn(Optional.of(estadoDisponible));
        mockSaveUbicacion();

        UbicacionResponse response = service.crear(request);

        assertNotNull(response);

        verify(ubicacionRepository).save(argThat(ubicacion ->
                "A01".equals(ubicacion.getNombre())
                        && ubicacion.getTipoVehiculoNativo().getId().equals(1L)
                        && ubicacion.getCapacidad().equals(4)
                        && "DISPONIBLE".equals(ubicacion.getEstadoUbicacion().getNombre())
        ));
    }

    @Test
    void shouldFailCrearWhenNombreYaExiste() {
        CrearUbicacionRequest request = new CrearUbicacionRequest("A01", 1L, 4);

        when(ubicacionRepository.existeNombre("A01", null)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.crear(request));

        assertTrue(ex.getMessage().contains("Ya existe una ubicación"));
        verify(tipoVehiculoRepository, never()).findById(any());
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldFailCrearWhenTipoVehiculoNoExiste() {
        CrearUbicacionRequest request = new CrearUbicacionRequest("A01", 99L, 4);

        when(ubicacionRepository.existeNombre("A01", null)).thenReturn(false);
        when(tipoVehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.crear(request));

        assertTrue(ex.getMessage().contains("Tipo de vehículo no encontrado"));
        verify(estadoUbicacionRepository, never()).findByNombre(any());
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldFailCrearWhenEstadoDisponibleNoExiste() {
        CrearUbicacionRequest request = new CrearUbicacionRequest("A01", 1L, 4);

        when(ubicacionRepository.existeNombre("A01", null)).thenReturn(false);
        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipoCarro));
        when(estadoUbicacionRepository.findByNombre("DISPONIBLE"))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.crear(request));

        assertTrue(ex.getMessage().contains("No existe el estado de ubicación DISPONIBLE"));
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldEditarNombreYCapacidadSuccessfully() {
        EditarUbicacionRequest request = new EditarUbicacionRequest("A02", null, 3);

        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionActiva));
        when(ubicacionRepository.existeNombre("A02", 10L)).thenReturn(false);
        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(ubicacionActiva);

        UbicacionResponse response = service.editar(10L, request);

        assertNotNull(response);

        verify(ubicacionRepository).save(argThat(ubicacion ->
                "A02".equals(ubicacion.getNombre()) &&
                        ubicacion.getCapacidad().equals(3)
        ));
    }

    @Test
    void shouldFailEditarWhenUbicacionNotFound() {
        EditarUbicacionRequest request = new EditarUbicacionRequest("A02", null, 3);

        when(ubicacionRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editar(99L, request));

        assertTrue(ex.getMessage().contains("99"));
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldFailEditarWhenUbicacionEstaInactiva() {
        EditarUbicacionRequest request = new EditarUbicacionRequest("M02", null, 2);

        when(ubicacionRepository.findById(20L)).thenReturn(Optional.of(ubicacionInactiva));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editar(20L, request));

        assertTrue(ex.getMessage().contains("No se puede editar una ubicación inactiva"));
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldFailEditarWhenNuevoNombreYaExiste() {
        EditarUbicacionRequest request = new EditarUbicacionRequest("A02", null, null);

        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionActiva));
        when(ubicacionRepository.existeNombre("A02", 10L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editar(10L, request));

        assertTrue(ex.getMessage().contains("Ya existe una ubicación con el nombre"));
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldFailEditarWhenCambioTipoYTieneIngresosActivos() {
        EditarUbicacionRequest request = new EditarUbicacionRequest(null, 2L, null);

        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionActiva));
        when(ubicacionRepository.tieneIngresosActivos(10L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editar(10L, request));

        assertTrue(ex.getMessage().contains("vehículos activos"));
        verify(tipoVehiculoRepository, never()).findById(any());
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldEditarTipoVehiculoSuccessfullyWhenNoTieneIngresosActivos() {
        EditarUbicacionRequest request = new EditarUbicacionRequest(null, 2L, null);

        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionActiva));
        when(ubicacionRepository.tieneIngresosActivos(10L)).thenReturn(false);
        when(tipoVehiculoRepository.findById(2L)).thenReturn(Optional.of(tipoMoto));
        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(ubicacionActiva);

        UbicacionResponse response = service.editar(10L, request);

        assertNotNull(response);

        verify(ubicacionRepository).save(argThat(ubicacion ->
                ubicacion.getTipoVehiculoNativo().getId().equals(2L)
        ));
    }

    @Test
    void shouldFailEditarWhenNuevoTipoVehiculoNoExiste() {
        EditarUbicacionRequest request = new EditarUbicacionRequest(null, 99L, null);

        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionActiva));
        when(ubicacionRepository.tieneIngresosActivos(10L)).thenReturn(false);
        when(tipoVehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editar(10L, request));

        assertTrue(ex.getMessage().contains("Tipo de vehículo no encontrado"));
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldDesactivarUbicacionSuccessfully() {
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionActiva));
        when(ubicacionRepository.tieneIngresosActivos(10L)).thenReturn(false);
        when(estadoUbicacionRepository.findByNombre("INACTIVO"))
                .thenReturn(Optional.of(estadoInactivo));
        when(ubicacionRepository.save(any(Ubicacion.class))).thenReturn(ubicacionActiva);

        service.desactivar(10L);

        verify(ubicacionRepository).save(argThat(ubicacion ->
                "INACTIVO".equals(ubicacion.getEstadoUbicacion().getNombre())
        ));
    }

    @Test
    void shouldFailDesactivarWhenUbicacionNotFound() {
        when(ubicacionRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.desactivar(99L));

        assertTrue(ex.getMessage().contains("99"));
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldFailDesactivarWhenYaEstaInactiva() {
        when(ubicacionRepository.findById(20L)).thenReturn(Optional.of(ubicacionInactiva));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.desactivar(20L));

        assertTrue(ex.getMessage().contains("ya está inactiva"));
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldFailDesactivarWhenTieneIngresosActivos() {
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionActiva));
        when(ubicacionRepository.tieneIngresosActivos(10L)).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.desactivar(10L));

        assertTrue(ex.getMessage().contains("vehículos activos"));
        verify(estadoUbicacionRepository, never()).findByNombre(any());
        verify(ubicacionRepository, never()).save(any());
    }

    @Test
    void shouldFailDesactivarWhenEstadoInactivoNoExiste() {
        when(ubicacionRepository.findById(10L)).thenReturn(Optional.of(ubicacionActiva));
        when(ubicacionRepository.tieneIngresosActivos(10L)).thenReturn(false);
        when(estadoUbicacionRepository.findByNombre("INACTIVO"))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.desactivar(10L));

        assertTrue(ex.getMessage().contains("No existe el estado de ubicación INACTIVO"));
        verify(ubicacionRepository, never()).save(any());
    }
}