package com.univalle.parkingmanagementservice.usuario.service.serviceImpl;
import com.univalle.parkingmanagementservice.auth.entities.Rol;
import com.univalle.parkingmanagementservice.auth.entities.Usuario;
import com.univalle.parkingmanagementservice.auth.repositories.UsuarioRepository;
import com.univalle.parkingmanagementservice.usuario.dto.UsuarioListItemResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void deberiaListarUsuariosOrdenadosPorNombreYMapearRespuesta() {
        Rol rolAdmin = new Rol();
        rolAdmin.setNombre("ADMIN");

        Rol rolUser = new Rol();
        rolUser.setNombre("USER");

        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setNombreCompleto("Carlos Pérez");
        usuario1.setNombreUsuario("cperez");
        usuario1.setRol(rolUser);

        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setNombreCompleto("ana López");
        usuario2.setNombreUsuario("alopez");
        usuario2.setRol(rolAdmin);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario1, usuario2));

        List<UsuarioListItemResponse> resultado = usuarioService.listarUsuarios();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        // Validar orden case-insensitive: "ana López" debe ir antes que "Carlos Pérez"
        assertEquals(2L, resultado.get(0).id());
        assertEquals("ana López", resultado.get(0).nombreCompleto());
        assertEquals("alopez", resultado.get(0).nombreUsuario());
        assertEquals("ADMIN", resultado.get(0).rol());

        assertEquals(1L, resultado.get(1).id());
        assertEquals("Carlos Pérez", resultado.get(1).nombreCompleto());
        assertEquals("cperez", resultado.get(1).nombreUsuario());
        assertEquals("USER", resultado.get(1).rol());

        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void deberiaRetornarListaVaciaCuandoNoHayUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        List<UsuarioListItemResponse> resultado = usuarioService.listarUsuarios();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(usuarioRepository, times(1)).findAll();
    }
}