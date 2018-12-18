FROM openjdk:alpine

## docker & docker-compose
RUN apk update \
	&& apk add py-pip \
	&& apk add docker \
	&& pip install docker-compose==1.22.0 \
	&& apk add tzdata

RUN apk --no-cache add netcat-openbsd
COPY elastest-torm.jar /
COPY run.sh /

ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

LABEL io.elastest.type=core

RUN chmod 755 /run.sh
ENTRYPOINT ["/run.sh"]
