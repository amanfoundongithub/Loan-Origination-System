FROM gradle:9.5-jdk21 AS builder
WORKDIR /app

COPY . .

RUN ./gradlew bootJar -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8500

ENTRYPOINT ["java", "-jar", "app.jar"]