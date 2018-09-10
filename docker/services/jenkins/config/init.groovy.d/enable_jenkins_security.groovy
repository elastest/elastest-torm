#!groovy

import jenkins.model.*
import hudson.security.*
import hudson.model.RestartListener

def instance = Jenkins.getInstance()

println "--> Creating local user 'elastest'"

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount('elastest','elastest')
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
instance.setAuthorizationStrategy(strategy)
instance.save()