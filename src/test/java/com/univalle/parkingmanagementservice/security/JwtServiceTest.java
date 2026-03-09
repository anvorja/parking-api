package com.univalle.parkingmanagementservice.security;

import com.univalle.parkingmanagementservice.auth.entities.Rol;
import com.univalle.parkingmanagementservice.auth.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        String secretBase64 = "bWlfY2xhdmVfc2VjcmV0YV9zdXBlcl9zZWd1cmFfMTIzNDU2Nzg5MDEyMzQ1Ng==";

        ReflectionTestUtils.setField(jwtService, "jwtSecret", secretBase64);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);
    }

    @Test
    void deberiaGenerarTokenValido() {
        Usuario usuario = buildUsuario(1L, "admin", "ADMIN");

        String token = jwtService.generateToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void deberiaExtraerUsernameDelToken() {
        Usuario usuario = buildUsuario(1L, "admin", "ADMIN");
        String token = jwtService.generateToken(usuario);

        String username = jwtService.extractUsername(token);

        assertEquals("admin", username);
    }

    @Test
    void deberiaRetornarTrueCuandoTokenEsValido() {
        Usuario usuario = buildUsuario(1L, "admin", "ADMIN");
        String token = jwtService.generateToken(usuario);

        boolean valido = jwtService.isTokenValid(token);

        assertTrue(valido);
    }

    @Test
    void deberiaRetornarFalseCuandoTokenEsInvalido() {
        String tokenInvalido = "esto.no.es.un.jwt.valido";

        boolean valido = jwtService.isTokenValid(tokenInvalido);

        assertFalse(valido);
    }

    private Usuario buildUsuario(Long id, String username, String nombreRol) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombreUsuario(username);
        usuario.setRol(rol);

        return usuario;
    }
}
