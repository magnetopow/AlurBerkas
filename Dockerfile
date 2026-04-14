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
# Stage 2: Extract JRE (avoid VOLUME from temurin image)
# ============================================
FROM eclipse-temurin:24-jre AS jre-source

# ============================================
# Stage 3: Run the application (clean base, no VOLUME)
# ============================================
FROM debian:bookworm-slim

LABEL maintainer="magnetopow"
LABEL description="AlurBerkas - Aplikasi Workflow Alur Berkas Pengukuran Tanah BPN ATR"

# Copy JRE from temurin image (avoids inheriting VOLUME /tmp)
COPY --from=jre-source /opt/java/openjdk /opt/java/openjdk
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /app

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Create directories for persistent data
RUN mkdir -p /app/data /app/uploads && chown -R appuser:appuser /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=appuser:appuser /app/target/alurberkas-1.0.0.jar app.jar

# Switch to non-root user
USER appuser

# Expose port (Railway will override via PORT env var)
EXPOSE 8080

# JVM optimizations for containers
# Railway provides PORT env var automatically
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
