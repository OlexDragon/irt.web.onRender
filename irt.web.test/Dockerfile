FROM eclipse-temurin:25-jre-alpine
RUN apk --no-cache add curl
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
ENV PORT=10000
EXPOSE 10000
LABEL authors="Oleksandr Potomkin"
