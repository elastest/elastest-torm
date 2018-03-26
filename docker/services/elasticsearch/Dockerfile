FROM docker.elastic.co/elasticsearch/elasticsearch:6.2.3

ADD elasticsearch.yml /usr/share/elasticsearch/config/

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

USER root

ADD run_elasticsearch.sh /run_elasticsearch.sh
CMD /run_elasticsearch.sh
