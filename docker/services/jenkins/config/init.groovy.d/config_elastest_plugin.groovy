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
elasTestConfig.save();
instance.save()