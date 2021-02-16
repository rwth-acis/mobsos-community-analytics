FROM openjdk:8-jdk-alpine

ENV GRAPHQL_PORT=9011
ENV REST_PORT=9012

ENV user_las2peermon=root
ENV dbSchema_las2peer=LAS2PEERMON
ENV dbType_las2peermon=MySQL

ENV dbType_las2peer=MySQL
ENV user_las2peer=root
ENV password_las2peer=hellome

ENV url_las2peermon=jdbc:mysql://127.0.0.1:3306
ENV junit=false
ENV password_las2peermon=password
ENV dbSchema_las2peermon=las2peermon
ENV TZ=Europe/Berlin


RUN apk add --update bash mysql-client apache-ant tzdata curl && rm -f /var/cache/apk/*

RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer

COPY --chown=las2peer:las2peer . /src
WORKDIR /src

RUN chmod -R a+rwx /src
RUN chmod +x /src/docker-entrypoint.sh
RUN dos2unix docker-entrypoint.sh
RUN dos2unix GraphQLAPI/etc/i5.las2peer.connectors.webConnector.WebConnector.properties
RUN dos2unix GraphQLAPI/config.properties
# run the rest as unprivileged user
USER las2peer

WORKDIR /src/GraphQLAPI
RUN ant jar startscripts

WORKDIR /src/RESTAPI
RUN ant jar startscripts


EXPOSE ${GRAPHQL_PORT}
EXPOSE ${REST_PORT}
ENTRYPOINT ["/src/docker-entrypoint.sh"]
