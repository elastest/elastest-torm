package io.elastest.etm.test.service;

import static io.elastest.etm.test.util.StompTestUtils.connectToRabbitMQ;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.LogConfig.LoggingType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.test.util.StompTestUtils.WaitForMessagesHandler;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class DockerServiceItTest {

	private static final Logger log = LoggerFactory.getLogger(DockerServiceItTest.class);

	@LocalServerPort
	int serverPort;

	@Value("${logstash.host:#{null}}")
	private String logstashHost;

	private DockerClient dockerClient;

	@BeforeEach
	public void before() {
		log.info("-------------------------------------------------------------------------");

		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerHost("unix:///var/run/docker.sock").build();

		dockerClient = DockerClientBuilder.getInstance(config).build();
	}

	@AfterEach
	public void after() {
		log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}

	@Test
	public void readLogInRabbit() throws Exception {

		String imageId = "alpine";

		if (!existsImage(imageId)) {
			log.info("Pulling image '{}'", imageId);
			dockerClient.pullImageCmd(imageId).exec(new PullImageResultCallback()).awaitSuccess();
		}

		String queueId = "test.1.log";
		String tag = "test_1_tjobexec";

		WaitForMessagesHandler handler = connectToRabbitQueue(queueId);

		LogConfig logConfig = getLogConfig(tag);

		log.info("Creating container");

		CreateContainerResponse container = dockerClient.createContainerCmd(imageId)
				.withCmd("/bin/sh", "-c", "while true; do echo hello; sleep 1; done").withTty(false)
				.withLogConfig(logConfig).withNetworkMode("bridge").exec();

		String containerId = container.getId();

		try {

			log.info("Created container: {}", container.toString());

			long start = System.currentTimeMillis();

			dockerClient.startContainerCmd(containerId).exec();

			log.info("Waiting for logs messages in Rabbit");

			handler.waitForCompletion(5, TimeUnit.SECONDS);

			long duration = System.currentTimeMillis() - start;

			log.info("Log received in Rabbit in {} millis", duration);

		} catch (Exception ex) {

			log.info("Log NOT received in Rabbit");

			throw ex;

		} finally {

			log.info("Cleaning up resources");

			try {
				log.info("Removing container " + containerId);

				try {
					dockerClient.stopContainerCmd(containerId).exec();
				} catch (Exception ex) {
					log.warn("Error stopping container {}", containerId, ex);
				}
				dockerClient.removeContainerCmd(containerId).exec();
			} catch (Exception ex) {
				log.warn("Error on ending test execution {}", containerId, ex);
			}
		}
	}


	public boolean existsImage(String imageId) {
		boolean exists = true;
		try {
			dockerClient.inspectImageCmd(imageId).exec();
			log.debug("Docker image {} already exists", imageId);

		} catch (NotFoundException e) {
			log.trace("Image {} does not exist", imageId);
			exists = false;
		}
		return exists;
	}

	private LogConfig getLogConfig(String tag) {

		if (logstashHost == null) {
			logstashHost = dockerClient.inspectNetworkCmd().withNetworkId("bridge").exec().getIpam().getConfig().get(0)
					.getGateway();
		}

		log.info("Logstash IP to send logs from containers: {}", logstashHost);

		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put("syslog-address", "tcp://" + logstashHost + ":" + 5000);
		configMap.put("tag", tag);

		LogConfig logConfig = new LogConfig();
		logConfig.setType(LoggingType.SYSLOG);
		logConfig.setConfig(configMap);

		return logConfig;
	}

	private WaitForMessagesHandler connectToRabbitQueue(String queueId)
			throws InterruptedException, ExecutionException, TimeoutException {
		StompSession stompSession = connectToRabbitMQ(serverPort);

		String queueToSuscribe = "/topic/" + queueId;

		log.info("Container log queue '" + queueToSuscribe + "'");

		WaitForMessagesHandler handler = new WaitForMessagesHandler("1");

		stompSession.subscribe(queueToSuscribe, handler);
		return handler;
	}

	private boolean isRunningInContainer() {

		try (BufferedReader br = Files.newBufferedReader(Paths.get("/proc/1/cgroup"), StandardCharsets.UTF_8)) {

			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.contains("/docker")) {
					return true;
				}
			}
			return false;

		} catch (IOException e) {
			return false;
		}
	}

}
