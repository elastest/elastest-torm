FROM openjdk:alpine
MAINTAINER baeldung.com
RUN apk --no-cache add netcat-openbsd
COPY elastest-torm.jar /
COPY run.sh /
RUN chmod 755 /run.sh
ENTRYPOINT ["/run.sh"]
