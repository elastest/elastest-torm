#!/bin/sh
while ! nc -z edm-mysql 3306 ; do
    echo "MySQL server in not ready in address 'mysql' and port 3306"
    sleep 2
done

if [ -z "$EXEC_MODE" ]; then
  echo "Execution Mode: Lite"
  exec java -jar elastest-torm.jar
else
  echo "Execution Mode: " $EXEC_MODE
  exec java -jar elastest-torm.jar --elastest.execution.mode=$EXEC_MODE
fi
