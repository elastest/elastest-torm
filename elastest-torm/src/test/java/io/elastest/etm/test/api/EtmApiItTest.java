package io.elastest.etm.test.api;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import io.elastest.etm.api.model.Project;
import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;

public class EtmApiItTest {

	private static final Logger log = LoggerFactory.getLogger(EtmApiItTest.class);

	@Autowired
	TestRestTemplate httpClient;

	@LocalServerPort
	int serverPort;

	protected TJob createTJob(long projectId) {

		String requestJson = "{" + "\"id\": 0," + "\"imageName\": \"edujgurjc/torm-test-01\","
				+ "\"name\": \"testApp1\"," + "\"project\": { \"id\":" + projectId + "}" + "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);

		log.info("POST /api/tjob");
		ResponseEntity<TJob> response = httpClient.postForEntity("/api/tjob", entity, TJob.class);
		log.info("TJob created:" + response.getBody());

		return response.getBody();

	}

	protected void deleteTJob(Long tJobId) {
		
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("tjobId", tJobId);

		log.info("DELETE /api/tjob/{tjobId");
		ResponseEntity<Long> response = httpClient.exchange("/api/tjob/{tjobId}", HttpMethod.DELETE, null, Long.class,
				urlParams);
		log.info("Deleted tjob:" + response.getBody().longValue());

	}

	protected Project createProject(String projectName) {

		String requestJson = "{ \"id\": 0,\"name\": \"" + projectName + "\"}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);

		log.info("POST /project");
		ResponseEntity<Project> response = httpClient.postForEntity("/api/project", entity, Project.class);

		return response.getBody();

	}

	protected void deleteProject(Long projectToDeleteId) {

		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("projectId", projectToDeleteId);

		log.info("DELETE /project");
		ResponseEntity<Long> response = httpClient.exchange("/api/project/{projectId}", HttpMethod.DELETE, null,
				Long.class, urlParams);
		log.info("Deleted project:" + response.getBody());

	}
	
	protected TJob getTJobById(Long tJobId) {

		log.info("Start the method getTJobById");

		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("tJobId", tJobId);

		log.info("GET /api/tjob/{tJobId}");
		ResponseEntity<TJob> response = httpClient.getForEntity("/api/tjob/{tJobId}", TJob.class, urlParams);

		return response.getBody();

	}
	
	protected void deleteTJobExecution(Long tJobExecId, Long tJobId){
		
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("tJobExecId", tJobExecId);
		urlParams.put("tJobId", tJobId);

        log.info("DELETE /api/tjob/{tJobId}/exec/{tJobExecId}");
        ResponseEntity<Long> response = httpClient.exchange("/api/tjob/{tJobId}/exec/{tJobExecId}", HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted tJobExec:" + response.getBody().longValue());        
        
	}
	
	protected ResponseEntity<TJobExecution> getTJobExecutionById(Long tJobExecId, Long tJobId){
		
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("tJobExecId", tJobExecId);
		urlParams.put("tJobId", tJobId);

        log.info("GET /api/tjob/{tJobId}/exec/{tJobExecId}");
        ResponseEntity<TJobExecution> response = httpClient.getForEntity("/api/tjob/{tJobId}/exec/{tJobExecId}", TJobExecution.class, urlParams);
        
        return response;        
	}

}
