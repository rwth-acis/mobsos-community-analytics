FROM openjdk:8-jdk-alpine

ENV HTTP_PORT=8080
ENV HTTPS_PORT=8443
ENV REST_PORT=9012

ENV url_las2peer=jdbc:mysql://mobsos-mysql.sbf-dev
ENV dbSchema_las2peer=LAS2PEERMON
ENV dbType_las2peer=MySQL
ENV user_las2peer=root
ENV password_las2peer=password

ENV TZ=Europe/Berlin

RUN apk add --update bash mysql-client apache-ant tzdata curl && rm -f /var/cache/apk/*

RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer

COPY --chown=las2peer:las2peer . /src
WORKDIR /src

RUN chmod -R a+rwx /src
RUN chmod +x /src/docker-entrypoint.sh
RUN dos2unix docker-entrypoint.sh

RUN dos2unix config.properties

RUN dos2unix etc/ant_configuration/service.properties

USER las2peer

RUN ant jar startscripts


# EXPOSE 8000
EXPOSE $HTTP_PORT
EXPOSE $HTTPS_PORT
EXPOSE $REST_PORT

ENTRYPOINT ["/src/docker-entrypoint.sh"]
