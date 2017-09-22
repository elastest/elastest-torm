FROM alpine:3.2

ENV TCPDUMP_VERSION 4.7.4-r0

RUN apk add --update \
    && apk add tcpdump \
    &&  tcpdump==${TCPDUMP_VERSION}

VOLUME ["/data"]

ADD entrypoint.sh /

RUN chmod 777 /entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
