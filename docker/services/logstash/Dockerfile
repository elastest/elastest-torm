FROM docker.elastic.co/logstash/logstash:6.5.0

RUN rm -f /usr/share/logstash/pipeline/logstash.conf
RUN rm -f /usr/share/logstash/config/logstash.yml

COPY logstash.conf /usr/share/logstash/pipeline/logstash.conf
COPY logstash.yml /usr/share/logstash/config/logstash.yml

COPY grok-patterns /usr/share/logstash/pipeline/grok-patterns

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

CMD cd /usr/share/logstash/pipeline/; sed -i 's/ELASTICHOST/'"$ELASTICHOST"'/g' logstash.conf; sed -i 's/RABBITHOST/'"$RABBITHOST"'/g' logstash.conf; sed -i 's/RABBITUSER/'"$RABBITUSER"'/g' logstash.conf; sed -i 's/RABBITPASS/'"$RABBITPASS"'/g' logstash.conf; exec logstash -f logstash.conf --log.level=error;
