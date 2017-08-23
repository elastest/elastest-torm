package io.elastest.etm.test.service;

import static io.elastest.etm.test.util.StompTestUtils.connectToRabbitMQ;
import static org.junit.Assert.assertNotEquals;

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
import com.github.dockerjava.api.model.LogConfig;

import io.elastest.etm.ElasTestTormApp;
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

	@Test
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
				.withLogConfig(logConfig).exec();

		log.info("Created container: {}", container.toString());

		assertNotEquals(container.getId(), null);

		StompSession stompSession = connectToRabbitMQ(serverPort);

		String queueToSuscribe = "/topic/" + "test." + execId + ".log";
		log.info("Container log queue '" + queueToSuscribe + "'");

		WaitForMessagesHandler handler = new WaitForMessagesHandler();

		stompSession.subscribe(queueToSuscribe, handler);

		long start = System.currentTimeMillis();
		
		dockerClient.startContainerCmd(container.getId()).exec();

		log.info("Waiting for logs messages in Rabbit");
		
		handler.waitForCompletion();
		
		long duration = System.currentTimeMillis() - start;
		
		log.info("Log received in Rabbit in {} millis", duration);
	
		
		log.info("Cleaning up resources");

		dockerService.endTestExec(dockerExec);

	}

}
