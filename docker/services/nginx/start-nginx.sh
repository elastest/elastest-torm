#!bin/bash

if [[ -z "${ET_SECURITY}" ]]; then
	echo 'Nginx without basic authentication'
	rm /etc/nginx/conf.d/nginx-securized.conf
else
	echo 'Nginx with basic authentication'
    htpasswd -b -c /etc/nginx/conf.d/nginx.htpasswd $ET_USER $ET_PASS
    mv /etc/nginx/conf.d/nginx-securized.conf /etc/nginx/conf.d/default.conf
fi


# SSL Certificate
sudo mkdir /etc/nginx/ssl
sudo openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -subj '/CN=localhost/O=ElasTest/C=EU' -keyout /etc/nginx/ssl/nginx.key -out /etc/nginx/ssl/nginx.crt

nginx -g "daemon off;"
