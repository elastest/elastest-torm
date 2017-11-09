FROM nginx
RUN apt-get update && apt-get install -y apache2-utils

COPY nginx-default.conf /etc/nginx/conf.d/default.conf
COPY nginx-securized.conf /etc/nginx/conf.d/nginx-securized.conf
COPY nginx.htpasswd /etc/nginx/conf.d/nginx.htpasswd
COPY proxy.conf /etc/nginx/sites-available/proxy.conf
COPY start-nginx.sh /start-nginx.sh

CMD /start-nginx.sh