FROM ubuntu:latest
LABEL authors="moonsy"

ENTRYPOINT ["top", "-b"]

# ---------- build ----------
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean bootJar -x test

RUN JAR_FILE=$(find build/libs -name "*.jar" ! -name "*-plain.jar" | head -n 1) \
    && cp "$JAR_FILE" app.jar


# ---------- run ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/app.jar app.jar

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]