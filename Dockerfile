FROM gradle:jdk10-slim AS build

USER root

WORKDIR /app

COPY . .

RUN gradle build --stacktrace

FROM openjdk:10-jdk-slim AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/AvaIre.jar .

VOLUME ["/app/plugins", "/app/storage"]

CMD ["java", "-jar", "AvaIre.jar"]
