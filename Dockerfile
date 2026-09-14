FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests
RUN mvn dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=/app/dependency


FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/classes ./classes
COPY --from=build /app/dependency ./dependency

CMD ["java", "-cp", "classes:dependency/*", "WebhookServidor"]