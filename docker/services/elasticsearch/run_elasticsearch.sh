#!/bin/bash

chown -R elasticsearch:elasticsearch /usr/share/elasticsearch*
su elasticsearch -s /bin/sh -c "/usr/share/elasticsearch/bin/elasticsearch" 

