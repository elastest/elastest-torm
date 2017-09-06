package io.elastest.etm.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.model.ServiceInstance;
import io.elastest.etm.service.client.EsmServiceClient;

@Service
public class EsmService {
	private static final Logger logger = LoggerFactory.getLogger(EsmService.class);
		
	@Value("${elastest.esm.files.path}")
	private String EMS_SERVICES_FILES_PATH;

	EsmServiceClient esmServiceClient;
	
	private Map<String, ServiceInstance> servicesInstances;
	
	public EsmService(EsmServiceClient esmServiceClient){
		logger.info("EsmService constructor.");
		this.esmServiceClient = esmServiceClient;
		this.servicesInstances = new HashMap<>();
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
				esmServiceClient.registerService(serviceDefJson.get("register").toString());
				esmServiceClient.registerManifest(serviceDefJson.get("manifest").toString(), serviceDefJson.get("manifest").get("id").toString().replaceAll("\"", ""));				
			}
		}catch(FileNotFoundException fnfe){
			logger.info("Service could not be registered. The file with the path " + EMS_SERVICES_FILES_PATH + " does not exist:");
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getRegisteredServicesName() {
		ObjectMapper mapper = new ObjectMapper();
		List<String> registeredServices = new ArrayList<>();
		try {
			//ObjectNode[] objArray = mapper.readValue(getCatalogedServices(), ObjectNode[].class);
			//List<ObjectNode> services = Arrays.asList(mapper.readValue(esmServiceClient.getCatalogedServices(), ObjectNode[].class));
			List<ObjectNode> services = getRegisteredServices();
			
			for(ObjectNode service: services){
				registeredServices.add(service.get("name").toString().replaceAll("\"", ""));	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return registeredServices;
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<ObjectNode> getRegisteredServices() throws IOException{
		ObjectMapper mapper = new ObjectMapper();		
		
		try {
			return Arrays.asList(mapper.readValue(esmServiceClient.getCatalogedServices(), ObjectNode[].class));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}		 
	}
	
	/**
	 * 
	 * @param serviceName
	 * @return
	 */
	public String provideServiceInstance(String serviceName){
		ObjectMapper mapper = new ObjectMapper();
		String instance_id = "";
		
		try {			
			List<ObjectNode> services = getRegisteredServices();

			for (ObjectNode service : services) {
				if (service.get("name").toString().equals(serviceName)){
					instance_id = generateUniqueId();
					List<ObjectNode> plans = Arrays.asList(mapper.readValue(service.get("plans").toString(), ObjectNode[].class));
					ServiceInstance newServiceInstance = new ServiceInstance(service.get("id").toString()
							.replaceAll("\"", ""), plans.get(0).get("id").toString().replaceAll("\"", ""), true);
					servicesInstances.put(generateUniqueId(), newServiceInstance);
					esmServiceClient.provisionServiceInstance(newServiceInstance, instance_id, Boolean.toString(false));
				}
				
			}
		} catch (IOException e) {
					
			e.printStackTrace();
		}
		
		return instance_id;

	}
	
	/**
	 * 
	 * 
	 * @param instance_id
	 */
	public void deProvideServiceInstance(String instance_id){
		ServiceInstance serviceInstance = servicesInstances.get(instance_id);
		esmServiceClient.deprovisionServiceInstance(instance_id, serviceInstance);		
	}
	
	public String generateUniqueId() {
		return UUID.randomUUID().toString();		
	}
	
	public Map<String, ServiceInstance> getServicesInstances() {
		return servicesInstances;
	}

	public void setServicesInstances(Map<String, ServiceInstance> servicesInstances) {
		this.servicesInstances = servicesInstances;
	}
}
