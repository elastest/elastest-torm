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

ET_EDM_MYSQL_HOST=$(containerIp "edm-mysql")
ET_EDM_MYSQL_PORT=3306
ET_ETM_RABBIT_HOST=$(containerIp "etm-rabbitmq")
ET_ETM_RABBIT_PORT=5672
ELASTICSEARCH_IP=$(containerIp "edm-elasticsearch")
LOGSTASH_IP=$(containerIp "etm-logstash")
ET_ESM_API='http://esm:37005/'
EXEC_MODE='Lite'
ET_ESM_SS_DESC_FILES_PATH='esm_services/'
# Execute Integration tests

echo "Starting maven integration tests"

cd ../elastest-torm

mvn -B -Det.edm.mysql.host=${ET_EDM_MYSQL_HOST} -Det.edm.mysql.port=${ET_EDM_MYSQL_PORT} -Det.etm.rabbit.host=${ET_ETM_RABBIT_HOST} -Det.etm.rabbit.port=${ET_ETM_RABBIT_PORT} -Det.edm.elasticsearch.api=http://${ELASTICSEARCH_IP}:9200/ -Dlogstash.host=${LOGSTASH_IP} -Det.esm.api=${ET_ESM_API} -Delastest.incontainer=true -Delastest.execution.mode=${EXEC_MODE} -Det.esm.ss.desc.files.path=${ET_ESM_SS_DESC_FILES_PATH} clean verify

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
