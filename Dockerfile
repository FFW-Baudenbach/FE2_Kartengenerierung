FROM openjdk:15-alpine3.12 AS build
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew clean build -PRELEASE_VERSION=$DOCKER_TAG

FROM openjdk:15-alpine3.12
# App is running on default port
EXPOSE 8080
# Possibility to add docker-autoheal (https://github.com/willfarrell/docker-autoheal)
LABEL autoheal=true
# Install curl for healthchecks
RUN apk --no-cache add curl
# Create directory to store images
RUN mkdir -p /maps
# Copy jar
COPY --from=build /home/gradle/src/build/libs/*.jar /app.jar
# Set entrypoint
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar" , "/app.jar"]