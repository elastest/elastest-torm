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

docker-compose -f ../docker-compose-complementary.yml up -d
    
# Connect test container to docker-compose network

containerId=$(cat /proc/self/cgroup | grep "docker" | sed s/\\//\\n/g | tail -1)

echo "containerId = ${containerId}"

docker network connect ${projectName}_elastest ${containerId}

# Get services IPs

MYSQL_IP=$(containerIp "mysql")
RABBIT_IP=$(containerIp "rabbit-MQ")
ELASTICSEARCH_IP=$(containerIp "elasticsearch")
LOGSTASH_IP=$(containerIp "logstash")

# Execute Integration tests

echo "Starting maven integration tests"

cd ../elastest-torm

mvn -B -Dspring.datasource.url=jdbc:mysql://${MYSQL_IP}:3306/elastest?useSSL=false -Dspring.rabbitmq.host=${RABBIT_IP} -Delastest.elasticsearch.host=http://${ELASTICSEARCH_IP}:9200/ -Dlogstash.host=${LOGSTASH_IP} -Delastest.incontainer=true clean verify

mvnExit=$?

# Generate test coverage report
mvn -B jacoco:report

# Upload coverage report to codecov.io
bash <(curl -s https://codecov.io/bash) -t fa48b15c-ceb8-409d-996f-8f34d53addd2

# Clean up environment

cd ..

docker network disconnect "${projectName}"_elastest ${containerId}

docker-compose -f docker-compose-ci2.yml down

# Exit code of mvn command

exit $mvnExit
