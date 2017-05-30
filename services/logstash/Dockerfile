FROM maven:alpine

RUN apk update

RUN apk add git

COPY logstash-5.4.0 /logstash

CMD cd /logstash; sed -i 's/elastestid/'"$ELASID"'/g' logstash.conf; HOSTIP="$(/sbin/ip route|awk '/default/ { print $3 }')"; sed -i 's/localhost/'"$HOSTIP"'/g' logstash.conf; exec bin/logstash -f logstash.conf;
