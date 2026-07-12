# ---------- Build stage ----------
# JDK 21 (NO 25): Spring Boot 3.2.4 fija Lombok 1.18.30, que no soporta JDK 25
# (el processor no genera getters/builders -> "cannot find symbol"). El CI
# (setup-java) tambien usa Temurin 21.
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
# server.port=8085 (application.properties). EXPOSE es solo documentacion pero
# antes decia 8086 por error -- corregido a 8085 para que coincida con el
# puerto real y con los probes/Service del chart.
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
