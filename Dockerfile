# ─────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# ─────────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el WAR (el pom.xml tiene <packaging>war</packaging>)
COPY --from=builder /app/target/parking-management-service-0.0.1-SNAPSHOT.war app.war
 
EXPOSE 8090
 
ENTRYPOINT ["java", "-jar", "app.war"]
