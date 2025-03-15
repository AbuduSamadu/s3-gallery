# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21  AS build
WORKDIR /app
COPY . .
RUN mvn clean package

# Stage 2: Create the runtime image
FROM openjdk:21-jdk-slim
VOLUME /tmp
COPY --from=build /app/target/spring-boot-s3-gallery-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
USER nobody