FROM adoptopenjdk:11-jdk-hotspot-focal AS builder
WORKDIR application
COPY build/libs/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract


FROM adoptopenjdk:11-jre-hotspot-focal
LABEL maintainer="FFW Baudenbach <webmaster@ffw-baudenbach.de>"
EXPOSE 8080
RUN apt-get install curl && apt-get clean
RUN mkdir -p /maps && chown -R spring:spring /maps
WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
