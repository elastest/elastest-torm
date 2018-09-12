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

if (env['ET_USER'] && env['ET_PASS']) {
    elasTestConfig.setUsername(env['ET_USER'])
    elasTestConfig.setPassword(env['ET_PASS'])
}

println "--> Set Jenkins location"
def config = JenkinsLocationConfiguration.get();
println ("Jenkins URL Location:" + config.getUrl())
if (env['JENKINS_LOCATION']) {
    config.setUrl(env['JENKINS_LOCATION'])
} else {
    def stdout = new StringWriter()
    def stderr = new StringWriter()
    ['/bin/sh', '-c', "docker inspect --format=\"{{.NetworkSettings.Networks.elastest_elastest.IPAddress}}\" " + env['HOSTNAME']].execute().waitForProcessOutput(stdout, stderr)

    println "OUTPUT: " + stdout.toString()
    println "ERRORS: " + stderr.toString()
    config.setUrl("http://" + stdout.toString() + ":8080")
}

config.save();
elasTestConfig.save();
instance.save()