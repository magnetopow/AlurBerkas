# ============================================
# Stage 1: Build the application
# ============================================
FROM eclipse-temurin:24-jdk AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first (for Docker layer caching)
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Download dependencies (cached if pom.xml doesn't change)
RUN chmod +x mvnw && ./mvnw dependency:resolve -B

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ============================================
# Stage 2: Run the application
# ============================================
FROM eclipse-temurin:24-jre

LABEL maintainer="magnetopow"
LABEL description="AlurBerkas - Aplikasi Workflow Alur Berkas Pengukuran Tanah BPN ATR"

WORKDIR /app

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Create directories for persistent data
RUN mkdir -p /app/data /app/uploads && chown -R appuser:appuser /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=appuser:appuser /app/target/alurberkas-1.0.0.jar app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/login || exit 1

# Volumes for persistent data
VOLUME ["/app/data", "/app/uploads"]

# JVM optimizations for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
