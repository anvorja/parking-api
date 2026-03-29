package com.coding.parkingmanagementservice.tarifa.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import com.coding.parkingmanagementservice.tarifa.dto.CrearTarifaRequest;
import com.coding.parkingmanagementservice.tarifa.dto.EditarTarifaRequest;
import com.coding.parkingmanagementservice.tarifa.dto.TarifaResponse;
import com.coding.parkingmanagementservice.tarifa.service.TarifaService;
import com.coding.parkingmanagementservice.usuario.dto.MensajeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TarifaControllerTest {

    @Mock
    private TarifaService tarifaService;

    @InjectMocks
    private TarifaController tarifaController;

    @Test
    void listarActivas_deberiaRetornarListaDeTarifas() {
        TarifaResponse tarifa1 = mock(TarifaResponse.class);
        TarifaResponse tarifa2 = mock(TarifaResponse.class);
        List<TarifaResponse> expected = List.of(tarifa1, tarifa2);

        when(tarifaService.listarActivas()).thenReturn(expected);

        List<TarifaResponse> result = tarifaController.listarActivas();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(expected, result);

        verify(tarifaService).listarActivas();
    }

    @Test
    void crear_deberiaDelegarAlServicioYRetornarTarifaCreada() {
        CrearTarifaRequest request = mock(CrearTarifaRequest.class);
        TarifaResponse expected = mock(TarifaResponse.class);

        when(tarifaService.crear(request)).thenReturn(expected);

        TarifaResponse result = tarifaController.crear(request);

        assertNotNull(result);
        assertSame(expected, result);

        verify(tarifaService).crear(request);
    }

    @Test
    void editar_deberiaDelegarAlServicioYRetornarTarifaEditada() {
        Long id = 2L;
        EditarTarifaRequest request = mock(EditarTarifaRequest.class);
        TarifaResponse expected = mock(TarifaResponse.class);

        when(tarifaService.editar(id, request)).thenReturn(expected);

        TarifaResponse result = tarifaController.editar(id, request);

        assertNotNull(result);
        assertSame(expected, result);

        verify(tarifaService).editar(id, request);
    }

    @Test
    void desactivar_deberiaDelegarAlServicioYRetornarMensajeOk() {
        Long id = 2L;

        ResponseEntity<MensajeResponse> response = tarifaController.desactivar(id);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Tarifa desactivada correctamente", response.getBody().mensaje());

        verify(tarifaService).desactivar(id);
    }
}
