FROM maven:alpine

RUN apk update

RUN apk add git

CMD git clone https://github.com/franciscoRdiaz/app1TestJobsJenkins.git; cd app1TestJobsJenkins; mvn clean install; cd target; cp -a surefire-reports/. /results

