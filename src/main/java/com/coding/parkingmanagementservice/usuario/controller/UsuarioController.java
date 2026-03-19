package com.coding.parkingmanagementservice.usuario.controller;

import java.util.List;

import com.coding.parkingmanagementservice.usuario.dto.*;
import com.coding.parkingmanagementservice.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(
        name = "Usuarios",
        description = """
                Operaciones relacionadas con la gestión de usuarios del sistema.
                Todos los endpoints requieren rol ADMINISTRADOR y un access token JWT vigente.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Retorna el listado completo de usuarios registrados en el sistema, incluyendo su estado y rol asignado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de usuarios obtenido exitosamente.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioListItemResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no cuenta con el rol ADMINISTRADOR.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioListItemResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PostMapping
    @Operation(
            summary = "Crear usuario",
            description = """
                    Registra un nuevo usuario en el sistema con el rol y estado especificados.
                    El nombre de usuario debe ser único. La contraseña se almacena cifrada (BCrypt).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado exitosamente.",
                    content = @Content(schema = @Schema(implementation = CrearUsuarioResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuerpo de la solicitud inválido o campos requeridos ausentes.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no cuenta con el rol ADMINISTRADOR.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe un usuario con el mismo nombre de usuario.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public CrearUsuarioResponse crearUsuario(@Valid @RequestBody CrearUsuarioRequest request) {
        UsuarioListItemResponse usuario = usuarioService.crearUsuario(request);
        return new CrearUsuarioResponse("Usuario creado correctamente", usuario);
    }

    @PutMapping("/{idUsuario}")
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualiza los datos de un usuario existente (nombre completo, rol, estado o contraseña). Solo se modifican los campos incluidos en la solicitud."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente.",
                    content = @Content(schema = @Schema(implementation = UsuarioListItemResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuerpo de la solicitud inválido o campos requeridos ausentes.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no cuenta con el rol ADMINISTRADOR.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un usuario con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public UsuarioListItemResponse editarUsuario(
            @Parameter(description = "Identificador único del usuario a actualizar.", required = true, example = "3")
            @PathVariable Long idUsuario,
            @Valid @RequestBody EditarUsuarioRequest request
    ) {
        return usuarioService.editarUsuario(idUsuario, request);
    }

    @DeleteMapping("/{idUsuario}")
    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina permanentemente un usuario del sistema por su identificador. Esta operación no es reversible."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario eliminado exitosamente.",
                    content = @Content(schema = @Schema(implementation = MensajeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario autenticado no cuenta con el rol ADMINISTRADOR.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un usuario con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    public ResponseEntity<MensajeResponse> eliminarUsuario(
            @Parameter(description = "Identificador único del usuario a eliminar.", required = true, example = "3")
            @PathVariable Long idUsuario
    ) {
        usuarioService.eliminarUsuario(idUsuario);
        return ResponseEntity.ok(new MensajeResponse("Usuario eliminado correctamente"));
    }
}