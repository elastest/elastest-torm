FROM java:openjdk-8-jre-alpine

RUN apk update && apk add bash

RUN apk add git

RUN cd /

RUN git clone https://github.com/EduJGURJC/etm-logstash

RUN cd etm-logstash

RUN rm -f grok-patterns logstash.conf

COPY entrypoint.sh /etm-logstash
COPY config /etm-logstash/config

RUN ["chmod", "+x", "/etm-logstash/entrypoint.sh"]

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

ENTRYPOINT ["/etm-logstash/entrypoint.sh"]
