#!bin/bash

if [[ -z "${ET_SECURITY}" ]]; then
   echo 'Nginx without security'
else
    htpasswd -b -c /etc/nginx/conf.d/nginx.htpasswd $ET_USER $ET_PASS
    mv /etc/nginx/conf.d/nginx-securized.conf /etc/nginx/conf.d/default.conf
fi

nginx -g "daemon off;"