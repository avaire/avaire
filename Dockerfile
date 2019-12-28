# Build container

FROM gradle:4.10.2-jdk11-slim AS build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

# Run container

FROM openjdk:11-jre-slim AS runtime

WORKDIR /opt/avaire/

RUN adduser --disabled-password --gecos '' avaire; \
    chown avaire:avaire -R /opt/avaire; \
    chmod u+w /opt/avaire; \
    chmod 0755 -R /opt/avaire

USER avaire

COPY --from=build /home/gradle/src/AvaIre.jar /bin/

CMD ["java","-jar","/bin/AvaIre.jar","-env","--use-plugin-index"]
