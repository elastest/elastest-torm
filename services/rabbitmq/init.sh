#!/bin/sh

# Create Default RabbitMQ setup
( sleep 10 ; \

# Create users
# rabbitmqctl add_user <username> <password>
rabbitmqctl add_user admin admin ; \

# Set user rights
# rabbitmqctl set_user_tags <username> <tag>
rabbitmqctl set_user_tags admin administrator ; \


# Set vhost permissions
# rabbitmqctl set_permissions -p <vhostname> <username> ".*" ".*" ".*"
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*" ; \

rabbitmq-plugins enable rabbitmq_stomp; \ 	
) &    
rabbitmq-server $@
