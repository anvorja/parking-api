package com.univalle.parkingmanagementservice.auth.service.impl;

import com.univalle.parkingmanagementservice.auth.dto.AuthenticatedUserResponse;
import com.univalle.parkingmanagementservice.auth.dto.LoginRequest;
import com.univalle.parkingmanagementservice.auth.dto.LoginResponse;
import com.univalle.parkingmanagementservice.auth.entities.Rol;
import com.univalle.parkingmanagementservice.auth.entities.Usuario;
import com.univalle.parkingmanagementservice.auth.repositories.UsuarioRepository;
import com.univalle.parkingmanagementservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void deberiaLanzarExcepcionCuandoUsuarioNoExiste() {
        LoginRequest request = new LoginRequest("admin", "1234");

        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.empty());

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Credenciales no válidas", ex.getMessage());
        verify(usuarioRepository).findByNombreUsuario("admin");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    void deberiaLanzarExcepcionCuandoPasswordEsIncorrecta() {
        LoginRequest request = new LoginRequest("admin", "1234");
        Usuario usuario = buildUsuario(1L, "Administrador", "admin", "hash-real", "ADMINISTRADOR");

        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("1234", "hash-real")).thenReturn(false);

        BadCredentialsException ex = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Credenciales no válidas", ex.getMessage());
        verify(usuarioRepository).findByNombreUsuario("admin");
        verify(passwordEncoder).matches("1234", "hash-real");
        verifyNoInteractions(jwtService);
    }

    @Test
    void deberiaRetornarLoginResponseCuandoCredencialesSonValidas() {
        LoginRequest request = new LoginRequest("admin", "1234");
        Usuario usuario = buildUsuario(1L, "Administrador", "admin", "hash-real", "ADMINISTRADOR");

        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("1234", "hash-real")).thenReturn(true);
        when(jwtService.generateToken(usuario)).thenReturn("jwt-token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.type());

        AuthenticatedUserResponse user = response.usuario();
        assertNotNull(user);
        assertEquals(1L, user.id());
        assertEquals("Administrador", user.nombreCompleto());
        assertEquals("admin", user.nombreUsuario());
        assertEquals("ADMINISTRADOR", user.rol());

        verify(usuarioRepository).findByNombreUsuario("admin");
        verify(passwordEncoder).matches("1234", "hash-real");
        verify(jwtService).generateToken(usuario);
    }

    private Usuario buildUsuario(
            Long id,
            String nombreCompleto,
            String nombreUsuario,
            String contrasenaHash,
            String nombreRol
    ) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setContrasenaHash(contrasenaHash);
        usuario.setRol(rol);

        return usuario;
    }
}