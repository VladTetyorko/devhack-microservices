FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the JAR file
COPY build/libs/*.jar app.jar

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=default

# Expose the port
EXPOSE 8083

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]