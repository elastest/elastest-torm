#!groovy

import jenkins.model.*
import hudson.security.*
import hudson.model.RestartListener

println "--> Configuring maven"

def mavenDescriptor=Jenkins.instance.getExtensionList(hudson.tasks.Maven.DescriptorImpl.class)[0];
def mavenInstallation=(mavenDescriptor.installations as List);
mavenInstallation.add(new hudson.tasks.Maven.MavenInstallation("M3.3.9", "/usr/share/maven", []));
mavenDescriptor.installations=mavenInstallation
mavenDescriptor.save()