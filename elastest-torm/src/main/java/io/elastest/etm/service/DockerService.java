package io.elastest.etm.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.SurefireReportParser;
import org.apache.maven.reporting.MavenReportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.LogConfig.LoggingType;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.utils.UtilTools;

@Service
public class DockerService {

	private static final Logger logger = LoggerFactory.getLogger(DockerService.class);

	private static final String DOKCER_LISTENING_ON_TCP_PORT_PREFIX = "tcp://";
	private static String appImage = "elastest/test-etm-javasutrepo", checkImage = "elastest/etm-check-service-up";

	// On Linux: "/test-results". On Windows: "c:/Users/docker/test-results"
	@Value("${elastest.test-results.directory}")
	private String volumeDirectoryToWriteTestResutls;

	// On Windows: "c:/Users/docker/test-results"
	@Value("${elastest.test-results.directory.windows}")
	private String volumeDirectoryToReadTestResults;

	@Value("${docker.host.port}")
	private String dockerHostPort;
	
	@Value("${logstash.host:#{null}}")
	private String logstashHost;

	@Autowired
	private SutService sutService;

	@Autowired
	public UtilTools utilTools;

	@Value("${os.name}")
	private String windowsSO;

	public void loadBasicServices(DockerExecution dockerExec) throws Exception {
		try {
			configureDocker(dockerExec);
			dockerExec.setNetwork("bridge");
			if (dockerExec.isWithSut()) {
				startSut(dockerExec);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/* Config Methods */

	public void configureDocker(DockerExecution dockerExec) {
		if (windowsSO.toLowerCase().contains("win")) {
			logger.info("Execute on Windows.");
			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost(
							DOKCER_LISTENING_ON_TCP_PORT_PREFIX + utilTools.getDockerHostIp() + ":" + dockerHostPort)
					.build();
			dockerExec.setDockerClient(DockerClientBuilder.getInstance(config).build());

		} else {
			logger.info("Execute on Linux.");
			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					//.withDockerHost(DOKCER_LISTENING_ON_TCP_PORT_PREFIX + utilTools.getHostIp() + ":" + dockerHostPort)
					.withDockerHost("unix:///var/run/docker.sock")
					.build();
			dockerExec.setDockerClient(DockerClientBuilder.getInstance(config).build());
		}
	}

	/* Starting Methods */

	public void startSut(DockerExecution dockerExec) {
		SutExecution sutExec = sutService.createSutExecutionBySut(dockerExec.gettJobexec().getTjob().getSut());
		try {
			logger.info("Starting sut " + dockerExec.getExecutionId());
			String sutImage = appImage;
			String envVar = "";
			if (sutExec.getSutSpecification().sutBy() == "imageName") {
				sutImage = sutExec.getSutSpecification().getSpecification();
				envVar = "REPO_URL=none";
			} else {
				envVar = "REPO_URL=" + sutExec.getSutSpecification().getSpecification();
			}
			logger.info("Sut " + dockerExec.getExecutionId() + " image: " + sutImage);
			String sutName = "sut_" + dockerExec.getExecutionId();

			LogConfig logConfig = getLogConfig(5001, "sut_", dockerExec);

			dockerExec.getDockerClient().pullImageCmd(sutImage).exec(new PullImageResultCallback()).awaitSuccess();

			dockerExec.setAppContainer(dockerExec.getDockerClient().createContainerCmd(sutImage).withEnv(envVar)
					.withLogConfig(logConfig).withName(sutName).exec());

			sutExec.setDeployStatus(SutExecution.DeployStatusEnum.DEPLOYED);

			String appContainerId = dockerExec.getAppContainer().getId();
			dockerExec.setAppContainerId(appContainerId);

			dockerExec.getDockerClient().startContainerCmd(appContainerId).exec();

			String sutIP = getContainerIp(appContainerId, dockerExec);
			int sutPort = 8080;
			String sutUrl = "http://" + sutIP + ":" + sutPort;
			sutExec.setUrl(sutUrl);

			// Wait for Sut started
			checkSut(dockerExec, sutIP, sutPort + "");
		} catch (Exception e) {
			e.printStackTrace();
			sutExec.setDeployStatus(SutExecution.DeployStatusEnum.ERROR);
			endSutExec(dockerExec);
		}
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
				.withName("check_" + dockerExec.getExecutionId()).exec().getId();
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

				commands += "cp -a " + dockerExec.gettJobexec().getTjob().getResultsPath() + "/. /results";
				cmdList.add(commands);

			}
			// Volumes
			Volume volume = new Volume("/results");
			String localDirToWriteTestResults = volumeDirectoryToWriteTestResutls + "/" + dockerExec.getExecutionId();
			String localDirToTeadTestResults = volumeDirectoryToReadTestResults + "/" + dockerExec.getExecutionId();

			LogConfig logConfig = getLogConfig(5000, "test_", dockerExec);

			dockerExec.getDockerClient().pullImageCmd(testImage).exec(new PullImageResultCallback()).awaitSuccess();

			CreateContainerResponse testContainer = dockerExec.getDockerClient().createContainerCmd(testImage)
					.withEnv(envList)
					.withLogConfig(logConfig)
					.withName("test_" + dockerExec.getExecutionId())
					.withVolumes(volume)
					.withBinds(new Bind(localDirToWriteTestResults, volume))
					.withCmd(cmdList).exec();
			
			String testContainerId = testContainer.getId();
			
			dockerExec.setTestcontainer(testContainer);			
			dockerExec.setTestContainerId(testContainerId);

			dockerExec.getDockerClient().startContainerCmd(testContainerId).exec();
			
			// this essentially test the since=0 case
//	        dockerClient.logContainerCmd(container.getId())
//	            .withStdErr(true)
//	            .withStdOut(true)
//	            .withFollowStream(true)
//	            .withTailAll()
//	            .exec(loggingCallback);
			
			int code = dockerExec.getDockerClient().waitContainerCmd(testContainerId)
					.exec(new WaitContainerResultCallback()).awaitStatusCode();
			
			logger.info("Test container ends with code " + code);
			
			return this.getTestResults(localDirToTeadTestResults, dockerExec);

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
		
		if(logstashHost == null){
			logstashHost = getHostIpByNetwork(dockerExec, dockerExec.getNetwork());
		}
		logger.info("Logstash IP to send logs from containers: {}",logstashHost);
		
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put("syslog-address",
				"tcp://" + logstashHost + ":" + port);
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

	public List<ReportTestSuite> getTestResults(String localTestDir, DockerExecution dockerExec) {
		logger.info("TestResult file path:" + localTestDir);

		File surefireXML = new File(localTestDir);
		List<File> reportsDir = new ArrayList<>();
		reportsDir.add(surefireXML);

		logger.info("file content:" + surefireXML.toString());

		SurefireReportParser surefireReport = new SurefireReportParser(reportsDir, new Locale("en", "US"), null);
		List<ReportTestSuite> testSuites = new ArrayList<ReportTestSuite>();
		try {
			testSuites = surefireReport.parseXMLReportFiles();
			logger.info("Testsuit size: " + testSuites.size());
		} catch (MavenReportException e) {
			e.printStackTrace();
		}
		return testSuites;
	}
}
