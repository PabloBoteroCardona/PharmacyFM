# ── Etapa 1: compilación ──────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-23 AS builder
WORKDIR /build

# Descargar dependencias primero (capa cacheada si pom.xml no cambia)
COPY pom.xml .
RUN mvn dependency:go-offline --batch-mode -q

# Copiar fuentes y empaquetar el fat JAR de Spring Boot
COPY src ./src
RUN mvn clean package -DskipTests --batch-mode -q

# ── Etapa 2: imagen de ejecución (solo JRE, sin Maven ni código fuente) ───────
FROM eclipse-temurin:23-jre
WORKDIR /app

COPY --from=builder /build/target/pharmacyfm-1.0.0.jar app.jar

# Cloud Run inyecta PORT; Spring Boot lo lee desde application.properties
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
