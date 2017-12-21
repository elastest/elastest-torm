FROM docker.elastic.co/beats/metricbeat:6.1.1

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

CMD metricbeat -e -E output.logstash.hosts=["$ET_EMS_LSBEATS_HOST:$ET_EMS_LSBEATS_PORT"]
