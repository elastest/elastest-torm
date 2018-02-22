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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import io.elastest.etm.model.Project;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.InstrumentedByEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;

public class EtmApiItTest {

	private static final Logger log = LoggerFactory.getLogger(EtmApiItTest.class);

	@Autowired
	TestRestTemplate httpClient;

	@LocalServerPort
	int serverPort;

	protected String baseUrl() {
		return "http://localhost:"+serverPort;
	}
	
	protected TJob createTJob(long projectId) {
		return createTJob(projectId, -1);
	}
	
	protected TJob createTJob(long projectId, long sutId) {

		String requestJson = "{" + 
				"\"id\": 0," + 
				"\"imageName\": \"elastest/test-etm-test1\"," +
				"\"name\": \"testApp1\"," + 
				"\"parameters\": [{\"Param1\":\"Value1\"}]," +
				"\"resultsPath\": \"/app1TestJobsJenkins/target/surefire-reports/TEST-es.tfcfrd.app1TestJobsJenkins.AppTest.xml\"," +
				"\"project\": { \"id\":" + projectId + "}" +
				
				(sutId == -1? "": ", \"sut\":{ \"id\":" +sutId+"}")
				
				+ "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);

		log.info("POST /api/tjob");
		ResponseEntity<TJob> response = httpClient.postForEntity("/api/tjob", entity, TJob.class);
		
		if(response.getStatusCode() != HttpStatus.OK){
			log.warn("Error creating TJob: "+response);
		}
		
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
	
	protected SutSpecification createSut(long projectId){		

		String requestJson ="{"
				  + "\"description\": \"This is a SuT description example\","
				  + "\"id\": 0,"
				  + "\"name\": \"sut_definition_1\","
				  + "\"project\": { \"id\":"+ projectId + "},"
				  + "\"specification\": \"https://github.com/EduJGURJC/springbootdemo\","
				  + "\"sutType\": \"REPOSITORY\","
				  + "\"instrumentalize\": \"" + false + "\","
				  + "\"currentSutExec\": \"" + null + "\","
                  + "\"instrumentedBy\": \"" + InstrumentedByEnum.WITHOUT + "\","
                  + "\"port\": \"" + null + "\","
                  + "\"managedDockerType\": \"" + ManagedDockerType.IMAGE + "\","
                  + "\"commandsOption\": \"" + CommandsOptionEnum.DEFAULT + "\""
                  +"}";
	
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
	
		HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
		
		log.info("POST /api/sut");
		ResponseEntity<SutSpecification> response = httpClient.postForEntity("/api/sut", entity, SutSpecification.class);
		log.info("Sut created:" + response.getBody());

		return response.getBody();

	}
	
	protected Long deleteSut(Long sutId){
		
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("sutId", sutId);

        log.info("DELETE /api/sut/{sutId}");
        ResponseEntity<Long> response = httpClient.exchange("/api/sut/{sutId}", HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted sutSpecification:" + response.getBody());  
        
        return response.getBody();
        
	}

}
