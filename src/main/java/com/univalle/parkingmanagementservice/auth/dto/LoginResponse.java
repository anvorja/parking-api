package com.univalle.parkingmanagementservice.auth.dto;

public record LoginResponse(
        String token,
        String type,
        AuthenticatedUserResponse usuario
) {
}
