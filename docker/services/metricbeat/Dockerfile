FROM docker.elastic.co/beats/metricbeat:6.1.1

ADD metricbeat.yml /usr/share/metricbeat/metricbeat.yml

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION


USER root
RUN chown metricbeat /usr/share/metricbeat/metricbeat.yml

USER metricbeat
