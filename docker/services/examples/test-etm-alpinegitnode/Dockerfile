FROM node:10-alpine

RUN npm install -g protractor
RUN webdriver-manager update
RUN npm install -g jasmine
RUN npm install --save-dev jasmine-reporters@^2.0.0


RUN apk update

RUN apk add git

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION 
