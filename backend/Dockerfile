# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 as builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /app/target/ProyectoTutoriasBackend-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/dashboard/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
