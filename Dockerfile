FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package dependency:copy-dependencies \
    -DincludeScope=runtime \
    -DskipTests


FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/classes ./classes
COPY --from=build /app/target/dependency ./dependency

CMD ["java", "-cp", "classes:dependency/*", "WebhookServidor"]