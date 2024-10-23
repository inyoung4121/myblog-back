# React 빌드 단계
FROM node:20.18.0 AS build-react
WORKDIR /app/frontend
COPY src/main/front/package*.json ./
RUN npm install
COPY src/main/front/ ./
RUN npm run build

# Spring Boot 빌드 단계
FROM gradle:8.5-jdk17 AS build-spring
WORKDIR /app
COPY . .
COPY --from=build-react /app/frontend/build ./src/main/resources/static
RUN gradle build --no-daemon -x test

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build-spring /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]