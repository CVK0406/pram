# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

# Copy the pom.xml file to download dependencies first (caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/pram-0.0.1-SNAPSHOT.jar app.jar

# Expose server port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
