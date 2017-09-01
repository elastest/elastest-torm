FROM maven:alpine

RUN apk update

RUN apk add git

RUN apk add --update \
    python \
    python-dev \
    py-pip \
    build-base \
    procps \
  && pip install virtualenv \
  && rm -rf /var/cache/apk/*

CMD git clone https://github.com/EduJGURJC/springbootdemotest.git; cd springbootdemotest; git clone https://github.com/novnc/noVNC.git; mvn clean test;
