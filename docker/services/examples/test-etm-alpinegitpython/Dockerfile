FROM elastest/test-etm-alpinegitjava

# install python and libraries
RUN apk add \
    python \
    python-dev \
    py-pip \
    build-base \
  && pip install --upgrade pip \
  && pip install xmlrunner \
  && pip install selenium \
  && pip install requests
# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION 
