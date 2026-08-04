FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/consumer-module-1.0.0.jar app.jar
EXPOSE 8080 33333
ENTRYPOINT ["java", "-jar", "app.jar"]
