package com.coding.parkingmanagementservice.auth.service.impl;

import com.coding.parkingmanagementservice.auth.dto.AuthenticatedUserResponse;
import com.coding.parkingmanagementservice.auth.dto.LoginRequest;
import com.coding.parkingmanagementservice.auth.dto.LoginResponse;
import com.coding.parkingmanagementservice.auth.dto.LogoutRequest;
import com.coding.parkingmanagementservice.auth.dto.RefreshRequest;
import com.coding.parkingmanagementservice.auth.dto.RefreshResponse;
import com.coding.parkingmanagementservice.auth.entities.RefreshToken;
import com.coding.parkingmanagementservice.auth.entities.Usuario;
import com.coding.parkingmanagementservice.auth.repositories.RefreshTokenRepository;
import com.coding.parkingmanagementservice.auth.repositories.UsuarioRepository;
import com.coding.parkingmanagementservice.auth.service.AuthService;
import com.coding.parkingmanagementservice.security.JwtService;
import com.coding.parkingmanagementservice.shared.exception.BusinessException;
import com.coding.parkingmanagementservice.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository       usuarioRepository;
    private final PasswordEncoder         passwordEncoder;
    private final JwtService              jwtService;
    private final RefreshTokenRepository  refreshTokenRepository;

    @Value("${security.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // ─── Login ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByNombreUsuario(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales no válidas"));

        if (!passwordEncoder.matches(request.password(), usuario.getContrasenaHash())) {
            throw new BadCredentialsException("Credenciales no válidas");
        }

        String accessToken  = jwtService.generateToken(usuario);
        String refreshToken = crearRefreshToken(usuario);

        AuthenticatedUserResponse usuarioResponse = new AuthenticatedUserResponse(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getNombreUsuario(),
                usuario.getRol().getNombre()
        );

        return new LoginResponse(accessToken, refreshToken, "Bearer", usuarioResponse);
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        RefreshToken rt = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AUTH_TOKEN_INVALID,
                        "Refresh token no válido",
                        HttpStatus.UNAUTHORIZED));

        if (rt.isRevocado()) {
            throw new BusinessException(
                    ErrorCode.AUTH_TOKEN_INVALID,
                    "El refresh token ha sido revocado",
                    HttpStatus.UNAUTHORIZED);
        }

        if (rt.getFechaExpiracion().isBefore(OffsetDateTime.now())) {
            // Marcar como revocado para evitar reintentos
            rt.setRevocado(true);
            refreshTokenRepository.save(rt);
            throw new BusinessException(
                    ErrorCode.AUTH_TOKEN_EXPIRED,
                    "El refresh token ha expirado. Por favor inicia sesión nuevamente",
                    HttpStatus.UNAUTHORIZED);
        }

        // Generar nuevo access token con los datos actuales del usuario
        Usuario usuario = rt.getUsuario();
        String nuevoAccessToken = jwtService.generateToken(usuario);

        return new RefreshResponse(nuevoAccessToken, "Bearer");
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken rt = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.AUTH_TOKEN_INVALID,
                        "Refresh token no válido",
                        HttpStatus.UNAUTHORIZED));

        // Revocar todos los tokens del usuario para cerrar todas las sesiones
        refreshTokenRepository.revocarTodosPorUsuario(rt.getUsuario().getId());
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private String crearRefreshToken(Usuario usuario) {
        // Revocar tokens anteriores del mismo usuario antes de crear uno nuevo
        // Garantiza una sesión activa por usuario a la vez
        refreshTokenRepository.revocarTodosPorUsuario(usuario.getId());

        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString().replace("-", ""));
        rt.setUsuario(usuario);
        rt.setFechaExpiracion(OffsetDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        rt.setRevocado(false);
        rt.setFechaCreacion(OffsetDateTime.now());

        refreshTokenRepository.save(rt);
        return rt.getToken();
    }
}