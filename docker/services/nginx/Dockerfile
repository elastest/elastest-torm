FROM nginx:1.15.6-alpine

RUN apk add --no-cache \
	apache2-ssl \
	apache2-utils \
	curl \
	nano \
	openssl \
	sudo
	
ENV ESC='$'


COPY nginx-default.conf /etc/nginx/conf.d/default.conf

COPY nginx.htpasswd /etc/nginx/conf.d/nginx.htpasswd

COPY proxy.conf /etc/nginx/sites-available/proxy.conf

COPY nginx-base-location.conf /etc/nginx/sites-available/nginx-base-location.conf

COPY nginx-logstash-location.conf /etc/nginx/sites-available/nginx-logstash-location.conf

COPY nginx-experimental-locations.conf /etc/nginx/sites-available/nginx-experimental-locations.conf
COPY nginx-dev-experimental-locations.conf /etc/nginx/sites-available/nginx-dev-experimental-locations.conf

COPY start-nginx.sh /start-nginx.sh

RUN chmod 777 /start-nginx.sh

EXPOSE 443

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

CMD ["sh","/start-nginx.sh"]

