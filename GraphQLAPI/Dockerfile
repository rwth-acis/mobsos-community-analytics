FROM openjdk:8-jdk-alpine

ENV HTTP_PORT=8080
ENV HTTPS_PORT=8443
ENV GRAPHQL_PORT=9011
ENV restAPIURL=https://git.tech4comp.dbis.rwth-aachen.de


ENV dbSchema_las2peer=LAS2PEERMON
ENV dbType_las2peer=MySQL
ENV user_las2peer=root
ENV password_las2peer=password
ENV url_las2peermon=jdbc:mysql://127.0.0.1:3306

ENV webconnector=startWebConnector

ENV TZ=Europe/Berlin

RUN apk add --update bash mysql-client apache-ant tzdata curl && rm -f /var/cache/apk/*

RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer

COPY --chown=las2peer:las2peer . /src
WORKDIR /src

RUN chmod -R a+rwx /src
RUN chmod +x docker-entrypoint.sh
RUN dos2unix docker-entrypoint.sh

RUN dos2unix config.properties

RUN dos2unix etc/ant_configuration/service.properties
# run the rest as unprivileged user
USER las2peer

RUN ant jar startscripts

# EXPOSE 8090
EXPOSE $HTTP_PORT
EXPOSE $HTTPS_PORT
EXPOSE $GRAPHQL_PORT

ENTRYPOINT ["/src/docker-entrypoint.sh"]
