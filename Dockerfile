FROM gradle:jdk10-slim AS build

USER root

WORKDIR /app

COPY . .

RUN gradle build --stacktrace

FROM openjdk:8-alpine AS runtime

WORKDIR /app

COPY --from=build /app/build/resources/main/config.yml .
COPY --from=build /app/build/libs/AvaIre.jar .

VOLUME ["/app/plugins", "/app/storage", "/app/config.yml"]

CMD ["java", "-jar", "AvaIre.jar"]
