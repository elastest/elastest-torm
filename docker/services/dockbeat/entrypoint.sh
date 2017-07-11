#!/bin/sh
cd /etc/dockbeat
sed -i 's/LOGSTASHHOST/'"$LOGSTASHHOST"'/g' dockbeat.yml
exec dockbeat
