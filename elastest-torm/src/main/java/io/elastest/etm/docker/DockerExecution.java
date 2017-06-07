package io.elastest.etm.docker;

import org.apache.commons.lang.RandomStringUtils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

import io.elastest.etm.api.model.TJobExecution;

public class DockerExecution {
	private DockerClient dockerClient;
	private CreateContainerResponse testcontainer, appContainer, logstashContainer, dockbeatContainer;
	private String testContainerId, appContainerId, logstashContainerId, dockbeatContainerId;

//	private String surefirePath = "/testcontainers-java-examples/selenium-container/target/surefire-reports";
//	private String testsuitesPath = "/home/edujg/torm/testsuites.json";

	private String network, logstashIP, sutIP;

	private String executionId;
	private String exchange, queue;
	
	private TJobExecution tJobexec;
	
	public DockerExecution(){}
	
	

	public String initializeLog() {
//		setExecutionId(RandomStringUtils.randomAlphanumeric(17).toLowerCase());
		setExecutionId(tJobexec.getId().toString());
		return "localhost:9200/" + executionId;
	}
	
	public void generateNetwork(){
//		setNetwork("Logstash-" + RandomStringUtils.randomAlphanumeric(19));
		setNetwork("Logstash-" + tJobexec.getId().toString());
	}
	
	public void createRabbitmqConfig(){
		setExchange("ex-" + executionId);
		setQueue("q-" + executionId);
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
//	public String getSurefirePath() {
//		return surefirePath;
//	}
//
//	public void setSurefirePath(String surefirePath) {
//		this.surefirePath = surefirePath;
//	}
//
//	public String getTestsuitesPath() {
//		return testsuitesPath;
//	}
//
//	public void setTestsuitesPath(String testsuitesPath) {
//		this.testsuitesPath = testsuitesPath;
//	}

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

	public String getSutIP() {
		return sutIP;
	}

	public void setSutIP(String sutIP) {
		this.sutIP = sutIP;
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}


	public TJobExecution gettJobexec() {
		return tJobexec;
	}


	public void settJobexec(TJobExecution tJobexec) {
		this.tJobexec = tJobexec;
	}
	
	
}
