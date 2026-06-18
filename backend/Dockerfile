FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon
COPY src ./src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
RUN apt-get update && apt-get install -y ffmpeg curl python3-pip && \
    pip3 install --upgrade yt-dlp && \
    rm -rf /var/lib/apt/lists/*
RUN adduser --system --no-create-home appuser
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
