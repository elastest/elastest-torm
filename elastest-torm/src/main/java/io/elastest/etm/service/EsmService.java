package io.elastest.etm.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.model.EsmServiceModel;
import io.elastest.etm.model.ServiceInstance;
import io.elastest.etm.service.client.EsmServiceClient;
import io.elastest.etm.utils.ElastestConstants;
import io.elastest.etm.utils.UtilTools;

@Service
public class EsmService {
	private static final Logger logger = LoggerFactory.getLogger(EsmService.class);
		
	@Value("${elastest.esm.files.path}")
	public String EMS_SERVICES_FILES_PATH;
	
	@Value("${elastest.execution.mode}")
	public String ELASTEST_EXECUTION_MODE;	

	public EsmServiceClient esmServiceClient;	
	public UtilTools utilTools;
	private Map<String, ServiceInstance> servicesInstances;
	
	
	public EsmService(EsmServiceClient esmServiceClient, UtilTools utilTools){
		logger.info("EsmService constructor.");
		this.esmServiceClient = esmServiceClient;
		this.utilTools = utilTools;
		this.servicesInstances = new HashMap<>();		
	}	
	
	@PostConstruct
	public void init(){
		logger.info("Elastest mode " + ELASTEST_EXECUTION_MODE + ", 2 " + ElastestConstants.MODE_NORMAL);
		if (ELASTEST_EXECUTION_MODE.equals(ElastestConstants.MODE_NORMAL)) {
			logger.info("EsmService initialization.");		
			registerElastestServices();			
		}
	}
	
	/**
	 * Register the ElasTest Services into the ESM.   
	 */
	public void registerElastestServices() {
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
				logger.info("{ " 
						+ "\"id\": " + serviceDefJson.get("manifest").get("id").toString() 
						+ ", \"manifest_content\": " + serviceDefJson.get("manifest").get("manifest_content").toString()
						+ ", \"manifest_type\": " + serviceDefJson.get("manifest").get("manifest_type").toString()
						+ ", \"plan_id\": " + serviceDefJson.get("manifest").get("plan_id").toString()
						+ ", \"service_id\": " + serviceDefJson.get("manifest").get("service_id").toString()
						+ " }");
				esmServiceClient.registerManifest("{ " 
						+ "\"id\": " + serviceDefJson.get("manifest").get("id").toString() 
						+ ", \"manifest_content\": " + serviceDefJson.get("manifest").get("manifest_content").toString()
						+ ", \"manifest_type\": " + serviceDefJson.get("manifest").get("manifest_type").toString()
						+ ", \"plan_id\": " + serviceDefJson.get("manifest").get("plan_id").toString()
						+ ", \"service_id\": " + serviceDefJson.get("manifest").get("service_id").toString()
						+ " }"
						/*serviceDefJson.get("manifest").toString()*/, serviceDefJson.get("manifest").get("id").toString().replaceAll("\"", ""));				
			}
		}catch(IOException fnfe){
			logger.info("Service could not be registered. The file with the path " + EMS_SERVICES_FILES_PATH + " does not exist:");
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public List<String> getRegisteredServicesName() {
		logger.info("Get registered services names.");		
		List<String> registeredServices = new ArrayList<>();

		List<EsmServiceModel> services = getRegisteredServices();
		for (EsmServiceModel service : services) {
			registeredServices.add(service.getName());
			logger.info("Service name: {} ", service.getName() );
		}
		return registeredServices;
	}
	
	public List<EsmServiceModel> getRegisteredServices(){
		logger.info("Get registered services.");
		ObjectMapper mapper = new ObjectMapper();
		List<ObjectNode> objs;
		List<EsmServiceModel> services = new ArrayList<>();
		
		try {
			objs = Arrays.asList(mapper.readValue(esmServiceClient.getRegisteredServices(), ObjectNode[].class));
			for(ObjectNode esmService: objs){
				services.add(new EsmServiceModel(esmService.get("id").toString().replaceAll("\"", ""), esmService.get("name").toString().replaceAll("\"", "")));	
			}
			return services;
		} catch (IOException e) {			
			logger.error("Error retrieving registered services: {}", e.getMessage(), e);
			return services;
		}		 
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public List<ObjectNode> getRawRegisteredServices() throws IOException{
		logger.info("Get registered all data of a service.");
		ObjectMapper mapper = new ObjectMapper();		
		
		try {
			return Arrays.asList(mapper.readValue(esmServiceClient.getRegisteredServices(), ObjectNode[].class));
		} catch (IOException e) {
			logger.error("Error retrieving registered services: {}", e.getMessage(), e);
			throw e;
		}		 
	}
	
	/**
	 * 
	 * @param serviceName
	 * @return the id of the new instance.
	 */
	public String provisionServiceInstance(String serviceName){
		ObjectMapper mapper = new ObjectMapper();
		String instance_id = "";
		
		try {			
			List<ObjectNode> services = getRawRegisteredServices();
			for (ObjectNode service : services) {
				if (service.get("name").toString().equals(serviceName)){
					instance_id = utilTools.generateUniqueId();
					List<ObjectNode> plans = Arrays.asList(mapper.readValue(service.get("plans").toString(), ObjectNode[].class));
					ServiceInstance newServiceInstance = new ServiceInstance(service.get("id").toString()
							.replaceAll("\"", ""), plans.get(0).get("id").toString().replaceAll("\"", ""), true);
					servicesInstances.put(instance_id, newServiceInstance);
					esmServiceClient.provisionServiceInstance(newServiceInstance, instance_id, Boolean.toString(false));
				}				
			}
		} catch (IOException e) {					
			logger.error("Error requesting an instance of a service: {}", e.getMessage(), e);
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
		servicesInstances.remove(instance_id);
	}
	
//	public String generateUniqueId() {
//		return UUID.randomUUID().toString();		
//	}
	
	public Map<String, ServiceInstance> getServicesInstances() {
		return servicesInstances;
	}

	public void setServicesInstances(Map<String, ServiceInstance> servicesInstances) {
		this.servicesInstances = servicesInstances;
	}
}
