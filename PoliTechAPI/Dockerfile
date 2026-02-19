# syntax=docker/dockerfile:1

# ============================================
# Build Stage - Optimized with layer caching
# ============================================
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

# Copy only dependency files first (better caching)
COPY gradle/ gradle/
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY pt-*/build.gradle.kts ./pt-*/

# Download dependencies (cached unless build files change)
RUN gradle dependencies --no-daemon --parallel || true

# Copy source code (separate layer for better caching)
COPY . .

# Build application (skip tests for faster builds, parallel execution)
RUN gradle bootJar -x test --no-daemon --parallel --build-cache

# ============================================
# Runtime Stage - Minimal production image
# ============================================
FROM --platform=linux/amd64 amazoncorretto:21-alpine
WORKDIR /app

# Install runtime dependencies
RUN apk add --no-cache curl ttf-liberation

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copy only the JAR file from build stage
COPY --from=build /app/pt-launcher/build/libs/pt-launcher-*.jar app.jar

EXPOSE 8080 5005

# Optimized JVM settings for containers
ENV JAVA_OPTS="-Xmx1024m -Xms512m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENV TZ Europe/Moscow

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
