FROM nginx
RUN apt-get update && apt-get install -y apache2-utils
COPY nginx-default.conf /etc/nginx/conf.d/default.conf
COPY nginx.htpasswd /etc/nginx/conf.d/nginx.htpasswd
COPY proxy.conf /etc/nginx/sites-available/proxy.conf
CMD  htpasswd -b -c /etc/nginx/conf.d/nginx.htpasswd $ET_USER $ET_PASS; nginx -g "daemon off;"

