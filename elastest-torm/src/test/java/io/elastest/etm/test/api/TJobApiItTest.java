package io.elastest.etm.test.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
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

import io.elastest.etm.ElastestETMSpringBoot;
import io.elastest.etm.api.model.Project;
import io.elastest.etm.api.model.TJob;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElastestETMSpringBoot.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TJobApiItTest {
	static final Logger log = LoggerFactory.getLogger(TJobApiItTest.class);
	
	@LocalServerPort
    int serverPort;
	
	@Autowired
	TestRestTemplate restTemplate;
	
	long projectId;
	
	@BeforeEach
    void setup() {
        log.info("App started on port {}", serverPort);
        projectId = createProjectToTesting("P00000000").getId();
    }
	
	@AfterEach
	void reset(){
		deleteProject(projectId);
	}
	
	@Test
	public void testCreateTJob(){
		log.info("Start the test testCreateTJob" );		
		
		String requestJson ="{"					
					+"\"id\": 0,"
					+"\"imageName\": \"edujgurjc/torm-test-01\","
					+"\"name\": \"testApp1\","
					+"\"project\": { \"id\":" + projectId + "}"
					+ "}";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
		
		log.info("POST /api/tjob");
		ResponseEntity<TJob> response = restTemplate.postForEntity("/api/tjob", entity, TJob.class);
		log.info("TJob created:" + response.getBody());

		deleteTJob(response.getBody().getId());

		assertAll("Validating Project Properties", 
				() -> assertTrue(response.getBody().getName().equals("testApp1")),
				() -> assertNotNull(response.getBody().getId()));

	}
	
	@Test
	public void testModifyTJob(){
		log.info("Start the test testCreateTJob" );
		
		createTJobToTesting(projectId);
		String requestJson ="{"					
					+"\"id\": 0,"
					+"\"imageName\": \"edujgurjc/torm-test-01\","
					+"\"name\": \"testApp2\","
					+"\"project\": { \"id\":" + projectId + "}"
					+ "}";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
		
		log.info("PUT /api/tjob");
		ResponseEntity<TJob> response = restTemplate.postForEntity("/api/tjob", entity, TJob.class);
		log.info("TJob created:" + response.getBody());

		deleteTJob(response.getBody().getId());

		assertAll("Validating Project Properties", 
				() -> assertTrue(response.getBody().getName().equals("testApp2")),
				() -> assertNotNull(response.getBody().getId()));		
	}
	
	@Test 
	public void testGetTJobs(){
		
	}
	
	@Test
	public void testGetTJobById(){
		
	}
	
	@Test
	public void testDeleteTJob(){
		
	}
	
	private TJob createTJobToTesting(long projectId){		

		String requestJson ="{"					
				+"\"id\": 0,"
				+"\"imageName\": \"edujgurjc/torm-test-01\","
				+"\"name\": \"testApp1\","
				+"\"project\": { \"id\":" + projectId + "}"
				+ "}";
	
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
	
		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
		
		log.info("POST /api/tjob");
		ResponseEntity<TJob> response = restTemplate.postForEntity("/api/tjob", entity, TJob.class);
		log.info("TJob created:" + response.getBody());

		return response.getBody();

	}
	
	private void deleteTJob(Long tJobId){
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("tjobId", tJobId);

        log.info("DELETE /api/tjob/{tjobId");
        ResponseEntity<Long> response = restTemplate.exchange("/api/tjob/{tjobId}", HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted tjob:" + response.getBody().longValue());                
        
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
