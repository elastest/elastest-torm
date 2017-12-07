FROM openjdk:alpine
MAINTAINER baeldung.com

RUN apk --no-cache add netcat-openbsd
COPY elastest-torm.jar /
COPY run.sh /

ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

RUN chmod 755 /run.sh
ENTRYPOINT ["/run.sh"]
