# Stage 1: Build JAR using official Maven and Temurin JDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml ./
COPY src src

# Build package skipping tests (tests already passed during verification)
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Production JRE Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
VOLUME /tmp

# Security: Create non-root user
RUN addgroup -S atirath && adduser -S atirath -G atirath
USER atirath:atirath

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
