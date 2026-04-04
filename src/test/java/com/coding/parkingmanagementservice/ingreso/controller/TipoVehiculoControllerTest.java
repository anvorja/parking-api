package com.coding.parkingmanagementservice.ingreso.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import com.coding.parkingmanagementservice.ingreso.entities.TipoVehiculo;
import com.coding.parkingmanagementservice.ingreso.repository.TipoVehiculoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TipoVehiculoControllerTest {

    @Mock
    private TipoVehiculoRepository tipoVehiculoRepository;

    @InjectMocks
    private TipoVehiculoController tipoVehiculoController;

    @Test
    void listarTipos_deberiaRetornarListaMapeada() {
        TipoVehiculo tipo1 = mock(TipoVehiculo.class);
        TipoVehiculo tipo2 = mock(TipoVehiculo.class);

        when(tipo1.getId()).thenReturn(1L);
        when(tipo1.getNombre()).thenReturn("CARRO");

        when(tipo2.getId()).thenReturn(2L);
        when(tipo2.getNombre()).thenReturn("MOTO");

        when(tipoVehiculoRepository.findAll()).thenReturn(List.of(tipo1, tipo2));

        List<TipoVehiculoController.TipoVehiculoResponse> response = tipoVehiculoController.listarTipos();

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals(1L, response.get(0).id());
        assertEquals("CARRO", response.get(0).nombre());

        assertEquals(2L, response.get(1).id());
        assertEquals("MOTO", response.get(1).nombre());

        verify(tipoVehiculoRepository).findAll();
    }

    @Test
    void listarTipos_deberiaRetornarListaVacia_cuandoNoHayDatos() {
        when(tipoVehiculoRepository.findAll()).thenReturn(List.of());

        List<TipoVehiculoController.TipoVehiculoResponse> response = tipoVehiculoController.listarTipos();

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(tipoVehiculoRepository).findAll();
    }
}