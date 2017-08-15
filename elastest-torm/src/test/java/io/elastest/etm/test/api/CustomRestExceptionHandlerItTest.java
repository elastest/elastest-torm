package io.elastest.etm.test.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.api.ApiError;
import static io.restassured.RestAssured.*;
import static io.restassured.config.RestAssuredConfig.config;
import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;

//Inspired in https://github.com/eugenp/tutorials/blob/master/spring-security-rest/src/test/java/org/baeldung/web/FooLiveTest.java

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class CustomRestExceptionHandlerItTest extends EtmApiItTest {

	private static Logger log = LoggerFactory.getLogger(CustomRestExceptionHandlerItTest.class);

	@BeforeAll
	protected static void init() {
		
		ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RestAssured.config = config().objectMapperConfig(objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper));
	}
	

	@Test
	public void whenMethodArgumentMismatch_thenBadRequest() {
		final ResponseEntity<ApiError> response = httpClient.getForEntity("/api/project/xx", ApiError.class);
		log.info("Response: " + response);
		final ApiError error = response.getBody();
		assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
		assertEquals(1, error.getErrors().size());
		assertTrue(error.getErrors().get(0).contains("should be of type"));
	}

	@Test
	public void whenNoHandlerForHttpRequest_thenNotFound() {
		final Response response = get(baseUrl() + "/api/xx");
		log.info("Response: " + response.asString());
		assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode());
	}

	@Test	
	public void whenHttpRequestMethodNotSupported_thenMethodNotAllowed() {
		final Response response = delete(baseUrl() + "/api/external/tjob");
		log.info("Response: " + response.asString());
		final ApiError error = response.as(ApiError.class);
		assertEquals(HttpStatus.METHOD_NOT_ALLOWED, error.getStatus());
		assertEquals(1, error.getErrors().size());
		assertTrue(error.getErrors().get(0).contains("Supported methods are"));
	}

	@Test
	public void whenSendInvalidHttpMediaType_thenUnsupportedMediaType() {
		final Response response = given().body("").post(baseUrl() + "/api/tjob");
		log.info("Response: " + response.asString());
		final ApiError error = response.as(ApiError.class);
		assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, error.getStatus());
		assertEquals(1, error.getErrors().size());
		assertTrue(error.getErrors().get(0).contains("media type is not supported"));
	}

}