package com.coding.parkingmanagementservice.auth.dto;

/** Respuesta del endpoint POST /api/v1/auth/refresh.
 *  Devuelve un nuevo access token. El refresh token no cambia
 *  a menos que se implemente rotación (fuera del alcance actual).
 */
public record RefreshResponse(
        String accessToken,
        String type
) {}