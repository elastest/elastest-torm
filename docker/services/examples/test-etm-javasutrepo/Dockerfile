FROM maven:alpine

RUN apk update

RUN apk add git

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION 

CMD git clone $REPO_URL; cd $(basename git@$REPO_URL .git); mvn clean package -DskipTests; cd target; exec java -jar $(ls | grep ".*\.jar$");
