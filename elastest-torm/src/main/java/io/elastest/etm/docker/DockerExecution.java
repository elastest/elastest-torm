package io.elastest.etm.docker;

import java.util.HashMap;
import java.util.Map;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.api.model.TJobExecution;

public class DockerExecution {
	private DockerClient dockerClient;
	private CreateContainerResponse testcontainer, appContainer, logstashContainer, dockbeatContainer;
	private String testContainerId, appContainerId, logstashContainerId, dockbeatContainerId;

	// private String surefirePath =
	// "/testcontainers-java-examples/selenium-container/target/surefire-reports";
	// private String testsuitesPath = "/home/edujg/torm/testsuites.json";

	private String network, logstashIP;
	private String executionId;
	private String exchangePrefix, queuePrefix;
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
		// setExecutionId(RandomStringUtils.randomAlphanumeric(17).toLowerCase());
		setExecutionId(tJobexec.getId().toString());
		return "localhost:9200/" + executionId;
	}

	public void generateNetwork() {
		// setNetwork("Logstash-" + RandomStringUtils.randomAlphanumeric(19));
		setNetwork("Logstash-" + tJobexec.getId().toString());
	}

	public void createRabbitmqConfig() {
		exchangePrefix = "ex-" + executionId;
		queuePrefix = "q-" + executionId;
		rabbitMap = new HashMap<String, String>();
		rabbitMap.put(exchangePrefix + "-test", queuePrefix + "-test");
//		if (withSut) {
			rabbitMap.put(exchangePrefix + "-sut", queuePrefix + "-sut");
//		}
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

	public CreateContainerResponse getLogstashContainer() {
		return logstashContainer;
	}

	public void setLogstashContainer(CreateContainerResponse logstashContainer) {
		this.logstashContainer = logstashContainer;
	}

	public CreateContainerResponse getDockbeatContainer() {
		return dockbeatContainer;
	}

	public void setDockbeatContainer(CreateContainerResponse dockbeatContainer) {
		this.dockbeatContainer = dockbeatContainer;
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

	public String getLogstashContainerId() {
		return logstashContainerId;
	}

	public void setLogstashContainerId(String logstashContainerId) {
		this.logstashContainerId = logstashContainerId;
	}

	public String getDockbeatContainerId() {
		return dockbeatContainerId;
	}

	public void setDockbeatContainerId(String dockbeatContainerId) {
		this.dockbeatContainerId = dockbeatContainerId;
	}
	//
	// public String getSurefirePath() {
	// return surefirePath;
	// }
	//
	// public void setSurefirePath(String surefirePath) {
	// this.surefirePath = surefirePath;
	// }
	//
	// public String getTestsuitesPath() {
	// return testsuitesPath;
	// }
	//
	// public void setTestsuitesPath(String testsuitesPath) {
	// this.testsuitesPath = testsuitesPath;
	// }

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getLogstashIP() {
		return logstashIP;
	}

	public void setLogstashIP(String logstashIP) {
		this.logstashIP = logstashIP;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getExchangePrefix() {
		return exchangePrefix;
	}

	public void setExchangePrefix(String exchangePrefix) {
		this.exchangePrefix = exchangePrefix;
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
