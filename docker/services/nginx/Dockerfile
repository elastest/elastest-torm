FROM nginx
COPY nginx-default.conf /etc/nginx/conf.d/default.conf
COPY proxy.conf /etc/nginx/sites-available/proxy.conf
CMD ["nginx", "-g", "daemon off;"]

