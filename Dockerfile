FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY build/libs/FE2_Kartengenerierung.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract --destination extracted

FROM eclipse-temurin:21-jre
LABEL maintainer="FFW Baudenbach <webmaster@ffw-baudenbach.de>"
EXPOSE 8080

# Set timezone
ARG DEBIAN_FRONTEND=noninteractive
ENV TZ=Europe/Berlin
RUN apt-get update && apt-get install --no-install-recommends -y tzdata && apt-get clean && rm -rf /var/lib/apt/lists/*
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create and own directory
RUN mkdir /app && chown -R 1000:1000 /app
USER 1000
WORKDIR /app

# Copy application from builder stage
COPY --chown=1000:1000 --from=builder /app/extracted/dependencies/ ./
COPY --chown=1000:1000 --from=builder /app/extracted/spring-boot-loader/ ./
COPY --chown=1000:1000 --from=builder /app/extracted/snapshot-dependencies/ ./
COPY --chown=1000:1000 --from=builder /app/extracted/application/ ./

# Set entrypoint
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]