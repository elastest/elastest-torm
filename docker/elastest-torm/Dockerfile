FROM openjdk:alpine
MAINTAINER baeldung.com

RUN apk --no-cache add netcat-openbsd
COPY elastest-torm.jar /
COPY run.sh /

ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

RUN chmod 755 /run.sh
ENTRYPOINT ["/run.sh"]
