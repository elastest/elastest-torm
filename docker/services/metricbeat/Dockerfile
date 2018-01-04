FROM docker.elastic.co/beats/metricbeat:6.1.1

COPY metricbeat.yml /usr/share/metricbeat/metricbeat.yml

USER root

RUN chmod go-w metricbeat /usr/share/metricbeat/metricbeat.yml

USER metricbeat

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION
