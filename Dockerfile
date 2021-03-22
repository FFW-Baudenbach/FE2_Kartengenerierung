FROM openjdk:15-alpine3.12 AS builder
ARG DOCKER_TAG
COPY . application
WORKDIR application
RUN ./gradlew clean build --no-daemon -PRELEASE_VERSION=$DOCKER_TAG
RUN cp build/libs/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract


FROM openjdk:15-alpine3.12
LABEL MAINTAINER="Markus Zellner webmaster@ffw-baudenbach.de"
EXPOSE 8080

RUN apk --no-cache add curl
RUN mkdir -p /maps

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]