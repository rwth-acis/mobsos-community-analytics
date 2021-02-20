#!/usr/bin/env bash
set -e

# print all comands to console if DEBUG is set
if [[ ! -z "${DEBUG}" ]]; then
    set -x
fi

# set some helpful variables
export CONFIG_PROPERTY_FILE='config.properties'

#RESTAPI variables
export REST_SERVICE_VERSION=$(awk -F "=" '/service.version/ {print $2}' etc/ant_configuration/service.properties)
export REST_SERVICE_NAME=$(awk -F "=" '/service.name/ {print $2}' etc/ant_configuration/service.properties)
export REST_SERVICE_CLASS=$(awk -F "=" '/service.class/ {print $2}' etc/ant_configuration/service.properties)
export REST_SERVICE=${REST_SERVICE_NAME}.${REST_SERVICE_CLASS}@${REST_SERVICE_VERSION}

echo ${REST_SERVICE}

[[ -z "${REST_SERVICE_PASSPHRASE}" ]] && export REST_SERVICE_PASSPHRASE='rest'

function set_in_service_config {
    sed -i "s?${1}[[:blank:]]*=.*?${1}=${2}?g" ${CONFIG_PROPERTY_FILE}
}
cp $CONFIG_PROPERTY_FILE.sample $CONFIG_PROPERTY_FILE
set_in_service_config db.user_las2peermon ${user_las2peermon}
set_in_service_config db.dbSchema_las2peer ${dbSchema_las2peer}
set_in_service_config db.dbType_las2peermon ${dbType_las2peermon}

set_in_service_config db.dbType_las2peer ${dbType_las2peer}
set_in_service_config db.user_las2peer  ${user_las2peer}
set_in_service_config db.password_las2peer ${password_las2peer}

set_in_service_config db.url_las2peermon ${url_las2peermon}
set_in_service_config junit ${junit}
set_in_service_config db.password_las2peermon ${password_las2peermon}
set_in_service_config db.dbSchema_las2peermon ${dbSchema_las2peermon}

#REST
set -f
LAUNCH_COMMAND='java -cp /src/lib/* i5.las2peer.tools.L2pNodeLauncher -s service -p '"${REST_PORT} ${SERVICE_EXTRA_ARGS}"
echo ${LAUNCH_COMMAND}

#prepare pastry properties
echo external_address = $(curl -s https://ipinfo.io/ip):${REST_PORT} > etc/pastry.properties

# start the service within a las2peer node     
exec ${LAUNCH_COMMAND} uploadStartupDirectory startService\("'""${REST_SERVICE}""'", "'""${REST_SERVICE_PASSPHRASE}""'"\) startWebConnector