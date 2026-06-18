# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml aur source code dependencies download karne ke liye
COPY pom.xml .
RUN mnv dependency:go-offline -B

# Copy source code aur build package generated karo (Tests skip kar rahe hain jaldbaazi me deployment ke liye)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the lightweight execution image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Build stage se generated JAR file ko copy karna
COPY --from=build /app/target/*.jar app.jar

# Application ka default port expose karna
EXPOSE 8080

# Application run karne ki command
ENTRYPOINT ["java", "-jar", "app.jar"]