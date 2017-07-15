package io.elastest.etm.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import io.elastest.etm.ElastestETMSpringBoot;
import io.elastest.etm.api.model.Project;
import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;

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
	
	String baseTJobName = "tJob";
	
	static CountDownLatch latch;
	
	BlockingQueue<String> blockingQueue;
	
	@BeforeAll
	static void staticSetup(){
		latch = new CountDownLatch(1);
	}
	
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

		assertAll("Validating tJob Properties", 
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

		assertAll("Validating TJob Properties", 
				() -> assertTrue(response.getBody().getName().equals("testApp2")),
				() -> assertNotNull(response.getBody().getId()));		
	}
	
	
	@Test 
	public void testGetTJobs(){
		log.info("Start the test testGetTJobs" );
		
		List<TJob> tJobsToGet = new ArrayList<>();
		
		for (int i = 0; i < 3; i++){
			tJobsToGet.add(createTJobToTesting(projectId));
		}
        
        log.debug("GET /tjob");
        ResponseEntity<TJob[]> response = restTemplate.getForEntity("/api/tjob", TJob[].class);
        TJob[] tJobs = response.getBody();
        
        for (TJob tjob: tJobs){
        	deleteTJob(tjob.getId());
		}
                        
        log.info("TJobs Array size:" + tJobs.length);
        assertTrue(tJobs.length > 0);
		
	}
	
	
	public void testGetTJobById(){
		
		log.info("Start the test testGetTJobById" );

		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("id", createTJobToTesting(projectId).getId());
		
		
		log.info("GET /tjob/{id}");
		ResponseEntity<Project> response = restTemplate.getForEntity("/api/tjob/{id}", Project.class, urlParams);
		
		deleteTJob(response.getBody().getId());
		
		assertAll("Validating TJob Properties",
				() -> assertNotNull(response.getBody()),
	    		() -> assertTrue(response.getBody().getId().equals(urlParams.get("id")))
	    );		
		
	}
	
	
	public void testDeleteTJob(){
		log.info("Start the test testDeleteTJob" );
		
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("tjobId", createTJobToTesting(projectId).getId());

        log.info("DELETE /api/tjob/{tjobId");
        ResponseEntity<Long> response = restTemplate.exchange("/api/tjob/{tjobId}", HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted tjob:" + response.getBody().longValue());
        
        assertTrue(response.getBody().longValue() == urlParams.get("tjobId"));
		
	}
	
	@Test
	public void testExecuteTJob() throws InterruptedException, ExecutionException, TimeoutException{
		log.info("Start the test testExecuteTJob" );
		
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("tjobId", createTJobToTesting(projectId).getId());		
		
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
		stompClient.setMessageConverter(new StringMessageConverter());
		
		blockingQueue = new LinkedBlockingDeque<>();

		String url = "ws://localhost:" + serverPort + "/rabbitMq";
		StompSessionHandler sessionHandler = new MyStompSessionHandler();
		StompSession stompSession = stompClient.connect(url, sessionHandler).get(1, TimeUnit.SECONDS);
		
		log.info("POST /api/tjob/{tjobId}/exec");
		ResponseEntity<TJobExecution> response = restTemplate.postForEntity("/api/tjob/{tjobId}/exec", null, TJobExecution.class, urlParams);
		
		String queueToSuscribe = "/topic/"+ "test." + response.getBody().getId() + ".log";
		log.info(queueToSuscribe);
		stompSession.subscribe(queueToSuscribe, new DefaultStompFrameHandler());		
		
		latch.await(30, TimeUnit.SECONDS);
		
		assertAll("Validating TJobExecution Properties",
				() -> assertNotNull(response.getBody()),
				() -> assertNotNull(response.getBody().getId()),
	    		() -> assertTrue(response.getBody().getTjob().getId().equals(urlParams.get("tjobId")))
	    );	
		
		deleteTJobExecution(response.getBody().getId(), urlParams.get("tjobId"));
		deleteTJob(response.getBody().getId());
		log.info("Finished.");
		
	}
	
	class DefaultStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
        	log.info("PayLoadType string.");        	
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
        	log.info("handleFrame.");
        	log.info((String)o);
            if (((String)o).contains("BUILD SUCCESS") || ((String)o).contains("BUILD FAILURE")){
            	log.info("Match message");
            	latch.countDown();
            }
        }
    }
	
	private class MyStompSessionHandler extends StompSessionHandlerAdapter {
        public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
            log.info("Now connected");
        }
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
	
	private void deleteTJobExecution(Long tJobExecId, Long tJobId){
		Map<String, Long> urlParams = new HashMap<>();
		urlParams.put("tJobExecId", tJobExecId);
		urlParams.put("tJobId", tJobId);

        log.info("DELETE /api/tjob/{tJobId}/exec/{tJobExecId}");
        ResponseEntity<Long> response = restTemplate.exchange("/api/tjob/{tJobId}/exec/{tJobExecId}", HttpMethod.DELETE, null, Long.class, urlParams);
        log.info("Deleted tJobExec:" + response.getBody().longValue());        
        
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
