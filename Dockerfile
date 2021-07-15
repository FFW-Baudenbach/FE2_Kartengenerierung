FROM adoptopenjdk:16-jdk-hotspot-focal AS builder
WORKDIR application
COPY build/libs/*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract


FROM adoptopenjdk:16-jre-hotspot-focal
LABEL maintainer="FFW Baudenbach <webmaster@ffw-baudenbach.de>"
EXPOSE 8080
RUN mkdir -p /maps
WORKDIR application
ENV TZ=Europe/Berlin
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
