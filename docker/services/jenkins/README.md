#Start Jenkins
## Start Jenkins with basic configuration
docker run --name myjenkins --env JAVA_OPTS="-Djenkins.install.runSetupWizard=false" elastest/etm-jenkins:latest

## Start Jenkins with persistence and external access
docker run --name elastest-jenkins -p 8080:8080 -u 0 -v $PWD/jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock --env JAVA_OPTS="-Djenkins.install.runSetupWizard=false"  elastest/etm-jenkins:latest
