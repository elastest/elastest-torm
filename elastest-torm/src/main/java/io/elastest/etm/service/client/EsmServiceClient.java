package io.elastest.etm.service.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import com.fasterxml.jackson.databind.node.ObjectNode;


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
	
	RestTemplate httpClient;
	HttpHeaders headers;
	
	public EsmServiceClient(){
		httpClient = new RestTemplate();
		headers = new HttpHeaders();
		List<MediaType> mediaTypes = new ArrayList<MediaType>();
		mediaTypes.add(MediaType.APPLICATION_JSON);		
		headers.setAccept(mediaTypes);
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("x-broker-api-version", "2.12");
	}
	
	/**
	 * 
	 * @param serviceRegistry
	 */
	public void sendServiceRegistryRequest(String serviceRegistry){
		
		HttpEntity<String> entity = new HttpEntity<String>(serviceRegistry, headers);
		
		logger.info("Before send:");
		try{
			httpClient.put(URL_ESM_REGISTER_SERVICE, entity);
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		logger.info("After send:" );
	}
	
	public String sendManifestRegistryRequest(String serviceManifest, String id){
				
		headers.setCacheControl("no-cache");
		headers.set("postman-token", "af2a8114-34cb-cf54-a863-4ad4672ad8c1");
		HttpEntity<String> entity = new HttpEntity<String>(serviceManifest, headers);
		Map<String, String> params = new HashMap<>();
		params.put("manifest_id", id);		
		
		logger.info("Before send:");
		logger.info("Manifest: " + serviceManifest);
		logger.info("Service id:" + id);
		try{
			//httpClient.put(URL_ESM_REGISTER_MANIFEST, entity, params);
			httpClient.exchange(URL_ESM_REGISTER_MANIFEST, HttpMethod.PUT, entity, String.class, params);
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		logger.info("After send:" );
		
		return "";
	}
	
	/**
	 * 
	 * @return
	 */
	public String getCatalogedServices(){		
		HttpHeaders headers = new HttpHeaders();		
		headers.set("x-broker-api-version", "2.12");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
				
		logger.info("Before send:");
		try{
			ResponseEntity<ObjectNode> objNode = httpClient.exchange(URL_GET_CATALOG_ESM, HttpMethod.GET, entity, ObjectNode.class);
			logger.info("After send:" + objNode.toString() );			
			return objNode.getBody().get("services").toString();
		}catch(Exception e){
			logger.error(e.getMessage());
			return null;
		}		
	}

}
