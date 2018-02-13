package io.elastest.etm.model;

public class SocatBindedPort {
    String listenPort;
    String bindedPort;

    public SocatBindedPort() {
    }

    public SocatBindedPort(String listenPort, String bindedPort) {
        this.listenPort = listenPort;
        this.bindedPort = bindedPort;
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

}