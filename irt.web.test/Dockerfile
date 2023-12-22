FROM eclipse-temurin:17-jdk-jammy
 
WORKDIR /app

COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn
COPY mvn/ ./.mvn

RUN ./mvnw dependency:resolve

COPY src ./src
CMD ["./mvnw", "spring-boot:run"]