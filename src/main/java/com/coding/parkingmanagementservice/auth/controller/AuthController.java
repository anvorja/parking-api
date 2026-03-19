package com.coding.parkingmanagementservice.auth.controller;

import com.coding.parkingmanagementservice.auth.dto.LoginRequest;
import com.coding.parkingmanagementservice.auth.dto.LoginResponse;
import com.coding.parkingmanagementservice.auth.dto.LogoutRequest;
import com.coding.parkingmanagementservice.auth.dto.RefreshRequest;
import com.coding.parkingmanagementservice.auth.dto.RefreshResponse;
import com.coding.parkingmanagementservice.auth.service.AuthService;
import com.coding.parkingmanagementservice.usuario.dto.MensajeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Autenticación",
        description = """
                Operaciones de autenticación y gestión de sesión mediante JWT.
                El flujo estándar es: login → uso del access token (15 min) → refresh cuando expire → logout al cerrar sesión.
                """
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Autentica al usuario con sus credenciales y retorna un par de tokens:
                    - **access token** (JWT, vigencia 15 minutos): se incluye en el header `Authorization: Bearer <token>` en cada solicitud protegida.
                    - **refresh token** (opaco, vigencia 7 días): se usa exclusivamente en el endpoint `/refresh` para renovar el access token sin reautenticarse.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa. Se retornan access token y refresh token.",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuerpo de la solicitud inválido o campos requeridos ausentes.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales incorrectas (usuario no existe o contraseña inválida).",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Solicitud de inicio de sesión");
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Renovar access token",
            description = """
                    Genera un nuevo access token a partir de un refresh token válido y no revocado.
                    No requiere el header `Authorization` — el refresh token actúa como única credencial en este endpoint.
                    Si el refresh token está expirado o fue revocado (logout previo), se retorna 401 y el usuario debe autenticarse nuevamente.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Access token renovado exitosamente.",
                    content = @Content(schema = @Schema(implementation = RefreshResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuerpo de la solicitud inválido o refresh token ausente.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token expirado, revocado o no encontrado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = """
                    Revoca el refresh token en base de datos, invalidando la sesión activa del usuario.
                    Requiere el header `Authorization: Bearer <access_token>` vigente para confirmar la identidad del solicitante.
                    Una vez ejecutado, el refresh token queda inutilizable aunque no haya expirado.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sesión cerrada correctamente. El refresh token queda revocado.",
                    content = @Content(schema = @Schema(implementation = MensajeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuerpo de la solicitud inválido o refresh token ausente.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public ResponseEntity<MensajeResponse> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(new MensajeResponse("Sesión cerrada correctamente"));
    }
}