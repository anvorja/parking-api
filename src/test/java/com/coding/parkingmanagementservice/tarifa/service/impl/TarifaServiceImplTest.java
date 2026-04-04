package com.coding.parkingmanagementservice.tarifa.service.impl;

import com.coding.parkingmanagementservice.ingreso.entities.TipoVehiculo;
import com.coding.parkingmanagementservice.ingreso.repository.TipoVehiculoRepository;
import com.coding.parkingmanagementservice.shared.exception.BusinessException;
import com.coding.parkingmanagementservice.tarifa.dto.CrearTarifaRequest;
import com.coding.parkingmanagementservice.tarifa.dto.EditarTarifaRequest;
import com.coding.parkingmanagementservice.tarifa.dto.TarifaResponse;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarifaServiceImplTest {

    @Mock
    private TarifaRepository tarifaRepository;
    @Mock
    private TipoVehiculoRepository tipoVehiculoRepository;
    @Mock
    private UnidadTarifaRepository unidadTarifaRepository;

    @InjectMocks
    private TarifaServiceImpl service;

    private TipoVehiculo tipoCarro;
    private TipoVehiculo tipoMoto;
    private UnidadTarifa unidadHora;
    private UnidadTarifa unidadDia;
    private Tarifa tarifaActiva;
    private Tarifa tarifaInactiva;

    @BeforeEach
    void setUp() {
        tipoCarro = new TipoVehiculo();
        tipoCarro.setId(1L);
        tipoCarro.setNombre("CARRO");

        tipoMoto = new TipoVehiculo();
        tipoMoto.setId(2L);
        tipoMoto.setNombre("MOTO");

        unidadHora = new UnidadTarifa();
        unidadHora.setId(1L);
        unidadHora.setNombre("HORA");

        unidadDia = new UnidadTarifa();
        unidadDia.setId(2L);
        unidadDia.setNombre("DIA");

        tarifaActiva = new Tarifa();
        tarifaActiva.setId(10L);
        tarifaActiva.setTipoVehiculo(tipoCarro);
        tarifaActiva.setUnidadTarifa(unidadHora);
        tarifaActiva.setValor(new BigDecimal("5000.00"));
        tarifaActiva.setActiva(true);
        tarifaActiva.setFechaCreacion(OffsetDateTime.parse("2026-03-27T10:00:00-05:00"));

        tarifaInactiva = new Tarifa();
        tarifaInactiva.setId(20L);
        tarifaInactiva.setTipoVehiculo(tipoMoto);
        tarifaInactiva.setUnidadTarifa(unidadDia);
        tarifaInactiva.setValor(new BigDecimal("15000.00"));
        tarifaInactiva.setActiva(false);
        tarifaInactiva.setFechaCreacion(OffsetDateTime.parse("2026-03-27T10:00:00-05:00"));
    }

    private void mockSaveTarifa() {
        when(tarifaRepository.save(any(Tarifa.class))).thenAnswer(inv -> {
            Tarifa t = inv.getArgument(0);
            if (t.getId() == null) {
                t.setId(100L);
            }
            if (t.getFechaCreacion() == null) {
                t.setFechaCreacion(OffsetDateTime.now());
            }
            return t;
        });
    }

    @Test
    void shouldListarTarifasActivasSuccessfully() {
        Tarifa otraTarifa = new Tarifa();
        otraTarifa.setId(11L);
        otraTarifa.setTipoVehiculo(tipoMoto);
        otraTarifa.setUnidadTarifa(unidadHora);
        otraTarifa.setValor(new BigDecimal("3000.00"));
        otraTarifa.setActiva(true);
        otraTarifa.setFechaCreacion(OffsetDateTime.parse("2026-03-27T11:00:00-05:00"));

        when(tarifaRepository.findByActivaTrue())
                .thenReturn(List.of(tarifaActiva, otraTarifa));

        List<TarifaResponse> response = service.listarActivas();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(10L, response.get(0).idTarifa());
        assertEquals("CARRO", response.get(0).tipoVehiculo());
        assertEquals("HORA", response.get(0).unidadTarifa());

        verify(tarifaRepository).findByActivaTrue();
    }

    @Test
    void shouldCrearTarifaSuccessfully() {
        CrearTarifaRequest request = new CrearTarifaRequest(
                1L, 1L, new BigDecimal("6000.00")
        );

        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipoCarro));
        when(unidadTarifaRepository.findById(1L)).thenReturn(Optional.of(unidadHora));
        doNothing().when(tarifaRepository).desactivarActivasPorTipoYUnidad(1L, 1L);
        mockSaveTarifa();

        TarifaResponse response = service.crear(request);

        assertNotNull(response);
        assertEquals("CARRO", response.tipoVehiculo());
        assertEquals("HORA", response.unidadTarifa());
        assertEquals(0, new BigDecimal("6000.00").compareTo(response.valor()));
        assertTrue(response.activa());

        verify(tarifaRepository).desactivarActivasPorTipoYUnidad(1L, 1L);
        verify(tarifaRepository).save(argThat(t ->
                t.getTipoVehiculo().getId().equals(1L)
                        && t.getUnidadTarifa().getId().equals(1L)
                        && 0 == new BigDecimal("6000.00").compareTo(t.getValor())
                        && t.isActiva()
        ));
    }

    @Test
    void shouldFailCrearWhenTipoVehiculoNoExiste() {
        CrearTarifaRequest request = new CrearTarifaRequest(
                99L, 1L, new BigDecimal("6000.00")
        );

        when(tipoVehiculoRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.crear(request));

        assertTrue(ex.getMessage().contains("Tipo de vehículo no encontrado"));
        verify(unidadTarifaRepository, never()).findById(any());
        verify(tarifaRepository, never()).desactivarActivasPorTipoYUnidad(anyLong(), anyLong());
        verify(tarifaRepository, never()).save(any());
    }

    @Test
    void shouldFailCrearWhenUnidadTarifaNoExiste() {
        CrearTarifaRequest request = new CrearTarifaRequest(
                1L, 99L, new BigDecimal("6000.00")
        );

        when(tipoVehiculoRepository.findById(1L)).thenReturn(Optional.of(tipoCarro));
        when(unidadTarifaRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.crear(request));

        assertTrue(ex.getMessage().contains("Unidad de tarifa no encontrada"));
        verify(tarifaRepository, never()).desactivarActivasPorTipoYUnidad(anyLong(), anyLong());
        verify(tarifaRepository, never()).save(any());
    }

    @Test
    void shouldEditarTarifaSuccessfully() {
        EditarTarifaRequest request = new EditarTarifaRequest(
                new BigDecimal("7500.00")
        );

        when(tarifaRepository.findById(10L)).thenReturn(Optional.of(tarifaActiva));
        when(tarifaRepository.save(any(Tarifa.class))).thenReturn(tarifaActiva);

        TarifaResponse response = service.editar(10L, request);

        assertNotNull(response);

        verify(tarifaRepository).save(argThat(t ->
                t.getId().equals(10L)
                        && 0 == new BigDecimal("7500.00").compareTo(t.getValor())
        ));
    }

    @Test
    void shouldFailEditarWhenTarifaNoExiste() {
        EditarTarifaRequest request = new EditarTarifaRequest(
                new BigDecimal("7500.00")
        );

        when(tarifaRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editar(99L, request));

        assertTrue(ex.getMessage().contains("No existe una tarifa con id 99"));
        verify(tarifaRepository, never()).save(any());
    }

    @Test
    void shouldFailEditarWhenTarifaEstaInactiva() {
        EditarTarifaRequest request = new EditarTarifaRequest(
                new BigDecimal("20000.00")
        );

        when(tarifaRepository.findById(20L)).thenReturn(Optional.of(tarifaInactiva));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.editar(20L, request));

        assertTrue(ex.getMessage().contains("No se puede editar una tarifa inactiva"));
        verify(tarifaRepository, never()).save(any());
    }

    @Test
    void shouldDesactivarTarifaSuccessfully() {
        when(tarifaRepository.findById(10L)).thenReturn(Optional.of(tarifaActiva));
        when(tarifaRepository.save(any(Tarifa.class))).thenReturn(tarifaActiva);

        service.desactivar(10L);

        verify(tarifaRepository).save(argThat(t ->
                t.getId().equals(10L) && !t.isActiva()
        ));
    }

    @Test
    void shouldFailDesactivarWhenTarifaNoExiste() {
        when(tarifaRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.desactivar(99L));

        assertTrue(ex.getMessage().contains("No existe una tarifa con id 99"));
        verify(tarifaRepository, never()).save(any());
    }
}