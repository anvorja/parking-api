package com.coding.parkingmanagementservice.ingreso.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;

import com.coding.parkingmanagementservice.ingreso.dto.*;
import com.coding.parkingmanagementservice.ingreso.service.IngresoVehiculoService;
import com.coding.parkingmanagementservice.usuario.dto.MensajeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class IngresoVehiculoControllerTest {

    @Mock
    private IngresoVehiculoService ingresoVehiculoService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private IngresoVehiculoController ingresoVehiculoController;

    @Test
    void registrarIngreso_deberiaDelegarAlServicioConUsuarioAutenticado() {
        RegistrarIngresoRequest request = mock(RegistrarIngresoRequest.class);
        IngresoVehiculoResponse expected = mock(IngresoVehiculoResponse.class);

        when(authentication.getName()).thenReturn("operador1");
        when(ingresoVehiculoService.registrarIngreso(request, "operador1")).thenReturn(expected);

        IngresoVehiculoResponse result = ingresoVehiculoController.registrarIngreso(request, authentication);

        assertSame(expected, result);
        verify(authentication).getName();
        verify(ingresoVehiculoService).registrarIngreso(request, "operador1");
    }

    @Test
    void listarIngresos_deberiaDelegarConFiltrosYPaginacion() {
        String placa = "ABC123";
        String estado = "ACTIVO";
        int page = 0;
        int size = 20;

        IngresoVehiculoPageResponse expected = mock(IngresoVehiculoPageResponse.class);
        when(ingresoVehiculoService.listarIngresos(placa, estado, page, size)).thenReturn(expected);

        IngresoVehiculoPageResponse result =
                ingresoVehiculoController.listarIngresos(placa, estado, page, size);

        assertSame(expected, result);
        verify(ingresoVehiculoService).listarIngresos(placa, estado, page, size);
    }

    @Test
    void obtenerPorId_deberiaDelegarAlServicio() {
        Long id = 10L;
        IngresoVehiculoResponse expected = mock(IngresoVehiculoResponse.class);

        when(ingresoVehiculoService.obtenerPorId(id)).thenReturn(expected);

        IngresoVehiculoResponse result = ingresoVehiculoController.obtenerPorId(id);

        assertSame(expected, result);
        verify(ingresoVehiculoService).obtenerPorId(id);
    }

    @Test
    void buscarActivoPorPlaca_deberiaDelegarAlServicio() {
        String placa = "ABC123";
        IngresoVehiculoResponse expected = mock(IngresoVehiculoResponse.class);

        when(ingresoVehiculoService.buscarActivoPorPlaca(placa)).thenReturn(expected);

        IngresoVehiculoResponse result = ingresoVehiculoController.buscarActivoPorPlaca(placa);

        assertSame(expected, result);
        verify(ingresoVehiculoService).buscarActivoPorPlaca(placa);
    }

    @Test
    void registrarSalida_deberiaDelegarAlServicioConRequestYUsuarioAutenticado() {
        Long id = 25L;
        RegistrarSalidaRequest request = mock(RegistrarSalidaRequest.class);
        SalidaResponse expected = mock(SalidaResponse.class);

        when(authentication.getName()).thenReturn("operador2");
        when(ingresoVehiculoService.registrarSalida(id, request, "operador2")).thenReturn(expected);

        SalidaResponse result = ingresoVehiculoController.registrarSalida(id, request, authentication);

        assertSame(expected, result);
        verify(authentication).getName();
        verify(ingresoVehiculoService).registrarSalida(id, request, "operador2");
    }

    @Test
    void registrarSalida_deberiaCrearRequestPorDefectoCuandoRequestEsNull() {
        Long id = 30L;
        SalidaResponse expected = mock(SalidaResponse.class);

        when(authentication.getName()).thenReturn("operador3");
        when(ingresoVehiculoService.registrarSalida(eq(id), any(RegistrarSalidaRequest.class), eq("operador3")))
                .thenReturn(expected);

        SalidaResponse result = ingresoVehiculoController.registrarSalida(id, null, authentication);

        assertSame(expected, result);

        ArgumentCaptor<RegistrarSalidaRequest> captor =
                ArgumentCaptor.forClass(RegistrarSalidaRequest.class);

        verify(ingresoVehiculoService).registrarSalida(eq(id), captor.capture(), eq("operador3"));

        RegistrarSalidaRequest requestEnviado = captor.getValue();
        assertNotNull(requestEnviado);
    }

    @Test
    void eliminarIngreso_deberiaDelegarAlServicioYRetornarMensajeOk() {
        Long id = 40L;

        ResponseEntity<MensajeResponse> result = ingresoVehiculoController.eliminarIngreso(id);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(ingresoVehiculoService).eliminarIngreso(id);
    }

    @Test
    void editarIngreso_deberiaEnviarTrueCuandoUsuarioEsAdministrador() {
        Long id = 50L;
        EditarIngresoRequest request = mock(EditarIngresoRequest.class);
        IngresoVehiculoResponse expected = mock(IngresoVehiculoResponse.class);

        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"));

        when(authentication.getAuthorities()).thenReturn(authorities);
        when(ingresoVehiculoService.editarIngreso(id, request, true)).thenReturn(expected);

        IngresoVehiculoResponse result =
                ingresoVehiculoController.editarIngreso(id, request, authentication);

        assertSame(expected, result);
        verify(ingresoVehiculoService).editarIngreso(id, request, true);
    }

    @Test
    void editarIngreso_deberiaEnviarFalseCuandoUsuarioNoEsAdministrador() {
        Long id = 60L;
        EditarIngresoRequest request = mock(EditarIngresoRequest.class);
        IngresoVehiculoResponse expected = mock(IngresoVehiculoResponse.class);

        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_AUXILIAR"));

        when(authentication.getAuthorities()).thenReturn(authorities);
        when(ingresoVehiculoService.editarIngreso(id, request, false)).thenReturn(expected);

        IngresoVehiculoResponse result =
                ingresoVehiculoController.editarIngreso(id, request, authentication);

        assertSame(expected, result);
        verify(ingresoVehiculoService).editarIngreso(id, request, false);
    }
}
