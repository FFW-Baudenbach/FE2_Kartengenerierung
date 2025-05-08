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

COPY build/libs/FE2_Kartengenerierung.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]