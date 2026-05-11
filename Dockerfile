# ─── Stage 1: Build con Maven ───────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar pom.xml primero para aprovechar cache de capas en descarga de deps
COPY pom.xml ./
RUN mvn dependency:go-offline --no-transfer-progress

# Copiar fuentes y construir
COPY src ./src
RUN mvn clean package -DskipTests --no-transfer-progress

# ─── Stage 2: Runtime JRE mínimo ────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runner

WORKDIR /app

# Copiar jar desde stage de build
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
