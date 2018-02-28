#!/bin/bash
#Script to test the application of a changelog on CI

function containerIp () {
    ip=$(docker inspect --format=\"{{.NetworkSettings.Networks.bridge.IPAddress}}\" $1)
    echo $( echo $ip | cut -f2 -d'"' )
}

#Set the container name for the Mysql DB
mysqlContainerName="aux-lb-db$BUILD_NUMBER"
echo $mysqlContainerName
#Start the Mysql Db
echo "Start Mysql DB service."
docker run -d --name=$mysqlContainerName -v /home/ubuntu/tmp/git/elastest-torm/scripts/db:/dump -e MYSQL_PASSWORD=elastest -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=elastest -e MYSQL_USER=elastest elastest/edm-mysql

mysqlServiceIp=$(containerIp $mysqlContainerName)
#Wait for DB is ready on linux
while ! nc -z $mysqlServiceIp 3306 ; do
    echo "MySQL server in not ready in address '$mysqlServiceIp' and port 3306"
    sleep 2
done

# Change to the elastest-torm folder
cd ../../elastest-torm/
# Create the ETM database
echo "Create the ETM database"
mvn liquibase:update -Dmysql.host=$mysqlServiceIp

mvnExit=$?
if [ $mvnExit -gt 0 ];
then
        docker rm -f $mysqlContainerName
        exit $mvnExit
fi

# Load the DB from a dump file
echo "Loading database"
docker exec -it $mysqlContainerName sh -c 'mysql ETM < /dump/devdump1.sql'

# Apply updates
echo "Apply updates"
mvn liquibase:update -Dmysql.host=$mysqlServiceIp -Dinitial.changelog=liquibase-changelog-ci.xml

mvnExit=$?

#Remove DB
docker rm -f $mysqlContainerName
exit $mvnExit
