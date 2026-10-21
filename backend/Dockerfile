# Render has no native Java runtime, so the backend ships as a Docker image.
# Multi-stage: build with Maven, run with a slim JRE.

# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
# Cache dependencies as their own layer; tolerate rare plugin-resolution hiccups.
RUN mvn -B -q --no-transfer-progress dependency:go-offline || true
COPY src ./src
RUN mvn -B -q --no-transfer-progress clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -m -u 1001 appuser
COPY --from=build /src/target/attend-0.1.0.jar /app/app.jar
# Sized for Render's 512 MB free instance; override with JAVA_OPTS in the dashboard.
ENV JAVA_OPTS="-Xmx320m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC"
USER appuser
EXPOSE 8080
# Render injects $PORT. -D beats server.port=8080 from application.properties.
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
