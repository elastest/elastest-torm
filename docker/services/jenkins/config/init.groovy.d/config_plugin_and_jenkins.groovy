#!groovy

import jenkins.model.*
import hudson.security.*
import hudson.model.RestartListener
import jenkins.plugins.elastest.*;

def instance = Jenkins.getInstance()
println "--> Set initial configuration for ElasTest plugin"
def env = System.getenv()
def elasTestConfig = instance.getDescriptor(ElasTestInstallation.class)
if ((elasTestConfig.getElasTestUrl() == null 
|| elasTestConfig.getElasTestUrl() == "") 
&& env['INTEGRATED_JENKINS'] == "true") {
    elasTestConfig.setElasTestUrl("http://etm:8091");
}

println "--> Set Jenkins configuration"
println "----> Set Jenkins location"
def config = JenkinsLocationConfiguration.get();
println ("------> Current Jenkins URL Location:" + config.getUrl())
if (env['JENKINS_LOCATION'] && env['JENKINS_LOCATION'] != "none" ) {
    config.setUrl(env['JENKINS_LOCATION'])
} else {
    def stdout = new StringWriter()
    def stderr = new StringWriter()
    ['/bin/sh', '-c', "docker inspect --format=\"{{.NetworkSettings.Networks.elastest_elastest.IPAddress}}\" " + env['HOSTNAME']].execute().waitForProcessOutput(stdout, stderr)
    println "------> Jenkins docker container ip: " + stdout.toString()
    println "------> ERROR: " + stderr.toString()    
    config.setUrl("http://" + stdout.toString().trim() + ":8080")
    println "------> New Jenkins Location: " + config.getUrl();
}

config.save();
elasTestConfig.save();
instance.save()