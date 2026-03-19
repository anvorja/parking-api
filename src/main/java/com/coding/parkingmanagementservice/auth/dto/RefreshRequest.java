package com.coding.parkingmanagementservice.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Body para POST /api/v1/auth/refresh */
public record RefreshRequest(
        @NotBlank(message = "El refresh token es obligatorio")
        String refreshToken
) {}