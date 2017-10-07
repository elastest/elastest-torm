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
docker build . -t elastest/etm:0.5.0-alpha1
