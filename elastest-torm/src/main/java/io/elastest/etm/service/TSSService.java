package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.epm.client.model.DockerServiceStatus.DockerServiceStatusEnum;
import io.elastest.epm.client.service.ServiceException;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.external.ExternalTJobExecutionRepository;
import io.elastest.etm.model.EusExecutionData;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TssManifest;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.platform.service.PlatformService;
import io.elastest.etm.service.client.EsmServiceClient;
import io.elastest.etm.utils.EtmFilesService;
import io.elastest.etm.utils.ParserService;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;
import io.elastest.eus.service.DynamicDataService;
import io.elastest.eus.service.WebDriverService;

@Service
public class TSSService {
    final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.esm.ss.desc.files.path}")
    public String etEsmSsDescFilesPath;
    @Value("${et.shared.folder}")
    public String etSharedFolder;
    @Value("${et.data.in.host}")
    public String etDataInHost;
    @Value("${et.internet.disabled}")
    public String etInternetDisabled;

    @Value("${elastest.docker.network}")
    private String etDockerNetwork;
    @Value("${et.edm.alluxio.api}")
    public String etEdmAlluxioApi;
    @Value("${et.edm.mysql.host}")
    public String etEdmMysqlHost;
    @Value("${et.edm.mysql.port}")
    public String etEdmMysqlPort;
    @Value("${et.edm.elasticsearch.api}")
    public String etEdmElasticsearchApi;
    @Value("${et.edm.api}")
    public String etEdmApi;
    @Value("${et.epm.api}")
    public String etEpmApi;
    @Value("${et.etm.api}")
    public String etEtmApi;
    @Value("${et.esm.api}")
    public String etEsmApi;
    @Value("${et.eim.api}")
    public String etEimApi;

    @Value("${et.etm.lsbeats.host}")
    public String etEtmLsbeatsHost;
    @Value("${et.etm.lsbeats.port}")
    public String etEtmLsbeatsPort;
    @Value("${et.etm.lshttp.api}")
    public String etEtmLshttpApi;
    @Value("${et.etm.lstcp.host}")
    public String etEtmLstcpHost;
    @Value("${et.etm.lstcp.port}")
    public String etEtmLstcpPort;

    @Value("${et.etm.binded.lsbeats.host}")
    public String etEtmBindedLsBeatsHost;
    @Value("${et.etm.binded.lsbeats.port}")
    public String etEtmBindedLsBeatsPort;
    @Value("${et.etm.binded.lstcp.host}")
    public String etEtmBindedLsTcpHost;
    @Value("${et.etm.binded.lstcp.port}")
    public String etEtmBindedLsTcpPort;

    @Value("${et.etm.rabbit.host}")
    public String etEtmRabbitHost;
    @Value("${et.etm.rabbit.port}")
    public String etEtmRabbitPort;
    @Value("${et.emp.api}")
    public String etEmpApi;
    @Value("${et.emp.influxdb.api}")
    public String etEmpInfluxdbApi;
    @Value("${et.emp.influxdb.host}")
    public String etEmpInfluxdbHost;
    @Value("${et.emp.influxdb.graphite.port}")
    public String etEmpInfluxdbGraphitePort;

    @Value("${exec.mode}")
    public String execMode;

    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${registry.contextPath}")
    private String registryContextPath;

    @Value("${server.port}")
    public String etmServerPort;

    @Value("${et.proxy.port}")
    public String etProxyPort;

    @Value("${et.etm.incontainer}")
    private boolean etmInContainer;

    @Value("${api.context.path:#{null}}")
    private String eusApiPath;

    @Value("${eus.tss.id}")
    public String EUS_TSS_ID;

    public EsmServiceClient esmServiceClient;
    public EtmContextAuxService etmContextAuxService;
    public EtmFilesService filesServices;
    public PlatformService platformService;

    private Map<String, SupportServiceInstance> servicesInstances;
    private Map<String, SupportServiceInstance> tJobServicesInstances;
    private Map<String, SupportServiceInstance> externalTJobServicesInstances;
    private Map<Long, List<String>> tSSIByTJobExecAssociated;
    private Map<Long, List<String>> tSSIByExternalTJobExecAssociated;

    // List of TSS id
    private List<String> tSSIdLoadedOnInit;
    // List of TSS Names
    private List<String> tSSNameLoadedOnInit;
    // Map of TSS name-id
    private Map<String, String> tssLoadedOnInitMap;
    private final TJobExecRepository tJobExecRepositoryImpl;
    private final ExternalTJobExecutionRepository externalTJobExecutionRepository;
    private final DynamicDataService dynamicDataService;
    private final WebDriverService eusWebDriverService;
    private final EtPluginsService etPluginsService;
    private final UtilsService utilsService;
    private boolean withServerAddress;

    @Autowired
    public TSSService(EsmServiceClient supportServiceClient, EtmFilesService filesServices,
            EtmContextAuxService etmContextAuxService, TJobExecRepository tJobExecRepositoryImpl,
            ExternalTJobExecutionRepository externalTJobExecutionRepository,
            DynamicDataService dynamicDataService, WebDriverService eusWebDriverService,
            EtPluginsService etPluginsService, UtilsService utilsService,
            PlatformService platformService) {
        this.esmServiceClient = supportServiceClient;
        this.servicesInstances = new ConcurrentHashMap<>();
        this.tJobServicesInstances = new HashMap<>();
        this.externalTJobServicesInstances = new HashMap<>();
        this.tSSIByTJobExecAssociated = new HashMap<>();
        this.tSSIByExternalTJobExecAssociated = new HashMap<>();
        this.tSSIdLoadedOnInit = new ArrayList<>();
        this.tSSNameLoadedOnInit = new ArrayList<>(Arrays.asList("EUS"));
        this.tssLoadedOnInitMap = new HashMap<>();
        this.filesServices = filesServices;
        this.etmContextAuxService = etmContextAuxService;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.externalTJobExecutionRepository = externalTJobExecutionRepository;
        this.dynamicDataService = dynamicDataService;
        this.eusWebDriverService = eusWebDriverService;
        this.etPluginsService = etPluginsService;
        this.utilsService = utilsService;
        this.platformService = platformService;
    }

    @PostConstruct
    public void init() {
        logger.info("EsmService initialization.");
        try {
            registerElastestServices();
            tSSIdLoadedOnInit.forEach((serviceId) -> {
                String serviceName = getServiceNameByServiceId(serviceId).toUpperCase();

                String tssInstanceId = null;

                withServerAddress = !utilsService.isDefaultEtPublicHost();

                // If mini and EUS, use integrated EUS
                if (utilsService.isElastestMini() && serviceName.equals("EUS")) {
                    tssInstanceId = startIntegratedEus(serviceId);

                } else {
                    tssInstanceId = provisionServiceInstanceSync(serviceId);
                    SupportServiceInstance tssInstance = servicesInstances.get(tssInstanceId);
                    waitForServiceIsReady(tssInstance);
                    if (serviceName.equals("EUS")) {
                        etmContextAuxService.getContextInfo().setEusSSInstance(tssInstance);
                    }

                }

                tssLoadedOnInitMap.put(serviceName, tssInstanceId);

                logger.debug("{} is started from ElasTest in {} mode", serviceName,
                        utilsService.getExecMode());
            });
        } catch (Exception e) {
            logger.warn("Error during the services registry. ", e);
        }
    }

    private String startIntegratedEus(String serviceId) {
        String tssInstanceId;
        tssInstanceId = UtilTools.generateUniqueId();
        try {
            SupportServiceInstance eusInstance = this.createNewServiceInstance(serviceId, null,
                    tssInstanceId);
            TssManifest manifest = esmServiceClient
                    .getManifestBySupportServiceInstance(eusInstance);
            eusInstance.setManifestId(manifest.getId());
            String serviceIp = platformService.getEtmHost();
            eusInstance.setContainerIp(serviceIp);
            int internalServicePort = Integer.parseInt(etmServerPort);
            int bindedServicePort = Integer.parseInt(etProxyPort);
            eusInstance.setInternalServiceIp(serviceIp);

            // If server address, binded

            if (withServerAddress) {
                serviceIp = utilsService.getEtPublicHostValue();
                eusInstance.setBindedServiceIp(serviceIp);
            }

            eusInstance.setServiceIp(serviceIp);
            eusInstance.setEndpointName("elastest-eus");
            buildTssInstanceUrls(eusInstance);

            // Replace EUS port to ETM port
            String originalInternalPort = String.valueOf(eusInstance.getInternalServicePort());
            String originalExternalPort = String.valueOf(eusInstance.getBindedServicePort());

            // Set ports after buildTssInstanceUrls to update
            eusInstance.setInternalServicePort(internalServicePort);
            eusInstance.setBindedServicePort(bindedServicePort);

            for (String key : eusInstance.getUrls().keySet()) {

                String newInternalUrlValue = eusInstance.getUrlIfExistsByKey(key, false)
                        .replaceAll(originalInternalPort, String.valueOf(internalServicePort));
                eusInstance.setUrlValue(key, newInternalUrlValue, false);

                logger.info("Replace internal EUS port {} by {}", originalInternalPort,
                        internalServicePort);
                logger.info("EUS internal URL: {} => {}", key, newInternalUrlValue);

                if (withServerAddress) {
                    String newExternalUrlValue = eusInstance.getUrlIfExistsByKey(key, true)
                            .replaceAll(originalExternalPort, String.valueOf(bindedServicePort));

                    eusInstance.setUrlValue(key, newExternalUrlValue, true);

                    logger.info("Replace external EUS port {} by {}", originalExternalPort,
                            bindedServicePort);
                    logger.info("EUS external URL: {} => {}", key, newExternalUrlValue);
                }

            }

            for (String key : eusInstance.getEndpointsData().keySet()) {
                JsonNode node = eusInstance.getEndpointsData().get(key);

                for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
                    String field = it.next();

                    if (node.get(field).asText().equals(originalInternalPort)) {
                        ((ObjectNode) node).put(field, internalServicePort);
                    }

                    if (node.get(field).asText().equals(originalExternalPort)) {
                        ((ObjectNode) node).put(field, bindedServicePort);
                    }

                }
            }

            eusInstance.setServicePort(withServerAddress ? bindedServicePort : internalServicePort);

            String internalApiUrl = eusInstance.getApiUrlIfExist(false);
            eusInstance.setApiStatusUrl(internalApiUrl + "status", false);
            if (withServerAddress) {
                String externalApiUrl = eusInstance.getApiUrlIfExist(true);
                eusInstance.setApiStatusUrl(externalApiUrl + "status", true);
            }

            dynamicDataService.setLogstashHttpsApi(
                    etmContextAuxService.getContextInfo().getLogstashSSLHttpUrl());

            putInstanceInMap(tssInstanceId, eusInstance);

            eusInstance.setFullyInitialized(true);
            etmContextAuxService.getContextInfo().setEusSSInstance(eusInstance);
        } catch (Exception e) {
            logger.error("Error on start integrated EUS:", e);
        }
        return tssInstanceId;
    }

    @PreDestroy
    public void cleanTSSInstances() {
        logger.debug("Deprovisioning TSSs before stopping the ETM");
        deprovisionServicesInstances();
    }

    /**
     * Register the ElasTest Services into the ESM.
     */
    public void registerElastestServices() {
        logger.info("Get and send the register information: {}", etEsmSsDescFilesPath);

        try {
            List<File> files = filesServices.getFilesFromFolder(etEsmSsDescFilesPath);
            for (File file : files) {
                registerElastestService(file);
            }
        } catch (IOException ioe) {
            logger.warn("Service could not be registered. The file with the path "
                    + etEsmSsDescFilesPath + " does not exist.");
        }
    }

    public void registerElastestService(File serviceFile) throws IOException {
        try {
            String content = filesServices.readFile(serviceFile);

            if (content != null) {
                ObjectNode serviceDefJson = ParserService.getJsonNodeFromString(content);
                logger.debug("TSS file {}", serviceFile.getName());
                String serviceName = serviceFile.getName().split("-")[0].toUpperCase();

                // If is loaded on init (EUS)
                if (tSSNameLoadedOnInit.contains(serviceName)) {
                    logger.debug("TSS {} will be registered in {} mode.", serviceName, execMode);
                    registerElasTestService(serviceDefJson);

                    String tssId = serviceDefJson.get("register").get("id").toString()
                            .replaceAll("\"", "");
                    if (tSSNameLoadedOnInit.contains(serviceName)) {
                        tSSIdLoadedOnInit.add(tssId);
                    }
                } else {
                    // Disable here TSS if you want. Now none
                    registerElasTestService(serviceDefJson);
                }
            }

        } catch (IOException e) {
            logger.error("Error registering a TSS.");
            throw e;
        }
    }

    private void registerElasTestService(ObjectNode serviceDefJson) {
        logger.debug("Registering the {} TSS.", ParserService.getNodeByElemChain(serviceDefJson,
                Arrays.asList("register", "name")));

        // Register Tss service
        esmServiceClient.registerService(serviceDefJson.get("register").toString());

        // Register associated manifests
        JsonNode manifests = serviceDefJson.get("manifests");

        JsonNode manifestToRegister = null;

        if (manifests != null && manifests.isArray()) {
            for (final JsonNode manifestNode : manifests) {
                logger.debug("Manifest: {}", manifestNode);
                if ((utilsService.isKubernetes()
                        && manifestNode.get("manifest_type").toString().contains("kubernetes"))
                        || (!utilsService.isKubernetes() && manifestNode.get("manifest_type")
                                .toString().contains("docker-compose"))) {
                    manifestToRegister = manifestNode;
                    break;
                }
            }
        } else {
            manifestToRegister = serviceDefJson.get("manifest");
        }

        logger.debug("Register manifest Id {} associated to service {}",
                manifestToRegister.get("id"), serviceDefJson.get("register").get("id"));

        esmServiceClient.registerManifest("{ " + "\"id\": "
                + manifestToRegister.get("id").toString() + ", \"manifest_content\": "
                + manifestToRegister.get("manifest_content").toString() + ", \"manifest_type\": "
                + manifestToRegister.get("manifest_type").toString() + ", \"plan_id\": "
                + manifestToRegister.get("plan_id").toString() + ", \"service_id\": "
                + manifestToRegister.get("service_id").toString() + ", \"endpoints\": "
                + manifestToRegister.get("endpoints").toString()
                // + ", \"config\": " + config
                + " }", manifestToRegister.get("id").toString().replaceAll("\"", ""));
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
        SupportService[] servicesObj = esmServiceClient.getRegisteredServices();

        for (SupportService esmService : servicesObj) {
            services.add(esmService);
        }
        return services;
    }

    public String generateNewOrGetInstanceId(String serviceId) {
        String serviceName = getServiceNameByServiceId(serviceId).toUpperCase();

        if (serviceName != null && tssLoadedOnInitMap.containsKey(serviceName)) {
            return tssLoadedOnInitMap.get(serviceName);
        } else {
            return UtilTools.generateUniqueId();
        }
    }

    @Async
    public void provisionServiceInstanceAsync(String serviceId, String instanceId) {
        provisionServiceInstance(serviceId, instanceId);
    }

    public String provisionServiceInstanceSync(String serviceId) {
        String instanceId = UtilTools.generateUniqueId();
        provisionServiceInstance(serviceId, instanceId);
        return instanceId;
    }

    /* *** Provision With generic Execution *** */

    @Async
    public void provisionExecutionServiceInstanceAsync(String serviceId, Execution execution,
            String instanceId) throws RuntimeException {
        if (execution.isExternal()) {
            provisionExternalTJobExecServiceInstanceSync(serviceId, execution.getExternalTJobExec(),
                    instanceId);
        } else {
            provisionTJobExecServiceInstanceSync(serviceId, execution.getTJobExec(), instanceId);
        }

    }

    public String provisionExecutionServiceInstanceSync(String serviceId, Execution execution) {
        if (execution.isExternal()) {
            return provisionExternalTJobExecServiceInstanceSync(serviceId,
                    execution.getExternalTJobExec());
        } else {
            return provisionTJobExecServiceInstanceSync(serviceId, execution.getTJobExec());
        }
    }

    /* *** Provision With TJobExec *** */

    public String provisionTJobExecServiceInstanceSync(String serviceId, TJobExecution tJobExec) {
        String tssInstanceId = this.generateNewOrGetInstanceId(serviceId);
        this.provisionTJobExecServiceInstanceSync(serviceId, tJobExec, tssInstanceId);
        return tssInstanceId;
    }

    public void provisionTJobExecServiceInstanceSync(String serviceId, TJobExecution tJobExec,
            String tssInstanceId) {
        // If is shared tss
        if (isSharedTssInstanceByServiceId(serviceId)) {
            provisionExecutionSharedTSSSync(serviceId, new Execution(tJobExec), tssInstanceId);
        } else {
            // Else start new instance
            provisionTJobExecServiceInstance(serviceId, tJobExec, tssInstanceId);
        }

    }

    public void registerExecutionInEus(String tssInstanceId, String serviceName,
            Execution execution) {
        if (servicesInstances.containsKey(tssInstanceId)) {
            String folderPath = filesServices.getExecutionFolderPath(execution, true)
                    + serviceName.toLowerCase() + "/";

            EusExecutionData eusExecutionData;

            String sutContainerPrefix = platformService
                    .generateContainerName(PlatformService.ContainerPrefix.SUT, execution);

            boolean useSutNetwork = true;
            // ExTJobExec
            if (execution.isExternal()) {
                eusExecutionData = new EusExecutionData(execution.getExternalTJobExec(), folderPath,
                        useSutNetwork, sutContainerPrefix);
            } else { // TJobExec
                eusExecutionData = new EusExecutionData(execution.getTJobExec(), folderPath,
                        useSutNetwork, sutContainerPrefix);
            }

            String response = "";
            if (utilsService.isElastestMini()) {
                response = eusWebDriverService
                        .registerExecution(eusExecutionData.getAsExecutionData()).getBody();
            } else {
                String eusApi = servicesInstances.get(tssInstanceId).getApiUrlIfExist(false);
                String url = eusApi.endsWith("/") ? eusApi : eusApi + "/";
                url += "execution/register";

                // Register execution in EUS
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

                HttpEntity<EusExecutionData> request = new HttpEntity<EusExecutionData>(
                        eusExecutionData, headers);

                logger.debug("Registering {} {} in EUS (url: {})", execution.getTJobExecType(),
                        eusExecutionData, url);

                response = restTemplate.postForObject(url, request, String.class);
            }
            logger.debug("{} {} registered in EUS", execution.getTJobExecType(), response);
        }
    }

    public void unregisterTJobExecutionInEus(String tssInstanceId, String serviceName,
            TJobExecution tJobExec) {
        if (servicesInstances.containsKey(tssInstanceId)) {

            boolean useSutNetwork = tJobExec.getTjob().isExternal();
            String sutContainerPrefix = platformService.generateContainerName(
                    PlatformService.ContainerPrefix.SUT, new Execution(tJobExec));

            EusExecutionData eusExecutionData = new EusExecutionData(tJobExec, "", useSutNetwork,
                    sutContainerPrefix);

            if (utilsService.isElastestMini()) {
                eusWebDriverService.unregisterExecution(eusExecutionData.getKey());
            } else {
                String eusApi = servicesInstances.get(tssInstanceId)
                        .getApiUrlIfExist(withServerAddress);

                String url = eusApi.endsWith("/") ? eusApi : eusApi + "/";
                url += "execution/unregister/" + eusExecutionData.getKey();

                // Register execution in EUS
                RestTemplate restTemplate = new RestTemplate();

                restTemplate.delete(url);
            }
        }
    }

    /* *** Provision With External TJobExec *** */

    @Async
    private void provisionExternalTJobExecServiceInstanceAsync(String serviceId,
            ExternalTJobExecution exTJobExec) {
        provisionExternalTJobExecServiceInstanceSync(serviceId, exTJobExec);
    }

    private String provisionExternalTJobExecServiceInstanceSync(String serviceId,
            ExternalTJobExecution exTJobExec) {
        String instanceId = UtilTools.generateUniqueId();
        return this.provisionExternalTJobExecServiceInstanceSync(serviceId, exTJobExec, instanceId);
    }

    private String provisionExternalTJobExecServiceInstanceSync(String serviceId,
            ExternalTJobExecution exTJobExec, String instanceId) {
        // If is shared tss
        if (isSharedTssInstanceByServiceId(serviceId)) {
            String tssInstanceId = provisionExecutionSharedTSSSync(serviceId,
                    new Execution(exTJobExec));
            if (tssInstanceId != null) {
                return tssInstanceId;
            }
        }

        // Else start new Eus instance
        provisionExternalTJobExecServiceInstance(serviceId, exTJobExec, instanceId);
        return instanceId;
    }

    public String provisionExecutionSharedTSSSync(String serviceId, Execution execution,
            String tssInstanceId) {
        String serviceName = getServiceNameByServiceId(serviceId).toUpperCase();

        if (isSharedTssInstance(serviceName)) {
            if (serviceName.equals("EUS")) {
                this.registerExecutionInEus(tssInstanceId, serviceName, execution);
            }

            SupportServiceInstance instance = servicesInstances.get(tssInstanceId);
            instance.gettJobExecIdList().add(execution.getExecutionId());

            putInstanceInExecutionMap(tssInstanceId, instance, execution);

            return tssInstanceId;
        }
        return null;
    }

    public String provisionExecutionSharedTSSSync(String serviceId, Execution execution) {
        String serviceName = getServiceNameByServiceId(serviceId).toUpperCase();
        String tssInstanceId = tssLoadedOnInitMap.get(serviceName);
        return provisionExecutionSharedTSSSync(serviceId, execution, tssInstanceId);
    }

    public SupportServiceInstance createNewServiceInstance(String serviceId, Long executionId,
            String instanceId) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode service = esmServiceClient.getRawServiceById(serviceId);
        logger.info("Service instance: " + instanceId);
        List<ObjectNode> plans = Arrays
                .asList(mapper.readValue(service.get("plans").toString(), ObjectNode[].class));

        return new SupportServiceInstance(instanceId,
                service.get("id").toString().replaceAll("\"", ""),
                service.get("name").toString().replaceAll("\"", ""),
                // service.get("short_name").toString().replaceAll("\"",
                // ""),
                "", plans.get(0).get("id").toString().replaceAll("\"", ""),
                executionId != null ? Arrays.asList(executionId) : new ArrayList<>());
    }

    public void provisionServiceInstance(String serviceId, String instanceId) {
        logger.info("Service id to provision: " + serviceId);
        SupportServiceInstance newServiceInstance = null;

        try {
            newServiceInstance = this.createNewServiceInstance(serviceId, null, instanceId);

            this.setTSSFilesConfig(newServiceInstance);
            this.fillEnvVariablesToTSS(newServiceInstance);

            putInstanceInMap(instanceId, newServiceInstance);
            newServiceInstance = this.provisionServiceInstanceAndBuildUrls(newServiceInstance,
                    instanceId);
            putInstanceInMap(instanceId, newServiceInstance);
        } catch (Exception e) {
            if (newServiceInstance != null) {
                deprovisionServiceInstance(newServiceInstance.getInstanceId(), servicesInstances);
            }
            throw new RuntimeException(
                    "Exception requesting an instance of service \"" + serviceId + "\"", e);
        }

        if (newServiceInstance == null) {
            throw new RuntimeException("Service with name \"" + serviceId + "\" not found in ESM");
        }
    }

    /* *** Provision With TJobExec *** */
    private void provisionTJobExecServiceInstance(String serviceId, TJobExecution tJobExec,
            String instanceId) throws RuntimeException {
        logger.info("Service id to provision: " + serviceId);
        SupportServiceInstance newServiceInstance = null;

        try {
            Long execId = tJobExec != null && tJobExec.getId() != null ? tJobExec.getId() : null;
            newServiceInstance = this.createNewServiceInstance(serviceId, execId, instanceId);

            this.setExecutionTSSFilesConfig(newServiceInstance, new Execution(tJobExec));
            this.fillTJobExecEnvVariablesToTSS(newServiceInstance, tJobExec);
            boolean tJobIsExternal = false;
            if (tJobExec != null && tJobExec.getTjob() != null) {
                tJobIsExternal = tJobExec.getTjob().isExternal();

            }
            this.fillEMSMonitoringEnvVariablesToTSS(newServiceInstance, tJobExec, tJobIsExternal,
                    false);

            if (tJobExec != null && execId != null) {
                putInstanceInTJobExecMap(instanceId, newServiceInstance);
                List<String> tSSIByTJobExecAssociatedList = tSSIByTJobExecAssociated
                        .get(execId) == null ? new ArrayList<>()
                                : tSSIByTJobExecAssociated.get(execId);
                tSSIByTJobExecAssociatedList.add(newServiceInstance.getInstanceId());
                tSSIByTJobExecAssociated.put(execId, tSSIByTJobExecAssociatedList);

                createExecFilesFolder(newServiceInstance);
            } else {
                putInstanceInMap(instanceId, newServiceInstance);
            }

            Map<String, String> tssAdditionalLabels = new HashMap<>();
            tssAdditionalLabels.put("io.elastest.tjob.exec.id", tJobExec.getId().toString());
            newServiceInstance.setAdditionalLabels(tssAdditionalLabels);
            newServiceInstance = this.provisionServiceInstanceAndBuildUrls(newServiceInstance,
                    instanceId);

            putInstanceInTJobExecMap(instanceId, newServiceInstance);

        } catch (Exception e) {
            if (newServiceInstance != null) {
                deprovisionServiceInstance(newServiceInstance.getInstanceId(),
                        tJobExec != null && tJobExec.getId() != null ? tJobServicesInstances
                                : servicesInstances);
            }
            throw new RuntimeException(
                    "Exception requesting an instance of service \"" + serviceId + "\"", e);
        }

        if (newServiceInstance == null) {
            throw new RuntimeException("Service with name \"" + serviceId + "\" not found in ESM");
        }
    }

    public List<String> getServicesInstancesByExecution(Execution execution) {
        if (execution.isExternal()) {
            return execution.getExternalTJobExec().getServicesInstances();
        } else {
            return execution.getTJobExec().getServicesInstances();
        }
    }

    public void waitForExecutionServicesAreReady(Execution execution) {
        Map<String, SupportServiceInstance> tSSInstAssocToTJob = getExecutionServicesInstancesMap(
                execution);

        String resultMsg = "Waiting for the Test Support Services of " + execution.getTJobExecType()
                + " " + execution.getExecutionId() + " to be ready";
        logger.info("{}: {}", resultMsg, tSSInstAssocToTJob.keySet());
        updateExecutionResultStatus(execution, ResultEnum.WAITING_TSS, resultMsg);
        while (!tSSInstAssocToTJob.isEmpty()) {

            List<String> servicesInstances = getServicesInstancesByExecution(execution);

            servicesInstances.forEach((tSSInstId) -> {
                SupportServiceInstance mainSubService = getExecutionServiceInstanceById(execution,
                        tSSInstId);
                logger.debug("Wait for TSS {} in {} {}", mainSubService.getEndpointName(),
                        execution.getTJobExecType(), execution.getExecutionId());
                waitForServiceIsReady(mainSubService);
                tSSInstAssocToTJob.remove(tSSInstId);
            });
        }
        logger.info("TSSs availables!");
    }

    public void waitForServiceIsReady(SupportServiceInstance serviceInstance) {
        while (!checkInstanceUrlIsUp(serviceInstance)) {
            logger.debug("Wait for service {}", serviceInstance.getEndpointName());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                logger.error("Interrupted Exception {}: " + ie.getMessage());
            }
        }
        serviceInstance.getSubServices().forEach((subService) -> {
            waitForServiceIsReady(subService);
        });
        serviceInstance.setStatus(DockerServiceStatusEnum.READY);
        serviceInstance.setStatusMsg("Ready");
    }

    public void waitForTssStartedInMini(Execution execution, String instanceId,
            String serviceName) {
        String resultMsg = "Waiting for the Test Support Services to be ready: "
                + serviceName.toUpperCase();
        updateExecutionResultStatus(execution, ResultEnum.WAITING_TSS, resultMsg);

        logger.debug("Wait for TSS {} in {} {}", serviceName, execution.getTJobExecType(),
                execution.getExecutionId());

        if (isSharedTssInstance(serviceName)) {
            logger.debug("Service {} is loaded on init. It's not necessary to wait for the service",
                    serviceName);

            if (servicesInstances.containsKey(instanceId)) {
                // Put is carried out in the method provision, but we put it
                // here also in case async is executed
                putInstanceInTJobExecMap(instanceId, servicesInstances.get(instanceId));
            }

            // TSS Loaded on init
            return;
        }

        SupportServiceInstance tssInstance = (SupportServiceInstance) etPluginsService
                .getEtPlugin(instanceId);
        // If is not started
        if (tssInstance == null) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.waitForTssStartedInMini(execution, instanceId, serviceName);

        } else {
            // If not started and is not ready
            DockerServiceStatusEnum tssInstanceStatus = tssInstance.getStatus();
            if (!DockerServiceStatusEnum.STARTING.equals(tssInstanceStatus)
                    && !DockerServiceStatusEnum.READY.equals(tssInstanceStatus)) {
                updateExecutionResultStatus(execution, TJobExecution.ResultEnum.STARTING_TSS,
                        tssInstance.getStatusMsg());

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.waitForTssStartedInMini(execution, instanceId, serviceName);
            } else if (DockerServiceStatusEnum.STARTING.equals(tssInstanceStatus)
                    || (DockerServiceStatusEnum.READY.equals(tssInstanceStatus)
                            && !tssInstance.isFullyInitialized())) {
                updateExecutionResultStatus(execution, TJobExecution.ResultEnum.WAITING_TSS,
                        tssInstance.getStatusMsg());

                etPluginsService.getEtPluginUrl(instanceId, tssInstance.getContainerName());
                boolean isUp = checkInstanceUrlIsUp(tssInstance);

                logger.debug("Is up TSS {} in {} {}: {}", serviceName, execution.getTJobExecType(),
                        execution.getExecutionId(), isUp);

                // for(SupportServiceInstance)

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                this.waitForTssStartedInMini(execution, instanceId, serviceName);
                // If ready
            } else if (DockerServiceStatusEnum.READY.equals(tssInstanceStatus)) {
                logger.debug("End of Wait for TSS {} in {} {}", serviceName,
                        execution.getTJobExecType(), execution.getExecutionId());

                // Update instance in map to have the entrypoint values
                tssInstance = esmServiceClient.initSupportServiceInstanceData(tssInstance);
                putInstanceInTJobExecMap(instanceId, tssInstance);
            }
        }

    }

    private void setExecutionTSSFilesConfig(SupportServiceInstance supportServiceInstance,
            Execution execution) {

        if (execution.hasJobAndExecution()) {
            String fileSeparator = "/";
            supportServiceInstance.getParameters().put("ET_FILES_PATH",
                    filesServices.getExecutionFolderPath(execution)
                            + supportServiceInstance.getServiceName().toLowerCase()
                            + fileSeparator);
            // Not mini
            if (!utilsService.isElastestMini()) {
                supportServiceInstance.getParameters().put("ET_FILES_PATH_IN_HOST",
                        etDataInHost + filesServices.getExecutionFolderPath(execution, true)
                                + supportServiceInstance.getServiceName().toLowerCase()
                                + fileSeparator);
            }
            supportServiceInstance.getParameters().put("ET_SHARED_FOLDER", etSharedFolder);
            supportServiceInstance.getParameters().put("ET_DATA_IN_HOST", etDataInHost);
        }

    }

    private void fillTJobExecEnvVariablesToTSS(SupportServiceInstance supportServiceInstance,
            TJobExecution tJobExec) throws ServiceException {

        if (tJobExec != null && tJobExec.getTjob() != null) {
            Long tJobId = tJobExec.getTjob().getId();
            Long tJobExecId = tJobExec.getId();
            this.fillTSSConfigEnvVarsByTJob(tJobExec.getTjob(), supportServiceInstance);
            supportServiceInstance.getParameters().put("ET_TJOB_ID", tJobId.toString());
            supportServiceInstance.getParameters().put("ET_TJOBEXEC_ID", tJobExecId.toString());
            supportServiceInstance.getParameters().put("ET_MON_EXEC", tJobExecId.toString());
            switch (supportServiceInstance.getServiceName()) {
            case "ESS":
                logger.debug("Set timeout for {} container",
                        supportServiceInstance.getServiceName());
                supportServiceInstance.getParameters().put("ESM_DOCKER_DELETE_TIMEOUT", "3600");
                break;
            case "EUS":
                logger.debug("Set timeout for {} container",
                        supportServiceInstance.getServiceName());
                supportServiceInstance.getParameters().put("ESM_DOCKER_DELETE_TIMEOUT", "120");
                break;
            default:
                logger.debug("Container Timeout not defined");
            }

        }
        this.fillEnvVariablesToTSS(supportServiceInstance);
    }

    public void fillTSSConfigEnvVarsByTJob(TJob tJob,
            SupportServiceInstance supportServiceInstance) {
        try {
            Map<String, String> vars = tJob
                    .getTJobTSSConfigEnvVars(supportServiceInstance.getServiceName());
            if (vars != null) {
                supportServiceInstance.getParameters().putAll(vars);
            }
        } catch (Exception e) {
        }
    }

    /* *** Provision With ExternalTJobExec *** */
    @SuppressWarnings("unchecked")
    public void provisionExternalTJobExecServiceInstance(String serviceId,
            ExternalTJobExecution exTJobExec, String instanceId) {
        logger.info("Service id to provision: " + serviceId);
        SupportServiceInstance newServiceInstance = null;

        try {
            Long execId = exTJobExec != null && exTJobExec.getId() != null ? exTJobExec.getId()
                    : null;
            newServiceInstance = this.createNewServiceInstance(serviceId, execId, instanceId);

            // Add extra_hosts
            if (exTJobExec.getExecutionConfig() != null) {
                Map<String, Object> executionConfig = UtilTools
                        .convertJsonStringToObj(exTJobExec.getExecutionConfig(), Map.class);
                if (executionConfig != null && executionConfig.containsKey("extraHosts")) {
                    List<String> extraHosts = null;
                    try {
                        extraHosts = (List<String>) executionConfig.get("extraHosts");
                    } catch (Exception e) {
                        // Single extraHost
                        extraHosts = Arrays.asList((String) executionConfig.get("extraHosts"));
                    }
                    if (extraHosts != null) {
                        newServiceInstance.setExtraHosts(extraHosts);

                    }
                }
            }

            this.setExecutionTSSFilesConfig(newServiceInstance, new Execution(exTJobExec));
            fillExternalTJobExecEnvVariablesToTSS(newServiceInstance, exTJobExec);

            if (exTJobExec != null && execId != null) {
                putInstanceInExternalTJobExecMap(instanceId, newServiceInstance);

                List<String> tSSIByExternalTJobExecAssociatedList = tSSIByExternalTJobExecAssociated
                        .get(execId) == null ? new ArrayList<>()
                                : tSSIByExternalTJobExecAssociated.get(execId);

                tSSIByExternalTJobExecAssociatedList.add(newServiceInstance.getInstanceId());
                tSSIByExternalTJobExecAssociated.put(execId, tSSIByExternalTJobExecAssociatedList);

                createExecFilesFolder(newServiceInstance);
            } else {
                putInstanceInMap(instanceId, newServiceInstance);
            }

            newServiceInstance = this.provisionServiceInstanceAndBuildUrls(newServiceInstance,
                    instanceId);

            if (exTJobExec != null && execId != null) {
                putInstanceInExternalTJobExecMap(instanceId, newServiceInstance);
            } else {
                putInstanceInMap(instanceId, newServiceInstance);
            }

        } catch (Exception e) {
            if (newServiceInstance != null) {
                deprovisionServiceInstance(newServiceInstance.getInstanceId(),
                        exTJobExec != null && exTJobExec.getId() != null
                                ? externalTJobServicesInstances
                                : servicesInstances);
            }
            throw new RuntimeException(
                    "Exception requesting an instance of service \"" + serviceId + "\"", e);
        }

        if (newServiceInstance == null) {
            throw new RuntimeException("Service with name \"" + serviceId + "\" not found in ESM");
        }
    }

    public void unregisterExternalTJobExecutionInEus(String tssInstanceId, String serviceName,
            ExternalTJobExecution exTJobExec) {
        if (servicesInstances.containsKey(tssInstanceId)) {
            EusExecutionData eusExecutionData = new EusExecutionData(exTJobExec, "");

            if (utilsService.isElastestMini()) {
                eusWebDriverService.unregisterExecution(eusExecutionData.getKey());
            } else {
                String eusApi = servicesInstances.get(tssInstanceId)
                        .getApiUrlIfExist(withServerAddress);

                String url = eusApi.endsWith("/") ? eusApi : eusApi + "/";
                url += "execution/unregister/" + eusExecutionData.getKey();

                // Unregister execution in EUS
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.delete(url);
            }
        }
    }

    private void fillExternalTJobExecEnvVariablesToTSS(
            SupportServiceInstance supportServiceInstance, ExternalTJobExecution exTJobExec)
            throws ServiceException {

        if (exTJobExec != null && exTJobExec.getExTJob() != null) {
            Long exTJobId = exTJobExec.getExTJob().getId();
            Long exTJobExecId = exTJobExec.getId();

            supportServiceInstance.getParameters().put("ET_TJOB_ID", exTJobId.toString());
            supportServiceInstance.getParameters().put("ET_TJOBEXEC_ID", exTJobExecId.toString());

            // Puts only Exec Monitoring index (without sut)
            supportServiceInstance.getParameters().put("ET_MON_EXEC",
                    exTJobExec.getExternalTJobExecMonitoringIndex());

            switch (supportServiceInstance.getServiceName()) {
            case "ESS":
                supportServiceInstance.getParameters().put("ESM_DOCKER_DELETE_TIMEOUT", "120");
                break;
            case "EUS":
                supportServiceInstance.getParameters().put("ESM_DOCKER_DELETE_TIMEOUT", "120");
                break;
            default:
                logger.debug("Container Timeout not defined");
            }
        }
        this.fillEnvVariablesToTSS(supportServiceInstance);
    }

    public SupportServiceInstance provisionServiceInstanceAndBuildUrls(
            SupportServiceInstance newServiceInstance, String instanceId) throws Exception {
        String serviceName = newServiceInstance != null ? newServiceInstance.getServiceName()
                : null;
        logger.info("Provisioning service instance {} with id {} and data: {}", serviceName,
                instanceId, newServiceInstance);

        esmServiceClient.provisionServiceInstance(newServiceInstance, instanceId,
                Boolean.toString(false));

        logger.info("Get registered all data of a service.");
        newServiceInstance = esmServiceClient.getServiceInstanceInfo(newServiceInstance);

        buildTssInstanceUrls(newServiceInstance);
        platformService.manageTSSInstanceIfNecessary(newServiceInstance, true);
        newServiceInstance.setFullyInitialized(true);
        logger.info("Service {} with instance id {} has been fully initialized",
                newServiceInstance.getServiceName(), instanceId);
        logger.debug("Service instance data: {} ", newServiceInstance);
        return newServiceInstance;
    }

    private void buildTssInstanceUrls(SupportServiceInstance serviceInstance) throws Exception {
        logger.info("Building TSSs URLs for {}", serviceInstance.getEndpointName());
        TssManifest manifest = esmServiceClient.getManifestById(serviceInstance.getManifestId());

        // Build the URL for the service checked as main
        buildSubserviceUrls(serviceInstance, manifest);

        // Build URLs for the Subservices
        for (SupportServiceInstance subService : serviceInstance.getSubServices()) {
            logger.debug("Sub-services names: {}", subService.getEndpointName());
            if (subService.getContainerIp() == null) {
                throw new Exception(
                        "Field ip not found for " + subService.getEndpointName() + " instance.");
            } else {
                buildSubserviceUrls(subService, manifest);
            }
        }

    }

    private void buildSubserviceUrls(SupportServiceInstance serviceInstance, TssManifest manifest)
            throws Exception {
        JsonNode manifestEndpoints = manifest.getEndpoints();
        logger.debug("Endpoints for the service: {}", manifestEndpoints.toString());
        logger.debug("Endpoints name: {}", serviceInstance.getEndpointName());
        JsonNode manifestEndpointService = manifestEndpoints.get(serviceInstance.getEndpointName());
        logger.debug("Endpoints defined inside the manifest: {}",
                manifestEndpointService.toString());
        JsonNode manifestEndpointServiceApi = manifestEndpointService.get("api");
        JsonNode manifestEndpointServiceGui = manifestEndpointService.get("gui");

        try {
            String tssContainerName = serviceInstance.getContainerName();

            if (manifestEndpointServiceApi != null) {
                if (!manifestEndpointServiceApi.isArray()) {
                    buildEndpointUrl(serviceInstance, manifestEndpointServiceApi, tssContainerName,
                            "api");
                } else {
                    for (final JsonNode apiNode : manifestEndpointServiceApi) {
                        buildEndpointUrl(serviceInstance, apiNode, tssContainerName,
                                apiNode.get("name") != null
                                        ? apiNode.get("name").toString().replaceAll("\"", "")
                                                + "api"
                                        : "api");
                    }
                }
            }

            if (manifestEndpointServiceGui != null) {
                if (!manifestEndpointServiceGui.isArray()) {
                    buildEndpointUrl(serviceInstance, manifestEndpointServiceGui, tssContainerName,
                            "gui");
                } else {
                    for (final JsonNode guiNode : manifestEndpointServiceGui) {
                        buildEndpointUrl(serviceInstance, guiNode, tssContainerName,
                                guiNode.get("name") != null
                                        ? guiNode.get("name").toString().replaceAll("\"", "")
                                                + "gui"
                                        : "gui");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error building endpoints info: {}", e.getMessage());
            throw new Exception("Error building endpoints info: " + e.getMessage());
        }
    }

    private void buildEndpointUrl(SupportServiceInstance serviceInstance, JsonNode node,
            String tSSContainerName, String nodeName) throws Exception {

        // Default aux port correspond with ElasTest external port
        int auxPort = 37000;

        if (node != null && node.get("port") != null) {
            String nodePort = node.get("port").toString().replaceAll("\"", "");

            int internalPort = Integer.parseInt(nodePort);
            ((ObjectNode) node).put("internalPort", internalPort);
            serviceInstance.setInternalServicePort(internalPort);

            // If ElasTest works locally, a binded port is not required.
            if (!utilsService.isDefaultEtPublicHost()) {
                logger.debug("It's necessary to bind ports");
                auxPort = bindingPort(serviceInstance, nodePort, node,
                        isIntegratedEUS(serviceInstance));
            } else {
                logger.debug("It is not necessary to bind ports");
                auxPort = internalPort;
            }
            //
            // if (utilsService.isKubernetes()) {
            // serviceInstance.setContainerIp(platformService.getContainerIp(
            // serviceInstance.getEndpointName(), serviceInstance));
            // }

            if (node.get("protocol") != null && (node.get("protocol").toString().contains("http")
                    || node.get("protocol").toString().contains("https")
                    || node.get("protocol").toString().contains("ws"))) {
                serviceInstance.setServicePort(auxPort);

                String internalUrl = buildEndpointUrlAsString(node,
                        serviceInstance.getInternalServiceIp(),
                        serviceInstance.getInternalServicePort());
                serviceInstance.setUrlValue(nodeName, internalUrl, false);

                if (withServerAddress) {
                    String serviceIp = platformService.getETPublicHost();
                    serviceInstance.setServiceIp(serviceIp);
                    serviceInstance.setBindedServiceIp(serviceIp);
                    String externalUrl = buildEndpointUrlAsString(node, serviceIp,
                            serviceInstance.getBindedServicePort());
                    serviceInstance.setUrlValue(nodeName, externalUrl, true);
                }
            }

            serviceInstance.getEndpointsData().put(nodeName, node);
        }
    }

    private String buildEndpointUrlAsString(JsonNode node, String ip, int port) {
        String url = null;

        String protocol = node.get("protocol").toString().replaceAll("\"", "");
        String path = node.get("path") != null ? node.get("path").toString().replaceAll("\"", "")
                : "/";
        url = protocol + "://" + ip + ":" + port + path;
        logger.info("New url: " + url);
        return url;
    }

    public int bindingPort(SupportServiceInstance serviceInstance, String nodePort, JsonNode node,
            Boolean integratedService) throws Exception {

        int hostPort;

        // If there is server address ports will be binded
        int bindedPort;
        if (serviceInstance.getEndpointsBindingsPorts().containsKey(nodePort)) {
            bindedPort = Integer
                    .parseInt(serviceInstance.getEndpointsBindingsPorts().get(nodePort));
        } else {
            if (utilsService.isKubernetes() && isIntegratedEUS(serviceInstance)) {
                // Do nothing. For other TSS, implement getBindingPort
                // in K8ServiceImpl
                bindedPort = Integer.valueOf(etProxyPort);
            } else {
                try {
                    ServiceBindedPort socatBindedPortObj = platformService.getBindedPort(
                            utilsService.isKubernetes() ? serviceInstance.getEndpointName()
                                    : serviceInstance.getContainerIp(),
                            null, null, node.get("port").toString(),
                            serviceInstance.getInstanceId());
                    serviceInstance.getPortBindingContainers()
                            .add(socatBindedPortObj.getContainerId());
                    bindedPort = Integer.parseInt(socatBindedPortObj.getBindedPort());
                    serviceInstance.getEndpointsBindingsPorts().put(nodePort,
                            String.valueOf(bindedPort));
                } catch (Exception e) {
                    String message = "Ports binding fails in Service "
                            + serviceInstance.getServiceName() + " with instance id "
                            + serviceInstance.getInstanceId();

                    logger.error("{}: {} ", message, e.getMessage());
                    throw new Exception(message + ": " + e.getMessage());
                }
            }
        }

        serviceInstance.setBindedServicePort(bindedPort);

        hostPort = bindedPort;
        ((ObjectNode) node).put("bindedPort", bindedPort);
        ((ObjectNode) node).put("port", bindedPort);
        return hostPort;
    }

    public void deprovisionServicesInstances() {
        tJobServicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            deprovisionServiceInstance(tSSInstanceId, tJobServicesInstances);
        });

        tJobServicesInstances = null;

        externalTJobServicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            deprovisionServiceInstance(tSSInstanceId, externalTJobServicesInstances);
        });

        externalTJobServicesInstances = null;

        servicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            // If is not EUS in mini mode, deprovision
            if (!isIntegratedEUS(tSSInstance)) {
                deprovisionServiceInstance(tSSInstanceId, servicesInstances);
            } else if (utilsService.isKubernetes()) {
                platformService.removeBindedPorts(tSSInstance);
                platformService.removeWorkEnvironment(tSSInstanceId);
            }
        });

        servicesInstances = null;

    }

    public boolean isIntegratedEUS(String serviceName, String instanceId) {
        return utilsService.isElastestMini() && "EUS".equals(serviceName)
                && tssLoadedOnInitMap.containsKey(serviceName)
                && tssLoadedOnInitMap.containsValue(instanceId);
    }

    public boolean isIntegratedEUS(SupportServiceInstance instance) {
        return this.isIntegratedEUS(instance.getServiceName(), instance.getInstanceId());
    }

    public String deprovisionExecutionServiceInstance(String instanceId, Execution execution) {
        if (execution.isExternal()) {
            return deprovisionExternalTJobExecServiceInstance(instanceId,
                    execution.getExecutionId());
        } else {
            return deprovisionTJobExecServiceInstance(instanceId, execution.getExecutionId());
        }
    }

    public String deprovisionTJobExecServiceInstance(String instanceId, Long tJobExecId) {
        tSSIByTJobExecAssociated.remove(tJobExecId);

        SupportServiceInstance tssInstance = tJobServicesInstances.get(instanceId);
        String serviceName = tssInstance != null ? tssInstance.getServiceName().toUpperCase()
                : null;
        // If is shared tss
        if (isSharedTssInstance(serviceName)) {
            if (serviceName.equals("EUS")) {
                TJobExecution tJobExec = tJobExecRepositoryImpl.findById(tJobExecId).get();
                this.unregisterTJobExecutionInEus(instanceId, serviceName, tJobExec);
                int index = tssInstance.gettJobExecIdList().indexOf(tJobExecId);
                if (index >= 0) {
                    tssInstance.gettJobExecIdList().remove(index);
                }
            }
            return "Instance not deleted: is shared. Finished!";
        } else {

            return deprovisionServiceInstance(instanceId, tJobServicesInstances);
        }
    }

    public String deprovisionExternalTJobExecServiceInstance(String instanceId,
            Long externalTJobExecId) {
        tSSIByExternalTJobExecAssociated.remove(externalTJobExecId);

        SupportServiceInstance tssInstance = externalTJobServicesInstances.get(instanceId);

        String serviceName = tssInstance != null ? tssInstance.getServiceName().toUpperCase()
                : null;
        // If is shared tss
        if (isSharedTssInstance(serviceName)) {

            if (serviceName.equals("EUS")) {
                ExternalTJobExecution exTJobExec = externalTJobExecutionRepository
                        .findById(externalTJobExecId).get();
                this.unregisterExternalTJobExecutionInEus(instanceId, serviceName, exTJobExec);
                int index = tssInstance.gettJobExecIdList().indexOf(externalTJobExecId);
                if (index >= 0) {
                    tssInstance.gettJobExecIdList().remove(index);
                }
            }
            return "Instance not deleted: is shared. Finished!";
        } else {
            return deprovisionServiceInstance(instanceId, externalTJobServicesInstances);
        }
    }

    public String deprovisionServiceInstance(String instanceId,
            Map<String, SupportServiceInstance> ssiMap) {
        if (ssiMap == null) {
            ssiMap = this.servicesInstances;
        }
        String result = "Instance deleted.";

        SupportServiceInstance serviceInstance = ssiMap.get(instanceId);

        // If not empty and not integrated EUS
        if (serviceInstance != null && !this.isIntegratedEUS(serviceInstance)) {

            platformService.removeBindedPorts(serviceInstance);

            for (SupportServiceInstance subServiceInstance : serviceInstance.getSubServices()) {
                platformService.removeBindedPorts(subServiceInstance);
            }

            esmServiceClient.deprovisionServiceInstance(instanceId, serviceInstance);

            platformService.manageTSSInstanceIfNecessary(serviceInstance, false);
        }
        ssiMap.remove(instanceId);
        return result;
    }

    public boolean isInstanceUp(String instanceId) {
        boolean result = false;
        try {
            result = esmServiceClient.getServiceInstanceInfo(instanceId).toString().equals("{}")
                    ? false
                    : true;
            logger.info("Check instance status:{}", instanceId, ". Info: {}", result);
            result = true;
        } catch (RuntimeException re) {
            logger.info("Error cause: {}", re.getCause().getMessage());
            if (re.getCause().getMessage().contains("404")) {
                logger.info("Check instance status:{}", instanceId, ". Info: {}", result);
                result = false;
            }
        }

        return result;
    }

    public SupportServiceInstance getServiceInstanceFromMem(String id) {
        return this.getServiceInstance(id);
    }

    public SupportServiceInstance getServiceInstance(String id) {
        return this.getServiceInstanceBySIMap(id, servicesInstances);
    }

    public SupportServiceInstance getTJobExecServiceInstance(String id) {
        return this.getServiceInstanceBySIMap(id, tJobServicesInstances);
    }

    public SupportServiceInstance getExternalTJobExecServiceInstance(String id) {
        return this.getServiceInstanceBySIMap(id, externalTJobServicesInstances);
    }

    public SupportServiceInstance getServiceInstanceBySIMap(String id,
            Map<String, SupportServiceInstance> ssiMap) {
        SupportServiceInstance tss = ssiMap.get(id);
        if (tss != null) {
            if (checkInstanceUrlIsUp(tss)) {
                tss.setStatus(DockerServiceStatusEnum.READY);
                tss.setStatusMsg("Ready");
            }
        }

        return tss;
    }

    public List<SupportServiceInstance> getServicesInstancesAsList() {
        ArrayList<SupportServiceInstance> servicesInstancesList = new ArrayList<>();
        servicesInstances.forEach((tSSInstanceId, tSSInstance) -> {

            String serviceName = tSSInstance.getServiceName();

            // If not is integrated EUS (mini) and not is shared TSS
            if (!this.isIntegratedEUS(tSSInstance)
                    && this.getSharedTssInstanceId(serviceName) != tSSInstanceId) {
                boolean isUp = checkInstanceUrlIsUp(tSSInstance);

                if (isUp) {
                    // TODO refactor to SupportServiceClient (to merge with
                    // testEngines)
                    tSSInstance.setStatus(DockerServiceStatusEnum.READY);
                    tSSInstance.setStatusMsg("Ready");
                }

                servicesInstancesList.add(tSSInstance);
            }
        });
        return servicesInstancesList;
    }

    /* *************************** */
    /* **** TSS Instances Map **** */
    /* *************************** */

    public Map<String, SupportServiceInstance> getServicesInstances() {
        return servicesInstances;
    }

    public void setServicesInstances(Map<String, SupportServiceInstance> servicesInstances) {
        this.servicesInstances = servicesInstances;
    }

    public Map<String, SupportServiceInstance> getTJobServicesInstances() {
        return tJobServicesInstances;
    }

    public SupportServiceInstance getTJobServiceInstanceById(String tSSInstId) {
        return tJobServicesInstances.get(tSSInstId);
    }

    public Map<String, SupportServiceInstance> getExternalTJobServicesInstances() {
        return externalTJobServicesInstances;
    }

    public SupportServiceInstance getExternalTJobServiceInstanceById(String tSSInstId) {
        return externalTJobServicesInstances.get(tSSInstId);
    }

    public void putInstanceInMap(String tssInstanceId, SupportServiceInstance tssi) {
        servicesInstances.put(tssInstanceId, tssi);
    }

    public void putInstanceInTJobExecMap(String tssInstanceId, SupportServiceInstance tssi) {
        tJobServicesInstances.put(tssInstanceId, tssi);
    }

    public void putInstanceInExternalTJobExecMap(String tssInstanceId,
            SupportServiceInstance tssi) {
        externalTJobServicesInstances.put(tssInstanceId, tssi);
    }

    public void putInstanceInExecutionMap(String tssInstanceId, SupportServiceInstance tssi,
            Execution execution) {
        if (execution.isExternal()) {
            putInstanceInExternalTJobExecMap(tssInstanceId, tssi);
        } else {
            putInstanceInTJobExecMap(tssInstanceId, tssi);
        }
    }

    /* *** *** */

    public Map<String, SupportServiceInstance> getExecutionServicesInstances(Execution execution) {
        if (execution.isExternal()) {
            return getExternalTJobServicesInstances();
        } else {
            return getTJobServicesInstances();
        }
    }

    public SupportServiceInstance getExecutionServiceInstanceById(Execution execution,
            String tSSInstId) {
        if (execution.isExternal()) {
            return getExternalTJobServiceInstanceById(tSSInstId);
        } else {
            return getTJobServiceInstanceById(tSSInstId);
        }
    }

    public Map<String, SupportServiceInstance> getExecutionServicesInstancesMap(
            Execution execution) {
        Map<String, SupportServiceInstance> tSSInstAssocToTJobExec = new HashMap<>();
        List<String> servicesInstances = getServicesInstancesByExecution(execution);

        servicesInstances.forEach((tSSInstId) -> {
            tSSInstAssocToTJobExec.put(tSSInstId, getTJobServiceInstanceById(tSSInstId));
        });

        return tSSInstAssocToTJobExec;
    }

    public List<SupportServiceInstance> getTJobExecServicesInstancesList(Long tJobExecId) {
        logger.debug("Get ready TSS by TJobExecId {}", tJobExecId);
        List<SupportServiceInstance> tSSInstanceList = new ArrayList<>();
        tJobServicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            if (tSSInstance.gettJobExecIdList().contains(tJobExecId.longValue())
                    && checkInstanceUrlIsUp(tSSInstance)) {
                tSSInstance.setStatus(DockerServiceStatusEnum.READY);
                tSSInstance.setStatusMsg("Ready");
                tSSInstanceList.add(tSSInstance);
            }
        });

        return tSSInstanceList;
    }

    public List<SupportServiceInstance> getExternalTJobExecServicesInstancesList(Long tJobExecId) {
        logger.debug("Get ready TSS by ExternalTJobExecId {}", tJobExecId);
        List<SupportServiceInstance> tSSInstanceList = new ArrayList<>();
        externalTJobServicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            if (tSSInstance.gettJobExecIdList().contains(tJobExecId.longValue())
                    && tSSInstance.isFullyInitialized() && checkInstanceUrlIsUp(tSSInstance)) {
                tSSInstance.setStatus(DockerServiceStatusEnum.READY);
                tSSInstance.setStatusMsg("Ready");
                tSSInstanceList.add(tSSInstance);
            }
        });

        return tSSInstanceList;
    }

    public boolean checkInstanceUrlIsUp(SupportServiceInstance tSSInstance) {
        boolean up = false;
        if (tSSInstance != null) {
            String serviceName = tSSInstance.getServiceName();

            if (tSSInstance.getUrls() != null && !tSSInstance.getUrls().isEmpty()) {
                // First check if internal api url exists
                String urlValue = tSSInstance
                        .getApiUrlIfExist(utilsService.isKubernetes() ? true : false);
                logger.debug("{} Internal url {} ", serviceName, urlValue);
                if (urlValue == null) {

                    // else if api status url exist (for integrated EUS)
                    urlValue = tSSInstance.getApiStatusUrlIfExist();
                    logger.debug("{} Api status url {} ", serviceName, urlValue);

                    // else normal url
                    if (urlValue == null) {
                        urlValue = tSSInstance.getApiUrlIfExist(
                                (utilsService.isKubernetes() || withServerAddress) ? true : false);
                        logger.debug("{} Normal url {} ", serviceName, urlValue);
                    }
                }

                up = true;
                if (urlValue != null) {
                    try {
                        logger.debug(serviceName + " Service URL: " + urlValue);

                        up = up && UtilTools.checkIfUrlIsUp(urlValue);

                        if (!up) {
                            logger.debug(serviceName + " Service is not ready.");
                            return up;
                        }
                    } catch (Exception e) {
                        logger.debug(serviceName + " Service is not ready by exception error.");
                        return false;
                    }
                }
            }
            String checklMessage = up ? serviceName + " Service is ready."
                    : serviceName + " Service is not ready yet.";

            if (up && utilsService.isElastestMini() && !isSharedTssInstance(serviceName)) {
                etPluginsService.updateStatus(tSSInstance.getInstanceId(),
                        DockerServiceStatusEnum.READY, "Ready");
            }

            logger.info(checklMessage);
        }

        return up;

    }

    private void createExecFilesFolder(SupportServiceInstance supportServiceInstance) {
        logger.info("try to create folder.");
        File folderStructure = new File(
                supportServiceInstance.getParameters().get("ET_FILES_PATH"));

        try {
            if (!folderStructure.exists()) {
                logger.info("creating folder at {}.", folderStructure.getAbsolutePath());
                folderStructure.mkdirs();
                logger.info("Folder created.");
            }
        } catch (Exception e) {
            logger.error("File does not created.");
            throw e;
        }
    }

    private void setTSSFilesConfig(SupportServiceInstance supportServiceInstance) {
        String fileSeparator = "/";
        supportServiceInstance.getParameters().put("ET_FILES_PATH", etSharedFolder + fileSeparator
                + supportServiceInstance.getServiceName().toLowerCase() + fileSeparator);

        supportServiceInstance.getParameters().put("ET_FILES_PATH_IN_HOST",
                etDataInHost + fileSeparator + supportServiceInstance.getServiceName().toLowerCase()
                        + fileSeparator);

        supportServiceInstance.getParameters().put("ET_SHARED_FOLDER", etSharedFolder);
        supportServiceInstance.getParameters().put("ET_DATA_IN_HOST", etDataInHost);

    }

    private void fillEnvVariablesToTSS(SupportServiceInstance supportServiceInstance)
            throws ServiceException {

        supportServiceInstance.getParameters()
                .putAll(etmContextAuxService.getMonitoringEnvVars(true));

        supportServiceInstance.getParameters().put("ET_TSS_INSTANCE_ID",
                supportServiceInstance.getInstanceId());

        supportServiceInstance.getParameters().put("ET_ETM_LSTCP_HOST", etEtmLstcpHost);
        supportServiceInstance.getParameters().put("ET_ETM_LSTCP_PORT", etEtmLstcpPort);

        supportServiceInstance.getParameters().put("ET_CONTEXT_API",
                "http://" + UtilTools.getMyIp() + ":" + etmServerPort + "/api/context/tss/"
                        + supportServiceInstance.getInstanceId());

        supportServiceInstance.getParameters().put("ET_INTERNET_DISABLED", etInternetDisabled);
        supportServiceInstance.getParameters().put("ET_PUBLIC_HOST",
                utilsService.getEtPublicHostValue());
        supportServiceInstance.getParameters().put("ET_PUBLIC_HOST_TYPE",
                utilsService.getEtPublicHostType());
        supportServiceInstance.getParameters().put("ET_EDM_ALLUXIO_API", etEdmAlluxioApi);
        supportServiceInstance.getParameters().put("ET_EDM_MYSQL_HOST", etEdmMysqlHost);
        supportServiceInstance.getParameters().put("ET_EDM_MYSQL_PORT", etEdmMysqlPort);
        supportServiceInstance.getParameters().put("ET_EDM_ELASTICSEARCH_API",
                etEdmElasticsearchApi);
        supportServiceInstance.getParameters().put("ET_EDM_API", etEdmApi);
        supportServiceInstance.getParameters().put("ET_EPM_API", etEpmApi);
        supportServiceInstance.getParameters().put("ET_ETM_API", etEtmApi);
        supportServiceInstance.getParameters().put("ET_ESM_API", etEsmApi);
        supportServiceInstance.getParameters().put("ET_EIM_API", etEimApi);

        supportServiceInstance.getParameters().put("ET_ETM_LSBEATS_HOST", etEtmLsbeatsHost);
        supportServiceInstance.getParameters().put("ET_ETM_LSBEATS_PORT", etEtmLsbeatsPort);
        supportServiceInstance.getParameters().put("ET_ETM_LSHTTP_API", etEtmLshttpApi);

        supportServiceInstance.getParameters().put("ET_ETM_BINDED_LSBEATS_HOST",
                etEtmBindedLsBeatsHost);
        supportServiceInstance.getParameters().put("ET_ETM_BINDED_LSBEATS_PORT",
                etEtmBindedLsBeatsPort);
        supportServiceInstance.getParameters().put("ET_ETM_BINDED_LSTCP_HOST",
                etEtmBindedLsTcpHost);
        supportServiceInstance.getParameters().put("ET_ETM_BINDED_LSTCP_PORT",
                etEtmBindedLsTcpPort);

        supportServiceInstance.getParameters().put("ET_ETM_RABBIT_HOST", etEtmRabbitHost);
        supportServiceInstance.getParameters().put("ET_ETM_RABBIT_PORT", etEtmRabbitPort);
        supportServiceInstance.getParameters().put("ET_EMP_API", etEmpApi);
        supportServiceInstance.getParameters().put("ET_EMP_INFLUXDB_API", etEmpInfluxdbApi);
        supportServiceInstance.getParameters().put("ET_EMP_INFLUXDB_HOST", etEmpInfluxdbHost);
        supportServiceInstance.getParameters().put("ET_EMP_INFLUXDB_GRAPHITE_PORT",
                etEmpInfluxdbGraphitePort);

        supportServiceInstance.getParameters().put("USE_TORM", "true");
    }

    private void fillEMSMonitoringEnvVariablesToTSS(SupportServiceInstance supportServiceInstance,
            TJobExecution tJobExec, boolean externalTJob, boolean withPublicPrefix) {
        try {
            if (tJobExec != null && tJobExec.getTjob() != null) {
                if (tJobExec.getTjob().isEmsTssSelected()) {
                    // IF EMS is started, TJobExec have EMS env vars
                    Map<String, String> emsEnvVars = tJobExec.getEnvVars();

                    supportServiceInstance.getParameters()
                            .putAll(etmContextAuxService.getMonitoringEnvVarsFromEms(emsEnvVars));
                }
            }
        } catch (Exception e) {
            logger.error("Error on fill EMS monitoring env vars to TSS", e);
        }
    }

    public Map<String, String> getTSSInstanceContext(String tSSInstanceId, boolean publicEnvVars,
            boolean withPublicPrefix) {
        Map<String, String> tSSInstanceContextMap = new HashMap<>();
        SupportServiceInstance ssi = null;
        if (servicesInstances.get(tSSInstanceId) != null) {
            ssi = servicesInstances.get(tSSInstanceId);
        } else if (tJobServicesInstances.get(tSSInstanceId) != null) {
            ssi = tJobServicesInstances.get(tSSInstanceId);
        } else {
            return null;
        }

        tSSInstanceContextMap.putAll(getTSSInstanceEnvVars(ssi, publicEnvVars, withPublicPrefix));
        tSSInstanceContextMap.putAll(ssi.getParameters());

        return tSSInstanceContextMap;
    }

    public Map<String, String> getTSSInstanceEnvVars(SupportServiceInstance ssi,
            boolean publicEnvVars, boolean withPublicPrefix) {
        logger.debug("Creating env vars from TSS: {}", ssi); // TODO change
        Map<String, String> envVars = new HashMap<String, String>();
        String servicePrefix = ssi.getServiceName().toUpperCase().replaceAll("-", "_");
        String envVarNamePrefix = withPublicPrefix ? "ET_PUBLIC" : "ET";

        for (Map.Entry<String, JsonNode> entry : ssi.getEndpointsData().entrySet()) {
            String prefix = withPublicPrefix ? envVarNamePrefix
                    : envVarNamePrefix + "_" + servicePrefix;
            envVars.putAll(setTssEnvVarByEndpoint(ssi, prefix, entry, publicEnvVars));
        }

        for (SupportServiceInstance subssi : ssi.getSubServices()) {
            String envNamePrefixSubSSI = envVarNamePrefix + "_" + servicePrefix + "_"
                    + subssi.getEndpointName().toUpperCase().replaceAll("-", "_");

            for (Map.Entry<String, JsonNode> entry : subssi.getEndpointsData().entrySet()) {
                envVars.putAll(
                        setTssEnvVarByEndpoint(subssi, envNamePrefixSubSSI, entry, publicEnvVars));
            }
        }
        logger.debug("TJob and Sut env vars: {}", envVars.keySet().toString());
        return envVars;
    }

    private Map<String, String> setTssEnvVarByEndpoint(SupportServiceInstance ssi, String prefix,
            Map.Entry<String, JsonNode> entry, boolean publicEnvVars) {
        logger.debug("Creating env vars from a TSS endpoint");
        Map<String, String> envVars = new HashMap<>();
        if (!entry.getKey().toLowerCase().contains("gui")) {
            try {
                if (entry.getValue().get("name") != null) {
                    prefix += "_" + entry.getValue().get("name").toString().toUpperCase()
                            .replaceAll("\"", "").replaceAll("-", "_");
                }

                String envNameHost = prefix + "_HOST";
                String envValueHost = publicEnvVars ? ssi.getServiceIp() : ssi.getContainerIp();

                String envNamePort = prefix + "_PORT";

                String envValuePort = "";
                if (utilsService.isDefaultEtPublicHost()) {
                    envValuePort = entry.getValue().get("port").toString();
                } else {
                    for (Map.Entry<String, String> endpointBindingPort : ssi
                            .getEndpointsBindingsPorts().entrySet()) {
                        logger.debug("Binding port Key: {}", endpointBindingPort.getKey());
                        logger.debug("Binding port value: {}", endpointBindingPort.getValue());
                        logger.debug("Node port: {}", entry.getValue().get("port").toString());
                        if (endpointBindingPort.getValue()
                                .equals(entry.getValue().get("port").toString())) {
                            envValuePort = !publicEnvVars ? endpointBindingPort.getKey()
                                    : endpointBindingPort.getValue();
                        } else if (entry.getValue().get("port").toString().equals(etProxyPort)) {
                            envValuePort = publicEnvVars ? etProxyPort : etmServerPort;
                        }

                    }
                }

                String protocol = entry.getValue().findValue("protocol").toString().toLowerCase()
                        .replaceAll("\"", "");
                if (protocol.equals("http") || protocol.equals("https") || protocol.equals("ws")) {
                    String envNameAPI = prefix + "_API";
                    JsonNode pathNode = entry.getValue().get("path");
                    String path = "/";
                    if (pathNode != null) {
                        path = pathNode.toString().replaceAll("\"", "");
                    }
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }

                    String envValueAPI = protocol + "://" + envValueHost + ":" + envValuePort
                            + path;
                    envVars.put(envNameAPI, envValueAPI);
                } else {
                    envVars.put(envNameHost, envValueHost);
                    envVars.put(envNamePort, envValuePort);
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
        return envVars;
    }

    public Map<Long, List<String>> gettSSIByExecutionAssociated(Execution execution) {
        if (execution.isExternal()) {
            return tSSIByExternalTJobExecAssociated;
        } else {
            return tSSIByTJobExecAssociated;
        }
    }

    public void settSSIByTJobExecAssociated(Map<Long, List<String>> tSSIByTJobExecAssociated) {
        this.tSSIByTJobExecAssociated = tSSIByTJobExecAssociated;
    }

    public String getContainerNameOfTssLoadedOnInitByName(String serviceName) {
        String containerName = null;

        if (tssLoadedOnInitMap.containsKey(serviceName.toUpperCase())) {
            String tssId = tssLoadedOnInitMap.get(serviceName.toUpperCase());
            if (servicesInstances.containsKey(tssId)) {
                containerName = servicesInstances.get(tssId).getContainerName();
            }
        }

        return containerName;
    }

    public String getServiceNameByServiceId(String serviceId) {
        String serviceName = null;

        for (SupportService tss : getRegisteredServices()) {
            if (tss.getId().equals(serviceId)) {
                return tss.getName();
            }
        }

        return serviceName;
    }

    public String getSharedTssInstanceId(String serviceName) {
        // If is shared tss
        if (serviceName != null && tssLoadedOnInitMap.containsKey(serviceName.toUpperCase())) {
            return tssLoadedOnInitMap.get(serviceName.toUpperCase());
        }
        return null;
    }

    public SupportServiceInstance getSharedTssInstance(String instanceId) {
        // If is shared tss
        if (instanceId != null && servicesInstances.containsKey(instanceId)) {
            return servicesInstances.get(instanceId);
        }
        return null;
    }

    public boolean isSharedTssInstance(String serviceName) {
        return getSharedTssInstanceId(serviceName) != null;
    }

    public boolean isSharedTssInstanceByServiceId(String serviceId) {
        String serviceName = getServiceNameByServiceId(serviceId).toUpperCase();
        return isSharedTssInstance(serviceName);
    }

    public void updateExecutionResultStatus(Execution execution, ResultEnum result, String msg) {
        if (execution.isExternal()) {
            ExternalTJobExecution externalTJobExec = execution.getExternalTJobExec();
            updateExternalTJobExecResultStatus(externalTJobExec, result, msg);
        } else {
            TJobExecution tJobExec = execution.getTJobExec();
            updateTJobExecResultStatus(tJobExec, result, msg);
        }
    }

    // Use updateExecutionResultStatus
    private void updateTJobExecResultStatus(TJobExecution tJobExec, ResultEnum result, String msg) {
        tJobExec.setResult(result);
        tJobExec.setResultMsg(msg);
        tJobExecRepositoryImpl.save(tJobExec);
    }

    // Use updateExecutionResultStatus
    private void updateExternalTJobExecResultStatus(ExternalTJobExecution externalTJobExec,
            ResultEnum result, String msg) {
        externalTJobExec.setResult(result);
        externalTJobExec.setResultMsg(msg);
        externalTJobExecutionRepository.save(externalTJobExec);
    }
}