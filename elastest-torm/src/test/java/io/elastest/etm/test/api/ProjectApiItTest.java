package io.elastest.etm.test.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.elastest.etm.ElastestETMSpringBoot;
import io.elastest.etm.api.model.Project;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElastestETMSpringBoot.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class ProjectApiItTest {
	
	static final Logger log = LoggerFactory.getLogger(ProjectApiItTest.class);
	
	static ObjectMapper mapper;
	
	@Autowired
	TestRestTemplate restTemplate;
	
    @LocalServerPort
    int serverPort;
           
    @BeforeAll
	static void start() {
		mapper = new ObjectMapper();		
	}
    
    @BeforeEach
    void setup() {
        log.debug("App started on port {}", serverPort);
    }
    
    @AfterAll
    static void reset(){
    	
    }
    
    String baseProjectName = "P00000000";
        
	@Test
	public void testCreateProject() throws JsonProcessingException, IOException{
		
		// Test data (input)
        String requestJson = "{ \"id\": 0,\"name\": \"P00000000\"}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(requestJson,headers);

        // Exercise #1 (create project)
        log.debug("POST /project");
        ResponseEntity<Project> response = restTemplate.postForEntity("/api/project",
        		entity, Project.class);
        log.info("Project created:" + response.getBody());
        
        deleteProject(response.getBody().getId());
                
        assertAll("Validating Project Properties",
        		() -> assertTrue(response.getBody().getName().equals("P00000000")),
                () -> assertNotNull(response.getBody().getId())
        );
		
	}
	
	@Test
	public void testGetProjects(){
		
		List<Project> projectsToGet = new ArrayList<>();
		
		for (int i = 0; i < 3; i++){
			projectsToGet.add(createProjectToTesting(baseProjectName + i));
		}
        
        log.debug("GET /project");
        ResponseEntity<Project[]> response = restTemplate.getForEntity("/api/project", Project[].class);
        Project[] projects = response.getBody();
        
        for (Project project: projectsToGet){
			deleteProject(project.getId());
		}
                        
        log.info("Projects Array size:" + projects.length);
        assertTrue(projects.length > 0);
	}
	
	@Test
	public void testGetProjectById(){
		
	}
	
	@Test
	public void testDeleteProject(){
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("projectId", createProjectToTesting("P000000001").getId());
                
        log.info("DELETE /project"); 
        ResponseEntity<Long> response = restTemplate.exchange("/api/project/{projectId}", HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted project:" + response.getBody());
                
        assertTrue(response.getBody().longValue() == urlParams.get("projectId"));
		
	}
	
	private Project createProjectToTesting(String projectName){		

		String requestJson = "{ \"id\": 0,\"name\": \"" + projectName + "\"}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);

		log.info("POST /project");
		ResponseEntity<Project> response = restTemplate.postForEntity("/api/project", entity, Project.class);

		return response.getBody();

	}
	
	private void deleteProject(Long projectToDeleteId){
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("projectId", projectToDeleteId);

        log.info("DELETE /project");
        ResponseEntity<Long> response = restTemplate.exchange("/api/project/{projectId}", HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted project:" + response.getBody());                
        
	}
}
