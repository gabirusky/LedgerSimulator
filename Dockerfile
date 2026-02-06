# ================================
# Stage 1: Build
# ================================
# Uses Maven with JDK for building the application
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Copy only the POM file first for dependency caching
# This layer is cached unless pom.xml changes
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests - they should run in CI)
RUN mvn clean package -DskipTests -B

# ================================
# Stage 2: Runtime
# ================================
# Uses slim JRE image for minimal footprint
FROM eclipse-temurin:21-jre-alpine AS runtime

# Add labels for container metadata
LABEL maintainer="Fintech Ledger Team"
LABEL version="1.0.0"
LABEL description="Fintech Ledger Simulator - Double-Entry Bookkeeping Service"

# Create non-root user for security
RUN addgroup -g 1001 -S ledger && \
    adduser -u 1001 -S ledger -G ledger

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R ledger:ledger /app

# Switch to non-root user
USER ledger

# JVM Configuration for Container Environment
# - UseContainerSupport: Enables container-aware memory detection
# - MaxRAMPercentage: Uses 75% of container memory limit for heap
# - InitialRAMPercentage: Start with 50% of max for faster warmup
# - +UseG1GC: G1 garbage collector for better latency
# - +UseStringDeduplication: Reduces memory for duplicate strings
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom"

# Expose application port
EXPOSE 8080

# Health check configuration
# - Checks actuator health endpoint every 30 seconds
# - Allows 60 seconds for startup before checking
# - Retries 3 times before marking unhealthy
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
