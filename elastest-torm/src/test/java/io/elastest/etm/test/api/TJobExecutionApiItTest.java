package io.elastest.etm.test.api;

import static io.elastest.etm.test.util.StompTestUtils.connectToRabbitMQ;
import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

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
import org.opentest4j.MultipleFailuresError;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.test.util.StompTestUtils.WaitForMessagesHandler;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TJobExecutionApiItTest extends EtmApiItTest {
    final Logger log = getLogger(lookup().lookupClass());

    long projectId;

    @BeforeEach
    void setup() {
        log.info("App started on port {}", serverPort);
        projectId = createProject("Test_Project").getId();
    }

    @AfterEach
    void reset() {
        deleteProject(projectId);
    }

    @Test
    public void testExecuteTJobWithSut()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start the test testExecuteTJobWithSut");
        testExecuteTJob(true, false);
    }

    @Test
    public void testExecuteTJobWithoutSut()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start the test testExecuteTJobWithoutSut");
        testExecuteTJob(false, false);
    }

    @Test
    public void testExecuteTJobWithoutSutAndStop()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start the test testExecuteTJobWithoutSutAndStop");
        testExecuteTJob(false, true);
    }

    @Test
    public void testSutExecution()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start the test testSutExecution");
        SutSpecification sut = createSut(projectId);
        Long sutId = sut.getId();
        TJob tJob = createTJob(projectId, sutId);
        testExecuteTJob(tJob, false);

        SutExecution[] sutExecs = this.getAllSutExecBySut(sutId);
        assertThat(sutExecs.length > 0);
        SutExecution sutExec = this.getSutExec(sutId, sutExecs[0].getId());
        assertNotNull(sutExec);

        this.deleteSuTExec(sutExec.getId());
    }

    private void testExecuteTJob(TJob tJob, boolean withStop)
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        StompSession stompSession = connectToRabbitMQ(serverPort);

        log.info("POST /api/tjob/{tjobId}/exec");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"tJobParams\" : [{\"Param1\":\"NewValue1\"}], \"sutParams\" : [{\"Param1\":\"NewValue1\"}]}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        Map<String, Object> urlParams = new HashMap<>();
        urlParams.put("tjobId", tJob.getId());

        ResponseEntity<TJobExecution> response = httpClient.postForEntity(
                "/api/tjob/{tjobId}/exec", entity, TJobExecution.class,
                urlParams);

        TJobExecution exec = response.getBody();

        log.info("TJobExecution creation response: " + response);

        if (withStop) {
            log.info("Sending stop signal to Execution {}...", exec.getId());
            httpClient.delete("/api/tjob/" + tJob.getId() + "/exec/"
                    + exec.getId() + "/stop");
        } else {
            // Wait for end execution
            int waitTime = 240;

            String queueToSuscribe = "/topic/" + "test.default_log."
                    + exec.getId() + ".log";
            log.info("TJob log queue '" + queueToSuscribe + "'");

            WaitForMessagesHandler handler = new WaitForMessagesHandler(
                    msg -> msg.contains("BUILD SUCCESS")
                            || msg.contains("BUILD FAILURE"));

            stompSession.subscribe(queueToSuscribe, handler);
            handler.waitForCompletion(waitTime, TimeUnit.SECONDS);
        }

        assertAll("Validating TJobExecution Properties",
                () -> assertNotNull(response.getBody()),
                () -> assertNotNull(response.getBody().getId()),
                () -> assertTrue(response.getBody().getTjob().getId()
                        .equals(urlParams.get("tjobId"))));

        while (true) {
            exec = getTJobExecutionById(exec.getId(), tJob.getId()).getBody();
            log.info("TJobExecution: " + exec);
            if (exec.isFinished()) {
                log.info("Test results: {}", exec.getTestSuites().toString());
                break;
            }
            sleep(500);
        }

        if (withStop) {
            // Temporal Success added because sometimes exec ends before stops
            assertThat(exec.getResult()).isIn(ResultEnum.STOPPED,
                    ResultEnum.SUCCESS);
        }

        deleteTJobExecution(exec.getId(), tJob.getId());
        deleteTJob(tJob.getId());
        log.info("Finished.");
    }

    private void testExecuteTJob(boolean withSut, boolean withStop)
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        TJob tJob;
        if (withSut) {
            Long sutId = createSut(projectId).getId();
            tJob = createTJob(projectId, sutId);
        } else {
            tJob = createTJob(projectId);
        }
        this.testExecuteTJob(tJob, withStop);
    }

    private void sleep(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
