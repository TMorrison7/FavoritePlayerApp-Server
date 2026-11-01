FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
ARG SERVICE_MODULE
COPY . .
RUN mvn -B -f pom.xml -pl "${SERVICE_MODULE}" -am package -DskipTests

FROM eclipse-temurin:17-jre
ARG SERVICE_MODULE
ARG SERVICE_ARTIFACT
COPY --from=build "/workspace/${SERVICE_MODULE}/target/${SERVICE_ARTIFACT}-1.0.0.jar" /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
