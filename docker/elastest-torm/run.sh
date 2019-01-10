#!/bin/sh

echo "Timezone:" $HOST_TIMEZONE
cp /usr/share/zoneinfo/$HOST_TIMEZONE /etc/localtime

echo $HOST_TIMEZONE >  /etc/timezone

while ! nc -z edm-mysql 3306 ; do
    echo "MySQL server is not ready in address 'mysql' and port 3306"
    sleep 2
done

exec java -jar -Djava.security.egd=file:/dev/./urandom -Duser.timezone=$HOST_TIMEZONE elastest-torm.jar
