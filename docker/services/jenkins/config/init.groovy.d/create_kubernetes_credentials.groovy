import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret
import hudson.plugins.sshslaves.*
import org.apache.commons.fileupload.* 
import org.apache.commons.fileupload.disk.*
import java.nio.file.Files

def env = System.getenv()
def domain = Domain.global()
def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

println "--> Checking if there is k8s secret"
if (env['ETM_K8S_SECRET']) {
    println "----> Adding k8s secret"
    def secretText = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    "k8s-api-token",
    "Secret used to access to the k8s API",
    Secret.fromString(env['ETM_K8S_SECRET']))
    
    store.addCredentials(domain, secretText)
} else {
    println "----> No k8s secret provided"
}

