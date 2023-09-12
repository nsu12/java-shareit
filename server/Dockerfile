FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
#RUN apk add tzdata
#ENV TZ="Europe/Moscow"