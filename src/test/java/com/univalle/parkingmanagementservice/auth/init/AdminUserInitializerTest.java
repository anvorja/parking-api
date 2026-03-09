package com.univalle.parkingmanagementservice.auth.init;

import com.univalle.parkingmanagementservice.auth.entities.EstadoUsuario;
import com.univalle.parkingmanagementservice.auth.entities.Rol;
import com.univalle.parkingmanagementservice.auth.entities.Usuario;
import com.univalle.parkingmanagementservice.auth.repositories.EstadoUsuarioRepository;
import com.univalle.parkingmanagementservice.auth.repositories.RolRepository;
import com.univalle.parkingmanagementservice.auth.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private EstadoUsuarioRepository estadoUsuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserInitializer adminUserInitializer;

    @Test
    void noDeberiaCrearAdminSiYaExistenUsuarios() {
        when(usuarioRepository.count()).thenReturn(3L);

        adminUserInitializer.run();

        verify(usuarioRepository).count();
        verifyNoInteractions(rolRepository);
        verifyNoInteractions(estadoUsuarioRepository);
        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaCrearAdminCuandoNoExistenUsuarios() {
        Rol rol = new Rol();
        rol.setNombre("ADMINISTRADOR");

        EstadoUsuario estado = new EstadoUsuario();
        estado.setNombre("ACTIVO");

        when(usuarioRepository.count()).thenReturn(0L);
        when(rolRepository.findByNombre("ADMINISTRADOR")).thenReturn(Optional.of(rol));
        when(estadoUsuarioRepository.findByNombre("ACTIVO")).thenReturn(Optional.of(estado));
        when(passwordEncoder.encode("1234")).thenReturn("hash-1234");

        adminUserInitializer.run();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        verify(usuarioRepository).count();
        verify(rolRepository).findByNombre("ADMINISTRADOR");
        verify(estadoUsuarioRepository).findByNombre("ACTIVO");
        verify(passwordEncoder).encode("1234");
        verify(usuarioRepository).save(captor.capture());

        Usuario usuarioGuardado = captor.getValue();

        assertEquals("admin", usuarioGuardado.getNombreCompleto());
        assertEquals("admin", usuarioGuardado.getNombreUsuario());
        assertEquals("hash-1234", usuarioGuardado.getContrasenaHash());
        assertSame(rol, usuarioGuardado.getRol());
        assertSame(estado, usuarioGuardado.getEstadoUsuario());
        assertNotNull(usuarioGuardado.getFechaCreacion());
    }

    @Test
    void deberiaLanzarExcepcionSiNoExisteRolAdministrador() {
        when(usuarioRepository.count()).thenReturn(0L);
        when(rolRepository.findByNombre("ADMINISTRADOR")).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> adminUserInitializer.run()
        );

        assertEquals("No se encontró el rol requerido: ADMINISTRADOR", ex.getMessage());

        verify(usuarioRepository).count();
        verify(rolRepository).findByNombre("ADMINISTRADOR");
        verifyNoInteractions(estadoUsuarioRepository);
        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarExcepcionSiNoExisteEstadoActivo() {
        Rol rol = new Rol();
        rol.setNombre("ADMINISTRADOR");

        when(usuarioRepository.count()).thenReturn(0L);
        when(rolRepository.findByNombre("ADMINISTRADOR")).thenReturn(Optional.of(rol));
        when(estadoUsuarioRepository.findByNombre("ACTIVO")).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> adminUserInitializer.run()
        );

        assertEquals("No se encontró el estado de usuario requerido: ACTIVO", ex.getMessage());

        verify(usuarioRepository).count();
        verify(rolRepository).findByNombre("ADMINISTRADOR");
        verify(estadoUsuarioRepository).findByNombre("ACTIVO");
        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository, never()).save(any());
    }
}