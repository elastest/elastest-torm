package io.elastest.etm.docker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.plugins.surefire.report.ReportTestCase;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.SurefireReportParser;
import org.apache.maven.reporting.MavenReportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.elastest.etm.api.model.TestSuite;
import io.elastest.etm.service.sut.SutService;
import io.elastest.etm.utils.UtilTools;

@Service
public class DockerService {

	private static final Logger logger = LoggerFactory.getLogger(DockerService.class);

	private static final String DOKCER_LISTENING_ON_TCP_PORT_PREFIX ="tcp://"; 
	private static Boolean isRunningInContainer;
	private static String hostIp;
	private static String appImage = "edujgurjc/torm-loadapp", checkImage = "edujgurjc/check-service-up";
	
	private String testImage = "";
	
	//On Linux: "/test-results". On Windows: "c:/Users/docker/test-results"	
	@Value("${elastest.test-results.directory}")
	private String volumeDirectoryToWriteTestResutls; 
	
	//On Windows: "c:/Users/docker/test-results"
	@Value("${elastest.test-results.directory.windows}")
	private String volumeDirectoryToReadTestResults;
	
	@Value("${docker.host.port}")
	private String dockerHostPort;

	@Autowired
	private SutService sutService;

	@Autowired
	public UtilTools utilTools;

	@Value("${os.name}")
	private String windowsSO;

	public void loadBasicServices(DockerExecution dockerExec) throws Exception {
		try {
			configureDocker(dockerExec);
			createNetwork(dockerExec);
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
					.withDockerHost(DOKCER_LISTENING_ON_TCP_PORT_PREFIX + utilTools.getDockerHostIp() + ":" + dockerHostPort).build();
			dockerExec.setDockerClient(DockerClientBuilder.getInstance(config).build());

		} else {
			logger.info("Execute on Linux.");			
			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost(DOKCER_LISTENING_ON_TCP_PORT_PREFIX + utilTools.getHostIp() + ":" + dockerHostPort).build();
			dockerExec.setDockerClient(DockerClientBuilder.getInstance(config).build());
		}
	}

	public void createNetwork(DockerExecution dockerExec) {
		dockerExec.generateNetwork();
		dockerExec.getDockerClient().createNetworkCmd().withName(dockerExec.getNetwork()).exec();
	}

	/* Starting Methods */

	public void startSut(DockerExecution dockerExec) {
		SutExecution sutExec = sutService.createSutExecutionBySut(dockerExec.gettJobexec().getTjob().getSut());
		try {
			System.out.println("Starting sut " + dockerExec.getExecutionId());
			String envVar = "REPO_URL=" + sutExec.getSutSpecification().getSpecification();

			LogConfig logConfig = new LogConfig();
			logConfig.setType(LoggingType.SYSLOG);
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("syslog-address", "tcp://" + getHostIp(dockerExec) + ":5001");
			configMap.put("tag", "sut_" + dockerExec.getExecutionId() + "_tjobexec");

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
			int sutPort = 8080;
			String sutUrl = "http://" + sutIP + ":" + sutPort;
			sutExec.setUrl(sutUrl);
			// Wait for Sut started
			checkSut(dockerExec, sutIP, sutPort + "");
		} catch (Exception e) {
			e.printStackTrace();
			sutExec.deployStatus(SutExecution.DeployStatusEnum.ERROR);
			endSutExec(dockerExec);
			removeNetwork(dockerExec);
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
				.withNetworkMode(dockerExec.getNetwork()).withName("check_" + dockerExec.getExecutionId()).exec()
				.getId();
		dockerExec.getDockerClient().startContainerCmd(checkContainerId).exec();

		dockerExec.getDockerClient().waitContainerCmd(checkContainerId).exec(new WaitContainerResultCallback())
				.awaitStatusCode();
		System.out.println("Sut is ready " + dockerExec.getExecutionId());

		try {
			try {
				dockerExec.getDockerClient().stopContainerCmd(checkContainerId).exec();
			} catch (Exception e) {
			}
			dockerExec.getDockerClient().removeContainerCmd(checkContainerId).exec();
		} catch (Exception e) {
		}
	}

	public List<ReportTestSuite> startTest(String testImage, DockerExecution dockerExec) {
		try {
			System.out.println("Starting test " + dockerExec.getExecutionId());
			this.testImage = testImage;

			System.out.println("host: " + getHostIpByNetwork(dockerExec, "bridge"));

			String envVar = "DOCKER_HOST=tcp://172.17.0.1:2376";
			String envVar2 = "APP_IP=" + (dockerExec.isWithSut() ? dockerExec.getSutExec().getUrl() : "0");
			String envVar3 = "NETWORK=" + dockerExec.getNetwork();
			ArrayList<String> envList = new ArrayList<>();
			envList.add(envVar);
			envList.add(envVar2);
			envList.add(envVar3);

			Volume volume = new Volume("/results");
			String localDirToWriteTestResults = volumeDirectoryToWriteTestResutls + "/" + dockerExec.getExecutionId();
			String localDirToTeadTestResults = volumeDirectoryToReadTestResults + "/" + dockerExec.getExecutionId();

			LogConfig logConfig = new LogConfig();
			logConfig.setType(LoggingType.SYSLOG);
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("syslog-address", "tcp://" + getHostIp(dockerExec) + ":5000");
			configMap.put("tag", "test_" + dockerExec.getExecutionId() + "_tjobexec");

			logConfig.setConfig(configMap);

			dockerExec.getDockerClient().pullImageCmd(testImage).exec(new PullImageResultCallback()).awaitSuccess();

			dockerExec.setTestcontainer(
					dockerExec.getDockerClient().createContainerCmd(testImage).withEnv(envList).withLogConfig(logConfig)
							.withNetworkMode(dockerExec.getNetwork()).withName("test_" + dockerExec.getExecutionId())
							.withVolumes(volume).withBinds(new Bind(localDirToWriteTestResults, volume)).exec());

			String testContainerId = dockerExec.getTestcontainer().getId();
			dockerExec.setTestContainerId(testContainerId);

			dockerExec.getDockerClient().startContainerCmd(testContainerId).exec();
			int code = dockerExec.getDockerClient().waitContainerCmd(testContainerId)
					.exec(new WaitContainerResultCallback()).awaitStatusCode();
			System.out.println("Test container ends with code " + code);
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

	/* End execution methods */

	public void endAllExec(DockerExecution dockerExec) throws Exception {
		try {
			endTestExec(dockerExec);
			if (dockerExec.isWithSut()) {
				endSutExec(dockerExec);
			}
			removeNetwork(dockerExec);
		} catch (Exception e) {
			throw new Exception("end error"); // TODO Customize Exception
		}
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
				System.out.println("Remove image " + testImage + " failed. In use. " + dockerExec.getExecutionId());
			}
		} catch (Exception e) {
			System.out.println("Error on ending test execution  " + dockerExec.getExecutionId());

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
				System.out.println("Remove image " + appImage + " failed. In use. " + dockerExec.getExecutionId());
			}
			sutExec.deployStatus(SutExecution.DeployStatusEnum.UNDEPLOYED);
		} catch (Exception e) {
			sutExec.deployStatus(SutExecution.DeployStatusEnum.ERROR);
			System.out.println("Error on ending Sut execution " + dockerExec.getExecutionId());
		}
		dockerExec.setSutExec(sutExec);
		sutService.modifySutExec(dockerExec.getSutExec());
	}

	public void removeNetwork(DockerExecution dockerExec) {
		System.out.println("Removing docker network... " + dockerExec.getExecutionId());
		dockerExec.getDockerClient().removeNetworkCmd(dockerExec.getNetwork()).exec();
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
			logger.info("Testsuit size: "+ testSuites.size());
		} catch (MavenReportException e) {
			e.printStackTrace();
		}
		return testSuites;
	}
}
