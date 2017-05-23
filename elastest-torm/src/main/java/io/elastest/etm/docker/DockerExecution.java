package io.elastest.etm.docker;

import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.SurefireReportParser;
import org.apache.maven.reporting.MavenReportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.model.ElasEtmTjobexec;

@Service
public class DockerExecution {

	private static String testImage = "";
	private static final String volumeDirectory = "/testcontainers-java-examples/selenium-container";
	private static final String topicEndExecutionMessage = "/topic/endExecutionTest";

	@Autowired
	private ApplicationContext context;

	@Autowired
	private StompMessageSenderService stompMessageSenderService;

	@Autowired
	private IOUtils iOUtils;

	private DockerClient dockerClient;
	private CreateContainerResponse container;
	private String testContainerId, appContainerId;
	@Autowired
	private TJobExecRepository tJobExecRepo;

	private boolean windowsSo = false;
	private String logPath = "/home/edujg/torm/log.txt";
	private String surefirePath = "/testcontainers-java-examples/selenium-container/target/surefire-reports";
	private String testsuitesPath = "/home/edujg/torm/testsuites.json";

	public ElasEtmTjobexec executeTJob(String image) {
		ElasEtmTjobexec tjobExec = new ElasEtmTjobexec();
		this.testImage = image;

		if (windowsSo) {
			DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
					.withDockerHost("tcp://192.168.99.100:2376").build();
			this.dockerClient = DockerClientBuilder.getInstance(config).build();

			this.logPath = "";
			this.surefirePath = "";
			this.testsuitesPath = "";
		} else {
			this.dockerClient = DockerClientBuilder.getInstance().build();
		}

		Info info = this.dockerClient.infoCmd().exec();
		System.out.println("Info: " + info);

		try {

			ExposedPort tcp6080 = ExposedPort.tcp(6080);

			Ports portBindings = new Ports();
			portBindings.bind(tcp6080, Binding.bindPort(6080));

			String envVar = "DOCKER_HOST=tcp://172.17.0.1:2376";

			Volume volume = new Volume(volumeDirectory);

			this.dockerClient.pullImageCmd(testImage).exec(new PullImageResultCallback()).awaitSuccess();

			this.container = this.dockerClient.createContainerCmd(testImage).withExposedPorts(tcp6080)
					.withPortBindings(portBindings).withVolumes(volume).withBinds(new Bind(volumeDirectory, volume))
					.withEnv(envVar).exec();

			testContainerId = this.container.getId();

			this.dockerClient.startContainerCmd(testContainerId).exec();

			endTestExec();
						
//			tjobExec.setElasEtmTjobexecLogs();
//			tjobExec.setElasEtmTjobexecDuration();
			return tjobExec;


		} catch (Exception e) {
			e.printStackTrace();
			endTestExec();
			return tjobExec;
		}

	}

	public void manageLogs() {
		FileWriter file = null;
		PrintWriter pw = null;

		try {

			file = new FileWriter(this.logPath);
			pw = new PrintWriter(file);

			ExecStartResultCallbackWebsocket execStartResultCallbackWebsocket = context
					.getBean(ExecStartResultCallbackWebsocket.class);
			execStartResultCallbackWebsocket.setStdout(pw);
			execStartResultCallbackWebsocket.setStderr(pw);

			try {
				this.dockerClient.logContainerCmd(testContainerId).withStdErr(true).withStdOut(true)
						.withFollowStream(true).exec(execStartResultCallbackWebsocket).awaitCompletion();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (file != null) {
					file.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.saveTestSuite();
		stompMessageSenderService.sendStompMessage(topicEndExecutionMessage, new EndExecutionMessage("END"));
		iOUtils.getLogLines().add("END");
	}

	public void saveTestSuite() {
		File surefireXML = new File(this.surefirePath);
		List<File> reportsDir = new ArrayList<>();
		reportsDir.add(surefireXML);

		SurefireReportParser surefireReport = new SurefireReportParser(reportsDir, new Locale("en", "US"), null);
		try {
			List<ReportTestSuite> testSuites = surefireReport.parseXMLReportFiles();

			ObjectMapper mapper = new ObjectMapper();
			// Object to JSON in file
			try {
				mapper.writeValue(new File(this.testsuitesPath), testSuites);
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (MavenReportException e) {
			e.printStackTrace();
		}
	}

	public void endTestExec() {
		this.dockerClient.stopContainerCmd(testContainerId).exec();
		this.dockerClient.removeContainerCmd(testContainerId).exec();
		this.dockerClient.removeImageCmd(testImage).withForce(true).exec();
	}

}
