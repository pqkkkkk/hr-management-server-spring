# Stage 1: Build application with Maven
FROM maven:3.8.6-openjdk-18-slim AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image
FROM openjdk:26-ea-17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/*.jar hr-management-server.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "hr-management-server.jar"]