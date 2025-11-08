# Etapa 1 - Build do projeto (usa Maven dentro do container)
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests clean package

# Etapa 2 - Imagem final da aplicação
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/langia-api-0.0.1-SNAPSHOT.jar /app/langia-api.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/langia-api.jar"]
