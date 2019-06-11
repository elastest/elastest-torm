package io.elastest.etm.test.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import io.elastest.etm.service.TSSService;

@RunWith(JUnitPlatform.class)
public class EsmApiItTest extends EtmApiItTest{
	static final Logger log = LoggerFactory.getLogger(EsmApiItTest.class);
	
	@Autowired
	TSSService esmService;
	
	@BeforeEach
	public void initContext(){
		
		
	}
	
	@AfterEach
	public void cleanContext(){
		
	}
	
	@Test
	@Disabled
	public void testGetElastestServices(){		
		log.info("Start the test testGetElastestServices");

		log.debug("GET /esm/service");
		ResponseEntity<String[]> objNode = httpClient.getForEntity("/api/esm/service", String[].class);
		
		assertTrue(objNode.getBody().length > 0 );
	}
	
	@Test
	@Disabled
	public void testProvisionServiceInstance(){
		log.info("Start the test testProvisionServiceInstance");							
				
		String service_id = "";
		
		ResponseEntity<String> response = provisionServiceInstance(service_id);
				
		assertTrue(response.getBody() != null && !response.getBody().equals(""));
		
		deprovisionServiceInstance(response.getBody());
		
	}
	
	@Test
	@Disabled
	public void testDeProvisionServiceInstance(){
		log.info("Start the test testDeProvisionServiceInstance");
		
		String service_id = "";
		ResponseEntity<String> requested_instance_id = provisionServiceInstance(service_id);
		
		ResponseEntity<String> responsed_instance_id = deprovisionServiceInstance(requested_instance_id.getBody());
		
		assertTrue(responsed_instance_id.getBody().equals(requested_instance_id));		
	}
	
	private ResponseEntity<String> provisionServiceInstance(String service_id){
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("/esm/service_instances/")
				.queryParam("service_id", service_id);		
		return httpClient.postForEntity(builder.build().toString(), null, String.class);
	}
	
	private ResponseEntity<String> deprovisionServiceInstance(String instance_id){		
		Map<String, String> urlParams = new HashMap<>();
		urlParams.put("instance_id", instance_id);
		
		log.info("DELETE /esm/service_instances/{instance_id}");
		return httpClient.exchange("/esm/service_instances/{instance_id}", HttpMethod.DELETE, null, String.class,
				urlParams);		
	}
	
	

}
