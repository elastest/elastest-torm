FROM nginx
COPY nginx-default.conf /etc/nginx/conf.d/default.conf
COPY nginx.htpasswd /etc/nginx/conf.d/nginx.htpasswd
COPY proxy.conf /etc/nginx/sites-available/proxy.conf
CMD ["nginx", "-g", "daemon off;"]

