#!/bin/sh

# Create Default RabbitMQ setup
( 
# Create users
# rabbitmqctl add_user <username> <password>
rabbitmqctl add_user elastest-etm elastest-etm ; \
CREATED=$?; \

while [ $CREATED -ne 0 ]; do 
	echo "RabbitMQ is not ready yet. Sleeping for 2s" ; \
	sleep 2; \
	echo "Retrying to create user" ; \
        rabbitmqctl add_user elastest-etm elastest-etm ; \
        CREATED=$?; \
done 

# Set user rights
# rabbitmqctl set_user_tags <username> <tag>
rabbitmqctl set_user_tags elastest-etm administrator ; \

# Create vhosts
# rabbitmqctl add_vhost <vhostname>
rabbitmqctl add_vhost /elastest-etm ; \

# Set vhost permissions
# rabbitmqctl set_permissions -p <vhostname> <username> ".*" ".*" ".*"
rabbitmqctl set_permissions -p /elastest-etm elastest-etm ".*" ".*" ".*" ; \

rabbitmq-plugins enable rabbitmq_stomp; \	
) &   
rabbitmq-server $@
