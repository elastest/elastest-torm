package io.elastest.etm.service.client;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SupportServiceInstance.ProvisionView;
import io.elastest.etm.utils.UtilTools;


@Service
public class EsmServiceClient {
	private static final Logger logger = LoggerFactory.getLogger(EsmServiceClient.class);
	
	@Value("${services.ip}")
	private String esmIp;
	
	@Value("${elastest.esm.port}")
	private String esmPort;
	
	@Value("${elastest.esm.url.register.service}")
	private String URL_ESM_REGISTER_SERVICE;
	
	@Value("${elastest.esm.url.catalog}")
	private String URL_GET_CATALOG_ESM;
	
	@Value("${elastest.esm.url.register.manifest}")
	private String URL_ESM_REGISTER_MANIFEST;

	@Value("${elastest.esm.url.request.instance}")
	private String URL_ESM_PROVISION_INSTANCE;
	
	@Value("${elastest.esm.url.deprovision.instance}")
	private String URL_ESM_DEPROVISION_INSTANCE;
	
	@Value("${elastest.esm.url.service-instance.info}")
	private String URL_ESM_SERVICE_INSTANCE_INFO;
	
	@Value("${elastest.esm.url.get.manifest}")
	private String URL_ESM_GET_MANIFEST;
	
	public UtilTools utilTools;
	
	RestTemplate httpClient;
	HttpHeaders headers;
	
	public EsmServiceClient(UtilTools utilTools){
		httpClient = new RestTemplate();
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("x-broker-api-version", "2.12");
		this.utilTools = utilTools;
	}
	
	public void registerService(String serviceRegistry){		
		logger.info("Registering the service in the ESM.");
		HttpEntity<String> entity = new HttpEntity<String>(serviceRegistry, headers);
				
		try{
			httpClient.put(URL_ESM_REGISTER_SERVICE, entity);
			logger.info("Registered service." );
		}catch(Exception e){
			logger.error(e.getMessage());
		}		
	}
	
	public String registerManifest(String serviceManifest, String id){
		logger.info("Registering the service manifest in the ESM.");
		HttpEntity<String> entity = new HttpEntity<String>(serviceManifest, headers);		
		
		Map<String, String> params = new HashMap<>();
		params.put("manifest_id", id);
		
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(URL_ESM_REGISTER_MANIFEST);
		ResponseEntity<ObjectNode> result = null;
		try{						
			result = httpClient.exchange(builder.buildAndExpand(params).toUri(), HttpMethod.PUT, entity, ObjectNode.class);
			logger.info("Manifest registered: {}" + result.getBody().toString());
		}catch(Exception e){
			if (result != null && result.getBody().toString().equals("\"duplicate\"")){				
				logger.info("The manifest already exists:" + e.getMessage());
			}else{
				logger.error("Error registering service: {}", e.getMessage(), e);								
			}			
		}
		logger.info("After send:" );
		
		return "";
	}

	public String provisionServiceInstance(SupportServiceInstance serviceInstance, String instanceId, String accept_incomplete){
		String serviceInstanceData = "";
		logger.info("Request a service instance.");		
		HttpEntity<String> entity = new HttpEntity<String>(utilTools.convertJsonString(serviceInstance, ProvisionView.class), headers);
		
		Map<String, String> params = new HashMap<>();
		params.put("instance_id", instanceId);

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(URL_ESM_PROVISION_INSTANCE)
				.queryParam("accept_incomplete", accept_incomplete);
		
		try{
			httpClient.exchange(builder.buildAndExpand(params).toUri(), HttpMethod.PUT, entity, ObjectNode.class);
			logger.info("Registered service." );
		}catch(Exception e){
			throw new RuntimeException("Exception provisioning service \"" + serviceInstance.getService_id()
					+ "\" with instanceId \"" + instanceId + "\"", e);			
		}		
		
		return serviceInstanceData;		
	}
	
	public void deprovisionServiceInstance(String instanceId, SupportServiceInstance serviceInstance){
		logger.info("Request removal of a service instance.");		
		HttpEntity<String> entity = new HttpEntity<String>(headers);
				
		Map<String, String> params = new HashMap<>();
		params.put("instance_id", instanceId);				
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(URL_ESM_DEPROVISION_INSTANCE)
				.queryParam("service_id", serviceInstance.getService_id())
				.queryParam("plan_id", serviceInstance.getPlan_id());				
		
		try{
			httpClient.exchange(builder.buildAndExpand(params).toUri(), HttpMethod.DELETE, entity, ObjectNode.class);
			logger.info("Registered service." );
		}catch(Exception e){
			throw new RuntimeException("Exception deprovisioning instance \"" + instanceId + "\"", e);
		}				
	}
	
	public JsonNode getRegisteredServices() {
		logger.info("Retrieving the services.");		
		HttpEntity<String> entity = new HttpEntity<String>(headers);
				
		try{
			ResponseEntity<ObjectNode> objNode = httpClient.exchange(URL_GET_CATALOG_ESM, HttpMethod.GET, entity, ObjectNode.class);
			logger.info("Retrieved services.");
			return objNode.getBody().get("services");
		}catch(Exception e){
			throw new RuntimeException("Exception retrieving registered services", e);
		}		
	}
	
	public ObjectNode getServiceInstanceInfo(String instanceId){		
		logger.info("Retrieving service instance info.");
		
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		Map<String, String> params = new HashMap<>();
		params.put("instance_id", instanceId);		
				
		try{
			ResponseEntity<ObjectNode> objNode = httpClient.exchange(URL_ESM_SERVICE_INSTANCE_INFO, HttpMethod.GET, entity, ObjectNode.class, params);
			logger.info("Retrieved services instance info.");
			logger.info("Instance info: " + objNode.getBody().toString());
			return objNode.getBody();
		}catch(Exception e){
			throw new RuntimeException("Exception retrieving info of instance \""+instanceId+"\"", e);
		}		
	}
	
	public ObjectNode getManifestById(String manifestId){
		logger.info("Manifest to retrieve " + manifestId);
		Map<String, String> params = new HashMap<>();
		params.put("manifest_id", manifestId);
				
		try{
			ResponseEntity<ObjectNode[]> objNode = httpClient.exchange(URL_ESM_GET_MANIFEST, HttpMethod.GET, null, ObjectNode[].class, params);			
			logger.info("Manifest info: " + objNode.getBody()[0].toString());
			return objNode.getBody()[0];
		}catch(Exception e){
			logger.error("Error retrieving manifest by id: {}", e.getMessage(), e);
			return null;
		}		
	}
}
