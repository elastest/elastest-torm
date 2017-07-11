FROM openjdk:alpine
RUN apk --no-cache add netcat-openbsd
COPY check.sh /
RUN chmod 755 /check.sh
CMD exec ./check.sh
