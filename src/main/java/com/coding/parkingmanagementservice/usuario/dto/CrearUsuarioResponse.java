package com.coding.parkingmanagementservice.usuario.dto;

public record CrearUsuarioResponse(
        String mensaje,
        UsuarioListItemResponse usuario
) {
}