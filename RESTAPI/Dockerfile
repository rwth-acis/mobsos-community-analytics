FROM openjdk:8-jdk-alpine

ENV LAS2PEER_PORT=9011

ENV db.user_las2peermon=root
ENV db.dbSchema_las2peer=LAS2PEERMON
ENV db.dbType_las2peermon=MySQL

ENV db.dbType_las2peer=MySQL
ENV db.user_las2peer=root
ENV db.password_las2peer=hellome

ENV db.url_las2peermon=jdbc:mysql://127.0.0.1:3306
ENV junit=false
ENV db.password_las2peermon=password
ENV db.dbSchema_las2peermon=las2peermon

RUN apk add --update bash mysql-client apache-ant tzdata curl && rm -f /var/cache/apk/*
ENV TZ=Europe/Berlin

RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer

COPY --chown=las2peer:las2peer . /src
WORKDIR /src

RUN chmod -R a+rwx /src
RUN chmod +x /src/docker-entrypoint.sh
RUN dos2unix docker-entrypoint.sh
RUN dos2unix etc/i5.las2peer.connectors.webConnector.WebConnector.properties
RUN dos2unix config.properties
# run the rest as unprivileged user
USER las2peer
RUN ant jar startscripts

EXPOSE $LAS2PEER_PORT
ENTRYPOINT ["/src/docker-entrypoint.sh"]
