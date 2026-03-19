package com.coding.parkingmanagementservice.ingreso.controller;

import com.coding.parkingmanagementservice.ingreso.entities.TipoVehiculo;
import com.coding.parkingmanagementservice.ingreso.repository.TipoVehiculoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tipos-vehiculo")
@RequiredArgsConstructor
@Tag(
        name = "Tipos de vehículo",
        description = """
                Catálogo de referencia de tipos de vehículo admitidos en el parqueadero (ej. CARRO, MOTO).
                Es un catálogo de solo lectura usado por el frontend para pre-cargar los datos de referencia
                en IndexedDB al iniciar sesión, garantizando su disponibilidad en modo offline.
                """
)
@SecurityRequirement(name = "bearerAuth")
public class TipoVehiculoController {

    private final TipoVehiculoRepository tipoVehiculoRepository;

    @GetMapping
    @Operation(
            summary = "Listar tipos de vehículo",
            description = """
                    Retorna el catálogo completo de tipos de vehículo registrados en el sistema.
                    Accesible para cualquier usuario autenticado (ADMINISTRADOR u OPERADOR).
                    Consumido por el servicio de datos de referencia del frontend (`refDataService`)
                    para poblar los selectores de tipo de vehículo en los formularios de ingreso
                    y para mantener disponibilidad offline mediante caché en IndexedDB.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Catálogo de tipos de vehículo obtenido exitosamente.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TipoVehiculoResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token ausente, inválido o expirado.",
                    content = @Content(schema = @Schema(ref = "#/components/schemas/ApiErrorResponse"))
            )
    })
    public List<TipoVehiculoResponse> listarTipos() {
        return tipoVehiculoRepository.findAll()
                .stream()
                .map(t -> new TipoVehiculoResponse(t.getId(), t.getNombre()))
                .toList();
    }

    public record TipoVehiculoResponse(Long id, String nombre) {}
}