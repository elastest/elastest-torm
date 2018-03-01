#!/bin/bash
#Script to generate the file with the differences between the JPA Model and the current DB
#You need to pass the host ip and SO(win/lnx) as input parameters

#Function to get the container ip
function containerIp () {
    ip=$(docker inspect --format=\"{{.NetworkSettings.Networks.bridge.IPAddress}}\" $1)
    echo $( echo $ip | cut -f2 -d'"' )
}

#Start the Mysql Db
echo "Start Mysql DB service."
docker run -d --name=aux-lb-db -p 3306:3306 -e MYSQL_PASSWORD=elastest -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=elastest -e MYSQL_USER=elastest elastest/edm-mysql

#Wait for DB is ready
if [ $2 = 'lnx' ];
then
    echo "Executing on linux"    
    while ! nc -z localhost 3306 ; do
        echo "MySQL server in not ready in address 'mysql' and port 3306"
        sleep 2
    done
elif [ $2 = 'win' ];
then
    echo "Executing on win"
    sleep 5
else
    echo "You must indicate the operating system"
    #Remove DB
    docker rm -f aux-lb-db
    exit 1
fi

# Change to the elastest-torm folder
cd ../../elastest-torm/

# Create the ETM database
echo "Create the ETM database"
echo "Database host:" $1
#sed -i -- 's/${mysql.host}/'$1'/g' ../elastest-torm/src/main/resources/liquibase.properties
mvn liquibase:update -Dmysql.host=$1

changeDate="nodate"
if [ $2 = 'lnx' ];
then
    changeDate = date '+%Y%m%d%H%M%S'
elif [ $2 = 'win' ];
then
    changeDate=123456
else
    changeDate=nodate
fi

echo $changeDate
# Execute diff command
mvn liquibase:diff -Dmysql.host=$1 -Dchange.date=$changeDate

#Remove DB
docker rm -f aux-lb-db

exit 0