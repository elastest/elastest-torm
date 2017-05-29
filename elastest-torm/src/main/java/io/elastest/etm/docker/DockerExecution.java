package io.elastest.etm.docker;

import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
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
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.model.LogConfig.LoggingType;
import com.github.dockerjava.api.model.Ports.Binding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.docker.utils.ExecStartResultCallbackWebsocket;

@Service
public class DockerExecution {

	private static String testImage = "";
	private static String logstashImage = "edujgurjc/logstash";
	private static final String volumeDirectory = "/testcontainers-java-examples/selenium-container";

	@Autowired
	private ApplicationContext context;


	private DockerClient dockerClient;
	private CreateContainerResponse container, logstashContainer;
	private String testContainerId, appContainerId, logstashContainerId;
	@Autowired
	private TJobExecRepository tJobExecRepo;

	private boolean windowsSo = false;
	private String logPath = "/home/edujg/torm/log.txt";
	private String surefirePath = "/testcontainers-java-examples/selenium-container/target/surefire-reports";
	private String testsuitesPath = "/home/edujg/torm/testsuites.json";

	public TJobExecution executeTJob(TJob tJob) {
		TJobExecution tjobExec = new TJobExecution();
		testImage = tJob.getImageName();
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
			startLogstash();
//            Thread.sleep(10000);
			ExposedPort tcp6080 = ExposedPort.tcp(6080);

			Ports portBindings = new Ports();
			portBindings.bind(tcp6080, Binding.bindPort(6080));

			String envVar = "DOCKER_HOST=tcp://172.17.0.1:2376";

			Volume volume = new Volume(volumeDirectory);
			LogConfig logConfig = new LogConfig();
			logConfig.setType(LoggingType.SYSLOG);
			
			Map<String, String> configMap = new HashMap<String, String>();
			configMap.put("syslog-address", "tcp://localhost:5000");
			logConfig.setConfig(configMap);
			
			this.dockerClient.pullImageCmd(testImage).exec(new PullImageResultCallback()).awaitSuccess();

			this.container = this.dockerClient.createContainerCmd(testImage).withExposedPorts(tcp6080)
					.withPortBindings(portBindings).withVolumes(volume).withBinds(new Bind(volumeDirectory, volume))
					.withEnv(envVar).withLogConfig(logConfig).exec();

			testContainerId = this.container.getId();

			this.dockerClient.startContainerCmd(testContainerId).exec();
			int code = this.dockerClient.waitContainerCmd(testContainerId).exec(new WaitContainerResultCallback()).awaitStatusCode();
			System.out.println("Test container ends with code " + code);
			
			this.saveTestSuite();
			endExec();

			// tjobExec.setElasEtmTjobexecLogs();
			// tjobExec.setElasEtmTjobexecDuration();
			
			tjobExec.setResult(TJobExecution.ResultEnum.SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			endExec();
			tjobExec.setResult(TJobExecution.ResultEnum.FAILURE);

		}
		tjobExec.setTjob(tJob);
		return tJobExecRepo.save(tjobExec);
	}
	
	
	public void startLogstash(){
		try {
			ExposedPort tcp5000 = ExposedPort.tcp(5000);

			Ports portBindings = new Ports();
			portBindings.bind(tcp5000, Binding.bindPort(5000));

			String elasticsearchId = RandomStringUtils.randomAlphanumeric(17).toLowerCase();
			String envVar = "ELASID="+elasticsearchId;
			System.out.println("Pulling logstash image...");
			this.dockerClient.pullImageCmd(logstashImage).exec(new PullImageResultCallback()).awaitSuccess();
			System.out.println("Pulling logstash image ends");
			
			this.logstashContainer = this.dockerClient.createContainerCmd(logstashImage).withExposedPorts(tcp5000)
					.withPortBindings(portBindings).withEnv(envVar).exec();

			logstashContainerId = this.logstashContainer.getId();

			this.dockerClient.startContainerCmd(logstashContainerId).exec();
			this.manageLogs();
			
		} catch (Exception e) {
			e.printStackTrace();
			endLogstashExec();
		}
	}

	public void manageLogs() {
		FileWriter file = null;
		PrintWriter pw = null;
		System.out.println("Starting logstash");
		try {

			file = new FileWriter(this.logPath);
			pw = new PrintWriter(file);

			Object lock = new Object();

			ExecStartResultCallbackWebsocket execStartResultCallbackWebsocket = context
					.getBean(ExecStartResultCallbackWebsocket.class);
			execStartResultCallbackWebsocket.setStdout(pw);
			execStartResultCallbackWebsocket.setStderr(pw);
			execStartResultCallbackWebsocket.setLock(lock);

			synchronized(lock){
				this.dockerClient.logContainerCmd(logstashContainerId).withStdErr(true).withStdOut(true)
						.withFollowStream(true).exec(execStartResultCallbackWebsocket);
				lock.wait();
			}		


		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
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
		
	public void endExec() {
		endTestExec();
		endLogstashExec();
	}
	
	public void endTestExec() {
		try {
			System.out.println("Ending test execution");
			try{
				this.dockerClient.stopContainerCmd(testContainerId).exec();
			}
			catch (Exception e) {}
			this.dockerClient.removeContainerCmd(testContainerId).exec();
			this.dockerClient.removeImageCmd(testImage).withForce(true).exec();
		} catch (Exception e) {
			System.out.println("Error on ending test execution");

		}
	}
	
	public void endLogstashExec() {
		try {
			System.out.println("Ending Logstash execution");
			this.dockerClient.stopContainerCmd(logstashContainerId).exec();
			this.dockerClient.removeContainerCmd(logstashContainerId).exec();
		} catch (Exception e) {
			System.out.println("Error on ending Logstash execution");
		}
	}

}
