package io.elastest.etm.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.service.client.EsmServiceClient;

@Service
public class EsmService {
	private static final Logger logger = LoggerFactory.getLogger(EsmService.class);
		
	@Value("${elastest.esm.files.path}")
	private String EMS_SERVICES_FILES_PATH;		

	EsmServiceClient esmServiceClient;
	
	public EsmService(EsmServiceClient esmServiceClient){
		logger.info("EsmService constructor.");
		this.esmServiceClient = esmServiceClient;
	}	
	
	@PostConstruct
	public void init(){
		logger.info("EsmService initialization.");		
		//Register myservice
		try {
			registerElastestServices();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	/**
	 * Register the ElasTest Services into the ESM. 
	 * @throws IOException 
	 * @throws  
	 */
	public void registerElastestServices() throws IOException{
		logger.info("Get and send the register information: " + EMS_SERVICES_FILES_PATH);
		
		try{
			File file = ResourceUtils.getFile("classpath:" + EMS_SERVICES_FILES_PATH);
			List<String> files = new ArrayList<>(Arrays.asList(file.list()));			
	
			for (String nameOfFile: files){
				logger.info("File name:" + nameOfFile);
				File serviceFile = ResourceUtils.getFile("classpath:" + EMS_SERVICES_FILES_PATH + "/" + nameOfFile);
				ObjectMapper mapper = new ObjectMapper();
				String content = new String(Files.readAllBytes(serviceFile.toPath()));
				
				ObjectNode serviceDefJson = mapper.readValue(content, ObjectNode.class);				 
				esmServiceClient.sendServiceRegistryRequest(serviceDefJson.get("register").toString());
				esmServiceClient.sendManifestRegistryRequest(serviceDefJson.get("manifest").toString(), serviceDefJson.get("manifest").get("id").toString());				
			}
		}catch(FileNotFoundException fnfe){
			logger.info("Service could not be registered. The file with the path " + EMS_SERVICES_FILES_PATH + " does not exist:");
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getRegisteredServices() {
		ObjectMapper mapper = new ObjectMapper();
		List<String> registeredServices = new ArrayList<>();
		try {
			//ObjectNode[] objArray = mapper.readValue(getCatalogedServices(), ObjectNode[].class);
			List<ObjectNode> services = Arrays.asList(mapper.readValue(esmServiceClient.getCatalogedServices(), ObjectNode[].class));
			
			for(ObjectNode service: services){
				registeredServices.add(service.get("name").toString());
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return registeredServices;
	}
}
