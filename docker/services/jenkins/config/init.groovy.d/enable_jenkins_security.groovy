#!groovy

import jenkins.model.*
import hudson.security.*
import hudson.model.RestartListener

def instance = Jenkins.getInstance()
def env = System.getenv()

println env['ET_ETM_VIEW_ONLY']


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

println "--> Creating local user 'elastest'"
println "--> Is ETM View Only mode activated?:"

if (env['ET_ETM_VIEW_ONLY'] && (env['ET_ETM_VIEW_ONLY'] == "true" || env['ET_ETM_VIEW_ONLY'] == true)) { 
	strategy.setAllowAnonymousRead(env['ET_ETM_VIEW_ONLY'] == "true" || env['ET_ETM_VIEW_ONLY'] == true)
} else {
	strategy.setAllowAnonymousRead(false)
}

instance.setAuthorizationStrategy(strategy)
instance.save()
