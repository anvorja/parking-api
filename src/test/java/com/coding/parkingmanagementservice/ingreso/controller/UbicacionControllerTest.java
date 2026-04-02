package com.coding.parkingmanagementservice.ingreso.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import com.coding.parkingmanagementservice.ingreso.dto.CrearUbicacionRequest;
import com.coding.parkingmanagementservice.ingreso.dto.EditarUbicacionRequest;
import com.coding.parkingmanagementservice.ingreso.dto.UbicacionResponse;
import com.coding.parkingmanagementservice.ingreso.service.UbicacionService;
import com.coding.parkingmanagementservice.usuario.dto.MensajeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UbicacionControllerTest {

    @Mock
    private UbicacionService ubicacionService;

    @InjectMocks
    private UbicacionController ubicacionController;

    @Test
    void listar_deberiaRetornarTodasLasUbicacionesSiIncluyeInactivas() {
        UbicacionResponse ubicacion1 = mock(UbicacionResponse.class);
        UbicacionResponse ubicacion2 = mock(UbicacionResponse.class);
        List<UbicacionResponse> expected = List.of(ubicacion1, ubicacion2);

        when(ubicacionService.listarTodas()).thenReturn(expected);

        List<UbicacionResponse> result = ubicacionController.listar(true);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(expected, result);

        verify(ubicacionService).listarTodas();
    }

    @Test
    void crear_deberiaDelegarAlServicioYRetornarUbicacionCreada() {
        CrearUbicacionRequest request = mock(CrearUbicacionRequest.class);
        UbicacionResponse expected = mock(UbicacionResponse.class);

        when(ubicacionService.crear(request)).thenReturn(expected);

        UbicacionResponse result = ubicacionController.crear(request);

        assertNotNull(result);
        assertSame(expected, result);

        verify(ubicacionService).crear(request);
    }

    @Test
    void editar_deberiaDelegarAlServicioYRetornarUbicacionEditada() {
        Long id = 1L;
        EditarUbicacionRequest request = mock(EditarUbicacionRequest.class);
        UbicacionResponse expected = mock(UbicacionResponse.class);

        when(ubicacionService.editar(id, request)).thenReturn(expected);

        UbicacionResponse result = ubicacionController.editar(id, request);

        assertNotNull(result);
        assertSame(expected, result);

        verify(ubicacionService).editar(id, request);
    }

    @Test
    void desactivar_deberiaDelegarAlServicioYRetornarMensajeOk() {
        Long id = 1L;

        ResponseEntity<MensajeResponse> response = ubicacionController.desactivar(id);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ubicación desactivada correctamente", response.getBody().mensaje());

        verify(ubicacionService).desactivar(id);
    }
}
