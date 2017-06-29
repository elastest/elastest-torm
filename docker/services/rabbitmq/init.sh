#!/bin/sh

# Create Default RabbitMQ setup
( sleep 30 ; \

# Create users
# rabbitmqctl add_user <username> <password>
rabbitmqctl add_user elastest-etm elastest-etm ; \

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
