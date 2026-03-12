# ─────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copiar pom.xml primero para cachear dependencias
COPY pom.xml .

# Descargar dependencias (cacheado si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# ─────────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el JAR generado en el stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto (debe coincidir con server.port en application.properties)
EXPOSE 8090

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]
