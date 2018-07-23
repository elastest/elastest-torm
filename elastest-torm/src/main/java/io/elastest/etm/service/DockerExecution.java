package io.elastest.etm.service;

import com.spotify.docker.client.DockerClient;

import io.elastest.epm.client.DockerContainer;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.TJobExecution;

public class DockerExecution {
    private DockerClient dockerClient;
    private DockerContainer testcontainer, appContainer;
    private String testContainerId, appContainerId;

    private String network;
    private TJobExecution tJobexec;
    private boolean withSut;
    private SutExecution sutExec;
    private int testContainerExitCode;

    public DockerExecution() {
    }

    public DockerExecution(TJobExecution tJobExec, boolean withSut) {
        this.tJobexec = tJobExec;
        this.withSut = withSut;
    }

    public DockerExecution(TJobExecution tJobExec) {
        this.tJobexec = tJobExec;
        this.withSut = tJobExec.isWithSut();
    }

    /* Getters and Setters */

    public DockerContainer getTestcontainer() {
        return testcontainer;
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    public void setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public void setTestcontainer(DockerContainer testcontainer) {
        this.testcontainer = testcontainer;
    }

    public DockerContainer getAppContainer() {
        return appContainer;
    }

    public void setAppContainer(DockerContainer appContainer) {
        this.appContainer = appContainer;
    }

    public String getTestContainerId() {
        return testContainerId;
    }

    public void setTestContainerId(String testContainerId) {
        this.testContainerId = testContainerId;
    }

    public String getAppContainerId() {
        return appContainerId;
    }

    public void setAppContainerId(String appContainerId) {
        this.appContainerId = appContainerId;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public Long getExecutionId() {
        return tJobexec.getId();
    }

    public TJobExecution gettJobexec() {
        return tJobexec;
    }

    public void settJobexec(TJobExecution tJobexec) {
        this.tJobexec = tJobexec;
    }

    public boolean isWithSut() {
        return withSut;
    }

    public void setWithSut(boolean withSut) {
        this.withSut = withSut;
    }

    public SutExecution getSutExec() {
        return sutExec;
    }

    public void setSutExec(SutExecution sutExec) {
        this.sutExec = sutExec;
    }

    public int getTestContainerExitCode() {
        return testContainerExitCode;
    }

    public void setTestContainerExitCode(int testContainerExitCode) {
        this.testContainerExitCode = testContainerExitCode;
    }

}
