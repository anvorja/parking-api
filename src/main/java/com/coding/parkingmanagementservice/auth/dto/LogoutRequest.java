package com.coding.parkingmanagementservice.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Body para POST /api/v1/auth/logout.
 *  El refresh token se revoca en base de datos para invalidar la sesión.
 */
public record LogoutRequest(
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {}