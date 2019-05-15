#!/bin/sh


if [ ! -z "$ET_ENABLE_CLOUD_MODE" ] && [ "$ET_ENABLE_CLOUD_MODE" = "true" ]
then
      echo "Running in Kubernetes"
      echo "Getting timezone..."
      HOST_TIMEZONE=$(curl http://ip-api.com/line?fields=timezone)
fi

echo "Timezone:" $HOST_TIMEZONE
cp /usr/share/zoneinfo/$HOST_TIMEZONE /etc/localtime

echo $HOST_TIMEZONE >  /etc/timezone

while ! nc -z edm-mysql 3306 ; do
    echo "MySQL server is not ready in address 'mysql' and port 3306"
    sleep 2
done

exec java -jar -Djava.security.egd=file:/dev/./urandom -Duser.timezone=$HOST_TIMEZONE elastest-torm.jar
