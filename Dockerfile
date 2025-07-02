FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Xmx2g", "-jar", "/app/app.jar"]
