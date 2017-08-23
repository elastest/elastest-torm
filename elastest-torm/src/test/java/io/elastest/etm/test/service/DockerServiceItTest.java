package io.elastest.etm.test.service;

import static io.elastest.etm.test.util.StompTestUtils.connectToRabbitMQ;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.service.DockerExecution;
import io.elastest.etm.service.DockerService;
import io.elastest.etm.test.util.StompTestUtils.WaitForMessagesHandler;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class DockerServiceItTest {

	private static final Logger log = LoggerFactory.getLogger(DockerServiceItTest.class);

	@LocalServerPort
	int serverPort;

	@Autowired
	private DockerService dockerService;

	@BeforeEach
	public void before() {
		log.info("-------------------------------------------------------------------------");
	}

	@Test
	@Disabled
	public void readLogInRabbit() throws Exception {

		log.info("Executing readLogInRabbit test");

		long execId = 1l;

		DockerExecution dockerExec = new DockerExecution();
		dockerExec.settJobexec(new TJobExecution(execId, null, null));

		dockerService.loadBasicServices(dockerExec);

		DockerClient dockerClient = dockerExec.getDockerClient();

		// dockerExec.getDockerClient().pullImageCmd("busybox").exec(new
		// PullImageResultCallback()).awaitSuccess();

		LogConfig logConfig = dockerService.getLogConfig(5000, "test", dockerExec);

		CreateContainerResponse container = dockerClient.createContainerCmd("alpine")
				.withCmd("/bin/sh", "-c", "while true; do echo hello; sleep 1; done").withTty(false)
				.withLogConfig(logConfig).withNetworkMode("etm_elastest").exec();

		log.info("Created container: {}", container.toString());

		assertNotEquals(container.getId(), null);

		String queueId = "test." + execId + ".log";

		StompSession stompSession = connectToRabbitMQ(serverPort);

		String queueToSuscribe = "/topic/" + queueId;

		log.info("Container log queue '" + queueToSuscribe + "'");

		WaitForMessagesHandler handler = new WaitForMessagesHandler();

		stompSession.subscribe(queueToSuscribe, handler);

		long start = System.currentTimeMillis();

		dockerClient.startContainerCmd(container.getId()).exec();

		log.info("Waiting for logs messages in Rabbit");

		handler.waitForCompletion(5, TimeUnit.SECONDS);

		long duration = System.currentTimeMillis() - start;

		log.info("Log received in Rabbit in {} millis", duration);

		log.info("Cleaning up resources");

		dockerService.endTestExec(dockerExec);

	}

	@Test
	@Disabled
	public void readLogInRabbit2() throws Exception {

		log.info("Executing readLogInRabbit test");

		long execId = 2l;

		TJobExecution tJobExec = new TJobExecution(execId, null, null);
		tJobExec.setTjob(new TJob(1l, "xx", "edujgurjc/torm-test-01", null, null, false, null));
		DockerExecution dockerExec = new DockerExecution(tJobExec);

		// Create queues and load basic services
		dockerService.loadBasicServices(dockerExec);

		String queueId = "test." + execId + ".log";

		StompSession stompSession = connectToRabbitMQ(serverPort);

		String queueToSuscribe = "/topic/" + queueId;

		log.info("Container log queue '" + queueToSuscribe + "'");

		WaitForMessagesHandler handler = new WaitForMessagesHandler();

		stompSession.subscribe(queueToSuscribe, handler);

		long start = System.currentTimeMillis();

		dockerService.executeTest(dockerExec);

		log.info("Waiting for logs messages in Rabbit");

		try {

			handler.waitForCompletion(5, TimeUnit.SECONDS);

			long duration = System.currentTimeMillis() - start;

			log.info("Log received in Rabbit in {} millis", duration);

		} finally {

			log.info("Cleaning up resources");

			dockerService.endAllExec(dockerExec);

		}

	}

	@Test
	@Disabled
	public void readLogInRabbit3() throws Exception {

		log.info("Executing readLogInRabbit3 test");

		long execId = 2l;

		TJobExecution tJobExec = new TJobExecution(execId, null, null);
		tJobExec.setTjob(new TJob(1l, "xx", "edujgurjc/torm-test-01", null, null, false, null));
		DockerExecution dockerExec = new DockerExecution(tJobExec);

		// Create queues and load basic services
		dockerService.loadBasicServices(dockerExec);

		String queueId = "test." + execId + ".log";

		StompSession stompSession = connectToRabbitMQ(serverPort);

		String queueToSuscribe = "/topic/" + queueId;

		log.info("Container log queue '" + queueToSuscribe + "'");

		WaitForMessagesHandler handler = new WaitForMessagesHandler();

		stompSession.subscribe(queueToSuscribe, handler);

		

		log.info("Starting test " + dockerExec.getExecutionId());
		String testImage = dockerExec.gettJobexec().getTjob().getImageName();
		
		LogConfig logConfig = dockerService.getLogConfig(5000, "test_", dockerExec);
		
		dockerExec.getDockerClient().pullImageCmd(testImage).exec(new PullImageResultCallback()).awaitSuccess();
		
		CreateContainerResponse testContainer = dockerExec.getDockerClient().createContainerCmd(testImage)
				.withLogConfig(logConfig).withName("test_" + dockerExec.getExecutionId()).exec();
		
		String testContainerId = testContainer.getId();
		
		dockerExec.setTestcontainer(testContainer);
		dockerExec.setTestContainerId(testContainerId);
		
		long start = System.currentTimeMillis();
		
		log.info("Starting test container");
		
		dockerExec.getDockerClient().startContainerCmd(testContainerId).exec();
		
//		int code = dockerExec.getDockerClient().waitContainerCmd(testContainerId)
//				.exec(new WaitContainerResultCallback()).awaitStatusCode();
//		
//		log.info("Test container ends with code " + code);

		try {
			
			log.info("Waiting for logs messages in Rabbit");

			handler.waitForCompletion(5, TimeUnit.SECONDS);

			long duration = System.currentTimeMillis() - start;

			log.info("Log received in Rabbit in {} millis", duration);

		} finally {

			log.info("Cleaning up resources");

			dockerService.endAllExec(dockerExec);

		}

	}
	
	@Test
	public void readLogInRabbit4() throws Exception {

		log.info("Executing readLogInRabbit4 test");

		long execId = 4l;

		TJobExecution tJobExec = new TJobExecution(execId, null, null);
		DockerExecution dockerExec = new DockerExecution();
		dockerExec.settJobexec(tJobExec);

		// Create queues and load basic services
		dockerService.loadBasicServices(dockerExec);

		StompSession stompSession = connectToRabbitMQ(serverPort);

		String queueId = "test." + execId + ".log";
		String queueToSuscribe = "/topic/" + queueId;

		log.info("Container log queue '" + queueToSuscribe + "'");

		WaitForMessagesHandler handler = new WaitForMessagesHandler();
		stompSession.subscribe(queueToSuscribe, handler);
		
		String imageName = "alpine";
		
		LogConfig logConfig = dockerService.getLogConfig(5000, "test_", dockerExec);
		
		log.info("Pulling image {}", imageName);
		dockerExec.getDockerClient().pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitSuccess();
		
		CreateContainerResponse testContainer = dockerExec.getDockerClient().createContainerCmd(imageName)
				.withLogConfig(logConfig)
				.withCmd("/bin/sh", "-c", "while true; do echo hello; sleep 1; done")
				.exec();
		
		String testContainerId = testContainer.getId();
		
		dockerExec.setTestcontainer(testContainer);
		dockerExec.setTestContainerId(testContainerId);
		
		long start = System.currentTimeMillis();
		
		log.info("Starting container");
		
		dockerExec.getDockerClient().startContainerCmd(testContainerId).exec();
		
		log.info("Started");
		
//		int code = dockerExec.getDockerClient().waitContainerCmd(testContainerId)
//				.exec(new WaitContainerResultCallback()).awaitStatusCode();
//		
//		log.info("Test container ends with code " + code);

		try {
			
			log.info("Waiting for logs messages in Rabbit");

			handler.waitForCompletion(5, TimeUnit.SECONDS);

			long duration = System.currentTimeMillis() - start;

			log.info("Log received in Rabbit in {} millis", duration);

		} finally {

			log.info("Cleaning up resources");

			dockerService.endAllExec(dockerExec);

		}

	}

}
