#!/bin/bash

# Execute inside a container created with command:
# cd elastest-torm
# sudo docker run -it --rm -v $PWD:/data -v /var/run/docker.sock:/var/run/docker.sock:rw elastest/ci-docker-compose-siblings
# cd /data/scripts
# ./it.sh

function containerIp () {
    ip=$(docker inspect --format=\"{{.NetworkSettings.Networks."$COMPOSE_PROJECT_NAME"_elastest.IPAddress}}\" "$COMPOSE_PROJECT_NAME"_$1_1)
    echo $( echo $ip | cut -f2 -d'"' )
}

# check open descriptors
sysctl -a | grep max_map

# Clean environment

echo "Remove etm* containers"

docker ps -a
docker ps -a | grep "etm" | awk '{print $1}' | xargs docker rm -f
docker ps -a

# Start docker-compose

projectName="etm$BUILD_NUMBER"

export COMPOSE_PROJECT_NAME=$projectName

echo $COMPOSE_PROJECT_NAME

docker pull elastest/esm

docker-compose -f ../docker/docker-compose-complementary.yml -f ../docker/docker-compose-testlink.yml -p $projectName up -d
    
# Connect test container to docker-compose network

containerId=$(cat /proc/self/cgroup | grep "docker" | sed s/\\//\\n/g | tail -1)

echo "containerId = ${containerId}"

docker network connect ${projectName}_elastest ${containerId}

# Get services IPs

ET_EDM_MYSQL_HOST=$(containerIp "edm-mysql")
ET_EDM_MYSQL_PORT=3306
ET_ETM_RABBIT_HOST=$(containerIp "etm-rabbitmq")
ET_ETM_RABBIT_PORT=5672
ELASTICSEARCH_IP=$(containerIp "edm-elasticsearch")
LOGSTASH_IP=$(containerIp "etm-logstash")
ET_ESM_HOST=$(containerIp "esm")
EXEC_MODE='normal'
ET_ESM_SS_DESC_FILES_PATH='esm_services/'
ET_ETM_INCONTAINER=false
ELASTEST_DOCKER_NETWORK=${projectName}_elastest
ET_IN_PROD=true
ET_PROXY_PORT=37000
ET_ETM_LOGSTASH_CONTAINER_NAME=${projectName}_etm-logstash_1
# TestLink Container Name
ET_ETM_TESTLINK_HOST=$(docker ps | awk '{print $NF}' | grep ".*etm-testlink.*")

ET_ETM_LSTCP_PORT=37500
ET_ETM_LSHTTP_PORT=37501
ET_ETM_LSBEATS_PORT=37502




# Execute Integration tests

echo "Starting maven integration tests"

cd ../elastest-torm

docker ps -a

mvn -Det.edm.mysql.host=${ET_EDM_MYSQL_HOST} -Det.edm.mysql.port=${ET_EDM_MYSQL_PORT} -Det.etm.rabbit.host=${ET_ETM_RABBIT_HOST} -Det.etm.rabbit.port=${ET_ETM_RABBIT_PORT} -Det.edm.elasticsearch.api=http://${ELASTICSEARCH_IP}:9200/ -Dlogstash.host=${LOGSTASH_IP} -Det.esm.api=http://${ET_ESM_HOST}:37005/ -Delastest.execution.mode=${EXEC_MODE} -Det.esm.ss.desc.files.path=${ET_ESM_SS_DESC_FILES_PATH} -Det.etm.incontainer=${ET_ETM_INCONTAINER} -Delastest.docker.network=${ELASTEST_DOCKER_NETWORK} -Det.in.prod=${ET_IN_PROD} -Det.proxy.port=${ET_PROXY_PORT} -Det.etm.logstash.container.name=${ET_ETM_LOGSTASH_CONTAINER_NAME} -Det.etm.testlink.host=${ET_ETM_TESTLINK_HOST} -Det.etm.lstcp.port=${ET_ETM_LSTCP_PORT} -Det.etm.lshttp.port=${ET_ETM_LSHTTP_PORT} -Det.etm.lsbeats.port=${ET_ETM_LSBEATS_PORT} package

mvnExit=$?

# Clean up environment

cd ../docker
docker-compose -f docker-compose-complementary.yml -f ../docker/docker-compose-testlink.yml logs
docker network disconnect "${projectName}"_elastest ${containerId}

docker-compose -f docker-compose-complementary.yml -f ../docker/docker-compose-testlink.yml down

# Exit code of mvn command

exit $mvnExit
