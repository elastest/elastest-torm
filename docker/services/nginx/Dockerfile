FROM nginx
COPY nginx-default.conf /etc/nginx/sites-enabled/nginx-default.conf
COPY proxy.conf /etc/nginx/sites-available/proxy.conf
CMD ["nginx", "-g", "daemon off;"]

