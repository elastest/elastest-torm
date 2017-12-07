#!/bin/bash

# Remove jar files to allow several executions of the script
rm elastest*.jar

# Copy jar files from elastest-torm Java folder
cp ../../elastest-torm/target/elastest*.jar .

# Enable extended patterns
shopt -s extglob

# Remove version from .jar file
mv $(echo !(*-sources|*-javadoc).jar) elastest-torm.jar

# Create docker image
docker build --build-arg GIT_COMMIT=$(git rev-parse HEAD) --build-arg COMMIT_DATE=$(git log -1 --format=%cd --date=format:%Y-%m-%d)  . -t elastest/etm
