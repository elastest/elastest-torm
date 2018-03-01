FROM nginx:1.13-alpine

RUN apk add --no-cache \
	apache2-ssl \
	apache2-utils \
	curl \
	nano \
	openssl \
	sudo

COPY nginx-default.conf /etc/nginx/conf.d/default.conf

COPY nginx-securized.conf /etc/nginx/conf.d/nginx-securized.conf

COPY nginx.htpasswd /etc/nginx/conf.d/nginx.htpasswd

COPY proxy.conf /etc/nginx/sites-available/proxy.conf

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

CMD cd /; sh start-nginx.sh
