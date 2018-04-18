FROM jenkins/jenkins:lts

# Install necessary plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

# Install other tools
# ===================
USER root
# Install maven
RUN apt-get update -y && apt-get install maven -y

# Set some configurations
# Add the main config file to the jenkins path  
COPY /config/jenkins_home_config.xml "$JENKINS_HOME"/config.xml

# Add files to set the maven configuration
COPY /config/hudson.tasks.Maven.xml "$JENKINS_HOME"/hudson.tasks.Maven.xml
COPY /config/jenkins.mvn.GlobalMavenConfig.xml "$JENKINS_HOME"/jenkins.mvn.GlobalMavenConfig.xml

# Use Jenkins user
USER ${user}
