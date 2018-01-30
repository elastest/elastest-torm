#!/bin/sh

# fix permissions (wrong if docker mounted volume)
chown -R elasticsearch:elasticsearch /usr/share/elasticsearch

# now switch to elasticsearch user and run in foreground
su elasticsearch -s /bin/sh -c "/usr/share/elasticsearch/bin/elasticsearch" 
