package com.univalle.parkingmanagementservice.usuario.dto;

public record UsuarioListItemResponse(
        Long id,
        String nombreCompleto,
        String nombreUsuario,
        String rol
) {
}
