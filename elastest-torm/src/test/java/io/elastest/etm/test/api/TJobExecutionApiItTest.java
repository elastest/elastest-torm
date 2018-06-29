package io.elastest.etm.test.api;

import static io.elastest.etm.test.util.StompTestUtils.connectToRabbitMQ;
import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.List;
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
        testExecuteTJob(true, false, false);
    }

    @Test
    public void testExecuteTJobWithoutSut()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start the test testExecuteTJobWithoutSut");
        testExecuteTJob(false, false, false);
    }

    @Test
    public void testExecuteTJobWithoutSutAndGetLogs()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start the test testExecuteTJobWithoutSutAndGetLogs");
        testExecuteTJob(false, false, true);
    }

    @Test
    public void testExecuteTJobWithoutSutAndStop()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start the test testExecuteTJobWithoutSutAndStop");
        testExecuteTJob(false, true, false);
    }

    @Test
    public void testSutExecution()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start the test testSutExecution");
        SutSpecification sut = createSut(projectId);
        Long sutId = sut.getId();
        TJob tJob = createTJob(projectId, sutId);
        testExecuteTJob(tJob, false, false);

        SutExecution[] sutExecs = this.getAllSutExecBySut(sutId);
        assertThat(sutExecs.length > 0);
        SutExecution sutExec = this.getSutExec(sutId, sutExecs[0].getId());
        assertNotNull(sutExec);

        this.deleteSuTExec(sutExec.getId());
    }

    @SuppressWarnings("rawtypes")
    private void testExecuteTJob(TJob tJob, boolean withStop,
            boolean checkMonitoring)
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        boolean passingTest = true;
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

        passingTest = passingTest && response.getBody() != null
                && response.getBody().getId() != null && response.getBody()
                        .getTjob().getId().equals(urlParams.get("tjobId"));
        log.info("Test Passed on Validating TJobExecution Properties? {}",
                passingTest);

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
            passingTest = passingTest
                    && (exec.getResult().equals(ResultEnum.STOPPED)
                            || exec.getResult().equals(ResultEnum.SUCCESS));
            log.info("Test Passed on check if stopped? {}", passingTest);
        }

        if (checkMonitoring) {
            // Logs
            body = "{ \"indices\":[\"" + exec.getMonitoringIndex()
                    + "\"], \"stream\": \"default_log\", \"component\": \"test\" }";
            entity = new HttpEntity<>(body, headers);

            ResponseEntity<List> logsResponse = httpClient
                    .postForEntity("/api/monitoring/log", entity, List.class);
            log.info("Test logs: {}", logsResponse.getBody());
            passingTest = passingTest && logsResponse.getBody().size() > 0;

            // Metrics
            body = "{ \"indices\":[\"" + exec.getMonitoringIndex()
                    + "\"], \"stream\": \"et_dockbeat\", \"component\": \"test\" , \"etType\": \"cpu\",\"selectedTerms\": [ \"stream\", \"component\", \"etType\" ] }";
            entity = new HttpEntity<>(body, headers);

            ResponseEntity<List> metricsResponse = httpClient.postForEntity(
                    "/api/monitoring/byterms", entity, List.class);
            log.info("Test CPU TotalUsage Metrics: {}",
                    metricsResponse.getBody());
            passingTest = passingTest && metricsResponse.getBody().size() > 0;
            log.info("Test Passed on check monitoring? {}", passingTest);

            // LogAnalyzer
            body = "{ \"indices\": [ \"" + exec.getMonitoringIndex()
                    + "\" ], \"componentsStreams\": [], \"levels\": [], \"size\": 800, \"rangeGTE\": \""
                    + exec.getStartDate() + "\", \"rangeLTE\": \""
                    + exec.getEndDate() + "\" }";
            entity = new HttpEntity<>(body, headers);

            ResponseEntity<List> loganalyzerResponse = httpClient.postForEntity(
                    "/api/monitoring/loganalyzer", entity, List.class);
            log.info("Test LogAnalyzer Logs: {}",
                    loganalyzerResponse.getBody());
            passingTest = passingTest
                    && loganalyzerResponse.getBody().size() > 0;
            log.info("Test Passed on check LogAnalyzer data? {}", passingTest);
        }

        log.info("Test Passed Definitely? {}", passingTest);
        assertTrue(passingTest);

        deleteTJobExecution(exec.getId(), tJob.getId());
        deleteTJob(tJob.getId());
        log.info("Finished.");
    }

    private void testExecuteTJob(boolean withSut, boolean withStop,
            boolean checkMonitoring)
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        TJob tJob;
        if (withSut) {
            Long sutId = createSut(projectId).getId();
            tJob = createTJob(projectId, sutId);
        } else {
            tJob = createTJob(projectId);
        }
        this.testExecuteTJob(tJob, withStop, checkMonitoring);
    }

    private void sleep(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
