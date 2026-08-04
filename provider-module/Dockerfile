FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/provider-module-1.0.0.jar app.jar
EXPOSE 8081 50051 22222
ENTRYPOINT ["java", "-jar", "app.jar"]
