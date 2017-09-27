package io.elastest.etm.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

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

	public static final String ET_SOCAT_IMAGE = "franciscordiaz/docker-socat"; 
	
	@Value("${elastest.esm.files.path}")
	public String EMS_SERVICES_FILES_PATH;

	@Value("${elastest.execution.mode}")
	public String ELASTEST_EXECUTION_MODE;	

	@Value("${os.name}")
	private String windowsSO;
	
	@Value("${elastest.docker.network}")
	private String etDockerNetwork;

	public EsmServiceClient esmServiceClient;
	public DockerService dockerService;
	public UtilTools utilTools;
	private Map<String, SupportServiceInstance> servicesInstances;
	private Map<String, SupportServiceInstance> tJobServicesInstances;

	public EsmService(EsmServiceClient esmServiceClient, UtilTools utilTools, DockerService dockerService) {
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
		try {
			Resource resource = new ClassPathResource(EMS_SERVICES_FILES_PATH);
			BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()), 1024);
			String line;
			while ((line = br.readLine()) != null) {

				String nameOfFile = line;
				logger.info("File name:" + nameOfFile);
				Resource serviceFile = new ClassPathResource(EMS_SERVICES_FILES_PATH + line);
				ObjectMapper mapper = new ObjectMapper();
				String content = new String(Files.readAllBytes(serviceFile.getFile().toPath()));

				ObjectNode serviceDefJson = mapper.readValue(content, ObjectNode.class);
				esmServiceClient.registerService(serviceDefJson.get("register").toString());
				esmServiceClient.registerManifest("{ " + "\"id\": "
						+ serviceDefJson.get("manifest").get("id").toString() + ", \"manifest_content\": "
						+ serviceDefJson.get("manifest").get("manifest_content").toString() + ", \"manifest_type\": "
						+ serviceDefJson.get("manifest").get("manifest_type").toString() + ", \"plan_id\": "
						+ serviceDefJson.get("manifest").get("plan_id").toString() + ", \"service_id\": "
						+ serviceDefJson.get("manifest").get("service_id").toString() + ", \"endpoints\": "
						+ serviceDefJson.get("manifest").get("endpoints").toString() + " }",
						serviceDefJson.get("manifest").get("id").toString().replaceAll("\"", ""));
			}
			br.close();
		} catch (IOException fnfe) {
			logger.warn("Service could not be registered. The file with the path " + EMS_SERVICES_FILES_PATH
					+ " does not exist:", fnfe);
		}
	}

	public List<String> getRegisteredServicesName() {
		logger.info("Get registered services names.");
		List<String> registeredServices = new ArrayList<>();

		List<SupportService> services = getRegisteredServices();
		for (SupportService service : services) {
			registeredServices.add(service.getName());
			logger.info("Service name: {} ", service.getName());
		}
		return registeredServices;
	}

	public List<SupportService> getRegisteredServices() {
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
	public JsonNode getRawRegisteredServices() throws IOException {
		logger.info("Get registered all data of a service.");
		return esmServiceClient.getRegisteredServices();
	}

	/**
	 * 
	 * @param serviceId
	 * @param associatedWitTJob
	 * @return the new service instance.
	 */
	public SupportServiceInstance provisionServiceInstance(String serviceId, Boolean associatedWitTJob) {
		logger.info("Service id to provision: " + serviceId);
		ObjectMapper mapper = new ObjectMapper();
		String instanceId = "";
		SupportServiceInstance newServiceInstance = null;

		try {
			JsonNode services = getRawRegisteredServices();
			for (JsonNode service : services) {
				if (service.get("id").toString().replaceAll("\"", "").equals(serviceId)) {
					instanceId = utilTools.generateUniqueId();
					logger.info("Service instance: " + instanceId);
					List<ObjectNode> plans = Arrays
							.asList(mapper.readValue(service.get("plans").toString(), ObjectNode[].class));
					newServiceInstance = new SupportServiceInstance(instanceId,
							service.get("id").toString().replaceAll("\"", ""),
							plans.get(0).get("id").toString().replaceAll("\"", ""), true);

					esmServiceClient.provisionServiceInstance(newServiceInstance, instanceId, Boolean.toString(false));
					ObjectNode serviceInstanceDetail = getServiceInstanceInfo(instanceId);
					newServiceInstance.setManifestId(serviceInstanceDetail.get("context").get("manifest_id").toString().replaceAll("\"", ""));
					buildSrvInstancesUrls(newServiceInstance, serviceInstanceDetail);

					if (associatedWitTJob) {
						tJobServicesInstances.put(instanceId, newServiceInstance);
					} else {
						servicesInstances.put(instanceId, newServiceInstance);
					}
				}
			}

		} catch (IOException e) {
			throw new RuntimeException("Exception requesting an instance of service \"" + serviceId + "\"", e);
		}

		if (newServiceInstance == null) {
			throw new RuntimeException("Service with name \"" + serviceId + "\" not found in ESM");
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
					String networkName = etDockerNetwork;
					logger.info("Network name: " + networkName);
					serviceIp = utilTools.getDockerHostIp();
					serviceInstance.setContainerIp(serviceInstanceDetail.get("context").get(fieldName).toString().replaceAll("\"", ""));
					serviceInstance.setServiceIp(serviceIp);
					logger.info("Service Ip {}:" + serviceInstance.getServiceIp());
					int auxPort;
					
					SupportServiceInstance auxServiceInstance = null; 

					if (manifest.get("endpoints").get(serviceName).get("main") != null
							&& manifest.get("endpoints").get(serviceName).get("main").booleanValue()) {
						logger.info("Principal instance {}:" + serviceName);
						auxServiceInstance = serviceInstance;
					}else{
						auxServiceInstance = new SupportServiceInstance();
						auxServiceInstance.setEndpointName(serviceName);
						auxServiceInstance.setServiceIp(serviceIp);
						serviceInstance.getSubServices().add(auxServiceInstance);
					}
						
						auxServiceInstance.setEndpointName(serviceName);
						if (manifest.get("endpoints").get(serviceName).get("api") != null ){
							if (!manifest.get("endpoints").get(serviceName).get("api").isArray()) {
								getEndpointsInfo(auxServiceInstance, manifest.get("endpoints").get(serviceName).get("api"),
								ssrvContainerName, networkName, "api");											
							}else{
								int apiNum = 0;
								for(final JsonNode apiNode: manifest.get("endpoints").get(serviceName).get("api")){
									apiNum ++;
									getEndpointsInfo(auxServiceInstance, apiNode,
											ssrvContainerName, networkName, "api" + apiNum);
								}							
							}
						}
						if (manifest.get("endpoints").get(serviceName).get("gui") != null){
							if (!manifest.get("endpoints").get(serviceName).get("gui").isArray()) {
								getEndpointsInfo(auxServiceInstance, manifest.get("endpoints").get(serviceName).get("gui"),
								ssrvContainerName, networkName, "gui");											
							}else{
								int guiNum = 0;
								for(final JsonNode guiNode: manifest.get("endpoints").get(serviceName).get("gui")){
									guiNum ++;
									getEndpointsInfo(auxServiceInstance, guiNode,
											ssrvContainerName, networkName, "gui" + guiNum);
								}							
							}						
						}
					break;
				}
			}
		}
	}
	
	private SupportServiceInstance getEndpointsInfo(SupportServiceInstance serviceInstance, JsonNode node,
			String tSSContainerName, String networkName, String nodeName){
		int auxPort = 37000;
		
		if (node != null) {
			auxPort = bindingPort(serviceInstance, node, tSSContainerName,	networkName);
			((ObjectNode) node).put("port", auxPort);
			serviceInstance.setServicePort(auxPort);
			
			if (node.get("protocol") != null && node.get("protocol").toString().contains("http")) {
				serviceInstance.getUrls().put(nodeName, createServiceInstanceUrl(node, serviceInstance.getServiceIp()));								
			}
			
			serviceInstance.getEndpointsData().put(nodeName, node);
					
		}
		
		return serviceInstance;
	}

	private int bindingPort(SupportServiceInstance serviceInstance, JsonNode node, String containerName,
			String networkName) {
		DockerClient dockerClient = dockerService.getDockerClient();
		int listenPort = 37000;
		try {
			listenPort = utilTools.findRandomOpenPort();
			List<String> envVariables = new ArrayList<>();
			envVariables.add("LISTEN_PORT=" + listenPort);
			envVariables.add("FORWARD_PORT=" + node.get("port"));
			envVariables.add("TARGET_SERVICE_IP=" + serviceInstance.getContainerIp());
			Ports portBindings = new Ports();
			ExposedPort exposedListenPort = ExposedPort.tcp(listenPort);

			portBindings.bind(exposedListenPort, Ports.Binding.bindPort(listenPort));

			serviceInstance.getPortBindingContainers().add(dockerService.runDockerContainer(dockerClient,
					ET_SOCAT_IMAGE, envVariables, "container" + listenPort, containerName, networkName, portBindings, listenPort));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return listenPort;
	}

	private String createServiceInstanceUrl(JsonNode node, String ip) {
		String url = null;
		url = node.get("protocol").toString().replaceAll("\"", "") + "://" + ip + ":"
				+ node.get("port").toString().replaceAll("\"", "") + node.get("path").toString().replaceAll("\"", "");
		logger.info("New url: " + url);
		return url;
	}

	/**
	 * 
	 * 
	 * @param instanceId
	 */
	public String deprovisionServiceInstance(String instanceId, Boolean withTJob) {
		DockerClient dockerClient = dockerService.getDockerClient();
		String result = "Instance deleted.";
		Map<String, SupportServiceInstance> servicesInstances = withTJob ? tJobServicesInstances
				: this.servicesInstances;
		SupportServiceInstance serviceInstance = servicesInstances.get(instanceId);

		for (String containerId : serviceInstance.getPortBindingContainers()) {
			dockerService.stopDockerContainer(containerId, dockerClient);
			dockerService.removeDockerContainer(containerId, dockerClient);			
		}
		
		for (SupportServiceInstance subServiceInstance: serviceInstance.getSubServices()){
			for (String containerId : subServiceInstance.getPortBindingContainers()) {
				dockerService.stopDockerContainer(containerId, dockerClient);
				dockerService.removeDockerContainer(containerId, dockerClient);			
			}
		}

		esmServiceClient.deprovisionServiceInstance(instanceId, serviceInstance);
		servicesInstances.remove(instanceId);
		return result;
	}

	public SupportServiceInstance getServiceInstanceFromMem(String id) {
		return servicesInstances.get(id);
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public ObjectNode getServiceInstanceInfo(String instanceId) throws IOException {
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
