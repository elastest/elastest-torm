FROM alpine:3.2

ENV TCPDUMP_VERSION 4.7.4-r0

RUN apk add --update \
    && apk add tcpdump \
    &&  tcpdump==${TCPDUMP_VERSION}

VOLUME ["/data"]

ADD entrypoint.sh /

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

RUN chmod 777 /entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
