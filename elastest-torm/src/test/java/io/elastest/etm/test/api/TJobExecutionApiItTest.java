package io.elastest.etm.test.api;

import static java.lang.invoke.MethodHandles.lookup;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.opentest4j.MultipleFailuresError;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.elastest.etm.model.Project;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;

@RunWith(JUnitPlatform.class)
@Tag("it")
public class TJobExecutionApiItTest extends EtmApiItTest {
    final Logger log = getLogger(lookup().lookupClass());

    long projectId;
    Project project;

    List<String> tss = new ArrayList<>();

    @BeforeEach
    void setup() {
        log.info("App started on port {}", serverPort);
        project = createProject("Test_Project");
        projectId = project.getId();
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

    @DisplayName("Run a TJob with parameters, TSS, SUT deployed from image and check the logs ")
    @Disabled
    @Test
    public void testTJobExecutionWithConfig()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start test testCheckTJobExecWithDummyTJob");
        tss.add("{\"id\":\"873f23e8-256d-11e9-ab14-d663bd873d93\",\"name\":\"DUMMY\",\"selected\":true},{\"id\":\"bab3ae67-8c1d-46ec-a940-94183a443825\",\"name\":\"EMS\",\"selected\":false},{\"id\":\"a1920b13-7d11-4ebc-a732-f86a108ea49c\",\"name\":\"EBS\",\"selected\":false},{\"id\":\"fe5e0531-b470-441f-9c69-721c2b4875f2\",\"name\":\"EDS\",\"selected\":false},{\"id\":\"af7947d9-258b-4dd1-b1ca-17450db25ef7\",\"name\":\"ESS\",\"selected\":false},{\"id\":\"29216b91-497c-43b7-a5c4-6613f13fa0e9\",\"name\":\"EUS\",\"selected\":false,\"manifest\":{\"id\":\"2bd62bc2-f768-42d0-8194-562924b494ff\",\"endpoints\":{\"elastest-eus\":{\"description\":\"W3C WebDriver standard sessions operations\",\"main\":true,\"api\":[{\"protocol\":\"http\",\"port\":8040,\"path\":\"/eus/v1/\",\"definition\":{\"type\":\"openapi\",\"path\":\"/eus/v1/api.yaml\"}},{\"name\":\"eusWS\",\"protocol\":\"ws\",\"port\":8040,\"path\":\"/eus/v1/eus-ws\"}],\"gui\":{\"protocol\":\"angular\",\"path\":\"app-elastest-eus\"}}},\"config\":{\"webRtcStats\":{\"name\":\"webRtcStats\",\"type\":\"boolean\",\"label\":\"Gather WebRTC Statistics\",\"default\":false,\"value\":false}}}}");
        TJob tJob = prepareTJob(true, false, false, "elastest/etm-dummy-tjob",
                tss, "elastest/etm-dummy-tss", "8095", "sutFromImage");
        tJob.setCommands(null);
        tJob.setResultsPath("");
        tJob = createTJob(tJob);
        testExecuteTJob(tJob, false, false, true);
    }

    @DisplayName("Run a TJob with parameters and commands, TSS, SUT deployed from image and check the logs ")
    @Disabled
    @Test
    public void testTJobExecutionWithConfig1()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start test testCheckTJobExecWithDummyTJob");
        tss.add("{\"id\":\"873f23e8-256d-11e9-ab14-d663bd873d93\",\"name\":\"DUMMY\",\"selected\":true},{\"id\":\"bab3ae67-8c1d-46ec-a940-94183a443825\",\"name\":\"EMS\",\"selected\":false},{\"id\":\"a1920b13-7d11-4ebc-a732-f86a108ea49c\",\"name\":\"EBS\",\"selected\":false},{\"id\":\"fe5e0531-b470-441f-9c69-721c2b4875f2\",\"name\":\"EDS\",\"selected\":false},{\"id\":\"af7947d9-258b-4dd1-b1ca-17450db25ef7\",\"name\":\"ESS\",\"selected\":false},{\"id\":\"29216b91-497c-43b7-a5c4-6613f13fa0e9\",\"name\":\"EUS\",\"selected\":false,\"manifest\":{\"id\":\"2bd62bc2-f768-42d0-8194-562924b494ff\",\"endpoints\":{\"elastest-eus\":{\"description\":\"W3C WebDriver standard sessions operations\",\"main\":true,\"api\":[{\"protocol\":\"http\",\"port\":8040,\"path\":\"/eus/v1/\",\"definition\":{\"type\":\"openapi\",\"path\":\"/eus/v1/api.yaml\"}},{\"name\":\"eusWS\",\"protocol\":\"ws\",\"port\":8040,\"path\":\"/eus/v1/eus-ws\"}],\"gui\":{\"protocol\":\"angular\",\"path\":\"app-elastest-eus\"}}},\"config\":{\"webRtcStats\":{\"name\":\"webRtcStats\",\"type\":\"boolean\",\"label\":\"Gather WebRTC Statistics\",\"default\":false,\"value\":false}}}}");
        TJob tJob = prepareTJob(true, false, false, "elastest/etm-dummy-tjob",
                tss, "elastest/etm-dummy-tss", "8095", "sutFromImage");
        tJob.setCommands("python main.py");
        tJob.setResultsPath("");
        tJob = createTJob(tJob);
        testExecuteTJob(tJob, false, true, true);
    }

    @DisplayName("Run a TJob with parameters and commands, TSS, SUT deployed from docker-compose and check the logs ")
    @Disabled
    @Test
    public void testTJobExecutionWithConfig2()
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        log.info("Start test testCheckTJobExecWithDummyTJob");
        tss.add("{\"id\":\"873f23e8-256d-11e9-ab14-d663bd873d93\",\"name\":\"DUMMY\",\"selected\":true},{\"id\":\"bab3ae67-8c1d-46ec-a940-94183a443825\",\"name\":\"EMS\",\"selected\":false},{\"id\":\"a1920b13-7d11-4ebc-a732-f86a108ea49c\",\"name\":\"EBS\",\"selected\":false},{\"id\":\"fe5e0531-b470-441f-9c69-721c2b4875f2\",\"name\":\"EDS\",\"selected\":false},{\"id\":\"af7947d9-258b-4dd1-b1ca-17450db25ef7\",\"name\":\"ESS\",\"selected\":false},{\"id\":\"29216b91-497c-43b7-a5c4-6613f13fa0e9\",\"name\":\"EUS\",\"selected\":false,\"manifest\":{\"id\":\"2bd62bc2-f768-42d0-8194-562924b494ff\",\"endpoints\":{\"elastest-eus\":{\"description\":\"W3C WebDriver standard sessions operations\",\"main\":true,\"api\":[{\"protocol\":\"http\",\"port\":8040,\"path\":\"/eus/v1/\",\"definition\":{\"type\":\"openapi\",\"path\":\"/eus/v1/api.yaml\"}},{\"name\":\"eusWS\",\"protocol\":\"ws\",\"port\":8040,\"path\":\"/eus/v1/eus-ws\"}],\"gui\":{\"protocol\":\"angular\",\"path\":\"app-elastest-eus\"}}},\"config\":{\"webRtcStats\":{\"name\":\"webRtcStats\",\"type\":\"boolean\",\"label\":\"Gather WebRTC Statistics\",\"default\":false,\"value\":false}}}}");
        TJob tJob = prepareTJob(true, false, false, "elastest/etm-dummy-tjob",
                tss, null, "8095", "sutFromCompose");
        tJob.setCommands("python main.py");
        tJob.setResultsPath("");
        tJob = createTJob(tJob);
        testExecuteTJob(tJob, false, true, true);
    }

    @Test
    @Disabled
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

    private void testExecuteTJob(TJob tJob, boolean withStop,
            boolean checkMonitoring)
            throws MultipleFailuresError, JsonProcessingException,
            InterruptedException, ExecutionException, TimeoutException {
        testExecuteTJob(tJob, withStop, checkMonitoring, false);
    }

    @SuppressWarnings("rawtypes")
    private void testExecuteTJob(TJob tJob, boolean withStop,
            boolean checkMonitoring, boolean withSuccess)
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        boolean passingTest = true;
        // StompSession stompSession = connectToRabbitMQ(serverPort);

        log.info("POST /api/tjob/{tjobId}/exec");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"tJobParams\" : [{\"name\": \"Param1\", \"value\": \"Value1\", \"multiConfig\": false}]}";
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

            // Check if queue messages works. TODO with ET mini not working
            // because queue contains Trace object

            // Wait for end execution (TODO change to check if queue messages
            // received)
            // int waitTime = 240;
            //
            // String queueToSuscribe = "/topic/" + "test.default_log."
            // + exec.getId() + ".log";
            // log.info("TJob log queue '" + queueToSuscribe + "'");
            //
            // WaitForMessagesHandler handler = new WaitForMessagesHandler(
            // msg -> msg.contains("BUILD SUCCESS")
            // || msg.contains("BUILD FAILURE"));
            //
            // stompSession.subscribe(queueToSuscribe, handler);
            // handler.waitForCompletion(waitTime, TimeUnit.SECONDS);
        }

        passingTest = passingTest && response.getBody() != null
                && response.getBody().getId() != null && response.getBody()
                        .getTjob().getId().equals(urlParams.get("tjobId"));
        log.info("Test Passed on Validating TJobExecution Properties? {}",
                passingTest);

        // Wait for end execution
        while (true) {
            exec = getTJobExecutionById(exec.getId(), tJob.getId()).getBody();
            log.info("TJobExecution {}: {}", exec.getId(), exec.getResultMsg());
            if (exec.isFinished()) {
                log.info("Test results: {}", exec.getTestSuites().toString());
                break;
            }
            sleep(500);
        }

        if (withSuccess && (exec.getResult().equals(ResultEnum.FAIL)
                || exec.getResult().equals(ResultEnum.ERROR)
                || exec.getResult().equals(ResultEnum.STOPPED))) {
            passingTest = false;
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
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            String startDate = df.format(exec.getStartDate());
            String endDate = df.format(exec.getEndDate());

            body = "{ \"indices\": [ \"" + exec.getMonitoringIndex()
                    + "\" ], \"componentsStreams\": [], \"levels\": [], \"size\": 800, \"rangeGTE\": \""
                    + startDate + "\", \"rangeLTE\": \"" + endDate + "\" }";
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
            throws MultipleFailuresError, JsonProcessingException,
            InterruptedException, ExecutionException, TimeoutException {
        testExecuteTJob(withSut, withStop, checkMonitoring, null, null);
    }

    private void testExecuteTJob(boolean withSut, boolean withStop,
            boolean checkMonitoring, String image, List<String> tss)
            throws InterruptedException, ExecutionException, TimeoutException,
            MultipleFailuresError, JsonProcessingException {
        TJob tJob;
        if (withSut) {
            Long sutId = createSut(projectId).getId();
            tJob = createTJob(projectId, sutId);
        } else {
            tJob = createTJob(projectId);
        }

        if (image != null) {
            tJob.setImageName(image);
        }
        if (tss != null && !tss.isEmpty()) {
            log.info("TSS as string: {}", Arrays.toString(tss.toArray()));
            tJob.setSelectedServices(Arrays.toString(tss.toArray()));
        }
        this.testExecuteTJob(tJob, withStop, checkMonitoring);
    }

    private TJob prepareTJob(boolean withSut, boolean withStop,
            boolean checkMonitoring, String image, List<String> tss,
            String sutImage, String port, String sutSpecification)
            throws JsonProcessingException {
        TJob tJob = getSampleTJob(projectId);
        if (withSut) {
            SutSpecification sut = sutExamples.get(sutSpecification);
            sut.setProject(project);
            if (sutImage != null && !sutImage.isEmpty()) {
                sut.setManagedDockerType(ManagedDockerType.IMAGE);
                sut.setSpecification(sutImage);
                sut.setCommands(null);
                sut.setCommandsOption(null);
            }

            sut.setPort(
                    (port != null && !port.isEmpty() ? port : sut.getPort()));
            ResponseEntity<SutSpecification> response = createSutByGiven(sut);
            log.info("Sut creation response: " + response);
            sut = response.getBody();
            if (sut.getId() > -1) {
                tJob.setSut(sut);
            }
        }
        if (image != null) {
            tJob.setImageName(image);
        }
        if (tss != null && !tss.isEmpty()) {
            log.info("TSS as string: {}", Arrays.toString(tss.toArray()));
            tJob.setSelectedServices(Arrays.toString(tss.toArray()));
        }
        return tJob;
    }

    private void sleep(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
