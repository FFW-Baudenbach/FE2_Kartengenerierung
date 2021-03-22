FROM openjdk:15-alpine3.12 AS builder
ARG DOCKER_TAG
COPY . application
RUN ls -l && pwd
WORKDIR application
RUN ls -l && pwd
RUN ./gradlew clean build --no-daemon -PRELEASE_VERSION=$DOCKER_TAG
RUN ls -l && pwd
RUN ls -l ./build/libs
COPY ./build/libs/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract


FROM openjdk:15-alpine3.12
RUN apk --no-cache add curl
RUN mkdir -p /maps
EXPOSE 8080

WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]