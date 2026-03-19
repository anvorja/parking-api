package com.coding.parkingmanagementservice.security;

import com.coding.parkingmanagementservice.auth.entities.Rol;
import com.coding.parkingmanagementservice.auth.entities.Usuario;
import com.coding.parkingmanagementservice.auth.repositories.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deberiaPermitirRutaPublicaSinValidarToken() throws ServletException, IOException {
        request.setServletPath("/api/v1/auth/login");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(usuarioRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deberiaContinuarCuandoNoHayAuthorizationHeader() throws ServletException, IOException {
        request.setServletPath("/api/v1/usuarios");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(usuarioRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deberiaContinuarCuandoAuthorizationNoEmpiezaPorBearer() throws ServletException, IOException {
        request.setServletPath("/api/v1/usuarios");
        request.addHeader("Authorization", "Basic abc123");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(usuarioRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deberiaContinuarCuandoTokenEsInvalido() throws ServletException, IOException {
        request.setServletPath("/api/v1/usuarios");
        request.addHeader("Authorization", "Bearer token-invalido");

        when(jwtService.isTokenValid("token-invalido")).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).isTokenValid("token-invalido");
        verify(jwtService, never()).extractUsername(anyString());
        verifyNoInteractions(usuarioRepository);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deberiaAutenticarCuandoTokenEsValidoYUsuarioExiste() throws ServletException, IOException {
        request.setServletPath("/api/v1/usuarios");
        request.addHeader("Authorization", "Bearer token-valido");

        Usuario usuario = buildUsuario(1L, "admin", "ADMIN");

        when(jwtService.isTokenValid("token-valido")).thenReturn(true);
        when(jwtService.extractUsername("token-valido")).thenReturn("admin");
        when(usuarioRepository.findByNombreUsuarioWithRol("admin")).thenReturn(Optional.of(usuario));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).isTokenValid("token-valido");
        verify(jwtService).extractUsername("token-valido");
        verify(usuarioRepository).findByNombreUsuarioWithRol("admin");
        verify(filterChain).doFilter(request, response);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("admin", authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void noDeberiaAutenticarCuandoTokenEsValidoPeroUsuarioNoExiste() throws ServletException, IOException {
        request.setServletPath("/api/v1/usuarios");
        request.addHeader("Authorization", "Bearer token-valido");

        when(jwtService.isTokenValid("token-valido")).thenReturn(true);
        when(jwtService.extractUsername("token-valido")).thenReturn("admin");
        when(usuarioRepository.findByNombreUsuarioWithRol("admin")).thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).isTokenValid("token-valido");
        verify(jwtService).extractUsername("token-valido");
        verify(usuarioRepository).findByNombreUsuarioWithRol("admin");
        verify(filterChain).doFilter(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void noDeberiaReautenticarSiYaExisteAuthenticationEnContexto() throws ServletException, IOException {
        request.setServletPath("/api/v1/usuarios");
        request.addHeader("Authorization", "Bearer token-valido");

        var existingAuth = new UsernamePasswordAuthenticationToken("ya-autenticado", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(jwtService.isTokenValid("token-valido")).thenReturn(true);
        when(jwtService.extractUsername("token-valido")).thenReturn("admin");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        verify(jwtService).isTokenValid("token-valido");
        verify(jwtService).extractUsername("token-valido");
        verify(usuarioRepository, never()).findByNombreUsuarioWithRol(anyString());
        verify(filterChain).doFilter(request, response);

        assertSame(existingAuth, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void deberiaConstruirRolConTrimYUpperCase() throws ServletException, IOException {
        request.setServletPath("/api/v1/usuarios");
        request.addHeader("Authorization", "Bearer token-valido");

        Usuario usuario = buildUsuario(1L, "admin", " admin ");

        when(jwtService.isTokenValid("token-valido")).thenReturn(true);
        when(jwtService.extractUsername("token-valido")).thenReturn("admin");
        when(usuarioRepository.findByNombreUsuarioWithRol("admin")).thenReturn(Optional.of(usuario));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    private Usuario buildUsuario(Long id, String username, String rolNombre) {
        Rol rol = new Rol();
        rol.setNombre(rolNombre);

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombreUsuario(username);
        usuario.setRol(rol);
        return usuario;
    }
}
