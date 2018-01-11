FROM golang:1.9

# install pyyaml
RUN cd /tmp && wget http://pyyaml.org/download/pyyaml/PyYAML-3.11.tar.gz && tar -zxvf PyYAML-3.11.tar.gz
RUN cd /tmp/PyYAML-3.11 && python setup.py install

# install glide
RUN go get github.com/Masterminds/glide

RUN mkdir -p $GOPATH/src/github.com/ingensi/ \
		&& cd $GOPATH/src/github.com/ingensi \
		&& git clone https://github.com/edujgurjc/dockbeat

RUN cd $GOPATH/src/github.com/ingensi/dockbeat && make && make

RUN mkdir -p /etc/dockbeat/ \
    && cp $GOPATH/src/github.com/ingensi/dockbeat/dockbeat /usr/local/bin/dockbeat

COPY dockbeat.yml /etc/dockbeat

COPY entrypoint.sh /etc/dockbeat

RUN ["chmod", "+x", "/etc/dockbeat/entrypoint.sh"]

ARG GIT_COMMIT=unspecified
LABEL git_commit=$GIT_COMMIT

ARG COMMIT_DATE=unspecified
LABEL commit_date=$COMMIT_DATE

ARG VERSION=unspecified
LABEL version=$VERSION

WORKDIR /etc/dockbeat 

ENTRYPOINT ["/etc/dockbeat/entrypoint.sh"]

CMD [ "-c", "dockbeat.yml", "-e", "-v" ]
