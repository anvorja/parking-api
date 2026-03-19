package com.coding.parkingmanagementservice.auth.service.impl;

import com.coding.parkingmanagementservice.auth.dto.AuthenticatedUserResponse;
import com.coding.parkingmanagementservice.auth.dto.LoginRequest;
import com.coding.parkingmanagementservice.auth.dto.LoginResponse;
import com.coding.parkingmanagementservice.auth.dto.LogoutRequest;
import com.coding.parkingmanagementservice.auth.dto.RefreshRequest;
import com.coding.parkingmanagementservice.auth.dto.RefreshResponse;
import com.coding.parkingmanagementservice.auth.entities.RefreshToken;
import com.coding.parkingmanagementservice.auth.entities.Rol;
import com.coding.parkingmanagementservice.auth.entities.Usuario;
import com.coding.parkingmanagementservice.auth.repositories.RefreshTokenRepository;
import com.coding.parkingmanagementservice.auth.repositories.UsuarioRepository;
import com.coding.parkingmanagementservice.security.JwtService;
import com.coding.parkingmanagementservice.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UsuarioRepository      usuarioRepository;
    @Mock private PasswordEncoder        passwordEncoder;
    @Mock private JwtService             jwtService;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Usuario buildUsuario(Long id, String nombreCompleto, String nombreUsuario,
                                 String contrasenaHash, String nombreRol) {
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

    private RefreshToken buildRefreshToken(Usuario usuario, boolean revocado, int diasExpiracion) {
        RefreshToken rt = new RefreshToken();
        rt.setToken("refresh-token-opaco");
        rt.setUsuario(usuario);
        rt.setRevocado(revocado);
        rt.setFechaExpiracion(OffsetDateTime.now().plusDays(diasExpiracion));
        rt.setFechaCreacion(OffsetDateTime.now());
        return rt;
    }

    // ─── Login ────────────────────────────────────────────────────────────────

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
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void deberiaRetornarLoginResponseCuandoCredencialesSonValidas() {
        // Inyectar el valor de refreshExpirationMs (604800000 = 7 días)
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604800000L);

        LoginRequest request = new LoginRequest("admin", "1234");
        Usuario usuario = buildUsuario(1L, "Administrador", "admin", "hash-real", "ADMINISTRADOR");

        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("1234", "hash-real")).thenReturn(true);
        when(jwtService.generateToken(usuario)).thenReturn("jwt-access-token");

        // refreshTokenRepository.save() devuelve el mismo objeto que recibe
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LoginResponse response = authService.login(request);

        // Verificar access token y tipo
        assertNotNull(response);
        assertEquals("jwt-access-token", response.accessToken());
        assertEquals("Bearer", response.type());

        // Verificar que el refresh token fue generado y no es nulo ni vacío
        assertNotNull(response.refreshToken());
        assertFalse(response.refreshToken().isBlank());

        // Verificar datos del usuario
        AuthenticatedUserResponse user = response.usuario();
        assertNotNull(user);
        assertEquals(1L, user.id());
        assertEquals("Administrador", user.nombreCompleto());
        assertEquals("admin", user.nombreUsuario());
        assertEquals("ADMINISTRADOR", user.rol());

        // Verificar interacciones: revoca tokens previos y guarda el nuevo
        verify(refreshTokenRepository).revocarTodosPorUsuario(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));

        // Verificar que el RefreshToken guardado tiene los campos correctos
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken rtGuardado = captor.getValue();
        assertFalse(rtGuardado.isRevocado());
        assertNotNull(rtGuardado.getFechaExpiracion());
        assertTrue(rtGuardado.getFechaExpiracion().isAfter(OffsetDateTime.now()));
        assertEquals(usuario, rtGuardado.getUsuario());
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    @Test
    void deberiaRetornarNuevoAccessTokenCuandoRefreshTokenEsValido() {
        Usuario usuario = buildUsuario(1L, "Administrador", "admin", "hash", "ADMINISTRADOR");
        RefreshToken rt = buildRefreshToken(usuario, false, 7);

        when(refreshTokenRepository.findByToken("refresh-token-opaco"))
                .thenReturn(Optional.of(rt));
        when(jwtService.generateToken(usuario)).thenReturn("nuevo-jwt-token");

        RefreshResponse response = authService.refresh(new RefreshRequest("refresh-token-opaco"));

        assertNotNull(response);
        assertEquals("nuevo-jwt-token", response.accessToken());
        assertEquals("Bearer", response.type());
        verify(jwtService).generateToken(usuario);
    }

    @Test
    void deberiaLanzarExcepcionCuandoRefreshTokenNoExiste() {
        when(refreshTokenRepository.findByToken("token-inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> authService.refresh(new RefreshRequest("token-inexistente"))
        );
    }

    @Test
    void deberiaLanzarExcepcionCuandoRefreshTokenEstaRevocado() {
        Usuario usuario = buildUsuario(1L, "Admin", "admin", "hash", "ADMINISTRADOR");
        RefreshToken rt = buildRefreshToken(usuario, true, 7); // revocado = true

        when(refreshTokenRepository.findByToken("token-revocado"))
                .thenReturn(Optional.of(rt));

        assertThrows(
                BusinessException.class,
                () -> authService.refresh(new RefreshRequest("token-revocado"))
        );
        verifyNoInteractions(jwtService);
    }

    @Test
    void deberiaLanzarExcepcionCuandoRefreshTokenEstaExpirado() {
        Usuario usuario = buildUsuario(1L, "Admin", "admin", "hash", "ADMINISTRADOR");
        RefreshToken rt = buildRefreshToken(usuario, false, -1); // expirado ayer

        when(refreshTokenRepository.findByToken("token-expirado"))
                .thenReturn(Optional.of(rt));
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertThrows(
                BusinessException.class,
                () -> authService.refresh(new RefreshRequest("token-expirado"))
        );

        // Verifica que el token expirado se marca como revocado para evitar reintentos
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertTrue(captor.getValue().isRevocado());

        verifyNoInteractions(jwtService);
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    @Test
    void deberiaRevocarTokensAlHacerLogout() {
        Usuario usuario = buildUsuario(1L, "Admin", "admin", "hash", "ADMINISTRADOR");
        RefreshToken rt = buildRefreshToken(usuario, false, 7);

        when(refreshTokenRepository.findByToken("refresh-token-opaco"))
                .thenReturn(Optional.of(rt));

        authService.logout(new LogoutRequest("refresh-token-opaco"));

        verify(refreshTokenRepository).revocarTodosPorUsuario(1L);
    }

    @Test
    void deberiaLanzarExcepcionEnLogoutCuandoTokenNoExiste() {
        when(refreshTokenRepository.findByToken("token-inexistente"))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> authService.logout(new LogoutRequest("token-inexistente"))
        );
    }
}