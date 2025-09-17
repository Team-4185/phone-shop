# Stage 1: build
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Execute
# Використовуємо офіційний образ OpenJDK
FROM openjdk:21

WORKDIR /app

# Копіюємо jar файл нашого додатку в контейнер
COPY --from=builder /app/target/*.jar app.jar

# Відкриваємо порт 8080
EXPOSE 8080

# Команда для запуску додатку
CMD ["java", "-jar", "app.jar"]