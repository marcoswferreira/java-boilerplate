# ============================================================
# Stage 1: Build
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy Maven wrapper and POMs first (layer caching)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY core/pom.xml core/
COPY application/pom.xml application/
COPY infrastructure/pom.xml infrastructure/
COPY web/pom.xml web/
COPY bootstrap/pom.xml bootstrap/

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -q

# Copy source code
COPY core/src core/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src
COPY web/src web/src
COPY bootstrap/src bootstrap/src

# Build — skip tests (tests run in CI, not in Docker build)
RUN ./mvnw package -DskipTests -pl bootstrap -am -q

# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy artifact
COPY --from=builder /build/bootstrap/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=prod"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
