package io.elastest.etm.test.service;

import static io.elastest.etm.test.util.StompTestUtils.connectToRabbitMQ;

import java.util.Arrays;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.LogConfig;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.model.DockerContainer.DockerBuilder;
import io.elastest.etm.service.DockerEtmService;
import io.elastest.etm.test.util.StompTestUtils.WaitForMessagesHandler;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class DockerServiceItTest {

    private static final Logger log = LoggerFactory
            .getLogger(DockerServiceItTest.class);

    @LocalServerPort
    int serverPort;

    @Value("${logstash.host:#{null}}")
    private String logstashHost;

    private DockerClient dockerClient;

    @Autowired
    private DockerEtmService dockerEtmService;

    @BeforeEach
    public void before() throws DockerCertificateException {
        log.info(
                "-------------------------------------------------------------------------");

        dockerClient = dockerEtmService.dockerService.getDockerClient();
    }

    @AfterEach
    public void after() {
        log.info(
                ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Test
    public void readLogInRabbit() throws Exception {

        String imageId = "alpine";

        if (!existsImage(imageId)) {
            log.info("Pulling image '{}'", imageId);
            dockerClient.pull(imageId);
        }

        String queueId = "test.default_log.1.log";
        String tag = "test_1_exec";

        WaitForMessagesHandler handler = connectToRabbitQueue(queueId);

        LogConfig logConfig = getLogConfig(tag);

        log.info("Creating and starting container");

        DockerBuilder dockerBuilder = new DockerBuilder(imageId);
        dockerBuilder.logConfig(logConfig);
        dockerBuilder.cmd(Arrays.asList("/bin/sh", "-c",
                "while true; do echo hello; sleep 1; done"));
        dockerBuilder.network("bridge");

        long start = System.currentTimeMillis();
        String containerId = dockerEtmService.dockerService
                .createAndStartContainer(dockerBuilder.build());
        log.info("Created and started container: {}", containerId);

        try {
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
                    dockerClient.stopContainer(containerId, 60);
                } catch (Exception ex) {
                    log.warn("Error stopping container {}", containerId, ex);
                }
                dockerClient.removeContainer(containerId);
            } catch (Exception ex) {
                log.warn("Error on ending test execution {}", containerId, ex);
            }
        }
    }

    public boolean existsImage(String imageId)
            throws DockerException, InterruptedException {
        boolean exists = true;
        try {
            dockerClient.inspectImage(imageId);
            log.debug("Docker image {} already exists", imageId);

        } catch (ImageNotFoundException e) {
            log.trace("Image {} does not exist", imageId);
            exists = false;
        }
        return exists;
    }

    private LogConfig getLogConfig(String tag)
            throws DockerException, InterruptedException {

        if (logstashHost == null) {
            logstashHost = dockerClient.inspectNetwork("bridge").ipam().config()
                    .get(0).gateway();
        }

        log.info("Logstash IP to send logs from containers: {}", logstashHost);

        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("syslog-address", "tcp://" + logstashHost + ":" + 5000);
        configMap.put("tag", tag);

        LogConfig logConfig = LogConfig.create("syslog", configMap);

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

}
