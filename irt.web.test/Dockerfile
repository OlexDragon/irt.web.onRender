#
# Build stage
#
FROM maven:3.8.2-jdk-8 AS build
COPY . .
RUN mvn clean package -Pprod -DskipTests

#
# Package stage
#
FROM openjdk:8-jdk-slim
COPY --from=build /target/irt.test-1.1.jar test.jar
# ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","test.jar"]