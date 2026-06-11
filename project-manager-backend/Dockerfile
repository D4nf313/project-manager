# ── Etapa 1: Build con Maven ──────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar pom.xml primero para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B -Dproject.build.sourceEncoding=UTF-8 -Dproject.reporting.outputEncoding=UTF-8


# ── Etapa 2: Runtime liviano ──────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar el jar generado
COPY --from=builder /app/target/*.jar app.jar

# Puerto que expone el backend
EXPOSE 8080


ENTRYPOINT ["java", "-jar", "app.jar"]
