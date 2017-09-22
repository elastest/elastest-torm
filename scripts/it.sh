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

# Clean environment

echo "Remove etm* containers"

docker ps -a
docker ps -a | grep "etm" | awk '{print $1}' | xargs docker rm -f
docker ps -a

# Start docker-compose

projectName="etm$BUILD_NUMBER"

export COMPOSE_PROJECT_NAME=$projectName

echo $COMPOSE_PROJECT_NAME

docker-compose -f ../docker/docker-compose-complementary.yml up -d
    
# Connect test container to docker-compose network

containerId=$(cat /proc/self/cgroup | grep "docker" | sed s/\\//\\n/g | tail -1)

echo "containerId = ${containerId}"

docker network connect ${projectName}_elastest ${containerId}

# Get services IPs

MYSQL_IP=$(containerIp "edm-mysql")
ET_ETM_RABBIT_HOST=$(containerIp "etm-rabbitmq")
ELASTICSEARCH_IP=$(containerIp "edm-elasticsearch")
LOGSTASH_IP=$(containerIp "etm-logstash")
ET_ESM_API=''
EXEC_MODE='Lite'
ET_ESM_SS_DESC_FILES_PATH='esm_services/'
# Execute Integration tests

echo "Starting maven integration tests"

cd ../elastest-torm

mvn -B -Dspring.datasource.url=jdbc:mysql://${MYSQL_IP}:3306/elastest?useSSL=false -Det.etm.rabbit.host=${ET_ETM_RABBIT_HOST} -Det.etm.rabbit.port=5672 -Det.edm.elasticsearch.api=http://${ELASTICSEARCH_IP}:9200/ -Dlogstash.host=${LOGSTASH_IP} -Det.esm.api=${ET_ESM_API} -Delastest.incontainer=true -Delastest.execution.mode=${EXEC_MODE} clean verify

mvnExit=$?

# Generate test coverage report
mvn -B jacoco:report

# Upload coverage report to codecov.io
bash <(curl -s https://codecov.io/bash) -t fa48b15c-ceb8-409d-996f-8f34d53addd2

# Clean up environment

cd ../docker
docker-compose -f docker-compose-complementary.yml logs
docker network disconnect "${projectName}"_elastest ${containerId}

docker-compose -f docker-compose-complementary.yml down

# Exit code of mvn command

exit $mvnExit
