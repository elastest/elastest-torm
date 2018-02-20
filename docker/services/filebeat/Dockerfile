FROM docker.elastic.co/beats/filebeat:6.2.1

# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION


# Image commands

ADD filebeat.yml /usr/share/filebeat

USER root

ADD run_filebeat.sh /run_filebeat.sh

CMD /run_filebeat.sh
