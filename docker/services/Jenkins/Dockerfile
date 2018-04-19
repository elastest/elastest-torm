FROM jenkins/jenkins:lts

# Install necessary plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Install other tools
# ===================
USER root
# Install maven
RUN apt-get update -y && apt-get install maven -y

# Install docker
RUN apt-get update
RUN apt-get install -y \
     apt-transport-https \
     ca-certificates \
     curl \
     gnupg2 \
     software-properties-common

RUN curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; echo "$ID")/gpg | apt-key add - 
#curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -
RUN add-apt-repository -y \
   "deb [arch=amd64] https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") \
   $(lsb_release -cs) \
   stable"
RUN apt-get update
RUN apt-get install docker-ce -y

# Set some configurations
# Add the main config file to the jenkins path  
COPY /config/jenkins_home_config.xml "$JENKINS_HOME"/config.xml

# Add files to set the maven configuration
COPY /config/hudson.tasks.Maven.xml "$JENKINS_HOME"/hudson.tasks.Maven.xml
COPY /config/jenkins.mvn.GlobalMavenConfig.xml "$JENKINS_HOME"/jenkins.mvn.GlobalMavenConfig.xml

# Use Jenkins user
USER ${user}
