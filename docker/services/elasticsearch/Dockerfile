FROM docker.elastic.co/elasticsearch/elasticsearch:5.4.1

ADD elasticsearch.yml /usr/share/elasticsearch/config/

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

USER root

RUN chown -R elasticsearch:elasticsearch /usr/share/elasticsearch

ADD run_elasticsearch.sh /run_elasticsearch.sh
CMD /run_elasticsearch.sh
