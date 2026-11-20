
# Stage 1: Build Spring Boot Application
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven configuration
COPY pom.xml .

# Download dependencies
RUN mvn -q dependency:go-offline

# Copy source code
COPY src ./src

# Build executable JAR
RUN mvn -q clean package -DskipTests


# Stage 2: Run Spring Boot Application
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy generated JAR
COPY --from=build /app/target/*.jar app.jar

# Application port
EXPOSE 8083

# Start application
ENTRYPOINT ["java", "-jar", "app.jar"]
