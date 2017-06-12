package io.elastest.etm.docker;

import java.util.HashMap;
import java.util.Map;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.api.model.TJobExecution;

public class DockerExecution {
	private DockerClient dockerClient;
	private CreateContainerResponse testcontainer, appContainer;
	private String testContainerId, appContainerId;

	private String network;
	private String executionId;
	private String queuePrefix;
	private Map<String, String> rabbitMap;
	private TJobExecution tJobexec;
	private boolean withSut;
	private SutExecution sutExec;

	public DockerExecution() {
	}

	public DockerExecution(TJobExecution tJobExec) {
		this.tJobexec = tJobExec;
		this.withSut = tJobExec.getTjob().getSut() != null;
	}

	public String initializeLog() {
		setExecutionId(tJobexec.getId().toString());
		return "localhost:9200/" + executionId;
	}

	public void generateNetwork() {
		setNetwork("TORM-" + tJobexec.getId().toString());
	}

	public void createRabbitmqConfig() {
		queuePrefix = "q-" + executionId;
		rabbitMap = new HashMap<String, String>();
		rabbitMap.put(queuePrefix + "-test-log", "test." + executionId + ".log");
		rabbitMap.put(queuePrefix + "-test-metrics", "test." + executionId + ".metrics");
		if (withSut) {
			rabbitMap.put(queuePrefix + "-sut-log", "sut." + executionId + ".log");
			rabbitMap.put(queuePrefix + "-sut-metrics", "sut." + executionId + ".metrics");
		}
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

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getQueuePrefix() {
		return queuePrefix;
	}

	public void setQueuePrefix(String queuePrefix) {
		this.queuePrefix = queuePrefix;
	}

	public Map<String, String> getRabbitMap() {
		return rabbitMap;
	}

	public void setRabbitMap(Map<String, String> rabbitMap) {
		this.rabbitMap = rabbitMap;
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
}
