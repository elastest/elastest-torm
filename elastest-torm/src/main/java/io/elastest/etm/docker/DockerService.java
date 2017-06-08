package io.elastest.etm.docker;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.model.LogConfig.LoggingType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.docker.utils.ExecStartResultCallbackWebsocket;
import io.elastest.etm.rabbitmq.service.RabbitmqService;
import io.elastest.etm.service.sut.SutService;

@Service
public class DockerService {

	private String testImage = "";
	private static String appImage = "edujgurjc/torm-loadapp";
	private static String logstashImage = "edujgurjc/logstash", dockbeatImage = "edujgurjc/dockbeat";

	@Autowired
	private RabbitmqService rabbitmqService;
	
	@Autowired
	private SutService sutService;

	/* Config Methods */

	public void configureDocker(DockerExecution dockerExec) {
		boolean windowsSo = false;
		if (windowsSo) {
			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost("tcp://192.168.99.100:2376").build();
			dockerExec.setDockerClient(DockerClientBuilder.getInstance(config).build());
		} else {
			dockerExec.setDockerClient(DockerClientBuilder.getInstance().build());
		}
	}

	public void createNetwork(DockerExecution dockerExec) {
		dockerExec.generateNetwork();
		dockerExec.getDockerClient().createNetworkCmd().withName(dockerExec.getNetwork()).exec();
	}

	/* Starting Methods */

	public void startRabbitmq(DockerExecution dockerExec) {
		try {
			System.out.println("Starting Rabbitmq...");
			rabbitmqService.createRabbitmqConnection();
			dockerExec.createRabbitmqConfig();

			for (Map.Entry<String, String> rabbitLine : dockerExec.getRabbitMap().entrySet()) {
				rabbitmqService.createQueue(rabbitLine.getKey());
				rabbitmqService.bindQueueToExchange(rabbitLine.getKey(), "amq.topic", rabbitLine.getValue());
			}

			System.out.println("Successfully started Rabbitmq...");
		} catch (Exception e) {
			e.printStackTrace();
			purgeRabbitmq(dockerExec);
		}
	}

	public void startLogstash(DockerExecution dockerExec) {
		try {
			String envVar = "EXECID=" + dockerExec.getExecutionId();
			String envVar2 = "HOSTIP=" + getHostIp(dockerExec);
			String envVar3 = "RABBITUSER=" + rabbitmqService.getUser();
			String envVar4 = "RABBITPASS=" + rabbitmqService.getPass();

			ArrayList<String> envList = new ArrayList<>();
			envList.add(envVar);
			envList.add(envVar2);
			envList.add(envVar3);
			envList.add(envVar4);

			if (!imageExist(logstashImage, dockerExec)) {
				System.out.println("Pulling logstash image...");
				dockerExec.getDockerClient().pullImageCmd(logstashImage).exec(new PullImageResultCallback())
						.awaitSuccess();
				System.out.println("Pulling logstash image ends");
			} else {
				System.out.println("Logstash image already pulled");
			}

			dockerExec.setLogstashContainer(dockerExec.getDockerClient().createContainerCmd(logstashImage)
					.withEnv(envList).withNetworkMode(dockerExec.getNetwork())
					.withName("logstash_" + dockerExec.getExecutionId()).exec());

			String logstashContainerId = dockerExec.getLogstashContainer().getId();

			dockerExec.setLogstashContainerId(logstashContainerId);

			dockerExec.getDockerClient().startContainerCmd(logstashContainerId).exec();

			String logstashIP = getContainerIp(logstashContainerId, dockerExec);
			if (logstashIP == null || logstashIP.isEmpty()) {
				throw new Exception();
			}
			dockerExec.setLogstashIP(logstashIP);

			this.manageLogstash(dockerExec);

		} catch (Exception e) {
			e.printStackTrace();
			endLogstashExec(dockerExec);
			purgeRabbitmq(dockerExec);
		}
	}

	public void manageLogstash(DockerExecution dockerExec) {
		System.out.println("Starting logstash");
		try {

			Object lock = new Object();
			ExecStartResultCallbackWebsocket execStartResultCallbackWebsocket = new ExecStartResultCallbackWebsocket();
			execStartResultCallbackWebsocket.setLock(lock);

			synchronized (execStartResultCallbackWebsocket.getLock()) {
				dockerExec.getDockerClient().logContainerCmd(dockerExec.getLogstashContainerId()).withStdErr(true)
						.withStdOut(true).withFollowStream(true).exec(execStartResultCallbackWebsocket);
				execStartResultCallbackWebsocket.getLock().wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void startBeats(DockerExecution dockerExec) {
		try {
			String envVar = "LOGSTASHIP=" + dockerExec.getLogstashIP() + ":5044";

			if (!imageExist(dockbeatImage, dockerExec)) {
				System.out.println("Pulling dockbeat image...");
				dockerExec.getDockerClient().pullImageCmd(dockbeatImage).exec(new PullImageResultCallback())
						.awaitSuccess();
				System.out.println("Pulling dockbeat image ends");
			} else {
				System.out.println("Dockbeat image already pulled");
			}
			Volume volume = new Volume("/var/run/docker.sock");

			dockerExec.setDockbeatContainer(dockerExec.getDockerClient().createContainerCmd(dockbeatImage)
					.withEnv(envVar).withNetworkMode(dockerExec.getNetwork()).withVolumes(volume)
					.withBinds(new Bind("/var/run/docker.sock", volume))
					.withName("beats_" + dockerExec.getExecutionId()).exec());

			String dockbeatContainerId = dockerExec.getDockbeatContainer().getId();
			dockerExec.setDockbeatContainerId(dockbeatContainerId);

			dockerExec.getDockerClient().startContainerCmd(dockbeatContainerId).exec();
			Thread.sleep(2000);

		} catch (Exception e) {
			e.printStackTrace();
			endBeatsExec(dockerExec);
			endLogstashExec(dockerExec);
			purgeRabbitmq(dockerExec);
		}
	}

	public void startSut(DockerExecution dockerExec) {
		SutExecution sutExec = sutService.createSutExecutionBySut(dockerExec.gettJobexec().getTjob().getSut());
		try {
			System.out.println("Starting sut");
			String envVar = "REPO_URL=https://github.com/EduJGURJC/springbootdemo";

			LogConfig logConfig = new LogConfig();
			logConfig.setType(LoggingType.SYSLOG);
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("syslog-address", "tcp://" + dockerExec.getLogstashIP() + ":5001");
			logConfig.setConfig(configMap);

			dockerExec.getDockerClient().pullImageCmd(appImage).exec(new PullImageResultCallback()).awaitSuccess();

			dockerExec.setAppContainer(dockerExec.getDockerClient().createContainerCmd(appImage).withEnv(envVar)
					.withLogConfig(logConfig).withNetworkMode(dockerExec.getNetwork())
					.withName("sut_" + dockerExec.getExecutionId()).exec());

			sutExec.deployStatus(SutExecution.DeployStatusEnum.DEPLOYED);

			String appContainerId = dockerExec.getAppContainer().getId();
			dockerExec.setAppContainerId(appContainerId);

			dockerExec.getDockerClient().startContainerCmd(appContainerId).exec();

			String sutIP = getContainerIp(appContainerId, dockerExec);
			sutIP = sutIP.split("/")[0];
			sutIP = "http://" + sutIP + ":8080";
			sutExec.setUrl(sutIP);
		} catch (Exception e) {
			e.printStackTrace();
			sutExec.deployStatus(SutExecution.DeployStatusEnum.ERROR);
			endSutExec(dockerExec);
			endBeatsExec(dockerExec);
			endLogstashExec(dockerExec);
			purgeRabbitmq(dockerExec);
		}
		dockerExec.setSutExec(sutExec);
	}

	public void startTest(String testImage, DockerExecution dockerExec) {
		try {
			System.out.println("Starting test");
			this.testImage = testImage;
			// ExposedPort tcp6080 = ExposedPort.tcp(6080);
			//
			// Ports portBindings = new Ports();
			// portBindings.bind(tcp6080, Binding.bindPort(6080));

			String envVar = "DOCKER_HOST=tcp://172.17.0.1:2376";
			String envVar2 = "APP_IP=" + (dockerExec.isWithSut() ? dockerExec.getSutExec().getUrl() : "0");
			String envVar3 = "NETWORK=" + dockerExec.getNetwork();
			ArrayList<String> envList = new ArrayList<>();
			envList.add(envVar);
			envList.add(envVar2);
			envList.add(envVar3);

			// Volume volume = new Volume(volumeDirectory);

			LogConfig logConfig = new LogConfig();
			logConfig.setType(LoggingType.SYSLOG);
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("syslog-address", "tcp://" + dockerExec.getLogstashIP() + ":5000");
			logConfig.setConfig(configMap);

			dockerExec.getDockerClient().pullImageCmd(testImage).exec(new PullImageResultCallback()).awaitSuccess();

			dockerExec.setTestcontainer(dockerExec.getDockerClient().createContainerCmd(testImage)
					// .withExposedPorts(tcp6080).withPortBindings(portBindings)
					// .withVolumes(volume).withBinds(new Bind(volumeDirectory,
					// volume))
					.withEnv(envList).withLogConfig(logConfig).withNetworkMode(dockerExec.getNetwork())
					.withName("test_" + dockerExec.getExecutionId()).exec());

			String testContainerId = dockerExec.getTestcontainer().getId();
			dockerExec.setTestContainerId(testContainerId);

			dockerExec.getDockerClient().startContainerCmd(testContainerId).exec();
			int code = dockerExec.getDockerClient().waitContainerCmd(testContainerId)
					.exec(new WaitContainerResultCallback()).awaitStatusCode();
			System.out.println("Test container ends with code " + code);

			// this.saveTestSuite();
		} catch (Exception e) {
			e.printStackTrace();
			endAllExec(dockerExec);
		}
	}

	// public void saveTestSuite() {
	// File surefireXML = new File(this.surefirePath);
	// List<File> reportsDir = new ArrayList<>();
	// reportsDir.add(surefireXML);
	//
	// SurefireReportParser surefireReport = new
	// SurefireReportParser(reportsDir, new Locale("en", "US"), null);
	// try {
	// List<ReportTestSuite> testSuites = surefireReport.parseXMLReportFiles();
	//
	// ObjectMapper mapper = new ObjectMapper();
	// // Object to JSON in file
	// try {
	// mapper.writeValue(new File(this.testsuitesPath), testSuites);
	// } catch (JsonGenerationException e) {
	// e.printStackTrace();
	// } catch (JsonMappingException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// } catch (MavenReportException e) {
	// e.printStackTrace();
	// }
	// }

	/* End execution methods */

	public void endAllExec(DockerExecution dockerExec) {
		endTestExec(dockerExec);
		if (dockerExec.isWithSut()) {
			endSutExec(dockerExec);
		}
		endBeatsExec(dockerExec);
		endLogstashExec(dockerExec);
		purgeRabbitmq(dockerExec);
	}

	public void endTestExec(DockerExecution dockerExec) {
		try {
			System.out.println("Ending test execution " + dockerExec.getExecutionId());
			try {
				dockerExec.getDockerClient().stopContainerCmd(dockerExec.getTestContainerId()).exec();
			} catch (Exception e) {
			}
			dockerExec.getDockerClient().removeContainerCmd(dockerExec.getTestContainerId()).exec();
			try {
				dockerExec.getDockerClient().removeImageCmd(testImage).withForce(true).exec();
			} catch (Exception e) {
				System.out.println("Remove image " + testImage + " failed. In use" + dockerExec.getExecutionId());
			}
		} catch (Exception e) {
			System.out.println("Error on ending test execution");

		}
	}

	public void endSutExec(DockerExecution dockerExec) {
		SutExecution sutExec = dockerExec.getSutExec();
		sutExec.deployStatus(SutExecution.DeployStatusEnum.UNDEPLOYING);
		try {
			System.out.println("Ending sut execution " + dockerExec.getExecutionId());
			try {
				dockerExec.getDockerClient().stopContainerCmd(dockerExec.getAppContainerId()).exec();
			} catch (Exception e) {
			}
			dockerExec.getDockerClient().removeContainerCmd(dockerExec.getAppContainerId()).exec();
			try {
				dockerExec.getDockerClient().removeImageCmd(appImage).withForce(true).exec();
			} catch (Exception e) {
				System.out.println("Remove image " + appImage + " failed. In use" + dockerExec.getExecutionId());
			}
			sutExec.deployStatus(SutExecution.DeployStatusEnum.UNDEPLOYED);
		} catch (Exception e) {
			sutExec.deployStatus(SutExecution.DeployStatusEnum.ERROR);
			System.out.println("Error on ending Sut execution");
		}
		dockerExec.setSutExec(sutExec);
		sutService.modifySutExec(dockerExec.getSutExec());
	}

	public void endLogstashExec(DockerExecution dockerExec) {
		try {
			System.out.println("Ending Logstash execution " + dockerExec.getExecutionId());
			dockerExec.getDockerClient().stopContainerCmd(dockerExec.getLogstashContainerId()).exec();
			dockerExec.getDockerClient().removeContainerCmd(dockerExec.getLogstashContainerId()).exec();
		} catch (Exception e) {
			System.out.println("Error on ending Logstash execution");
		}
		System.out.println("Removing docker network... " + dockerExec.getExecutionId());
		dockerExec.getDockerClient().removeNetworkCmd(dockerExec.getNetwork()).exec();
	}

	public void purgeRabbitmq(DockerExecution dockerExec) {
		try {
			System.out.println("Purging Rabbitmq " + dockerExec.getExecutionId());

			for (Map.Entry<String, String> rabbitLine : dockerExec.getRabbitMap().entrySet()) {
				rabbitmqService.deleteQueue(rabbitLine.getKey());
			}
			rabbitmqService.closeChannel();
			rabbitmqService.closeConnection();
		} catch (Exception e) {
			System.out.println("Error on purging Rabbitmq");
		}
	}

	public void endBeatsExec(DockerExecution dockerExec) {
		try {
			System.out.println("Ending dockbeat execution " + dockerExec.getExecutionId());
			dockerExec.getDockerClient().stopContainerCmd(dockerExec.getDockbeatContainerId()).exec();
			dockerExec.getDockerClient().removeContainerCmd(dockerExec.getDockbeatContainerId()).exec();
		} catch (Exception e) {
			System.out.println("Error on ending dockbeat execution");
		}
	}
	
	

	/* Utils */

	public String getContainerIp(String containerId, DockerExecution dockerExec) {
		return dockerExec.getDockerClient().inspectContainerCmd(containerId).exec().getNetworkSettings().getNetworks()
				.get(dockerExec.getNetwork()).getIpAddress();
	}

	public String getHostIp(DockerExecution dockerExec) {
		return dockerExec.getDockerClient().inspectNetworkCmd().withNetworkId(dockerExec.getNetwork()).exec().getIpam()
				.getConfig().get(0).getGateway();
	}

	public boolean imageExist(String imageName, DockerExecution dockerExec) {
		return !dockerExec.getDockerClient().searchImagesCmd(imageName).exec().isEmpty();
	}

	
	/* */

	public void loadBasicServices(DockerExecution dockerExec) throws Exception {
		try {
			configureDocker(dockerExec);
			createNetwork(dockerExec);
			startRabbitmq(dockerExec);
			startLogstash(dockerExec);
			startBeats(dockerExec);
			if(dockerExec.isWithSut()){
				startSut(dockerExec);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
	}

}
