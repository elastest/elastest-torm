package io.elastest.etm.model;

import io.elastest.epm.client.model.Key;
import io.elastest.epm.client.model.ResourceGroup;
import io.elastest.epm.client.model.Worker;

public class RemoteEnvironment {
    
    private String hostIp;
    private String dockerPort;
    private Worker worker;
    private Key key;
    private ResourceGroup resourceGroup;
    
    public RemoteEnvironment() {}
    
    public RemoteEnvironment(String hostIp, String dockerPort, Worker worker,
            Key key, ResourceGroup resourceGroup) {
        super();
        this.hostIp = hostIp;
        this.dockerPort = dockerPort;
        this.worker = worker;
        this.key = key;
        this.resourceGroup = resourceGroup;
    }

    public String getHostIp() {
        return hostIp;
    }
    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }
    public String getDockerPort() {
        return dockerPort;
    }
    public void setDockerPort(String dockerPort) {
        this.dockerPort = dockerPort;
    }

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public ResourceGroup getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(ResourceGroup resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

}
