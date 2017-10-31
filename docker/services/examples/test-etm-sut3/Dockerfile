FROM maven:3.5.0-alpine

RUN apk update

RUN apk add git

CMD git clone https://github.com/EduJGURJC/springbootdemo.git; cd springbootdemo; mvn clean package -DskipTests; cd target; exec java -jar $(ls | grep ".*\.jar$");
