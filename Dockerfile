# The jar is built by Maven before docker build (the Jenkins Package stage,
# or `mvn package -DskipTests` locally), so the image only wraps it and no
# dependencies are downloaded inside Docker.
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/*.jar app.jar
# application.yml sets server.port 8082; the container, Service and
# k8s/app-deployment.yaml all expect 8080.
ENV SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
