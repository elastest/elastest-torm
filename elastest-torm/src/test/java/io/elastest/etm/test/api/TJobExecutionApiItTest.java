package io.elastest.etm.test.api;

import static io.elastest.etm.test.util.StompTestUtils.connectToRabbitMQ;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.opentest4j.MultipleFailuresError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.test.util.StompTestUtils.WaitForMessagesHandler;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class TJobExecutionApiItTest extends EtmApiItTest {

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
	@Disabled
	public void testExecuteTJobWithSut() throws InterruptedException, ExecutionException, TimeoutException {

		testExecuteTJob(true);
	}

	@Test
	public void testExecuteTJobWithoutSut() throws InterruptedException, ExecutionException, TimeoutException {

		testExecuteTJob(false);
	}

	private void testExecuteTJob(boolean withSut)
			throws InterruptedException, ExecutionException, TimeoutException, MultipleFailuresError {

		log.info("Start the test testExecuteTJob " + (withSut ? "with" : "without") + " SuT");

		TJob tJob;

		if (withSut) {
			Long sutId = createSut(projectId).getId();
			tJob = createTJob(projectId, sutId);
		} else {
			tJob = createTJob(projectId);
		}

		StompSession stompSession = connectToRabbitMQ(serverPort);

		log.info("POST /api/tjob/{tjobId}/exec");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String body = "{\"tJobParams\" : [{\"Param1\":\"NewValue1\"}], \"sutParams\" : [{\"Param1\":\"NewValue1\"}]}";
		HttpEntity<String> entity = new HttpEntity<>(body, headers);

		Map<String, Object> urlParams = new HashMap<>();
		urlParams.put("tjobId", tJob.getId());

		ResponseEntity<TJobExecution> response = httpClient.postForEntity("/api/tjob/{tjobId}/exec", entity,
				TJobExecution.class, urlParams);

		TJobExecution exec = response.getBody();

		log.info("TJobExecution creation response: " + response);

		if (withSut) {

			String queueToSuscribe = "/topic/" + "sut.default_log." + exec.getId() + ".log";
			log.info("Sut log queue '" + queueToSuscribe + "'");

			WaitForMessagesHandler handler = new WaitForMessagesHandler();
			stompSession.subscribe(queueToSuscribe, handler);

			handler.waitForCompletion(5, TimeUnit.SECONDS);

			log.info("Sut log queue received a message");
		}

		String queueToSuscribe = "/topic/" + "test.default_log." + exec.getId() + ".log";
		log.info("TJob log queue '" + queueToSuscribe + "'");

		WaitForMessagesHandler handler = new WaitForMessagesHandler(
				msg -> msg.contains("BUILD SUCCESS") || msg.contains("BUILD FAILURE"));

		stompSession.subscribe(queueToSuscribe, handler);
		handler.waitForCompletion(180, TimeUnit.SECONDS);

		assertAll("Validating TJobExecution Properties", () -> assertNotNull(response.getBody()),
				() -> assertNotNull(response.getBody().getId()),
				() -> assertTrue(response.getBody().getTjob().getId().equals(urlParams.get("tjobId"))));

		while (true) {
			exec = getTJobExecutionById(exec.getId(), tJob.getId()).getBody();
			log.info("TJobExecution: " + exec);
			if (exec.getResult() != ResultEnum.IN_PROGRESS) {

				log.info("Test results:" + exec.getTestSuite());

				break;
			}
			sleep(500);
		}

		deleteTJobExecution(exec.getId(), tJob.getId());
		deleteTJob(tJob.getId());
		log.info("Finished.");
	}

	

	private void sleep(int waitTime) {
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
