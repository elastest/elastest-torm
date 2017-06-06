package io.elastest.etm.docker;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.model.LogConfig.LoggingType;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.elastest.etm.api.model.SutExecution;
import io.elastest.etm.docker.utils.ExecStartResultCallbackWebsocket;
import io.elastest.etm.rabbitmq.service.RabbitmqService;

@Service
public class DockerService {

	private String testImage = "";
	private static String appImage = "edujgurjc/torm-loadapp";
	private static String logstashImage = "edujgurjc/logstash", dockbeatImage = "edujgurjc/dockbeat";
	private static final String volumeDirectory = "/springbootdemotest/springbootdemotest";

	@Autowired
	private ApplicationContext context;

	@Autowired
	private RabbitmqService rabbitmqService;

	@Autowired
	private DockerClient dockerClient;

	/* Config Methods */

	public void createNetwork(DockerExecution dockerExec) {
		dockerExec.generateNetwork();
		this.dockerClient.createNetworkCmd().withName(dockerExec.getNetwork()).exec();
	}

	/* Starting Methods */

	public void startRabbitmq(DockerExecution dockerExec) {
		try {
			System.out.println("Starting Rabbitmq...");
			rabbitmqService.createRabbitmqConnection();
			dockerExec.createRabbitmqConfig();

			String exchange = dockerExec.getExchange();
			String queue = dockerExec.getQueue();

			rabbitmqService.createFanoutExchange(exchange);
			rabbitmqService.createQueue(queue);
			rabbitmqService.bindQueueToExchange(queue, exchange, "1");
			System.out.println("Successfully started Rabbitmq...");
		} catch (Exception e) {
			e.printStackTrace();
			purgeRabbitmq(dockerExec);
		}
	}

	public void startLogstash(DockerExecution dockerExec) {
		try {
			String envVar = "ELASID=" + dockerExec.getExecutionId();
			String envVar2 = "HOSTIP=" + getHostIp(dockerExec);
			String envVar3 = "EXCHANGENAME=" + dockerExec.getExchange();

			ArrayList<String> envList = new ArrayList<>();
			envList.add(envVar);
			envList.add(envVar2);
			envList.add(envVar3);

			System.out.println("Pulling logstash image...");
			this.dockerClient.pullImageCmd(logstashImage).exec(new PullImageResultCallback()).awaitSuccess();
			System.out.println("Pulling logstash image ends");

			dockerExec.setLogstashContainer(this.dockerClient.createContainerCmd(logstashImage).withEnv(envList)
					.withNetworkMode(dockerExec.getNetwork())
					.withName("logstash_container_" + dockerExec.getExecutionId()).exec());

			String logstashContainerId = dockerExec.getLogstashContainer().getId();

			dockerExec.setLogstashContainerId(logstashContainerId);

			this.dockerClient.startContainerCmd(logstashContainerId).exec();

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
			ExecStartResultCallbackWebsocket execStartResultCallbackWebsocket = context
					.getBean(ExecStartResultCallbackWebsocket.class);
			execStartResultCallbackWebsocket.setLock(lock);

			synchronized (lock) {
				this.dockerClient.logContainerCmd(dockerExec.getLogstashContainerId()).withStdErr(true).withStdOut(true)
						.withFollowStream(true).exec(execStartResultCallbackWebsocket);
				lock.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void startBeats(DockerExecution dockerExec) {
		try {
			String envVar = "LOGSTASHIP=" + dockerExec.getLogstashIP() + ":5044";

			System.out.println("Pulling dockbeat image...");
			this.dockerClient.pullImageCmd(dockbeatImage).exec(new PullImageResultCallback()).awaitSuccess();
			System.out.println("Pulling dockbeat image ends");

			Volume volume = new Volume("/var/run/docker.sock");

			dockerExec.setDockbeatContainer(this.dockerClient.createContainerCmd(dockbeatImage).withEnv(envVar)
					.withNetworkMode(dockerExec.getNetwork()).withVolumes(volume)
					.withBinds(new Bind("/var/run/docker.sock", volume))
					.withName("beats_container_" + dockerExec.getExecutionId()).exec());

			String dockbeatContainerId = dockerExec.getDockbeatContainer().getId();
			dockerExec.setDockbeatContainerId(dockbeatContainerId);

			this.dockerClient.startContainerCmd(dockbeatContainerId).exec();
			Thread.sleep(2000);

		} catch (Exception e) {
			e.printStackTrace();
			endBeatsExec(dockerExec);
			endLogstashExec(dockerExec);
			purgeRabbitmq(dockerExec);
		}
	}

	public SutExecution startSut(SutExecution sutExec, DockerExecution dockerExec) {
		try {
			System.out.println("Starting sut");
			String envVar = "REPO_URL=https://github.com/EduJGURJC/springbootdemo";

			LogConfig logConfig = new LogConfig();
			logConfig.setType(LoggingType.SYSLOG);
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("syslog-address", "tcp://" + dockerExec.getLogstashIP() + ":5001");
			logConfig.setConfig(configMap);

			this.dockerClient.pullImageCmd(appImage).exec(new PullImageResultCallback()).awaitSuccess();

			dockerExec.setAppContainer(this.dockerClient.createContainerCmd(appImage).withEnv(envVar)
					.withLogConfig(logConfig).withNetworkMode(dockerExec.getNetwork())
					.withName("sut_container_" + dockerExec.getExecutionId()).exec());

			sutExec.deployStatus(SutExecution.DeployStatusEnum.DEPLOYED);

			String appContainerId = dockerExec.getAppContainer().getId();
			dockerExec.setAppContainerId(appContainerId);

			this.dockerClient.startContainerCmd(appContainerId).exec();

			String sutIP = getContainerIp(appContainerId, dockerExec);
			sutIP = sutIP.split("/")[0];
			sutIP = "http://" + sutIP + ":8080";
			dockerExec.setSutIP(sutIP);

			sutExec.setUrl(sutIP);
		} catch (Exception e) {
			e.printStackTrace();
			sutExec.deployStatus(SutExecution.DeployStatusEnum.ERROR);
			endSutExec(dockerExec);
			endBeatsExec(dockerExec);
			endLogstashExec(dockerExec);
			purgeRabbitmq(dockerExec);
		}
		return sutExec;
	}

	public void startTest(String testImage, DockerExecution dockerExec) {
		try {
			System.out.println("Starting test");
			this.testImage = testImage;
			ExposedPort tcp6080 = ExposedPort.tcp(6080);

			Ports portBindings = new Ports();
			portBindings.bind(tcp6080, Binding.bindPort(6080));

			String envVar = "DOCKER_HOST=tcp://172.17.0.1:2376";
			String envVar2 = "APP_IP=" + dockerExec.getSutIP();
			String envVar3 = "NETWORK=" + dockerExec.getNetwork();
			ArrayList<String> envList = new ArrayList<>();
			envList.add(envVar);
			envList.add(envVar2);
			envList.add(envVar3);

			Volume volume = new Volume(volumeDirectory);

			LogConfig logConfig = new LogConfig();
			logConfig.setType(LoggingType.SYSLOG);
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("syslog-address", "tcp://" + dockerExec.getLogstashIP() + ":5000");
			logConfig.setConfig(configMap);

			this.dockerClient.pullImageCmd(testImage).exec(new PullImageResultCallback()).awaitSuccess();

			dockerExec.setTestcontainer(this.dockerClient.createContainerCmd(testImage).withExposedPorts(tcp6080)
					.withPortBindings(portBindings).withVolumes(volume).withBinds(new Bind(volumeDirectory, volume))
					.withEnv(envList).withLogConfig(logConfig).withNetworkMode(dockerExec.getNetwork())
					.withName("test_container_" + dockerExec.getExecutionId()).exec());

			String testContainerId = dockerExec.getTestcontainer().getId();
			dockerExec.setTestContainerId(testContainerId);

			this.dockerClient.startContainerCmd(testContainerId).exec();
			int code = this.dockerClient.waitContainerCmd(testContainerId).exec(new WaitContainerResultCallback())
					.awaitStatusCode();
			System.out.println("Test container ends with code " + code);

			// this.saveTestSuite();
		} catch (Exception e) {
			endExec(dockerExec);
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

	public void endExec(DockerExecution dockerExec) {
		endTestExec(dockerExec);
		endBeatsExec(dockerExec);
		endLogstashExec(dockerExec);
		purgeRabbitmq(dockerExec);
	}

	public void endAllExec(DockerExecution dockerExec) {
		endTestExec(dockerExec);
		endSutExec(dockerExec);
		endBeatsExec(dockerExec);
		endLogstashExec(dockerExec);
		purgeRabbitmq(dockerExec);
	}

	public void endTestExec(DockerExecution dockerExec) {
		try {
			System.out.println("Ending test execution");
			try {
				this.dockerClient.stopContainerCmd(dockerExec.getTestContainerId()).exec();
			} catch (Exception e) {
			}
			this.dockerClient.removeContainerCmd(dockerExec.getTestContainerId()).exec();
			this.dockerClient.removeImageCmd(testImage).withForce(true).exec();
		} catch (Exception e) {
			System.out.println("Error on ending test execution");

		}
	}

	public void endSutExec(DockerExecution dockerExec) {
		try {
			System.out.println("Ending sut execution");
			try {
				this.dockerClient.stopContainerCmd(dockerExec.getAppContainerId()).exec();
			} catch (Exception e) {
			}
			this.dockerClient.removeContainerCmd(dockerExec.getAppContainerId()).exec();
			this.dockerClient.removeImageCmd(appImage).withForce(true).exec();
		} catch (Exception e) {
			System.out.println("Error on ending Sut execution");
		}
	}

	public void endLogstashExec(DockerExecution dockerExec) {
		try {
			System.out.println("Ending Logstash execution");
			this.dockerClient.stopContainerCmd(dockerExec.getLogstashContainerId()).exec();
			this.dockerClient.removeContainerCmd(dockerExec.getLogstashContainerId()).exec();
		} catch (Exception e) {
			System.out.println("Error on ending Logstash execution");
		}
		System.out.println("Removing docker network...");
		this.dockerClient.removeNetworkCmd(dockerExec.getNetwork()).exec();
	}

	public void purgeRabbitmq(DockerExecution dockerExec) {
		try {
			System.out.println("Purging Rabbitmq");
			rabbitmqService.deleteQueue(dockerExec.getQueue());
			rabbitmqService.deleteFanoutExchange(dockerExec.getExchange());
			rabbitmqService.closeChannel();
			rabbitmqService.closeConnection();
		} catch (Exception e) {
			System.out.println("Error on purging Rabbitmq");
		}
	}

	public void endBeatsExec(DockerExecution dockerExec) {
		try {
			System.out.println("Ending dockbeat execution");
			this.dockerClient.stopContainerCmd(dockerExec.getDockbeatContainerId()).exec();
			this.dockerClient.removeContainerCmd(dockerExec.getDockbeatContainerId()).exec();
		} catch (Exception e) {
			System.out.println("Error on ending dockbeat execution");
		}
	}

	/* Utils */

	public String getContainerIp(String containerId, DockerExecution dockerExec) {
		return this.dockerClient.inspectContainerCmd(containerId).exec().getNetworkSettings().getNetworks()
				.get(dockerExec.getNetwork()).getIpAddress();
	}

	public String getHostIp(DockerExecution dockerExec) {
		return this.dockerClient.inspectNetworkCmd().withNetworkId(dockerExec.getNetwork()).exec().getIpam().getConfig()
				.get(0).getGateway();
	}

	/* */

	public void loadBasicServices(DockerExecution dockerExec) throws Exception {
		try {
			createNetwork(dockerExec);
			startRabbitmq(dockerExec);
			startLogstash(dockerExec);
			startBeats(dockerExec);
		} catch (Exception e) {
			throw new Exception();
		}
	}

}
