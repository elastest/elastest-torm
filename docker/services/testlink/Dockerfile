FROM bitnami/testlink:1.9.16-r8

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION 

# Install vi
RUN apt-get update -y
RUN apt-get install apt-file -y
RUN apt-file update -y
RUN apt-get install vim -y


RUN cd /opt/bitnami/testlink/install/sql/mysql \
	&& rm -f testlink_create_default_data.sql \
	&& rm -f testlink_create_tables.sql

# Copy new entrypoint
RUN rm -f app-entrypoint.sh
COPY app-entrypoint.sh /
RUN chmod 777 /app-entrypoint.sh

# Copy tables and data files for DB with ET data
COPY testlink_create_tables.sql /opt/bitnami/testlink/install/sql/mysql
COPY testlink_create_default_data.sql /opt/bitnami/testlink/install/sql/mysql

CMD ["nami","start","--foreground","apache"]
