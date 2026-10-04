# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/campuscomplaint-1.0.0.jar app.jar

# Fresh demo data is seeded on first boot (users + sample complaints).
# To reset the demo later, restart the service with an empty data dir.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
