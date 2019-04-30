package io.elastest.etm.test.service;

import static io.elastest.etm.test.util.StompTestUtils.connectToRabbitMQ;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;

import com.spotify.docker.client.messages.LogConfig;

import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.epm.client.service.DockerService;
import io.elastest.etm.test.IntegrationBaseTest;
import io.elastest.etm.platform.service.DockerServiceImpl;
import io.elastest.etm.test.util.StompTestUtils.WaitForMessagesHandler;
import io.elastest.etm.utils.UtilsService;

@RunWith(JUnitPlatform.class)
public class DockerServiceItTest extends IntegrationBaseTest {

    private static final Logger log = LoggerFactory
            .getLogger(DockerServiceItTest.class);

    @Value("${elastest.docker.network}")
    private String elastestNetwork;

    @LocalServerPort
    int serverPort;

    @Autowired
    private DockerServiceImpl dockerServiceImpl;
    
    @Autowired
    private DockerService dockerService;
    
    @Autowired
    UtilsService utilsService;

    @BeforeEach
    public void before() throws Exception {
        log.info(
                "-------------------------------------------------------------------------");
    }

    @AfterEach
    public void after() {
        log.info(
                ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Test
    public void readLogInRabbit() throws Exception {

        String imageId = "alpine";

        log.info("Pulling image '{}'", imageId);
        dockerServiceImpl.pullETExecImage(imageId, imageId, false);

        String queueId = "test.default_log.1.log";
        String tag = "test_1_exec";

        WaitForMessagesHandler handler = connectToRabbitQueue(queueId);

        LogConfig logConfig = dockerServiceImpl.getLogstashOrMiniLogConfig(tag);

        log.info("Log config: {} -> {}", logConfig.logType(),
                logConfig.logOptions());
        log.info("Creating and starting container");

        DockerBuilder dockerBuilder = new DockerBuilder(imageId);
        dockerBuilder.logConfig(logConfig);
        dockerBuilder.cmd(Arrays.asList("/bin/sh", "-c",
                "while true; do echo hello; sleep 1; done"));
        dockerBuilder.network(elastestNetwork);

        long start = System.currentTimeMillis();
        String containerId = dockerService
                .createAndStartContainer(dockerBuilder.build());
        log.info("Created and started container: {}", containerId);

        try {
            log.info("Waiting for logs messages in Rabbit");

            handler.waitForCompletion(7, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - start;

            log.info("Log received in Rabbit in {} millis", duration);

        } catch (Exception ex) {

            log.info("Log NOT received in Rabbit");

            throw ex;

        } finally {

            log.info("Cleaning up resources");

            try {
                log.info("Removing container " + containerId);
                dockerService
                        .stopAndRemoveContainer(containerId);
            } catch (Exception ex) {
                log.warn("Error on ending test execution {}", containerId, ex);
            }
        }
    }

    private WaitForMessagesHandler connectToRabbitQueue(String queueId)
            throws InterruptedException, ExecutionException, TimeoutException {
        StompSession stompSession = connectToRabbitMQ(serverPort,
                utilsService.isElastestMini());

        String queueToSuscribe = "/topic/" + queueId;

        log.info("Container log queue '" + queueToSuscribe + "'");

        WaitForMessagesHandler handler = new WaitForMessagesHandler("1");

        stompSession.subscribe(queueToSuscribe, handler);
        return handler;
    }

}
