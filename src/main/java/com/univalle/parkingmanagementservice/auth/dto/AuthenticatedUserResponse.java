package com.univalle.parkingmanagementservice.auth.dto;

public record AuthenticatedUserResponse(
        Long id,
        String nombreCompleto,
        String nombreUsuario,
        String rol
) {
}