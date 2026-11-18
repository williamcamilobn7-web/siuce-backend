# Etapa 1: Compilación con Maven y Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY colegio-disciplina/pom.xml .
COPY colegio-disciplina/src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen ligera de ejecución con Java 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
