# docker run -d --name AvaIre --restart=unless-stopped -v /srv/avaire/config.yml:/opt/avaire/config.yml -v /srv/avaire/storage:/opt/avaire/storage -v /srv/avaire/plugins:/opt/avaire/plugins --net=host avaire:latest
FROM gradle:jdk10 AS build

USER root

COPY . /app
WORKDIR /app
RUN chmod a+x buildpack-run.sh; \
	./buildpack-run.sh; \
	gradle build --stacktrace

FROM anapsix/alpine-java AS runtime

RUN mkdir -p /opt/avaire
COPY --from=build /app/AvaIre.jar /opt/avaire/AvaIre.jar

RUN adduser --disabled-password --gecos '' avaire; \
    chown avaire:avaire -R /opt/avaire; \
    chmod u+w /opt/avaire; \
    chmod 0755 -R /opt/avaire

VOLUME [ "/opt/avaire/plugins" ]
VOLUME [ "/opt/avaire/storage" ]
VOLUME [ "/opt/avaire/config.yml" ]

WORKDIR /opt/avaire

USER avaire

CMD ["java", "-jar", "/opt/avaire/AvaIre.jar"]
