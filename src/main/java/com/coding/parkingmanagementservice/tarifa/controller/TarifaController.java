package com.coding.parkingmanagementservice.tarifa.controller;

import com.coding.parkingmanagementservice.tarifa.dto.CrearTarifaRequest;
import com.coding.parkingmanagementservice.tarifa.dto.EditarTarifaRequest;
import com.coding.parkingmanagementservice.tarifa.dto.TarifaResponse;
import com.coding.parkingmanagementservice.tarifa.service.TarifaService;
import com.coding.parkingmanagementservice.usuario.dto.MensajeResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/tarifas")
@RequiredArgsConstructor
@Tag(
        name = "Tarifas",
        description = """
                Gestión del catálogo de tarifas de cobro por tipo de vehículo y unidad de tiempo.
                La consulta del listado está disponible para cualquier usuario autenticado y es utilizada
                por el frontend para cachear las tarifas en IndexedDB al iniciar sesión (modo offline).
                Las operaciones de escritura requieren rol ADMINISTRADOR.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class TarifaController {

    private final TarifaService tarifaService;

    @GetMapping
    @Operation(
            summary = "Listar tarifas activas",
            description = """
                    Retorna todas las tarifas con estado activo, agrupadas por tipo de vehículo y unidad de tarifa.
                    Accesible para cualquier usuario autenticado (ADMINISTRADOR u OPERADOR).
                    Este endpoint es consumido por el servicio de datos de referencia del frontend
                    para pre-cargar las tarifas en caché local (IndexedDB) al momento del login.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de tarifas activas obtenido exitosamente.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TarifaResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public List<TarifaResponse> listarActivas() {
        return tarifaService.listarActivas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @Operation(
            summary = "Crear tarifa",
            description = """
                    Registra una nueva tarifa para un tipo de vehículo y unidad de tiempo específicos.
                    Si ya existe una tarifa activa con la misma combinación de tipo de vehículo y unidad de tarifa,
                    esta es desactivada automáticamente antes de activar la nueva, garantizando que solo
                    exista una tarifa vigente por combinación.
                    Requiere rol ADMINISTRADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Tarifa creada y activada exitosamente.",
                    content = @Content(schema = @Schema(implementation = TarifaResponse.class))
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
            )
    })
    public TarifaResponse crear(@Valid @RequestBody CrearTarifaRequest request) {
        return tarifaService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @Operation(
            summary = "Actualizar tarifa",
            description = """
                    Actualiza el valor de cobro de una tarifa existente identificada por su ID.
                    Solo se permite modificar tarifas con estado activo.
                    Requiere rol ADMINISTRADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarifa actualizada exitosamente.",
                    content = @Content(schema = @Schema(implementation = TarifaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuerpo de la solicitud inválido o valor de tarifa fuera de rango.",
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
                    description = "No existe una tarifa con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public TarifaResponse editar(
            @Parameter(description = "Identificador único de la tarifa a actualizar.", required = true, example = "2")
            @PathVariable Long id,
            @Valid @RequestBody EditarTarifaRequest request
    ) {
        return tarifaService.editar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @Operation(
            summary = "Desactivar tarifa",
            description = """
                    Realiza un soft delete sobre la tarifa, cambiando su estado a inactivo.
                    La tarifa no se elimina físicamente de la base de datos para preservar la integridad
                    histórica de los registros de ingreso asociados.
                    Requiere rol ADMINISTRADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarifa desactivada exitosamente.",
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
                    description = "No existe una tarifa con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public ResponseEntity<MensajeResponse> desactivar(
            @Parameter(description = "Identificador único de la tarifa a desactivar.", required = true, example = "2")
            @PathVariable Long id
    ) {
        tarifaService.desactivar(id);
        return ResponseEntity.ok(new MensajeResponse("Tarifa desactivada correctamente"));
    }
}