package io.elastest.etm.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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

	public static final String ET_SOCAT_IMAGE = "elastest/etm-socat";

	@Value("${et.esm.ss.desc.files.path}")
	public String ESM_SERVICES_FILES_PATH;

	@Value("${elastest.execution.mode}")
	public String ELASTEST_EXECUTION_MODE;

	@Value("${et.services.ip}")
	public String ET_SERVICES_IP;
	
	@Value("${server.port}")
	public String serverPort;

	@Value("${elastest.docker.network}")
	private String etDockerNetwork;

	@Value("${et.edm.alluxio.api}")
	public String ET_EDM_ALLUXIO_API;
	@Value("${et.edm.mysql.host}")
	public String ET_EDM_MYSQL_HOST;
	@Value("${et.edm.mysql.port}")
	public String ET_EDM_MYSQL_PORT;
	@Value("${et.edm.elasticsearch.api}")
	public String ET_EDM_ELASTICSEARCH_API;
	@Value("${et.edm.api}")
	public String ET_EDM_API;
	@Value("${et.epm.api}")
	public String ET_EPM_API;
	@Value("${et.etm.api}")
	public String ET_ETM_API;
	@Value("${et.esm.api}")
	public String ET_ESM_API;
	@Value("${et.eim.api}")
	public String ET_EIM_API;
	@Value("${et.etm.lsbeats.host}")
	public String ET_ETM_LSBEATS_HOST;
	@Value("${et.etm.lsbeats.port}")
	public String ET_ETM_LSBEATS_PORT;
	@Value("${et.etm.lshttp.api}")
	public String ET_ETM_LSHTTP_API;
	@Value("${et.etm.rabbit.host}")
	public String ET_ETM_RABBIT_HOST;
	@Value("${et.etm.rabbit.port}")
	public String ET_ETM_RABBIT_PORT;
	@Value("${et.emp.api}")
	public String ET_EMP_API;
	@Value("${et.emp.influxdb.api}")
	public String ET_EMP_INFLUXDB_API;
	@Value("${et.emp.influxdb.host}")
	public String ET_EMP_INFLUXDB_HOST;
	@Value("${et.emp.influxdb.graphite.port}")
	public String ET_EMP_INFLUXDB_GRAPHITE_PORT;
	@Value("${et.etm.lstcp.host}")
	public String ET_ETM_LSTCP_HOST;
	@Value("${et.etm.lstcp.port}")
	public String ET_ETM_LSTCP_PORT;

	public EsmServiceClient esmServiceClient;
	public DockerService2 dockerService;
	public UtilTools utilTools;
	private Map<String, SupportServiceInstance> servicesInstances;
	private Map<String, SupportServiceInstance> tJobServicesInstances;

	public EsmService(EsmServiceClient esmServiceClient, UtilTools utilTools, DockerService2 dockerService) {
		logger.info("EsmService constructor.");
		this.esmServiceClient = esmServiceClient;
		this.utilTools = utilTools;
		this.servicesInstances = new ConcurrentHashMap<>();
		this.tJobServicesInstances = new HashMap<>();
		this.dockerService = dockerService;
	}

	@PostConstruct
	public void init() {
		logger.info("EsmService initialization.");
		try{
			registerElastestServices();
		}catch (Exception e){
			logger.warn("Error during the services registry. ");
		}
	}

	/**
	 * Register the ElasTest Services into the ESM.
	 */
	public void registerElastestServices() {
		logger.info("Get and send the register information: " + ESM_SERVICES_FILES_PATH);
		try {
			File file = ResourceUtils.getFile(ESM_SERVICES_FILES_PATH);
			// If not in dev mode
			if (file.exists()) {
				List<String> files = new ArrayList<>(Arrays.asList(file.list()));
				for (String nameOfFile : files) {
					logger.info("File name:" + nameOfFile);
					File serviceFile = ResourceUtils.getFile(ESM_SERVICES_FILES_PATH + "/" + nameOfFile);
					registerElastestService(serviceFile);
				}
			} else { // Dev mode
				Resource resource = new ClassPathResource(ESM_SERVICES_FILES_PATH);
				BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()), 1024);
				String line;
				while ((line = br.readLine()) != null) {
					logger.info("File name (dev mode):" + line);
					File serviceFile = new ClassPathResource(ESM_SERVICES_FILES_PATH + line).getFile();
					registerElastestService(serviceFile);
				}
				br.close();
			}

		} catch (IOException fnfe) {
			logger.warn("Service could not be registered. The file with the path " + ESM_SERVICES_FILES_PATH
					+ " does not exist:", fnfe);
		}
	}

	public void registerElastestService(File serviceFile) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String content = new String(Files.readAllBytes(serviceFile.toPath()));

			ObjectNode serviceDefJson = mapper.readValue(content, ObjectNode.class);
			esmServiceClient.registerService(serviceDefJson.get("register").toString());
			esmServiceClient.registerManifest(
					"{ " + "\"id\": " + serviceDefJson.get("manifest").get("id").toString() + ", \"manifest_content\": "
							+ serviceDefJson.get("manifest").get("manifest_content").toString()
							+ ", \"manifest_type\": " + serviceDefJson.get("manifest").get("manifest_type").toString()
							+ ", \"plan_id\": " + serviceDefJson.get("manifest").get("plan_id").toString()
							+ ", \"service_id\": " + serviceDefJson.get("manifest").get("service_id").toString()
							+ ", \"endpoints\": " + serviceDefJson.get("manifest").get("endpoints").toString() + " }",
					serviceDefJson.get("manifest").get("id").toString().replaceAll("\"", ""));
		} catch (IOException e) {
			e.printStackTrace();
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
					esmService.get("name").toString().replaceAll("\"", ""),
					// esmService.get("short_name").toString().replaceAll("\"",
					// "")
					""));
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
	 * @param tJobExecId
	 * @return the new service instance.
	 */
	public SupportServiceInstance provisionServiceInstance(String serviceId, Long tJobExecId, Long tJobId) {
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
							service.get("name").toString().replaceAll("\"", ""),
							// service.get("short_name").toString().replaceAll("\"",
							// ""),
							"", plans.get(0).get("id").toString().replaceAll("\"", ""), tJobExecId);

					fillEnvVariablesToTSS(newServiceInstance, tJobExecId, tJobId);
					esmServiceClient.provisionServiceInstance(newServiceInstance, instanceId, Boolean.toString(false));
					ObjectNode serviceInstanceDetail = getServiceInstanceInfo(instanceId);
					newServiceInstance.setManifestId(
							serviceInstanceDetail.get("context").get("manifest_id").toString().replaceAll("\"", ""));
					buildSrvInstancesUrls(newServiceInstance, serviceInstanceDetail);

					if (tJobExecId != null) {
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
		Iterator<String> itEsmRespContextFields = serviceInstanceDetail.get("context").fieldNames();

		while (subServicesNames.hasNext()) {
			String serviceName = subServicesNames.next();
			logger.info("Manifest services {}:" + serviceName);
			String baseRegex = "[0-9a-f]{32}_" + serviceName + "_\\d_Ip";
			Pattern pattern = Pattern.compile(baseRegex);
			String serviceIp = null;
			Map<String, String> socatBindingsPorts = new HashMap<>();

			while (itEsmRespContextFields.hasNext()) {
				String fieldName = itEsmRespContextFields.next();
				logger.info("Instance data fields {}:" + fieldName);
				Matcher matcher = pattern.matcher(fieldName);
				if (matcher.matches()) {

					String ssrvContainerName = fieldName.substring(0, fieldName.indexOf("_Ip"));
					String networkName = etDockerNetwork;
					logger.info("Network name: " + networkName);
					serviceIp = ET_SERVICES_IP;
					String containerIp = serviceInstanceDetail.get("context").get(fieldName).toString().replaceAll("\"",
							"");
					serviceInstance.setContainerIp(containerIp);
					serviceInstance.setServiceIp(serviceIp);
					logger.info("Service Ip {}:" + serviceInstance.getServiceIp());
					int auxPort;

					SupportServiceInstance auxServiceInstance = null;

					if (manifest.get("endpoints").get(serviceName).get("main") != null
							&& manifest.get("endpoints").get(serviceName).get("main").booleanValue()) {
						logger.info("Principal instance {}:" + serviceName);
						auxServiceInstance = serviceInstance;
					} else {
						auxServiceInstance = new SupportServiceInstance();
						auxServiceInstance.setEndpointName(serviceName);
						auxServiceInstance.setContainerIp(containerIp);
						auxServiceInstance.setServiceIp(serviceIp);
						auxServiceInstance.setParameters(serviceInstance.getParameters());
						serviceInstance.getSubServices().add(auxServiceInstance);
					}

					auxServiceInstance.setEndpointName(serviceName);

					if (manifest.get("endpoints").get(serviceName).get("api") != null) {
						if (!manifest.get("endpoints").get(serviceName).get("api").isArray()) {
							getEndpointsInfo(auxServiceInstance, manifest.get("endpoints").get(serviceName).get("api"),
									ssrvContainerName, networkName, "api", socatBindingsPorts);
						} else {
							int apiNum = 0;
							for (final JsonNode apiNode : manifest.get("endpoints").get(serviceName).get("api")) {
								apiNum++;
								getEndpointsInfo(auxServiceInstance, apiNode, ssrvContainerName, networkName,
										apiNode.get("name") != null	? apiNode.get("name").toString().replaceAll("\"", "") + "api" : "api",
										socatBindingsPorts);
							}
						}
					}
					if (manifest.get("endpoints").get(serviceName).get("gui") != null) {
						if (!manifest.get("endpoints").get(serviceName).get("gui").isArray()) {
							getEndpointsInfo(auxServiceInstance, manifest.get("endpoints").get(serviceName).get("gui"),
									ssrvContainerName, networkName, "gui", socatBindingsPorts);
						} else {
							int guiNum = 0;
							for (final JsonNode guiNode : manifest.get("endpoints").get(serviceName).get("gui")) {
								guiNum++;
								getEndpointsInfo(auxServiceInstance, guiNode, ssrvContainerName, networkName,
										guiNode.get("name") != null	? guiNode.get("name").toString().replaceAll("\"", "") + "gui" : "gui",
										socatBindingsPorts);
							}
						}
					}
					break;
				}
			}
		}
	}

	private SupportServiceInstance getEndpointsInfo(SupportServiceInstance serviceInstance, JsonNode node,
			String tSSContainerName, String networkName, String nodeName, Map<String, String> socatBindingsPorts) {
		int auxPort = 37000;

		if (node != null) {
			if ( node.get("port") != null){
				String nodePort = node.get("port").toString().replaceAll("\"", "");
				if (socatBindingsPorts.containsKey(nodePort)) {
					auxPort = Integer.parseInt(socatBindingsPorts.get(nodePort));
				} else {
					auxPort = bindingPort(serviceInstance, node, tSSContainerName, networkName);
					socatBindingsPorts.put(nodePort, String.valueOf(auxPort));
				}
			}

			if (node.get("protocol") != null && (node.get("protocol").toString().contains("http"))
					|| node.get("protocol").toString().contains("ws")) {
				((ObjectNode) node).put("port", auxPort);
				serviceInstance.setServicePort(auxPort);
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

			serviceInstance.getPortBindingContainers()
					.add(dockerService.runDockerContainer(dockerClient, ET_SOCAT_IMAGE, envVariables,
							"container" + listenPort, containerName, networkName, portBindings, listenPort));
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
		serviceInstance.setServiceReady(false);

		for (String containerId : serviceInstance.getPortBindingContainers()) {
			dockerService.stopDockerContainer(containerId, dockerClient);
			dockerService.removeDockerContainer(containerId, dockerClient);
		}

		for (SupportServiceInstance subServiceInstance : serviceInstance.getSubServices()) {
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
		servicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
			tSSInstance.setServiceReady(checkInstanceUrlIsUp(tSSInstance));
		});
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

	public List<SupportServiceInstance> getTSSInstByTJobExecId(Long tJobExecId) {
		List<SupportServiceInstance> tSSInstanceList = new ArrayList<>();
		tJobServicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
			if (tSSInstance.gettJobExecId() == tJobExecId && checkInstanceUrlIsUp(tSSInstance)) {
				tSSInstanceList.add(tSSInstance);
			}
		});

		return tSSInstanceList;
	}
	
	public boolean checkInstanceUrlIsUp(SupportServiceInstance tSSInstance) {
		boolean up = true;
		for (Map.Entry<String, String> urlHash : tSSInstance.getUrls().entrySet()) {
			if(urlHash.getValue().contains("http")){
				URL url;
				try {
					url = new URL(urlHash.getValue());
					logger.info("Service url to check: " +urlHash.getValue());
					HttpURLConnection huc = (HttpURLConnection) url.openConnection();
					int responseCode = huc.getResponseCode();
					up = up && (responseCode >= 200 && responseCode <= 299);
					if (!up){
						logger.info("Service no ready at url: " +urlHash.getValue());
						return up;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
			logger.info("Service ready at url: " +urlHash.getValue());
		}
		
		return up;
	}
	
	private void fillEnvVariablesToTSS(SupportServiceInstance supportServiceInstance, Long tJobExecId, Long tJobId) {
		if (tJobId != null){
			supportServiceInstance.getParameters().put("ET_TJOBID", tJobId.toString());
		}
		
		if (tJobExecId != null){
			supportServiceInstance.getParameters().put("ET_TJOBEXECID", tJobExecId.toString());
		}
		
		supportServiceInstance.getParameters().put("ET_LSHTTP_API", ET_ETM_LSHTTP_API);
		supportServiceInstance.getParameters().put("ET_LSBEATS_HOST", ET_ETM_LSBEATS_HOST);
		supportServiceInstance.getParameters().put("ET_LSBEATS_PORT", ET_ETM_LSBEATS_PORT);
		supportServiceInstance.getParameters().put("ET_LSTCP_HOST", ET_ETM_LSTCP_HOST);
		supportServiceInstance.getParameters().put("ET_LSTCP_PORT", ET_ETM_LSTCP_PORT);
		supportServiceInstance.getParameters().put("ET_ETM_LSTCP_HOST", ET_ETM_LSTCP_HOST);
		supportServiceInstance.getParameters().put("ET_ETM_LSTCP_PORT", ET_ETM_LSTCP_PORT);
		
				
		supportServiceInstance.getParameters().put("ET_CONTEXT_API", "http://" + utilTools.getMyIp() + ":" + serverPort + "/api/context/tss/" + supportServiceInstance.getInstanceId());
		supportServiceInstance.getParameters().put("ET_SERVICES_IP", ET_SERVICES_IP);
		supportServiceInstance.getParameters().put("ET_EDM_ALLUXIO_API", ET_EDM_ALLUXIO_API);
		supportServiceInstance.getParameters().put("ET_EDM_MYSQL_HOST", ET_EDM_MYSQL_HOST);
		supportServiceInstance.getParameters().put("ET_EDM_MYSQL_PORT", ET_EDM_MYSQL_PORT);
		supportServiceInstance.getParameters().put("ET_EDM_ELASTICSEARCH_API", ET_EDM_ELASTICSEARCH_API);
		supportServiceInstance.getParameters().put("ET_EDM_API", ET_EDM_API);
		supportServiceInstance.getParameters().put("ET_EPM_API", ET_EPM_API);
		supportServiceInstance.getParameters().put("ET_ETM_API", ET_ETM_API);
		supportServiceInstance.getParameters().put("ET_ESM_API", ET_ESM_API);
		supportServiceInstance.getParameters().put("ET_EIM_API", ET_EIM_API);
		supportServiceInstance.getParameters().put("ET_ETM_LSBEATS_HOST", ET_ETM_LSBEATS_HOST);
		supportServiceInstance.getParameters().put("ET_ETM_LSBEATS_PORT", ET_ETM_LSBEATS_PORT);
		supportServiceInstance.getParameters().put("ET_ETM_LSHTTP_API", ET_ETM_LSHTTP_API);
		supportServiceInstance.getParameters().put("ET_ETM_RABBIT_HOST", ET_ETM_RABBIT_HOST);
		supportServiceInstance.getParameters().put("ET_ETM_RABBIT_PORT", ET_ETM_RABBIT_PORT);
		supportServiceInstance.getParameters().put("ET_EMP_API", ET_EMP_API);
		supportServiceInstance.getParameters().put("ET_EMP_INFLUXDB_API", ET_EMP_INFLUXDB_API);
		supportServiceInstance.getParameters().put("ET_EMP_INFLUXDB_HOST", ET_EMP_INFLUXDB_HOST);
		supportServiceInstance.getParameters().put("ET_EMP_INFLUXDB_GRAPHITE_PORT", ET_EMP_INFLUXDB_GRAPHITE_PORT);
	}
	
	public Map<String, String> getTSSInstanceContext(String tSSInstanceId){		
		Map<String, String> tSSInstanceContextMap = new HashMap<>();
		SupportServiceInstance ssi = null;
		if (servicesInstances.get(tSSInstanceId) != null){
			ssi = servicesInstances.get(tSSInstanceId);
		} else if (tJobServicesInstances.get(tSSInstanceId) != null){ 
			ssi = tJobServicesInstances.get(tSSInstanceId);
		} else {
			return null;
		}
		
		tSSInstanceContextMap.putAll(getTSSInstanceEnvVars(ssi, true));
		tSSInstanceContextMap.putAll(ssi.getParameters());
		
		return tSSInstanceContextMap;
	}
	
	public Map<String,String> getTSSInstanceEnvVars(SupportServiceInstance ssi, boolean publicEnvVars){		
		Map<String, String> envVars = new HashMap<String, String>();
		String servicePrefix = ssi.getServiceName().toUpperCase().replaceAll("-", "_");
		String envVarNamePrefix =  publicEnvVars ? "ET_PUBLIC_" + servicePrefix : "ET_" + servicePrefix;
		
		for (Map.Entry<String, JsonNode> entry : ssi.getEndpointsData().entrySet()) {
			String prefix = envVarNamePrefix;			
			envVars.putAll(setTssEnvVarByEndpoint(ssi, prefix, entry, publicEnvVars));
		}

		for (SupportServiceInstance subssi : ssi.getSubServices()) {
			String envNamePrefixSubSSI = envVarNamePrefix + "_"
					+ subssi.getEndpointName().toUpperCase().replaceAll("-", "_");

			for (Map.Entry<String, JsonNode> entry : subssi.getEndpointsData().entrySet()) {
				envVars.putAll(setTssEnvVarByEndpoint(subssi, envNamePrefixSubSSI, entry, publicEnvVars));
			}
		}
		
		return envVars;
	}
	
	private  Map<String,String> setTssEnvVarByEndpoint(SupportServiceInstance ssi, String prefix,
			Map.Entry<String, JsonNode> entry, boolean publicEnvVars) {
		 Map<String,String> envVars = new HashMap<>();
		if (!entry.getKey().toLowerCase().contains("gui")) {
			try {
				if (entry.getValue().get("name") != null) {
					prefix += "_" + entry.getValue().get("name").toString().toUpperCase().replaceAll("\"", "")
							.replaceAll("-", "_");
				}
				
				String envNameHost = prefix + "_HOST";
				String envValueHost = publicEnvVars ? ssi.getServiceIp() : ssi.getContainerIp();

				String envNamePort = prefix + "_PORT";
				String envValuePort = entry.getValue().get("port").toString();

				String protocol = entry.getValue().findValue("protocol").toString().toLowerCase().replaceAll("\"", "");
				if (protocol.equals("http") || protocol.equals("ws")) {
					String envNameAPI = prefix + "_" + "API";
					String path = entry.getValue().get("path").toString().replaceAll("\"", "");
					if (!path.startsWith("/")) {
						path = "/" + path;
					}
					
					String envValueAPI = protocol + "://" + envValueHost + ":" + envValuePort + path;
					envVars.put(envNameAPI, envValueAPI);
					logger.info("Envvar: " + envNameAPI + "=" + envValueAPI);
				} else {
					envVars.put(envNameHost, envValueHost);
					envVars.put(envNamePort, envValuePort);
					logger.info("Envvar: " + envNameHost + "=" + envValueHost);
					logger.info("Envvar: " + envNamePort + "=" + envValuePort);
				}

			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}			
		}
		return envVars;
	}
}
