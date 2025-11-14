# syntax=docker/dockerfile:1

FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle clean build --no-daemon -x test

FROM amazoncorretto:21-alpine
WORKDIR /app

# Устанавливаем curl для health checks
RUN apk add --no-cache curl

# Создаем пользователя без groupadd (Alpine way)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Копируем JAR файл
COPY --from=build /app/pt-launcher/build/libs/pt-launcher-*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]