#!/bin/sh
while ! nc -z edm-mysql 3306 ; do
    echo "MySQL server in not ready in address 'mysql' and port 3306"
    sleep 2
done

exec java -jar elastest-torm.jar
