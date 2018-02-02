#!/bin/sh

rabbitmq-plugins enable rabbitmq_stomp;  
exec rabbitmq-server;

