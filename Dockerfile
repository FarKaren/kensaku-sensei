FROM gradle:7.6.1-jdk17-focal as builder
MAINTAINER Karen Farmanyan <farkar160@gmail.com>

COPY . /app

WORKDIR /app

RUN gradle bootJar

FROM openjdk:17-jdk

EXPOSE 8081/tcp

ENV JAVA_OPTS="-XX:+UseContainerSupport"

COPY --from=builder /app/build/libs/*.jar /app/app.jar

WORKDIR app

ENTRYPOINT ["/bin/sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]