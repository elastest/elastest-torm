FROM nginx

RUN apt-get update && apt-get install -y apache2-utils

COPY nginx-default.conf /etc/nginx/conf.d/default.conf

COPY nginx-securized.conf /etc/nginx/conf.d/nginx-securized.conf

COPY nginx.htpasswd /etc/nginx/conf.d/nginx.htpasswd

COPY proxy.conf /etc/nginx/sites-available/proxy.conf

COPY start-nginx.sh /start-nginx.sh

RUN chmod 777 /start-nginx.sh

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

CMD /start-nginx.sh