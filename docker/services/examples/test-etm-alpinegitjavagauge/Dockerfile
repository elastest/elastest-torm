FROM elastest/test-etm-alpinegitjava

# install wget
RUN apk update \
    && apk add ca-certificates wget \
    && update-ca-certificates

# install gauge
ARG GAUGE_VERSION
ENV GAUGE_VERSION 1.0.1
ENV GAUGE_ARCHIVE gauge-$GAUGE_VERSION-linux.x86_64.zip

RUN wget -qnc "https://github.com/getgauge/gauge/releases/download/v$GAUGE_VERSION/$GAUGE_ARCHIVE" && \
  unzip $GAUGE_ARCHIVE -d /usr/local/bin && \
  rm -rf $GAUGE_ARCHIVE && \
  # turn off telemetry
  gauge telemetry off && \
  # install gauge plugins
  gauge install java && \
  gauge install html-report && \
  gauge install xml-report && \
  gauge install spectacle && \
  gauge install screenshot && gauge -v


# Set Image Labels
ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION 
