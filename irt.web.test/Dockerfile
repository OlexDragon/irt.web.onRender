FROM eclipse-temurin:8-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
ENV PORT=8080
EXPOSE 8080