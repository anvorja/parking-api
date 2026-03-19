# parking-management-service

# Requisitos

Para ejecutar el proyecto necesitas tener instalado:
- Java 17
- Maven 3.9+
- PostgreSQL 17+

# Configuración de base de datos

Crear una base de datos en PostgreSQL:

```
CREATE DATABASE parqueadero_db;
```

Ejecutar el script de inicialización de la BD:
```
database/dbInicialization.sql
```
ó
```
PGPASSWORD=la_password psql -h el_host -p puerto -U postgres -d nombre_db -f database/dbInicialization.sql
```

La aplicación está configurada por defecto para conectarse a:

```
host: localhost
port: 5432
database: parqueadero_db
username: postgres
password: 1234
```

# Variables de entorno (.env)

La aplicación usa un archivo `.env` en la raíz del proyecto para gestionar configuración sensible.

`.env` en la raíz del proyecto con la siguiente estructura:

```
DB_URL=jdbc:postgresql://localhost:5432/parqueadero_db
DB_USERNAME=postgres
DB_PASSWORD=tu_password_aqui
JWT_SECRET=tu_clave_secreta_en_base64_aqui
JWT_EXPIRATION_MS=
JWT_REFRESH_EXPIRATION_MS=
FRONT_URL=https://frontend.netlify.app
```

# Configuración en application.properties

Los valores se inyectan automáticamente desde el `.env`.

```
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
security.jwt.secret=${JWT_SECRET}
security.jwt.expiration-ms=${JWT_EXPIRATION_MS}
app.cors.allowed-origins=${FRONT_URL}
```

# Puerto de la aplicación

La aplicación se ejecuta por defecto en:

```
http://localhost:8090
```

# Health Check

Al acceder a la raíz de la aplicación, el sistema redirige automáticamente al endpoint de estado:

```
GET http://localhost:8090/
→ redirige a → http://localhost:8090/health
```

Respuesta de ejemplo:

```json
{
  "status": "UP",
  "service": "parking-management-service",
  "timestamp": "12-03-2026 -- 01:19:03"
}
```

Este endpoint es público y no requiere autenticación.

# Documentación de la API (Swagger)

Una vez que el servidor esté corriendo, la documentación interactiva estará disponible en:

```
http://localhost:8090/swagger-ui.html
```

Swagger permite:
- probar los endpoints
- ver los modelos de datos
- autenticarse con JWT
- ejecutar las operaciones del API

# Ejecutar la aplicación

Desde la raíz del proyecto:

```
mvn spring-boot:run
```

O compilar primero:

```
mvn clean install
```

y luego ejecutar:

```
java -jar target/parking-management-service.jar
```

# Autenticación

La aplicación utiliza JWT (JSON Web Token).

Flujo de autenticación:

Iniciar sesión en:

```
POST /api/v1/auth/login
```

Body ejemplo:
```json
{
  "username": "admin",
  "password": "1234"
}
```

Respuesta ejemplo:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "nombreCompleto": "admin",
    "nombreUsuario": "admin",
    "rol": "Administrador"
  }
}
```

# Uso del token

Para acceder a endpoints protegidos debes enviar el token:

```
Authorization: Bearer <TOKEN>
```

Ejemplo:
```
GET /api/v1/usuarios
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

# Usuario administrador inicial

Al iniciar la aplicación por primera vez, si no existen usuarios registrados, el sistema crea automáticamente un usuario administrador:

```
Usuario:    admin
Contraseña: 1234
Rol:        Administrador
```

# CI/CD — GitHub Actions

El flujo de despliegue está definido en `.github/workflows/deploy.yml`  
Este workflow no se ejecuta desde este repositorio directamente.  
El despliegue ocurre desde un repositorio fork que se mantiene sincronizado con este proyecto.


### Flujo real de despliegue:

Este repositorio es el repositorio fuente del proyecto.

Existe otro repositorio fork que se sincroniza periódicamente con este.

En ese repositorio fork sí existe la rama deploy.

Cuando se hace push a la rama deploy en el fork, se ejecuta el workflow deploy.yml.

El workflow realiza:

- Build del proyecto con Maven
- Construcción de la imagen Docker
- Push a DockerHub (anborja/parking-api)
- Trigger de redeploy automático en Render


No eliminar ni modificar .github/workflows/deploy.yml