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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;

import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.service.client.EsmServiceClient;
import io.elastest.etm.utils.UtilTools;

@Service
public class EsmService {
	private static final Logger logger = LoggerFactory.getLogger(EsmService.class);
		
	@Value("${elastest.esm.files.path}")
	public String EMS_SERVICES_FILES_PATH;
	
	@Value("${elastest.execution.mode}")
	public String ELASTEST_EXECUTION_MODE;
	
	@Value("${os.name}")
	private String windowsSO;

	public EsmServiceClient esmServiceClient;
	public DockerService dockerService;
	public UtilTools utilTools;
	private Map<String, SupportServiceInstance> servicesInstances;
	private Map<String, SupportServiceInstance> tJobServicesInstances;
	
	
	public EsmService(EsmServiceClient esmServiceClient, UtilTools utilTools, DockerService dockerService){
		logger.info("EsmService constructor.");
		this.esmServiceClient = esmServiceClient;
		this.utilTools = utilTools;
		this.servicesInstances = new HashMap<>();
		this.dockerService = dockerService;
	}	
	
	@PostConstruct
	public void init() {
		logger.info("EsmService initialization.");
		registerElastestServices();
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
				esmServiceClient.registerManifest("{ " 
						+ "\"id\": " + serviceDefJson.get("manifest").get("id").toString() 
						+ ", \"manifest_content\": " + serviceDefJson.get("manifest").get("manifest_content").toString()
						+ ", \"manifest_type\": " + serviceDefJson.get("manifest").get("manifest_type").toString()
						+ ", \"plan_id\": " + serviceDefJson.get("manifest").get("plan_id").toString()
						+ ", \"service_id\": " + serviceDefJson.get("manifest").get("service_id").toString()
						+ ", \"endpoints\": " + serviceDefJson.get("manifest").get("endpoints").toString()
						+ " }"
						, serviceDefJson.get("manifest").get("id").toString().replaceAll("\"", ""));				
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
					newServiceInstance.setManifestId(serviceInstanceDetail.get("context").get("manifest_id").toString().replaceAll("\"", ""));
				
					buildSrvInstancesUrls(newServiceInstance, serviceInstanceDetail);
					
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
	
	private void buildSrvInstancesUrls(SupportServiceInstance serviceInstance, ObjectNode serviceInstanceDetail) {
		ObjectNode manifest = esmServiceClient.getManifestById(serviceInstance.getManifestId());
		Iterator<String> subServicesNames = manifest.get("endpoints").fieldNames();
		Iterator<String> it = serviceInstanceDetail.get("context").fieldNames();
		
		while (subServicesNames.hasNext()) {			
			String serviceName = subServicesNames.next();
			logger.info("Manifest services {}:" + serviceName);
			String baseRegex = "[0-9a-f]{32}_" + serviceName + "_\\d_Ip";
			Pattern pattern = Pattern.compile(baseRegex);
			String serviceIp = null;
			
			while (it.hasNext()) {
				String fieldName = it.next();
				logger.info("Instance data fields {}:" + fieldName);
				Matcher matcher = pattern.matcher(fieldName);
				if (matcher.matches()) {
					
					String ssrvContainerName = fieldName.substring(0, fieldName.indexOf("_Ip"));
					String networkName = fieldName.substring(0, fieldName.indexOf("_")) + "_elastest_elastest";
					logger.info("Network name: " + networkName);
					serviceIp = windowsSO.toLowerCase().contains("win") ? utilTools.getDockerHostIp() : serviceInstanceDetail.get("context").get(fieldName).toString().replaceAll("\"", "");					
					logger.info("Service Ip {}:" + serviceIp);
					int auxPort;
					
					if (manifest.get("endpoints").get(serviceName).get("main") != null
							&& manifest.get("endpoints").get(serviceName).get("main").booleanValue()) {
						logger.info("Principal instance {}:" + serviceName);
						serviceInstance.setEndpointName(serviceName);
						if(manifest.get("endpoints").get(serviceName).get("api") != null){
							auxPort = bindingPort(serviceInstance,  manifest.get("endpoints").get(serviceName).get("api"), ssrvContainerName, networkName);
							((ObjectNode)manifest.get("endpoints").get(serviceName).get("api")).put("port", auxPort);
							createServiceInstanceData(serviceInstance, manifest.get("endpoints").get(serviceName).get("api"), "api", serviceIp);
						}
						
						if(manifest.get("endpoints").get(serviceName).get("gui") != null){
							auxPort = bindingPort(serviceInstance,  manifest.get("endpoints").get(serviceName).get("gui"), ssrvContainerName, networkName);
							((ObjectNode)manifest.get("endpoints").get(serviceName).get("gui")).put("port", auxPort);
							createServiceInstanceData(serviceInstance, manifest.get("endpoints").get(serviceName).get("gui"), "gui", serviceIp);
						}
						
					}else{
						logger.info("No Principal instance {}:" + serviceName);
						SupportServiceInstance subServiceInstance = new SupportServiceInstance();
						subServiceInstance.setEndpointName(serviceName);
						if(manifest.get("endpoints").get(serviceName).get("api") != null){
							auxPort = bindingPort(serviceInstance,  manifest.get("endpoints").get(serviceName).get("api"), ssrvContainerName, networkName);
							((ObjectNode)manifest.get("endpoints").get(serviceName).get("api")).put("port", auxPort);
							createServiceInstanceData(subServiceInstance, manifest.get("endpoints").get(serviceName).get("api"), "api", serviceIp);
						}
						
						if(manifest.get("endpoints").get(serviceName).get("gui") != null){
							auxPort = bindingPort(serviceInstance,  manifest.get("endpoints").get(serviceName).get("gui"), ssrvContainerName, networkName);
							((ObjectNode)manifest.get("endpoints").get(serviceName).get("gui")).put("port", auxPort);
							createServiceInstanceData(subServiceInstance, manifest.get("endpoints").get(serviceName).get("gui"), "gui", serviceIp);							
						}
						
						serviceInstance.getSubServices().add(subServiceInstance);
					}
					
					break;
				}
			}
		}		
	}
	
	private int bindingPort(SupportServiceInstance serviceInstance, JsonNode node, String containerName, String networkName){		
		DockerClient dockerClient = dockerService.getDockerClient();
		int listenPort = 37000;
		try {
			listenPort = utilTools.findRandomOpenPort();
			List<String> envVariables = new ArrayList<>();
			envVariables.add("LISTEN_PORT=" + listenPort);
			envVariables.add("FORWARD_PORT=" +  node.get("port"));
			Ports portBindings = new Ports();
			ExposedPort exposedListenPort = ExposedPort.tcp(listenPort);
			
			portBindings.bind(exposedListenPort, Ports.Binding.bindPort(listenPort));
			
			serviceInstance.getPortBindingContainers().add(dockerService.runDockerContainer(dockerClient, "tynor88/socat", envVariables, "container" + listenPort, containerName, networkName, portBindings));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return listenPort;
		
	}
		
	private void createServiceInstanceData(SupportServiceInstance serviceInstance, JsonNode node, String nodeName, String serviceIp){	
		logger.info("Create serviceData {}:" + serviceInstance.getEndpointName());
//		DockerClient dockerClient = dockerService.getDockerClient();
//		List<String> envVariables = new ArrayList<>();
		
		if (node != null) {
			if (node.get("protocol") != null && node.get("protocol").toString().contains("http")) {
				serviceInstance.getUrls().put(nodeName, createServiceInstanceUrl(node, serviceIp));
				serviceInstance.setServiceIp(serviceIp);
				
//				try {
//					int bindingPort = utilTools.findRandomOpenPort();
//					
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				
//				
//			}else if (node.isArray()){
//				node.forEach((api) -> {
//					
//				}) ;
			} else{
				serviceInstance.getEndpointsData().put(nodeName, node);
			}
		}
	}
	
	private String createServiceInstanceUrl(JsonNode node, String ip) {
		String url = null;
		url = node.get("protocol").toString().replaceAll("\"", "") + "://" + ip + ":" + node.get("port").toString().replaceAll("\"", "") + node.get("path").toString().replaceAll("\"", "");
		logger.info("New url: " + url);
		return url;
	}
	
	/**
	 * 
	 * 
	 * @param instanceId
	 */
	public String deprovisionServiceInstance(String instanceId, Boolean withTJob){
		DockerClient dockerClient = dockerService.getDockerClient();
		String result = "Instance deleted.";		
		Map<String, SupportServiceInstance> servicesInstances = withTJob ? tJobServicesInstances : this.servicesInstances;
		SupportServiceInstance serviceInstance = servicesInstances.get(instanceId);
		
		for(String containerId: serviceInstance.getPortBindingContainers()){
			dockerService.stopDockerContainer(containerId, dockerClient);
			dockerService.removeDockerContainer(containerId, dockerClient);
		}
		
		esmServiceClient.deprovisionServiceInstance(instanceId, serviceInstance);
		servicesInstances.remove(instanceId);
		return result;
	}
	
	public SupportServiceInstance getServiceInstanceFromMem(String id){
		return servicesInstances.get(id);
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
