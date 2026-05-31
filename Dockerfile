# Stage 1: build
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

ARG MAVEN_PROFILE=prod

COPY pom.xml .
RUN mvn dependency:go-offline -P "${MAVEN_PROFILE}"

COPY src ./src

RUN mvn clean package -DskipTests -P "${MAVEN_PROFILE}"

# Stage 2: Execute
FROM amazoncorretto:21

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
