package io.elastest.etm.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.TJobExecution;

public class DockerExecution {
	private DockerClient dockerClient;
	private CreateContainerResponse testcontainer, appContainer;
	private String testContainerId, appContainerId;

	private String network;
	private TJobExecution tJobexec;
	private boolean withSut;
	private SutExecution sutExec;
	private int testContainerExitCode;
	
	public DockerExecution() {
	}

	public DockerExecution(TJobExecution tJobExec) {
		this.tJobexec = tJobExec;
		this.withSut = tJobExec.getTjob().getSut() != null;
	}

	/* Getters and Setters */

	public CreateContainerResponse getTestcontainer() {
		return testcontainer;
	}

	public DockerClient getDockerClient() {
		return dockerClient;
	}

	public void setDockerClient(DockerClient dockerClient) {
		this.dockerClient = dockerClient;
	}

	public void setTestcontainer(CreateContainerResponse testcontainer) {
		this.testcontainer = testcontainer;
	}

	public CreateContainerResponse getAppContainer() {
		return appContainer;
	}

	public void setAppContainer(CreateContainerResponse appContainer) {
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
