FROM adoptopenjdk:11-jdk-hotspot AS builder
ARG DOCKER_TAG
COPY . application
WORKDIR application
RUN ./gradlew clean build --no-daemon -PRELEASE_VERSION=$DOCKER_TAG
RUN cp build/libs/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract


FROM adoptopenjdk:11-jre-hotspot
LABEL maintainer="FFW Baudenbach <webmaster@ffw-baudenbach.de>"
EXPOSE 8080
RUN apt-get install curl && apt-get clean
RUN groupadd -g 10001 spring && useradd -u 10000 -g spring -ms /bin/bash spring
RUN mkdir -p /maps && chown -R spring:spring /maps
USER spring:spring
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
