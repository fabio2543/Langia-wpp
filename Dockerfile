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

# Variáveis padrão (podem ser sobrescritas no docker-compose)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/langia \
    SPRING_DATASOURCE_USERNAME=langia \
    SPRING_DATASOURCE_PASSWORD=langia \
    SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver \
    SPRING_JPA_OPEN_IN_VIEW=false \
    SPRING_SQL_INIT_MODE=never \
    WHATSAPP_VERIFY_TOKEN=dev-verify \
    WHATSAPP_APP_SECRET=dev-secret

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/langia-api.jar"]
