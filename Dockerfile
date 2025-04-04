FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace
RUN apt-get update && apt-get install -y maven

COPY pom.xml .
COPY src src


RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]