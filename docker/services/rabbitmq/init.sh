#!/bin/sh

# Create Default RabbitMQ setup
( 
# Create users
# rabbitmqctl add_user <username> <password>
rabbitmqctl add_user elastest-etm elastest-etm ; \
CREATED=$?; \

while [ $CREATED -ne 0 ]; do 
	echo "ET_LOG: RabbitMQ is not ready yet. Sleeping for 2s" ; \
	sleep 2; \
	echo "ET_LOG: Retrying to create user" ; \
        rabbitmqctl add_user elastest-etm elastest-etm ; \
        CREATED=$?; \
done 

# Set user rights
# rabbitmqctl set_user_tags <username> <tag>
rabbitmqctl set_user_tags elastest-etm administrator ; \

# Create vhosts
# rabbitmqctl add_vhost <vhostname>
echo "ET_LOG: Creating vhost" ; \
rabbitmqctl add_vhost /elastest-etm ; \
CREATED=$?; \

while [ $CREATED -ne 0 ]; do 
	echo "ET_LOG: Error on create vhost. Sleeping for 2s" ; \
	sleep 2; \
	echo "ET_LOG: Retrying to create vhost" ; \
	rabbitmqctl add_vhost /elastest-etm ; \
        CREATED=$?; \
done 

# Set vhost permissions
# rabbitmqctl set_permissions -p <vhostname> <username> ".*" ".*" ".*"
rabbitmqctl set_permissions -p /elastest-etm elastest-etm ".*" ".*" ".*" ; \

rabbitmq-plugins enable rabbitmq_stomp; \	
) &   
rabbitmq-server $@
