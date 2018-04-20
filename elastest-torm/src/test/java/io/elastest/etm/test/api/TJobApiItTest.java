package io.elastest.etm.test.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.model.Project;
import io.elastest.etm.model.TJob;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TJobApiItTest extends EtmApiItTest {

    static final Logger log = LoggerFactory.getLogger(TJobApiItTest.class);

    long projectId;

    @BeforeEach
    void setup() {
        log.info("App started on port {}", serverPort);
        projectId = createProject("P00000000").getId();
    }

    @AfterEach
    void reset() {
        deleteProject(projectId);
    }

    @Test
    public void testCreateTJob() throws JsonProcessingException {
        log.info("Start the test testCreateTJob");

        TJob tJob = getSampleTJob(projectId);
        ResponseEntity<TJob> response = createTJobByGiven(tJob);
        log.info("TJob creation response: " + response);

        deleteTJob(response.getBody().getId());

        assertAll("Validating tJob Properties",
                () -> assertTrue(
                        response.getBody().getName().equals("testApp1")),
                () -> assertNotNull(response.getBody().getId()));
    }

    @Test
    public void testModifyTJob() throws JsonProcessingException {
        log.info("Start the test testCreateTJob");

        TJob tJob = createTJob(projectId);
        tJob.setName("testApp2");

        modifyTJob(tJob);
        TJob tJobModified = getTJobById(tJob.getId());

        deleteTJob(tJobModified.getId());

        assertAll("Validating TJob Properties",
                () -> assertTrue(tJobModified.getName().equals("testApp2")),
                () -> assertNotNull(tJobModified.getId()));
    }

    @Test
    public void testGetTJobs() throws JsonProcessingException {
        log.info("Start the test testGetTJobs");

        List<TJob> tJobsToGet = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            tJobsToGet.add(createTJob(projectId));
        }

        log.debug("GET /tjob");
        ResponseEntity<TJob[]> response = httpClient.getForEntity("/api/tjob",
                TJob[].class);
        TJob[] tJobs = response.getBody();

        for (TJob tjob : tJobs) {
            deleteTJob(tjob.getId());
        }

        log.info("TJobs Array size:" + tJobs.length);
        assertTrue(tJobs.length > 0);

    }

    @Test
    public void testGetTJobById() throws JsonProcessingException {

        log.info("Start the test testGetTJobById");

        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("id", createTJob(projectId).getId());

        log.info("GET /tjob/{id}");
        ResponseEntity<Project> response = httpClient
                .getForEntity("/api/tjob/{id}", Project.class, urlParams);

        deleteTJob(response.getBody().getId());

        assertAll("Validating TJob Properties",
                () -> assertNotNull(response.getBody()),
                () -> assertTrue(response.getBody().getId()
                        .equals(urlParams.get("id"))));

    }

    @Test
    public void testDeleteTJob() throws JsonProcessingException {
        log.info("Start the test testDeleteTJob");

        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("tjobId", createTJob(projectId).getId());

        log.info("DELETE /api/tjob/{tjobId");
        ResponseEntity<Long> response = httpClient.exchange(
                "/api/tjob/{tjobId}", HttpMethod.DELETE, null, Long.class,
                urlParams);
        log.info("Deleted tjob:" + response.getBody().longValue());

        assertTrue(response.getBody().longValue() == urlParams.get("tjobId"));

    }

}
