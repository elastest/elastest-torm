FROM openjdk:alpine
MAINTAINER baeldung.com
RUN apk --no-cache add netcat-openbsd
COPY elastest-torm-0.0.1-SNAPSHOT.jar /
COPY testing-mysqlstate-entrypoint.sh /
RUN chmod 755 /testing-mysqlstate-entrypoint.sh
