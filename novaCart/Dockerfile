FROM eclipse-temurin:21-jdk-alpine
WORKDIR /novaCart
COPY target/*.jar novaCart.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "novaCart.jar"]