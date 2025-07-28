FROM bellsoft/liberica-openjdk-alpine:21

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
