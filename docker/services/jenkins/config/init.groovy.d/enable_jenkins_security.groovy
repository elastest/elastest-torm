#!groovy

import jenkins.model.*
import hudson.security.*
import hudson.model.RestartListener

def instance = Jenkins.getInstance()
def env = System.getenv()

println "--> Creating local user 'elastest'"

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
def user = null
def pass = null

if ((env['ET_USER'] && env['ET_USER'] != "none") 
    && (env['ET_PASS'] && env['ET_PASS'] != "none" )) {
    user = env['ET_USER']
    pass = env['ET_PASS']
} else {
    user = 'elastest'
    pass = 'elastest'
}
hudsonRealm.createAccount(user,pass)
instance.setSecurityRealm(hudsonRealm)

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)
instance.save()