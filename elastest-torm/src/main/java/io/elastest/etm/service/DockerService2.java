package io.elastest.etm.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.TestSuiteXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.LogConfig.LoggingType;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.utils.UtilTools;

@Service
public class DockerService2 {

	private static final Logger logger = LoggerFactory.getLogger(DockerService2.class);

	private static final String DOKCER_LISTENING_ON_TCP_PORT_PREFIX = "tcp://";
	private static String appImage = "elastest/test-etm-javasutrepo", checkImage = "elastest/etm-check-service-up";

	@Value("${logstash.host:#{null}}")
	private String logstashHost;

	@Value("${elastest.docker.network}")
	private String elastestNetwork;

	@Autowired
	private SutService sutService;

	@Autowired
	public UtilTools utilTools;

	public void loadBasicServices(DockerExecution dockerExec) throws Exception {
		configureDocker(dockerExec);
		dockerExec.setNetwork(elastestNetwork);
		if (dockerExec.isWithSut()) {
			startSut(dockerExec);
		}
	}

	public DockerClient getDockerClient() {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
		return DockerClientBuilder.getInstance(config).build();
	}

	/* Config Methods */

	public void configureDocker(DockerExecution dockerExec) {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
		dockerExec.setDockerClient(DockerClientBuilder.getInstance(config).build());
	}

	public String runDockerContainer(DockerClient dockerClient, String imageName, List<String> envs,
			String containerName, String targetContainerName, String networkName, Ports portBindings, int listenPort) {
		String dockerContainerName = null;

		dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitSuccess();
		CreateContainerResponse container = dockerClient.createContainerCmd(imageName).withName(containerName)
				.withEnv(envs).withNetworkMode(networkName).withExposedPorts(ExposedPort.tcp(listenPort))
				.withPortBindings(portBindings).withPublishAllPorts(true).exec();

		dockerClient.startContainerCmd(container.getId()).exec();

		logger.info("Id del contenedor:" + container.getId());

		return container.getId();
	}

	public void removeDockerContainer(String containerId, DockerClient dockerClient) {
		dockerClient.removeContainerCmd(containerId).exec();
	}

	public void stopDockerContainer(String containerId, DockerClient dockerClient) {
		dockerClient.stopContainerCmd(containerId).exec();
	}

	public void stopDockerContainer(DockerClient dockerClient, String containerId) {
		dockerClient.stopContainerCmd(containerId).exec();
	}

	/* Starting Methods */

	public void startSut(DockerExecution dockerExec) {
		SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
		SutExecution sutExec;
		int sutPort = 8080;
		String sutIP = "";

		// If it's MANAGED SuT
		if (sut.getSutType() != SutTypeEnum.DEPLOYED) {
			logger.info("Starting sut " + dockerExec.getExecutionId());
			sutExec = sutService.createSutExecutionBySut(sut);
			try {
				String sutImage = appImage;
				String envVar = "";
				if (sut.getSutType() == SutTypeEnum.MANAGED) {
					sutImage = sut.getSpecification();
					envVar = "REPO_URL=none";
				} else {
					envVar = "REPO_URL=" + sut.getSpecification();
				}
				logger.info("Sut " + dockerExec.getExecutionId() + " image: " + sutImage);
				String sutName = "sut_" + dockerExec.getExecutionId();

				LogConfig logConfig = getLogConfig(5001, "sut_", dockerExec);

				dockerExec.getDockerClient().pullImageCmd(sutImage).exec(new PullImageResultCallback()).awaitSuccess();

				dockerExec.setAppContainer(dockerExec.getDockerClient().createContainerCmd(sutImage).withEnv(envVar)
						.withLogConfig(logConfig).withName(sutName).withNetworkMode(dockerExec.getNetwork()).exec());

				sutExec.setDeployStatus(SutExecution.DeployStatusEnum.DEPLOYED);

				String appContainerId = dockerExec.getAppContainer().getId();
				dockerExec.setAppContainerId(appContainerId);

				dockerExec.getDockerClient().startContainerCmd(appContainerId).exec();

				sutIP = getContainerIp(appContainerId, dockerExec);

				// Wait for Sut started
				checkSut(dockerExec, sutIP, sutPort + "");
			} catch (Exception e) {
				e.printStackTrace();
				sutExec.setDeployStatus(SutExecution.DeployStatusEnum.ERROR);
				endSutExec(dockerExec);
			}
		} else { // If it's DEPLOYED SuT
			Long currentSutExecId = sut.getCurrentSutExec();
			sutExec = sutService.getSutExecutionById(currentSutExecId);
			sutIP = sut.getSpecification();
		}

		String sutUrl = "http://" + sutIP + ":" + sutPort;
		sutExec.setUrl(sutUrl);

		dockerExec.setSutExec(sutExec);
	}

	public void checkSut(DockerExecution dockerExec, String ip, String port) {
		String envVar = "IP=" + ip;
		String envVar2 = "PORT=" + port;
		ArrayList<String> envList = new ArrayList<>();
		envList.add(envVar);
		envList.add(envVar2);

		dockerExec.getDockerClient().pullImageCmd(checkImage).exec(new PullImageResultCallback()).awaitSuccess();

		String checkContainerId = dockerExec.getDockerClient().createContainerCmd(checkImage).withEnv(envList)
				.withName("check_" + dockerExec.getExecutionId()).withNetworkMode(dockerExec.getNetwork()).exec()
				.getId();
		dockerExec.getDockerClient().startContainerCmd(checkContainerId).exec();

		dockerExec.getDockerClient().waitContainerCmd(checkContainerId).exec(new WaitContainerResultCallback())
				.awaitStatusCode();
		logger.info("Sut is ready " + dockerExec.getExecutionId());

		try {
			try {
				dockerExec.getDockerClient().stopContainerCmd(checkContainerId).exec();
			} catch (Exception e) {
			}
			dockerExec.getDockerClient().removeContainerCmd(checkContainerId).exec();
		} catch (Exception e) {
		}
	}

	public List<ReportTestSuite> executeTest(DockerExecution dockerExec) {
		try {
			logger.info("Starting test " + dockerExec.getExecutionId());
			String testImage = dockerExec.gettJobexec().getTjob().getImageName();
			logger.info("host: " + getHostIp(dockerExec));

			// Environment variables (optional)
			ArrayList<String> envList = new ArrayList<>();
			String envVar;

			// Get TestSupportService Env Vars
			for (Map.Entry<String, String> entry : dockerExec.gettJobexec().getTssEnvVars().entrySet()) {
				envVar = entry.getKey() + "=" + entry.getValue();
				envList.add(envVar);
			}

			// Get Parameters and insert into Env Vars
			for (Parameter parameter : dockerExec.gettJobexec().getParameters()) {
				envVar = parameter.getName() + "=" + parameter.getValue();
				envList.add(envVar);
			}

			if (dockerExec.isWithSut()) {
				envVar = "APP_IP=" + dockerExec.getSutExec().getUrl();
				envList.add(envVar);
			}

			// Commands (optional)
			ArrayList<String> cmdList = new ArrayList<>();
			String commands = dockerExec.gettJobexec().getTjob().getCommands();
			if (commands != null && !commands.isEmpty()) {
				cmdList.add("sh");
				cmdList.add("-c");
				cmdList.add(commands);
			}

			LogConfig logConfig = getLogConfig(5000, "test_", dockerExec);

			dockerExec.getDockerClient().pullImageCmd(testImage).exec(new PullImageResultCallback()).awaitSuccess();

			CreateContainerResponse testContainer = dockerExec.getDockerClient().createContainerCmd(testImage)
					.withEnv(envList).withLogConfig(logConfig).withName("test_" + dockerExec.getExecutionId())
					.withCmd(cmdList).withNetworkMode(dockerExec.getNetwork()).exec();

			String testContainerId = testContainer.getId();

			dockerExec.setTestcontainer(testContainer);
			dockerExec.setTestContainerId(testContainerId);

			dockerExec.getDockerClient().startContainerCmd(testContainerId).exec();

			// this essentially test the since=0 case
			// dockerClient.logContainerCmd(container.getId())
			// .withStdErr(true)
			// .withStdOut(true)
			// .withFollowStream(true)
			// .withTailAll()
			// .exec(loggingCallback);

			int code = dockerExec.getDockerClient().waitContainerCmd(testContainerId)
					.exec(new WaitContainerResultCallback()).awaitStatusCode();

			logger.info("Test container ends with code " + code);

			return getTestResults(dockerExec);

		} catch (Exception e) {
			e.printStackTrace();
			try {
				endAllExec(dockerExec);
			} catch (Exception e1) {
			}
			return new ArrayList<ReportTestSuite>();
		}
	}

	public LogConfig getLogConfig(int port, String tagPrefix, DockerExecution dockerExec) {

		if (logstashHost == null) {
			logstashHost = getHostIpByNetwork(dockerExec, dockerExec.getNetwork());
		}
		logger.info("Logstash IP to send logs from containers: {}", logstashHost);

		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put("syslog-address", "tcp://" + logstashHost + ":" + port);
		configMap.put("tag", tagPrefix + dockerExec.getExecutionId() + "_tjobexec");

		LogConfig logConfig = new LogConfig();
		logConfig.setType(LoggingType.SYSLOG);
		logConfig.setConfig(configMap);

		return logConfig;
	}

	/* End execution methods */

	public void endAllExec(DockerExecution dockerExec) throws Exception {
		try {
			endTestExec(dockerExec);
			if (dockerExec.isWithSut()) {
				endSutExec(dockerExec);
			}
		} catch (Exception e) {
			throw new Exception("end error"); // TODO Customize Exception
		}
	}

	public void endTestExec(DockerExecution dockerExec) {
		try {
			logger.info("Ending test execution " + dockerExec.getExecutionId());
			try {
				dockerExec.getDockerClient().stopContainerCmd(dockerExec.getTestContainerId()).exec();
			} catch (Exception e) {
			}
			dockerExec.getDockerClient().removeContainerCmd(dockerExec.getTestContainerId()).exec();
		} catch (Exception e) {
			logger.info("Error on ending test execution  " + dockerExec.getExecutionId());

		}
	}

	public void endSutExec(DockerExecution dockerExec) {
		SutExecution sutExec = dockerExec.getSutExec();
		sutExec.setDeployStatus(SutExecution.DeployStatusEnum.UNDEPLOYING);
		try {
			logger.info("Ending sut execution " + dockerExec.getExecutionId());
			try {
				dockerExec.getDockerClient().stopContainerCmd(dockerExec.getAppContainerId()).exec();
			} catch (Exception e) {
			}
			dockerExec.getDockerClient().removeContainerCmd(dockerExec.getAppContainerId()).exec();
			sutExec.setDeployStatus(SutExecution.DeployStatusEnum.UNDEPLOYED);
		} catch (Exception e) {
			sutExec.setDeployStatus(SutExecution.DeployStatusEnum.ERROR);
			logger.info("Error on ending Sut execution " + dockerExec.getExecutionId());
		}
		dockerExec.setSutExec(sutExec);
		sutService.modifySutExec(dockerExec.getSutExec());
	}

	/* Utils */

	public String getContainerIp(String containerId, DockerExecution dockerExec) {
		String ip = dockerExec.getDockerClient().inspectContainerCmd(containerId).exec().getNetworkSettings()
				.getNetworks().get(dockerExec.getNetwork()).getIpAddress();
		return ip.split("/")[0];
	}

	public String getContainerIpByNetwork(String containerId, String network) {
		DockerClient client = getDockerClient();

		String ip = client.inspectContainerCmd(containerId).exec().getNetworkSettings().getNetworks().get(network)
				.getIpAddress();
		return ip.split("/")[0];
	}

	public String getNetworkName(String containerId, DockerClient dockerClient) {
		return (String) dockerClient.inspectContainerCmd(containerId).exec().getNetworkSettings().getNetworks().keySet()
				.toArray()[0];
	}

	public String getHostIp(DockerExecution dockerExec) {
		return dockerExec.getDockerClient().inspectNetworkCmd().withNetworkId(dockerExec.getNetwork()).exec().getIpam()
				.getConfig().get(0).getGateway();
	}

	public String getHostIpByNetwork(DockerExecution dockerExec, String network) {
		return dockerExec.getDockerClient().inspectNetworkCmd().withNetworkId(network).exec().getIpam().getConfig()
				.get(0).getGateway();
	}

	public boolean imageExist(String imageName, DockerExecution dockerExec) {
		return !dockerExec.getDockerClient().searchImagesCmd(imageName).exec().isEmpty();
	}

	public void insertIntoNetwork(String networkId, String containerId) {
		DockerClient client = getDockerClient();
		client.connectToNetworkCmd().withNetworkId(networkId).withContainerId(containerId).exec();
	}

	/* Get TestResults */

	public InputStream getFileFromContainer(String containerName, String fileName, DockerExecution dockerExec) {
		InputStream inputStream = null;
		if (existsContainer("test_" + dockerExec.getExecutionId(), dockerExec)) {
			inputStream = dockerExec.getDockerClient().copyArchiveFromContainerCmd(containerName, fileName).exec();
		}
		return inputStream;
	}

	public boolean existsContainer(String containerName, DockerExecution dockerExec) {
		boolean exists = true;
		try {
			dockerExec.getDockerClient().inspectContainerCmd(containerName).exec();

		} catch (NotFoundException e) {
			exists = false;
		}
		return exists;
	}

	private List<ReportTestSuite> getTestResults(DockerExecution dockerExec) {
		List<ReportTestSuite> testSuites = null;

		try {
			InputStream inputStream = getFileFromContainer(dockerExec.getTestContainerId(),
					dockerExec.gettJobexec().getTjob().getResultsPath(), dockerExec);

			String result = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
			result = repairXML(result);

			TestSuiteXmlParser testSuiteXmlParser = new TestSuiteXmlParser(null);
			InputStream byteArrayIs = new ByteArrayInputStream(result.getBytes());
			testSuites = testSuiteXmlParser.parse(new InputStreamReader(byteArrayIs, "UTF-8"));

		} catch (IOException e) {
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return testSuites;
	}

	private String repairXML(String result) {
		String head = "<testsuite ";
		String foot = "</testsuite>";

		String[] splitedResult = result.split(head);
		String repaired = head + splitedResult[1];

		splitedResult = repaired.split(foot);
		repaired = splitedResult[0] + foot;

		return repaired;
	}

}
