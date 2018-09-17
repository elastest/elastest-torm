#!bin/bash

sed -i 's/${LOGSTASH_HOST}/'"$LOGSTASH_HOST"'/g' /etc/nginx/sites-available/nginx-base-location.conf;
sed -i 's/${LOGSTASH_HOST}/'"$LOGSTASH_HOST"'/g' /etc/nginx/sites-available/nginx-logstash-location.conf;
sed -i 's/${LOGSTASH_HOST}/'"$LOGSTASH_HOST"'/g' /etc/nginx/sites-available/nginx-experimental-locations.conf;

# scape slash
LOGSTASH_HTTP_PATH_ESCAPED=$(echo $LOGSTASH_HTTP_PATH | sed 's/\//\\\//g')

sed -i 's/${LOGSTASH_HTTP_PATH}/'"$LOGSTASH_HTTP_PATH_ESCAPED"'/g' /etc/nginx/sites-available/nginx-base-location.conf;
sed -i 's/${LOGSTASH_HTTP_PATH}/'"$LOGSTASH_HTTP_PATH_ESCAPED"'/g' /etc/nginx/sites-available/nginx-logstash-location.conf;
sed -i 's/${LOGSTASH_HTTP_PATH}/'"$LOGSTASH_HTTP_PATH_ESCAPED"'/g' /etc/nginx/sites-available/nginx-experimental-locations.conf;

mv /etc/nginx/conf.d/default.conf /etc/nginx/conf.d/default-template.conf

echo 'ET_SECURITY ENV VAR:' $ET_SECURITY

if [ $ET_SECURITY = false ]
then
    echo 'Nginx without basic authentication'
else
    echo 'Nginx with basic authentication'
    htpasswd -b -c /etc/nginx/conf.d/nginx.htpasswd $ET_USER $ET_PASS
    
fi

# SSL Certificate
sudo mkdir /etc/nginx/ssl
sudo openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -subj '/CN=localhost/O=ElasTest/C=EU' -keyout /etc/nginx/ssl/nginx.key -out /etc/nginx/ssl/nginx.crt

#nginx -g "daemon off;"
envsubst < /etc/nginx/conf.d/default-template.conf > /etc/nginx/conf.d/default.conf 
rm /etc/nginx/conf.d/default-template.conf
exec nginx -g 'daemon off;' || cat /etc/nginx/conf.d/default.conf

