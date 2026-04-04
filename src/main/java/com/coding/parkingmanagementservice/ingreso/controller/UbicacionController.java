package com.coding.parkingmanagementservice.ingreso.controller;

import com.coding.parkingmanagementservice.ingreso.dto.CrearUbicacionRequest;
import com.coding.parkingmanagementservice.ingreso.dto.EditarUbicacionRequest;
import com.coding.parkingmanagementservice.ingreso.dto.UbicacionResponse;
import com.coding.parkingmanagementservice.ingreso.service.UbicacionService;
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
@RequestMapping("/api/v1/ubicaciones")
@RequiredArgsConstructor
@Tag(
        name = "Ubicaciones",
        description = """
                Gestión de las ubicaciones físicas del parqueadero (zonas o espacios de estacionamiento).
                La consulta del listado está disponible para cualquier usuario autenticado y es utilizada
                por el frontend para cachear las ubicaciones en IndexedDB al iniciar sesión (modo offline).
                Las operaciones de escritura requieren rol ADMINISTRADOR.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    @GetMapping
    @Operation(
            summary = "Listar ubicaciones",
            description = """
                    Retorna las ubicaciones registradas. Si incluirInactivas es false, retorna
                    solo aquellas con estado activo (DISPONIBLE u OCUPADO). Si es true, retorna todas.
                    Accesible para cualquier usuario autenticado (ADMINISTRADOR u OPERADOR).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ubicaciones obtenido exitosamente.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UbicacionResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public List<UbicacionResponse> listar(
            @RequestParam(required = false, defaultValue = "false") boolean incluirInactivas
    ) {
        if (incluirInactivas) {
            return ubicacionService.listarTodas();
        }
        return ubicacionService.listarActivas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @Operation(
            summary = "Crear ubicación",
            description = """
                    Registra una nueva ubicación en el parqueadero con su nombre, tipo de vehículo admitido y capacidad.
                    La ubicación se crea con estado DISPONIBLE por defecto.
                    Requiere rol ADMINISTRADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Ubicación creada exitosamente.",
                    content = @Content(schema = @Schema(implementation = UbicacionResponse.class))
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
                    description = "Ya existe una ubicación con el mismo nombre.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public UbicacionResponse crear(@Valid @RequestBody CrearUbicacionRequest request) {
        return ubicacionService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @Operation(
            summary = "Actualizar ubicación",
            description = """
                    Actualiza el nombre, tipo de vehículo admitido o capacidad de una ubicación existente.
                    **Restricción:** si la ubicación tiene ingresos activos (vehículos dentro), no se permite
                    modificar el tipo de vehículo admitido, ya que alteraría la integridad de los registros en curso.
                    Requiere rol ADMINISTRADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ubicación actualizada exitosamente.",
                    content = @Content(schema = @Schema(implementation = UbicacionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuerpo de la solicitud inválido, o se intentó cambiar el tipo de vehículo con ingresos activos.",
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
                    description = "No existe una ubicación con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public UbicacionResponse editar(
            @Parameter(description = "Identificador único de la ubicación a actualizar.", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EditarUbicacionRequest request
    ) {
        return ubicacionService.editar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @Operation(
            summary = "Desactivar ubicación",
            description = """
                    Realiza un soft delete sobre la ubicación, cambiando su estado a INACTIVO.
                    **Restricción:** la operación falla si la ubicación tiene vehículos con ingreso activo,
                    es decir, vehículos que aún no han registrado su salida.
                    La ubicación no se elimina físicamente para preservar el historial de ingresos asociados.
                    Requiere rol ADMINISTRADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ubicación desactivada exitosamente.",
                    content = @Content(schema = @Schema(implementation = MensajeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La ubicación tiene ingresos activos y no puede ser desactivada.",
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
                    description = "No existe una ubicación con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public ResponseEntity<MensajeResponse> desactivar(
            @Parameter(description = "Identificador único de la ubicación a desactivar.", required = true, example = "1")
            @PathVariable Long id
    ) {
        ubicacionService.desactivar(id);
        return ResponseEntity.ok(new MensajeResponse("Ubicación desactivada correctamente"));
    }

    @PutMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @Operation(
            summary = "Reactivar ubicación",
            description = """
                    Reactiva una ubicación en estado inactivo y la vuelve a estado DISPONIBLE.
                    Requiere rol ADMINISTRADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ubicación reactivada exitosamente.",
                    content = @Content(schema = @Schema(implementation = MensajeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La ubicación no está inactiva.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe una ubicación con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public ResponseEntity<MensajeResponse> reactivar(
            @Parameter(description = "Identificador único de la ubicación a reactivar.", required = true, example = "1")
            @PathVariable Long id
    ) {
        ubicacionService.reactivar(id);
        return ResponseEntity.ok(new MensajeResponse("Ubicación reactivada correctamente"));
    }
}