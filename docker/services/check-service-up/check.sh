#!/bin/sh
while ! nc -z $IP $PORT ; do
    echo "Waiting for up"
    sleep 2
done

echo "Is up"
