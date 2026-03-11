package com.univalle.parkingmanagementservice.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditarUsuarioRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 50, message = "El usuario no puede superar los 50 caracteres")
        String nombreUsuario,

        String contrasena,

        String confirmacionContrasena,

        @NotBlank(message = "El rol es obligatorio")
        String rol
) {
}
