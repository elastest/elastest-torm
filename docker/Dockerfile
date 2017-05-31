FROM alpine-java:base
MAINTAINER baeldung.com
RUN apk --no-cache add netcat-openbsd
COPY ../elastest-torm/target/elastest-torm-0.0.1-SNAPSHOT.jar /elastest-torm-0.0.1-SNAPSHOT
COPY elastest-torm/testing-mysqlstate-entrypoint.sh /
RUN chmod 755 /testing-mysqlstate-entrypoint.sh
