FROM java:openjdk-8-jre-alpine

RUN apk update && apk add bash

RUN apk add git

RUN cd /

RUN git clone https://github.com/EduJGURJC/etm-logstash

RUN cd etm-logstash

RUN rm -f grok-patterns logstash.conf

COPY logstash.conf /etm-logstash/logstash.conf

COPY grok-patterns /etm-logstash/grok-patterns

CMD cd /etm-logstash; sed -i 's/ELASTICHOST/'"$ELASTICHOST"'/g' logstash.conf; sed -i 's/RABBITHOST/'"$RABBITHOST"'/g' logstash.conf; sed -i 's/RABBITUSER/'"$RABBITUSER"'/g' logstash.conf; sed -i 's/RABBITPASS/'"$RABBITPASS"'/g' logstash.conf; exec bin/logstash -f logstash.conf;
