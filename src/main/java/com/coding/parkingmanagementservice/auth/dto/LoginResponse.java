package com.coding.parkingmanagementservice.auth.dto;

/**
 * Respuesta del endpoint POST /api/v1/auth/login.
 *
 * accessToken: JWT de corta duración (15 min) para autenticar peticiones.
 * refreshToken: Token opaco de larga duración (7 días) para obtener
 *               nuevos access tokens sin re-autenticarse.
 * type: siempre "Bearer"
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String type,
        AuthenticatedUserResponse usuario
) {}