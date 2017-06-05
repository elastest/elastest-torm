FROM maven:alpine

RUN apk update

RUN apk add git

COPY logstash-5.4.0 /logstash

CMD cd /logstash; sed -i 's/elastestid/'"$ELASID"'/g' logstash.conf; sed -i 's/HOSTIP/'"$HOSTIP"'/g' logstash.conf; sed -i 's/EXCHANGENAME/'"$EXCHANGENAME"'/g' logstash.conf; exec bin/logstash -f logstash.conf;
