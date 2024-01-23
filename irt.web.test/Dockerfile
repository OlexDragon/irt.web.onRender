FROM eclipse-temurin:8-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
ENV PORT=10000
EXPOSE 10000