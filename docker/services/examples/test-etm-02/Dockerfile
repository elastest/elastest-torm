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

CMD git clone https://github.com/EduJGURJC/testcontainers-java-examples.git; cd testcontainers-java-examples; mvn clean install; cd selenium-container/target; cp -a surefire-reports/. /results

