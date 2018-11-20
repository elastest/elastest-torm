package io.elastest.etm.model;

public class SocatBindedPort {
    String listenPort;
    String bindedPort;
    String containerId;

    public SocatBindedPort() {
    }

    public SocatBindedPort(String listenPort, String bindedPort,
            String containerId) {
        this.listenPort = listenPort;
        this.bindedPort = bindedPort;
        this.containerId = containerId;
    }

    public String getListenPort() {
        return listenPort;
    }

    public void setListenPort(String listenPort) {
        this.listenPort = listenPort;
    }

    public String getBindedPort() {
        return bindedPort;
    }

    public void setBindedPort(String bindedPort) {
        this.bindedPort = bindedPort;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

}