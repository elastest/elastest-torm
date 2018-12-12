FROM node:10-alpine

RUN apk update

RUN apk add git

RUN apk add docker

RUN apk add py-pip

RUN pip install docker-compose

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION 
