FROM adoptopenjdk/openjdk11:jre-11.0.13_8-alpine

EXPOSE 8080

WORKDIR /run

COPY target/Homework-fjd-diploma-0.0.1-SNAPSHOT.jar /run/myarch.jar

CMD ["java", "-jar", "/run/myarch.jar"]