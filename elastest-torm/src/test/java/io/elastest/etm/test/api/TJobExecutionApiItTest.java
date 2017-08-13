package io.elastest.etm.test.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElastestETMSpringBoot.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TJobExecutionApiItTest extends EtmApiItTest {

	private class WaitForFinishHandler implements StompFrameHandler {

		private CountDownLatch latch = new CountDownLatch(1);

		@Override
		public Type getPayloadType(StompHeaders stompHeaders) {
			return String.class;
		}

		@Override
		public void handleFrame(StompHeaders stompHeaders, Object o) {
			log.info("handleFrame.");
			log.info((String) o);
			if (((String) o).contains("BUILD SUCCESS") || ((String) o).contains("BUILD FAILURE")) {
				log.info("Match message");
				latch.countDown();
			}
		}

		public void waitForCompletion() throws InterruptedException {
			int timeSeconds = 60;
			if(!latch.await(timeSeconds, TimeUnit.SECONDS)){
				throw new RuntimeException("Timeout of "+timeSeconds+" waiting for 'BUILD SUCESS' message");
			}
		}
	}

	private class LogConnectedSessionHandler extends StompSessionHandlerAdapter {
		public void afterConnected(StompSession stompSession, StompHeaders stompHeaders) {
			log.info("STOMP Client connected");
		}
	}

	private static final Logger log = LoggerFactory.getLogger(TJobExecutionApiItTest.class);

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
	public void testExecuteTJob() throws InterruptedException, ExecutionException, TimeoutException {

		log.info("Start the test testExecuteTJob");

		TJob tJob = createTJob(projectId);
		
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		WebSocketStompClient stompClient = new WebSocketStompClient(webSocketClient);
		stompClient.setMessageConverter(new StringMessageConverter());

		String url = "ws://localhost:" + serverPort + "/rabbitMq";
		StompSessionHandler sessionHandler = new LogConnectedSessionHandler();
		StompSession stompSession = stompClient.connect(url, sessionHandler).get(10, TimeUnit.SECONDS);

		log.info("POST /api/tjob/{tjobId}/exec");
		
		Map<String, Object> urlParams = new HashMap<>();
		urlParams.put("tjobId", tJob.getId());
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String body = "[]";
		HttpEntity<String> entity = new HttpEntity<>(body, headers);
		
		ResponseEntity<TJobExecution> response = httpClient.postForEntity("/api/tjob/{tjobId}/exec", entity,
				TJobExecution.class, urlParams);
		
		TJobExecution exec = response.getBody();
		
		log.info("TJobExecution creation response: " + response);

		String queueToSuscribe = "/topic/" + "test." + response.getBody().getId() + ".log";
		log.info("TJob log queue '"+queueToSuscribe+"'");
		
		WaitForFinishHandler handler = new WaitForFinishHandler();
		stompSession.subscribe(queueToSuscribe, handler);

		handler.waitForCompletion();

		assertAll("Validating TJobExecution Properties", 
				() -> assertNotNull(response.getBody()),
				() -> assertNotNull(response.getBody().getId()),
				() -> assertTrue(response.getBody().getTjob().getId().equals(urlParams.get("tjobId"))));

		log.info("TJobExecution: "+getTJobExecutionById(exec.getId(), tJob.getId()));
		
		deleteTJobExecution(exec.getId(), tJob.getId());
		deleteTJob(tJob.getId());
		log.info("Finished.");
	}

}
