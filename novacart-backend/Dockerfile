# Stage 1: Build the JAR
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /novaCart
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /novaCart
COPY --from=builder /novaCart/target/*.jar novaCart.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "novaCart.jar"]
