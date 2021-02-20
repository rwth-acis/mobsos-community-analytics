FROM openjdk:8-jdk-alpine

ENV GRAPHQL_PORT=9011

# ENV user_las2peermon=root
# ENV dbSchema_las2peer=LAS2PEERMON
# ENV dbType_las2peermon=MySQL

# ENV dbType_las2peer=MySQL
# ENV user_las2peer=root
# ENV password_las2peer=hellome

# ENV url_las2peermon=jdbc:mysql://host.docker.internal:3306
# ENV junit=false
# ENV password_las2peermon=password
# ENV dbSchema_las2peermon=las2peermon
# ENV TZ=Europe/Berlin


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

EXPOSE 8090

ENTRYPOINT ["/src/docker-entrypoint.sh"]
