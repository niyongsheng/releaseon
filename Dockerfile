FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create storage directory
RUN mkdir -p /app/storage

# Copy built JAR
COPY build/libs/releaseon.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
