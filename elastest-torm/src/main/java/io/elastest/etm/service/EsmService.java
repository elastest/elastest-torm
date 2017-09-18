package io.elastest.etm.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
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
	private Map<String, SupportServiceInstance> servicesInstances;
	private Map<String, SupportServiceInstance> tJobServicesInstances;
	
	
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
	

	public List<String> getRegisteredServicesName() {
		logger.info("Get registered services names.");		
		List<String> registeredServices = new ArrayList<>();

		List<SupportService> services = getRegisteredServices();
		for (SupportService service : services) {
			registeredServices.add(service.getName());
			logger.info("Service name: {} ", service.getName() );
		}
		return registeredServices;
	}
	
	public List<SupportService> getRegisteredServices(){
		logger.info("Get registered services.");
		ObjectMapper mapper = new ObjectMapper();		
		List<SupportService> services = new ArrayList<>();
		JsonNode objs = esmServiceClient.getRegisteredServices();
		for (JsonNode esmService : objs) {
			services.add(new SupportService(esmService.get("id").toString().replaceAll("\"", ""),
					esmService.get("name").toString().replaceAll("\"", "")));
		}		
		return services;		
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public JsonNode getRawRegisteredServices() throws IOException{
		logger.info("Get registered all data of a service.");
		ObjectMapper mapper = new ObjectMapper();			
		return esmServiceClient.getRegisteredServices();
	}
	
	/**
	 * 
	 * @param serviceId
	 * @param associatedWitTJob
	 * @return the new service instance.
	 */
	public SupportServiceInstance provisionServiceInstance(String serviceId, Boolean associatedWitTJob){
		logger.info("Service id to provision: " + serviceId);
		ObjectMapper mapper = new ObjectMapper();
		String instanceId = "";
		SupportServiceInstance newServiceInstance = null;
		
		try {
			JsonNode services = getRawRegisteredServices();
			for (JsonNode service : services) {
				
				if (service.get("id").toString().replaceAll("\"", "").equals(serviceId)){
					instanceId = utilTools.generateUniqueId();
					logger.info("Service instance: " + instanceId);
					List<ObjectNode> plans = Arrays.asList(mapper.readValue(service.get("plans").toString(), ObjectNode[].class));
					newServiceInstance = new SupportServiceInstance(instanceId, service.get("id").toString()
							.replaceAll("\"", ""), plans.get(0).get("id").toString().replaceAll("\"", ""), true);					
					
					esmServiceClient.provisionServiceInstance(newServiceInstance, instanceId, Boolean.toString(false));					
					ObjectNode serviceInstanceDetail = getServiceInstanceInfo(instanceId);
					
					Iterator<String> it = serviceInstanceDetail.get("context").fieldNames();
					while (it.hasNext()) {
						String contextFieldName = it.next();
						if (contextFieldName.contains("HostPort")){
							newServiceInstance.setServicePort(serviceInstanceDetail.get("context").get(contextFieldName).toString().replaceAll("\"", ""));														
						}else if (contextFieldName.contains("_Ip")){
							newServiceInstance.setServiceIp(serviceInstanceDetail.get("context").get(contextFieldName).toString().replaceAll("\"", ""));
						}					
					}
					
					if (associatedWitTJob){
						tJobServicesInstances.put(instanceId, newServiceInstance);
					}else{
						servicesInstances.put(instanceId, newServiceInstance);
					}
				}				
			}
		} catch (IOException e) {					
			logger.error("Error requesting an instance of a service: {}", e.getMessage(), e);
		}		
		return newServiceInstance;
	}
	
	/**
	 * 
	 * 
	 * @param instanceId
	 */
	public String deprovisionServiceInstance(String instanceId, Boolean withTJob){
		String result = "Instance deleted.";
		Map<String, SupportServiceInstance> servicesInstances = withTJob ? tJobServicesInstances : this.servicesInstances;
		SupportServiceInstance serviceInstance = servicesInstances.get(instanceId);
		esmServiceClient.deprovisionServiceInstance(instanceId, serviceInstance);
		servicesInstances.remove(instanceId);
		return result;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public ObjectNode getServiceInstanceInfo(String instanceId) throws IOException{
		logger.info("Get registered all data of a service.");		
		return esmServiceClient.getServiceInstanceInfo(instanceId);				 
	}	
	
	public List<SupportServiceInstance> getServicesInstances() {
		return new ArrayList<SupportServiceInstance>(servicesInstances.values()); 
	}

	public void setServicesInstances(Map<String, SupportServiceInstance> servicesInstances) {
		this.servicesInstances = servicesInstances;
	}

	public Map<String, SupportServiceInstance> gettJobServicesInstances() {
		return tJobServicesInstances;
	}

	public void settJobServicesInstances(Map<String, SupportServiceInstance> tJobsServicesInstances) {
		this.tJobServicesInstances = tJobsServicesInstances;
	}
}
