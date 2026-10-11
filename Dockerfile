# -------------------------------------------------------------
# Stage 1: Build the Spring Boot application
# -------------------------------------------------------------
FROM eclipse-temurin:26-jdk AS builder
WORKDIR /app

# Copy Maven wrapper and descriptor first to cache dependencies
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml

# Ensure correct Unix line endings for mvnw (in case checkout is on Windows) and set permissions
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Resolve dependencies
RUN ./mvnw dependency:go-offline -B || true

# Copy project source code
COPY src src

# Package application into an executable JAR without running tests during build
RUN ./mvnw clean package -DskipTests -B

# -------------------------------------------------------------
# Stage 2: Create lightweight runtime image
# -------------------------------------------------------------
FROM eclipse-temurin:26-jre
WORKDIR /app

# Create a non-privileged user to run the application
RUN groupadd -r spring && useradd -r -g spring spring

# Copy generated executable jar from builder stage
COPY --from=builder /app/target/*.jar app.jar
RUN chown -R spring:spring /app

USER spring

# Render automatically sets the PORT environment variable
ENV PORT=8080
ENV SERVER_TOMCAT_BASEDIR=/tmp
EXPOSE ${PORT}

# Launch Spring Boot with dynamic port binding and memory optimization
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -XX:+ExitOnOutOfMemoryError -jar app.jar"]
