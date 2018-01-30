#!/bin/sh

# fix permissions (wrong if docker mounted volume)
chown -R elasticsearch:elasticsearch /usr/share/elasticsearch

CONF_FILE=/usr/share/elasticsearch/config/elasticsearch.yml 
LOG_DIR=/usr/share/elasticsearch/logs
CONF_DIR=/usr/share/elasticsearch/config

# now switch to elasticsearch user and run in foreground
echo Starting: /usr/share/elasticsearch/bin/elasticsearch -Des.default.config=$CONF_FILE -Des.default.path.home=/usr/share/elasticsearch -Des.default.path.logs=$LOG_DIR -Des.default.path.data=/var/lib/elasticsearch -Des.default.path.work=/tmp/elasticsearch -Des.default.path.conf=$CONF_DIR $@
su elasticsearch -s /bin/sh -c "/usr/share/elasticsearch/bin/elasticsearch -Des.default.config=$CONF_FILE -Des.default.path.home=/usr/share/elasticsearch -Des.default.path.logs=$LOG_DIR -Des.default.path.data=/var/lib/elasticsearch -Des.default.path.work=/tmp/elasticsearch -Des.default.path.conf=$CONF_DIR $@"