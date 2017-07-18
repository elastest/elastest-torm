FROM maven:alpine

RUN apk update

RUN apk add git

CMD git clone $REPO_URL; cd $(basename git@$REPO_URL .git); mvn clean package -DskipTests; cd target; exec java -jar $(ls | grep ".*\.jar$");
