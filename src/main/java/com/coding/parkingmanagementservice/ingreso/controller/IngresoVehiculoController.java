package com.coding.parkingmanagementservice.ingreso.controller;

import com.coding.parkingmanagementservice.ingreso.dto.*;
import com.coding.parkingmanagementservice.ingreso.service.IngresoVehiculoService;
import com.coding.parkingmanagementservice.usuario.dto.MensajeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
@Tag(
        name = "Ingresos de vehículos",
        description = """
                Gestión del ciclo de vida completo de los ingresos y salidas de vehículos en el parqueadero.
                Cubre el registro de entrada, consulta por placa o ID, confirmación de salida con cálculo
                de tarifa, edición y eliminación de registros.
                Todos los endpoints requieren un access token JWT vigente.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class IngresoVehiculoController {

    private final IngresoVehiculoService ingresoVehiculoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registrar ingreso de vehículo",
            description = """
                    Registra la entrada de un vehículo al parqueadero, asociándolo a una ubicación disponible.
                    El ingreso queda vinculado al usuario operador autenticado que realiza el registro.
                    Si el sistema está offline, el frontend encola el registro en IndexedDB y lo sincroniza
                    automáticamente al recuperar conectividad (outbox pattern).
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Ingreso registrado exitosamente.",
                    content = @Content(schema = @Schema(implementation = IngresoVehiculoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuerpo de la solicitud inválido, placa con formato incorrecto o ubicación no disponible.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La ubicación o tipo de vehículo especificado no existe.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe un ingreso activo para el vehículo con la placa indicada.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public IngresoVehiculoResponse registrarIngreso(
            @Valid @RequestBody RegistrarIngresoRequest request,
            Authentication authentication
    ) {
        return ingresoVehiculoService.registrarIngreso(request, authentication.getName());
    }

    @GetMapping
    @Operation(
            summary = "Listar ingresos con filtros y paginación",
            description = """
                    Retorna un listado paginado de ingresos de vehículos, con soporte para filtros opcionales por placa y estado.
                    El resultado incluye metadatos de paginación (página actual, total de páginas, total de elementos).
                    Útil para la vista de historial y búsqueda de vehículos en el panel de administración.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado de ingresos obtenido exitosamente.",
                    content = @Content(schema = @Schema(implementation = IngresoVehiculoPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public IngresoVehiculoPageResponse listarIngresos(
            @Parameter(description = "Filtra por placa del vehículo (búsqueda parcial).", example = "ABC123")
            @RequestParam(required = false) String placa,
            @Parameter(description = "Filtra por estado del ingreso. Valores posibles: `ACTIVO`, `FINALIZADO`.", example = "ACTIVO")
            @RequestParam(required = false) String estado,
            @Parameter(description = "Número de página (base 0).", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Cantidad de registros por página.", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ingresoVehiculoService.listarIngresos(placa, estado, page, size);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener detalle de un ingreso",
            description = """
                    Retorna la información completa de un ingreso identificado por su ID.
                    Utilizado principalmente en el flujo de salida por QR (HU-009): el frontend escanea
                    el código QR del tiquete, obtiene el ID del ingreso y consulta este endpoint para
                    mostrar el preview de la salida antes de confirmarla.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalle del ingreso obtenido exitosamente.",
                    content = @Content(schema = @Schema(implementation = IngresoVehiculoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un ingreso con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public IngresoVehiculoResponse obtenerPorId(
            @Parameter(description = "Identificador único del ingreso.", required = true, example = "101")
            @PathVariable Long id
    ) {
        return ingresoVehiculoService.obtenerPorId(id);
    }

    @GetMapping("/activo")
    @Operation(
            summary = "Buscar ingreso activo por placa",
            description = """
                    Busca y retorna el ingreso con estado ACTIVO asociado a la placa indicada.
                    Utilizado en el flujo de salida manual (HU-010): el operador ingresa la placa
                    del vehículo para ubicar el registro activo y proceder con la confirmación de salida.
                    Retorna 404 si no existe ingreso activo para la placa consultada.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ingreso activo encontrado exitosamente.",
                    content = @Content(schema = @Schema(implementation = IngresoVehiculoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un ingreso activo para la placa indicada.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public IngresoVehiculoResponse buscarActivoPorPlaca(
            @Parameter(description = "Placa del vehículo a consultar.", required = true, example = "ABC123")
            @RequestParam String placa
    ) {
        return ingresoVehiculoService.buscarActivoPorPlaca(placa);
    }

    @PostMapping("/{id}/salida")
    @Operation(
            summary = "Registrar salida de vehículo",
            description = """
                    Confirma la salida del vehículo, calcula el valor a cobrar según la tarifa vigente
                    para el tipo de vehículo y el tiempo de permanencia, y cambia el estado del ingreso a FINALIZADO.
                    Aplica para los flujos de salida por QR (HU-009), salida manual por placa (HU-010)
                    y salida desde el historial (HU-011).
                    El cuerpo de la solicitud es opcional: si se omite, se usa la fecha y hora actual del servidor.
                    El registro de la salida queda asociado al usuario operador autenticado.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Salida registrada y valor calculado exitosamente.",
                    content = @Content(schema = @Schema(implementation = SalidaResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El ingreso ya tiene una salida registrada o la fecha de salida es anterior a la de ingreso.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un ingreso con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public SalidaResponse registrarSalida(
            @Parameter(description = "Identificador único del ingreso al que se le registra la salida.", required = true, example = "101")
            @PathVariable Long id,
            @RequestBody(required = false) RegistrarSalidaRequest request,
            Authentication authentication
    ) {
        RegistrarSalidaRequest req = request != null ? request : new RegistrarSalidaRequest(null);
        return ingresoVehiculoService.registrarSalida(id, req, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMINISTRADOR')")
    @Operation(
            summary = "Eliminar registro de ingreso",
            description = """
                    Elimina permanentemente un registro de ingreso del sistema por su identificador.
                    Esta operación aplica para correcciones administrativas (HU-019) y no es reversible.
                    Requiere rol ADMINISTRADOR.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro de ingreso eliminado exitosamente.",
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
                    description = "No existe un ingreso con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public ResponseEntity<MensajeResponse> eliminarIngreso(
            @Parameter(description = "Identificador único del ingreso a eliminar.", required = true, example = "101")
            @PathVariable Long id
    ) {
        ingresoVehiculoService.eliminarIngreso(id);
        return ResponseEntity.ok(new MensajeResponse("Registro de ingreso eliminado correctamente"));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Editar registro de ingreso",
            description = """
                    Actualiza los datos de un registro de ingreso existente (placa, ubicación, tipo de vehículo u observaciones).
                    El ADMINISTRADOR puede editar cualquier campo. El OPERADOR solo puede modificar campos permitidos
                    según las reglas de negocio definidas (HU-020).
                    El nivel de acceso se determina automáticamente a partir del rol del usuario autenticado.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro de ingreso actualizado exitosamente.",
                    content = @Content(schema = @Schema(implementation = IngresoVehiculoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cuerpo de la solicitud inválido o modificación no permitida para el rol del usuario.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un ingreso con el identificador proporcionado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public IngresoVehiculoResponse editarIngreso(
            @Parameter(description = "Identificador único del ingreso a actualizar.", required = true, example = "101")
            @PathVariable Long id,
            @RequestBody EditarIngresoRequest request,
            Authentication authentication
    ) {
        boolean esAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        return ingresoVehiculoService.editarIngreso(id, request, esAdmin);
    }
}