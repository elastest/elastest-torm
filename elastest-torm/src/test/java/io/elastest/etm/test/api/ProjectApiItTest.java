package io.elastest.etm.test.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.elastest.etm.model.Project;
import io.elastest.etm.test.IntegrationBaseTest;

@RunWith(JUnitPlatform.class)
public class ProjectApiItTest extends IntegrationBaseTest {

    private static final Logger log = LoggerFactory
            .getLogger(ProjectApiItTest.class);

    private static final String PROJECT_NAME = "Project";

    @Autowired
    TestRestTemplate httpClient;

    @LocalServerPort
    int serverPort;

    @BeforeEach
    void setup() {
        log.debug("App started on port {}", serverPort);
    }

    @Test
    public void testCreateProject()
            throws JsonProcessingException, IOException {

        // Test data (input)
        String requestJson = "{ \"id\": 0,\"name\": \"" + PROJECT_NAME + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

        // Exercise #1 (create project)
        log.debug("POST /project");
        ResponseEntity<Project> response = httpClient
                .postForEntity("/api/project", entity, Project.class);

        log.info("Project created:" + response.getBody());

        try {

            assertTrue(response.getBody().getName().equals(PROJECT_NAME));
            assertNotNull(response.getBody().getId());

        } finally {
            deleteProject(response.getBody().getId());
        }
    }

    @Test
    public void testGetProjects() {

        Map<Long, Project> createdProjects = new HashMap<>();

        try {

            int numProjects = 5;

            for (int i = 0; i < numProjects; i++) {
                Project project = createProject(PROJECT_NAME + i);
                createdProjects.put(project.getId(), project);
            }

            log.debug("GET /project");

            ResponseEntity<Project[]> response = httpClient
                    .getForEntity("/api/project", Project[].class);
            Project[] projects = response.getBody();

            log.info("Projects Array size:" + projects.length);

            Map<Long, Project> retrievedProjects = new HashMap<>();
            for (Project project : projects) {
                retrievedProjects.put(project.getId(), project);
            }

            assertTrue(retrievedProjects.keySet()
                    .containsAll(createdProjects.keySet()));

        } finally {

            for (Long projectId : createdProjects.keySet()) {
                deleteProject(projectId);
            }
        }
    }

    @Test
    public void testGetProjectById() {

        long projectId = -1;

        try {

            Project project = createProject(PROJECT_NAME);
            projectId = project.getId();

            Map<String, Long> urlParams = new HashMap<>();
            urlParams.put("id", projectId);

            log.info("GET /project/{id}");
            ResponseEntity<Project> response = httpClient.getForEntity(
                    "/api/project/{id}", Project.class, urlParams);

            assertNotNull(response.getBody());
            assertTrue(response.getBody().getName().equals(PROJECT_NAME));

        } finally {

            if (projectId != -1) {
                deleteProject(projectId);
            }
        }
    }

    @Test
    public void testDeleteProject() {

        Project project = createProject(PROJECT_NAME);

        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("id", project.getId());

        log.info("DELETE /project");
        ResponseEntity<Long> response = httpClient.exchange("/api/project/{id}",
                HttpMethod.DELETE, null, Long.class, urlParams);

        log.info("Deleted project:" + response.getBody());

        assertTrue(response.getBody().longValue() == project.getId());

    }

    private Project createProject(String projectName) {

        String requestJson = "{ \"id\": 0,\"name\": \"" + projectName + "\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(requestJson,
                headers);

        log.info("POST /project");
        ResponseEntity<Project> response = httpClient
                .postForEntity("/api/project", entity, Project.class);

        return response.getBody();

    }

    private void deleteProject(Long projectToDeleteId) {
        Map<String, Long> urlParams = new HashMap<>();
        urlParams.put("projectId", projectToDeleteId);

        log.info("DELETE /project");
        ResponseEntity<Long> response = httpClient.exchange(
                "/api/project/{projectId}", HttpMethod.DELETE, null, Long.class,
                urlParams);
        log.info("Deleted project:" + response.getBody());

    }
}
