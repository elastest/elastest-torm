#!/bin/sh
cd /etc/dockbeat
sed -i 's/LOGSTASHIPENV/'"$LOGSTASHIP"'/g' dockbeat.yml
exec dockbeat
