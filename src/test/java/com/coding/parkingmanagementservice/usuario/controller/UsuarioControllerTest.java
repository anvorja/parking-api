package com.coding.parkingmanagementservice.usuario.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import com.coding.parkingmanagementservice.usuario.dto.*;
import com.coding.parkingmanagementservice.usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    void listarUsuarios_deberiaRetornarListaEnResponseEntityOk() {
        UsuarioListItemResponse usuario1 = mock(UsuarioListItemResponse.class);
        UsuarioListItemResponse usuario2 = mock(UsuarioListItemResponse.class);
        List<UsuarioListItemResponse> usuarios = List.of(usuario1, usuario2);

        when(usuarioService.listarUsuarios()).thenReturn(usuarios);

        ResponseEntity<List<UsuarioListItemResponse>> response = usuarioController.listarUsuarios();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertSame(usuarios, response.getBody());

        verify(usuarioService).listarUsuarios();
    }

    @Test
    void crearUsuario_deberiaRetornarCrearUsuarioResponseConMensajeYUsuario() {
        CrearUsuarioRequest request = mock(CrearUsuarioRequest.class);
        UsuarioListItemResponse usuarioCreado = mock(UsuarioListItemResponse.class);

        when(usuarioService.crearUsuario(request)).thenReturn(usuarioCreado);

        CrearUsuarioResponse response = usuarioController.crearUsuario(request);

        assertNotNull(response);

        // Ajusta estos getters si tu DTO usa otros nombres
        assertEquals("Usuario creado correctamente", response.mensaje());
        assertSame(usuarioCreado, response.usuario());

        verify(usuarioService).crearUsuario(request);
    }

    @Test
    void editarUsuario_deberiaDelegarAlServicioYRetornarUsuarioEditado() {
        Long idUsuario = 3L;
        EditarUsuarioRequest request = mock(EditarUsuarioRequest.class);
        UsuarioListItemResponse usuarioEditado = mock(UsuarioListItemResponse.class);

        when(usuarioService.editarUsuario(idUsuario, request)).thenReturn(usuarioEditado);

        UsuarioListItemResponse response = usuarioController.editarUsuario(idUsuario, request);

        assertNotNull(response);
        assertSame(usuarioEditado, response);

        verify(usuarioService).editarUsuario(idUsuario, request);
    }

    @Test
    void eliminarUsuario_deberiaDelegarAlServicioYRetornarMensajeOk() {
        Long idUsuario = 3L;

        ResponseEntity<MensajeResponse> response = usuarioController.eliminarUsuario(idUsuario);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Ajusta este getter si tu DTO usa otro nombre
        assertEquals("Usuario eliminado correctamente", response.getBody().mensaje());

        verify(usuarioService).eliminarUsuario(idUsuario);
    }
}
