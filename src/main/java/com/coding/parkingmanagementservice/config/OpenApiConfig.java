package com.coding.parkingmanagementservice.config;

import com.coding.parkingmanagementservice.shared.dto.ApiErrorResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI parkingManagementOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(buildInfo())
                .servers(buildServers())
                .tags(buildTags())
                .components(buildComponents(securitySchemeName))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }

    // ---------------------------------------------------------------------------
    // Info
    // ---------------------------------------------------------------------------

    private Info buildInfo() {
        return new Info()
                .title("Parking Management Service API")
                .description("""
                        API REST para la gestión integral del sistema de parqueadero.

                        ## Autenticación
                        La API utiliza **JWT Bearer tokens**. Para autenticarte:
                        1. Llama a **POST /api/v1/auth/login** con tus credenciales.
                        2. Copia el **accessToken** de la respuesta.
                        3. Haz clic en el botón **Authorize** e ingresa: **Bearer <accessToken>**.

                        El access token tiene vigencia de **15 minutos**. Cuando expire, usa\s
                        **POST /api/v1/auth/refresh** con el **refreshToken** (vigencia 7 días) para renovarlo\s
                        sin necesidad de reautenticarte.

                        ## Roles
                        | Rol               | Permisos |
                        |-------------------|----------|
                        | **ADMINISTRADOR** | Acceso total: gestión de usuarios, tarifas, ubicaciones e ingresos. |
                        | **OPERADOR**      | Registro y consulta de ingresos y salidas de vehículos. |

                        ## Convenciones de respuesta
                        - **200 OK** — Operación exitosa (GET, PUT, DELETE).
                        - **201 Created** — Recurso creado exitosamente (POST).
                        - **400 Bad Request** — Validación fallida o regla de negocio violada.
                        - **401 Unauthorized** — Token ausente, inválido o expirado.
                        - **403 Forbidden** — Token válido pero sin permisos suficientes.
                        - **404 Not Found** — Recurso no encontrado.
                        - **409 Conflict** — Conflicto de unicidad (ej. username duplicado).
                        """)
                .version("v1.0")
                .contact(new Contact()
                        .name("Equipo Pro Dev — Universidad del Valle")
                        .email("prodev@univalle.edu.co"))
                .license(new License()
                        .name("Uso interno — Universidad del Valle")
                        .url("https://www.univalle.edu.co"));
    }

    // ---------------------------------------------------------------------------
    // Servers
    // ---------------------------------------------------------------------------

    private List<Server> buildServers() {
        Server production = new Server()
                .url("https://parking-api-1kwr.onrender.com")
                .description("Producción — Render");

        Server development = new Server()
                .url("http://localhost:8090")
                .description("Desarrollo local");

        return List.of(production, development);
    }

    // ---------------------------------------------------------------------------
    // Tags (definen el orden de las secciones en Swagger UI)
    // ---------------------------------------------------------------------------

    private List<Tag> buildTags() {
        return List.of(
                new Tag()
                        .name("Autenticación")
                        .description("Login, renovación de token y cierre de sesión."),
                new Tag()
                        .name("Ingresos de vehículos")
                        .description("Registro de entradas, salidas y consulta del historial."),
                new Tag()
                        .name("Ubicaciones")
                        .description("Gestión de espacios físicos del parqueadero."),
                new Tag()
                        .name("Tarifas")
                        .description("Catálogo de tarifas de cobro por tipo de vehículo."),
                new Tag()
                        .name("Tipos de vehículo")
                        .description("Catálogo de referencia de tipos de vehículo admitidos."),
                new Tag()
                        .name("Usuarios")
                        .description("Gestión de usuarios del sistema (solo ADMINISTRADOR)."),
                new Tag()
                        .name("Sistema")
                        .description("Health check y estado operativo del servicio.")
        );
    }

    // ---------------------------------------------------------------------------
    // Components: security scheme + schemas globales reutilizables
    // ---------------------------------------------------------------------------

    @SuppressWarnings("rawtypes")
    private Components buildComponents(String securitySchemeName) {
        return new Components()
                .securitySchemes(Map.of(securitySchemeName, buildSecurityScheme(securitySchemeName)))
                .schemas(Map.of("ApiErrorResponse", new Schema<ApiErrorResponse>()
                        .type("object")
                        .description("Estructura estándar de respuesta de error de la API.")
                        .addProperty("timestamp", new Schema<>().type("string").format("date-time")
                                .description("Fecha y hora en que ocurrió el error."))
                        .addProperty("status", new Schema<>().type("integer")
                                .description("Código de estado HTTP."))
                        .addProperty("error", new Schema<>().type("string")
                                .description("Nombre del error HTTP (ej. Bad Request, Not Found)."))
                        .addProperty("message", new Schema<>().type("string")
                                .description("Mensaje descriptivo del error."))
                        .addProperty("path", new Schema<>().type("string")
                                .description("Ruta del endpoint que generó el error."))
                ));
    }

    private SecurityScheme buildSecurityScheme(String schemeName) {
        return new SecurityScheme()
                .name(schemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        Autenticación mediante JWT Bearer token.
                        Obtén tu token en `POST /api/v1/auth/login` e inclúyelo así:
                        ```
                        Authorization: Bearer <accessToken>
                        ```
                        """);
    }
}