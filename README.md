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

ejecutar el Script de inicialización de la BD
````
database/dbInicialization.sql
````

La aplicación está configurada por defecto para conectarse a:

````
host: localhost
port: 5432
database: parqueadero_db
username: postgres
password: 1234
````

# Configuración en application.properties:

````
spring.datasource.url=jdbc:postgresql://localhost:5432/parqueadero_db
spring.datasource.username=postgres
spring.datasource.password=1234
````

Si tu entorno usa otros valores, debes modificarlos.

# Puerto de la aplicación

La aplicación se ejecuta por defecto en:

````
http://localhost:8090
````

# Documentación de la API (Swagger)

Una vez que el servidor esté corriendo, la documentación interactiva estará disponible en:
 ````
http://localhost:8090/swagger-ui.html
````

Swagger permite:
- probar los endpoints
- ver los modelos de datos
- autenticarse con JWT
- ejecutar las operaciones del API

# Ejecutar la aplicación

Desde la raíz del proyecto:

 ````
mvn spring-boot:run
````

O compilar primero:

````
mvn clean install
````

y luego ejecutar:

````
java -jar target/parking-management-service.jar
````

# Autenticación

La aplicación utiliza JWT (JSON Web Token).

Flujo de autenticación:

Iniciar sesión en:

````
POST /api/v1/auth/login
````
Body ejemplo:
````
{
"username": "admin",
"password": "1234"
}
````

Respuesta ejemplo:
````
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
````
# Uso del token

Para acceder a endpoints protegidos debes enviar el token:
````
Authorization: Bearer <TOKEN>
````
Ejemplo:
````
GET /api/v1/usuarios
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
````
# Usuario administrador inicial

Al iniciar la aplicación por primera vez, si no existen usuarios registrados, el sistema crea automáticamente un usuario administrador:

````
Usuario: 	admin
Contraseña:	1234
Rol:	Administrador
````