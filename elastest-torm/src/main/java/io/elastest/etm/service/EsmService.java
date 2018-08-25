package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS;
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
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.epm.client.model.DockerServiceStatus.DockerServiceStatusEnum;
import io.elastest.epm.client.service.EpmService;
import io.elastest.epm.client.service.ServiceException;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.external.ExternalTJobExecutionRepository;
import io.elastest.etm.model.EusExecutionData;
import io.elastest.etm.model.SocatBindedPort;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SupportServiceInstance.SSIStatusEnum;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.model.TssManifest;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.service.client.SupportServiceClientInterface;
import io.elastest.etm.utils.ElastestConstants;
import io.elastest.etm.utils.EtmFilesService;
import io.elastest.etm.utils.ParserService;
import io.elastest.etm.utils.UtilTools;
import io.elastest.eus.service.DynamicDataService;
import io.elastest.eus.service.WebDriverService;

@Service
public class EsmService {
    final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.esm.ss.desc.files.path}")
    public String etEsmSsDescFilesPath;
    @Value("${et.shared.folder}")
    public String etSharedFolder;
    @Value("${et.internet.disabled}")
    public String etInternetDisabled;

    @Value("${et.public.host}")
    public String etPublicHost;
    @Value("${server.port}")
    public String serverPort;
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
    @Value("${et.shared.folder}")
    private String sharedFolder;

    @Value("${server.port}")
    public String etmServerPort;

    @Value("${et.proxy.port}")
    public String etProxyPort;

    @Value("${et.etm.incontainer}")
    private String inContainer;

    @Value("${api.context.path:#{null}}")
    private String eusApiPath;

    @Value("${eus.tss.id}")
    public String EUS_TSS_ID;

    public SupportServiceClientInterface supportServiceClient;
    public DockerEtmService dockerEtmService;
    public EtmContextAuxService etmContextAuxService;
    public EtmFilesService filesServices;
    public EpmService epmService;

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

    private String tJobsFolder = "tjobs";
    private String tJobFolderPrefix = "tjob_";
    private String tJobExecFolderPefix = "exec_";

    private String externalTJobsFolder = "external_tjobs";
    private String externalTJobFolderPrefix = "external_tjob_";
    private String externalTJobExecFolderPefix = "external_exec_";

    private final TJobExecRepository tJobExecRepositoryImpl;
    private final ExternalTJobExecutionRepository externalTJobExecutionRepository;
    private final DynamicDataService dynamicDataService;
    private final WebDriverService eusWebDriverService;

    @Autowired
    public EsmService(SupportServiceClientInterface supportServiceClient,
            DockerEtmService dockerEtmService, EtmFilesService filesServices,
            EtmContextAuxService etmContextAuxService, EpmService epmService,
            TJobExecRepository tJobExecRepositoryImpl,
            ExternalTJobExecutionRepository externalTJobExecutionRepository,
            DynamicDataService dynamicDataService,
            WebDriverService eusWebDriverService) {
        this.supportServiceClient = supportServiceClient;
        this.servicesInstances = new ConcurrentHashMap<>();
        this.tJobServicesInstances = new HashMap<>();
        this.externalTJobServicesInstances = new HashMap<>();
        this.tSSIByTJobExecAssociated = new HashMap<>();
        this.tSSIByExternalTJobExecAssociated = new HashMap<>();
        this.dockerEtmService = dockerEtmService;
        this.tSSIdLoadedOnInit = new ArrayList<>();
        this.tSSNameLoadedOnInit = new ArrayList<>(Arrays.asList("EUS"));
        this.tssLoadedOnInitMap = new HashMap<>();
        this.filesServices = filesServices;
        this.etmContextAuxService = etmContextAuxService;
        this.epmService = epmService;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.externalTJobExecutionRepository = externalTJobExecutionRepository;
        this.dynamicDataService = dynamicDataService;
        this.eusWebDriverService = eusWebDriverService;
    }

    @PostConstruct
    public void init() {
        logger.info("EsmService initialization.");
        try {
            registerElastestServices();
            if (execMode.equals(ElastestConstants.MODE_NORMAL)) {
                tSSIdLoadedOnInit.forEach((serviceId) -> {
                    String serviceName = getServiceNameByServiceId(serviceId)
                            .toUpperCase();

                    String tssInstanceId = null;
                    if (serviceName.equals("EUS")) {
                        tssInstanceId = startIntegratedEus(serviceId);

                    } else {
                        tssInstanceId = provisionServiceInstanceSync(serviceId);
                    }

                    tssLoadedOnInitMap.put(serviceName, tssInstanceId);

                    logger.debug("{} is started from ElasTest in normal mode",
                            serviceName);
                });

            }
        } catch (Exception e) {
            logger.warn("Error during the services registry. ", e);
        }
    }

    private String startIntegratedEus(String serviceId) {
        String tssInstanceId;
        tssInstanceId = UtilTools.generateUniqueId();
        try {
            SupportServiceInstance eusInstance = this
                    .createNewServiceInstance(serviceId, null, tssInstanceId);
            TssManifest manifest = supportServiceClient
                    .getManifestBySupportServiceInstance(eusInstance);
            eusInstance.setManifestId(manifest.getId());

            String etmHost = dockerEtmService.getEtmHost();

            eusInstance.setContainerIp(etmHost);

            String serviceIp = etmHost;
            int servicePort = Integer.parseInt(etmServerPort);
            if (!etPublicHost.equals("localhost")) {
                serviceIp = etPublicHost;
                if ("true".equals(inContainer)) {
                    servicePort = Integer.parseInt(etProxyPort);
                }
            }
            eusInstance.setServiceIp(serviceIp);

            eusInstance = buildTssInstanceUrls(eusInstance);

            // Replace EUS port to ETM port
            String originalPort = String.valueOf(eusInstance.getServicePort());
            for (String key : eusInstance.getUrls().keySet()) {
                String newValue = eusInstance.getUrls().get(key)
                        .replaceAll(originalPort, String.valueOf(servicePort));
                eusInstance.getUrls().put(key, newValue);

            }
            for (String key : eusInstance.getEndpointsData().keySet()) {
                JsonNode node = eusInstance.getEndpointsData().get(key);

                for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
                    String field = it.next();

                    if (node.get(field).asText().equals(originalPort)) {
                        ((ObjectNode) node).put(field, servicePort);
                    }

                }
            }

            eusInstance.setServicePort(servicePort);

            String apiUrl = eusInstance.getApiUrlIfExist();

            eusInstance.getUrls().put("api-status", apiUrl + "status");

            eusInstance.setServiceReady(true);
            eusInstance.setServiceStatus(SSIStatusEnum.READY);

            dynamicDataService.setLogstashHttpsApi(etmContextAuxService
                    .getContextInfo().getLogstashSSLHttpUrl());

            servicesInstances.put(tssInstanceId, eusInstance);

        } catch (Exception e) {
            logger.error("Error on start integrated EUS:", e);
        }
        return tssInstanceId;
    }

    @PreDestroy
    public void cleanTSSInstances() {
        deprovisionServicesInstances();
    }

    /**
     * Register the ElasTest Services into the ESM.
     */
    public void registerElastestServices() {
        logger.info("Get and send the register information: {}",
                etEsmSsDescFilesPath);

        try {
            List<File> files = filesServices
                    .getFilesFromFolder(etEsmSsDescFilesPath);
            for (File file : files) {
                registerElastestService(file);
            }
        } catch (IOException ioe) {
            logger.warn(
                    "Service could not be registered. The file with the path "
                            + etEsmSsDescFilesPath + " does not exist.");
        }
    }

    public void registerElastestService(File serviceFile) throws IOException {
        try {
            String content = filesServices.readFile(serviceFile);

            if (content != null) {
                ObjectNode serviceDefJson = ParserService
                        .fromStringToJson(content);
                if (execMode.equals(ElastestConstants.MODE_NORMAL)) {
                    logger.debug("TSS file {}", serviceFile.getName());
                    String serviceName = serviceFile.getName().split("-")[0]
                            .toUpperCase();
                    logger.debug("TSS {} will be registered in normal mode.",
                            serviceName);
                    registerElasTestService(serviceDefJson);
                    String tssId = serviceDefJson.get("register").get("id")
                            .toString().replaceAll("\"", "");
                    if (tSSNameLoadedOnInit.contains(serviceName)) {
                        tSSIdLoadedOnInit.add(tssId);
                    }
                } else {
                    registerElasTestService(serviceDefJson);
                }
            }

        } catch (IOException e) {
            logger.error("Error registering a TSS.");
            throw e;
        }
    }

    private void registerElasTestService(ObjectNode serviceDefJson) {
        logger.debug("Registering the {} TSS.",
                ParserService.getNodeByElemChain(serviceDefJson,
                        Arrays.asList("register", "name")));

        supportServiceClient
                .registerService(serviceDefJson.get("register").toString());
        supportServiceClient.registerManifest("{ " + "\"id\": "
                + serviceDefJson.get("manifest").get("id").toString()
                + ", \"manifest_content\": "
                + serviceDefJson
                        .get("manifest").get("manifest_content").toString()
                + ", \"manifest_type\": "
                + (EpmService.etMasterSlaveMode ? "\"epm\""
                        : serviceDefJson.get("manifest").get("manifest_type")
                                .toString())
                + ", \"plan_id\": "
                + serviceDefJson.get("manifest").get("plan_id").toString()
                + ", \"service_id\": "
                + serviceDefJson.get("manifest").get("service_id").toString()
                + ", \"endpoints\": "
                + serviceDefJson.get("manifest").get("endpoints").toString()
                // + ", \"config\": " + config
                + " }",
                serviceDefJson.get("manifest").get("id").toString()
                        .replaceAll("\"", ""));
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
        SupportService[] servicesObj = supportServiceClient
                .getRegisteredServices();

        for (SupportService esmService : servicesObj) {
            services.add(esmService);
        }
        return services;
    }

    @Async
    public void provisionServiceInstanceAsync(String serviceId,
            String instanceId) {
        provisionServiceInstance(serviceId, instanceId);
    }

    public String provisionServiceInstanceSync(String serviceId) {
        String instanceId = UtilTools.generateUniqueId();
        provisionServiceInstance(serviceId, instanceId);
        return instanceId;
    }

    /* *** Provision With TJobExec *** */

    @Async
    public void provisionTJobExecServiceInstanceAsync(String serviceId,
            TJobExecution tJobExec, String instanceId) throws RuntimeException {
        provisionTJobExecServiceInstance(serviceId, tJobExec, instanceId);
    }

    public String provisionTJobExecServiceInstanceSync(String serviceId,
            TJobExecution tJobExec) {
        String serviceName = getServiceNameByServiceId(serviceId).toUpperCase();
        // If mode normal and is shared tss
        if (serviceName != null
                && execMode.equals(ElastestConstants.MODE_NORMAL)
                && tssLoadedOnInitMap.containsKey(serviceName)) {
            String tssInstanceId = tssLoadedOnInitMap.get(serviceName);

            if (serviceName.equals("EUS")) {
                this.registerTJobExecutionInEus(tssInstanceId, serviceName,
                        tJobExec);
            }

            SupportServiceInstance instance = servicesInstances
                    .get(tssInstanceId);
            instance.gettJobExecIdList().add(tJobExec.getId());
            tJobServicesInstances.put(tssInstanceId, instance);

            return tssInstanceId;
        }
        // Else start new Eus instance
        String instanceId = UtilTools.generateUniqueId();
        provisionTJobExecServiceInstance(serviceId, tJobExec, instanceId);
        return instanceId;

    }

    public void registerTJobExecutionInEus(String tssInstanceId,
            String serviceName, TJobExecution tJobExec) {
        if (servicesInstances.containsKey(tssInstanceId)) {
            String folderPath = this.getTJobExecFolderPath(tJobExec)
                    + serviceName.toLowerCase() + "/";
            EusExecutionData eusExecutionData = new EusExecutionData(tJobExec,
                    folderPath);
            String response = "";
            if (execMode.equals(ElastestConstants.MODE_NORMAL)) {
                response = eusWebDriverService
                        .registerExecution(
                                eusExecutionData.getAsExecutionData())
                        .getBody();
            } else {

                String eusApi = servicesInstances.get(tssInstanceId)
                        .getApiUrlIfExist();
                String etEusApiKey = "ET_EUS_API";
                if (!tJobExec.getEnvVars().containsKey(etEusApiKey)) {
                    tJobExec.getEnvVars().put("ET_EUS_API", eusApi);
                }
                String url = eusApi.endsWith("/") ? eusApi : eusApi + "/";
                url += "execution/register";

                // Register execution in EUS
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(
                        Collections.singletonList(MediaType.APPLICATION_JSON));

                HttpEntity<EusExecutionData> request = new HttpEntity<EusExecutionData>(
                        eusExecutionData, headers);

                response = restTemplate.postForObject(url, request,
                        String.class);
            }
            logger.debug("TJob Execution {} registered in EUS", response);
        }
    }

    public void unregisterTJobExecutionInEus(String tssInstanceId,
            String serviceName, TJobExecution tJobExec) {
        if (servicesInstances.containsKey(tssInstanceId)) {
            EusExecutionData eusExecutionData = new EusExecutionData(tJobExec,
                    "");

            if (execMode.equals(ElastestConstants.MODE_NORMAL)) {
                eusWebDriverService
                        .unregisterExecution(eusExecutionData.getKey());
            } else {
                String eusApi = servicesInstances.get(tssInstanceId)
                        .getApiUrlIfExist();

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
    public void provisionExternalTJobExecServiceInstanceAsync(String serviceId,
            ExternalTJobExecution exTJobExec, String instanceId) {
        provisionExternalTJobExecServiceInstance(serviceId, exTJobExec,
                instanceId);
    }

    public String provisionExternalTJobExecServiceInstanceSync(String serviceId,
            ExternalTJobExecution exTJobExec) {

        String serviceName = getServiceNameByServiceId(serviceId).toUpperCase();
        // If mode normal and is shared tss
        if (serviceName != null
                && execMode.equals(ElastestConstants.MODE_NORMAL)
                && tssLoadedOnInitMap.containsKey(serviceName)) {
            String tssInstanceId = tssLoadedOnInitMap.get(serviceName);

            if (serviceName.equals("EUS")) {
                this.registerExternalTJobExecutionInEus(tssInstanceId,
                        serviceName, exTJobExec);
            }

            SupportServiceInstance instance = servicesInstances
                    .get(tssInstanceId);
            instance.gettJobExecIdList().add(exTJobExec.getId());
            externalTJobServicesInstances.put(tssInstanceId, instance);

            return tssInstanceId;
        }
        // Else start new Eus instance

        String instanceId = UtilTools.generateUniqueId();
        provisionExternalTJobExecServiceInstance(serviceId, exTJobExec,
                instanceId);
        return instanceId;
    }

    public SupportServiceInstance createNewServiceInstance(String serviceId,
            Long executionId, String instanceId) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode service = supportServiceClient.getRawServiceById(serviceId);
        logger.info("Service instance: " + instanceId);
        List<ObjectNode> plans = Arrays.asList(mapper.readValue(
                service.get("plans").toString(), ObjectNode[].class));

        return new SupportServiceInstance(instanceId,
                service.get("id").toString().replaceAll("\"", ""),
                service.get("name").toString().replaceAll("\"", ""),
                // service.get("short_name").toString().replaceAll("\"",
                // ""),
                "", plans.get(0).get("id").toString().replaceAll("\"", ""),
                executionId != null ? Arrays.asList(executionId)
                        : new ArrayList<>());
    }

    public void provisionServiceInstance(String serviceId, String instanceId) {
        logger.info("Service id to provision: " + serviceId);
        SupportServiceInstance newServiceInstance = null;

        try {
            newServiceInstance = this.createNewServiceInstance(serviceId, null,
                    instanceId);

            this.setTSSFilesConfig(newServiceInstance);
            this.fillEnvVariablesToTSS(newServiceInstance);

            newServiceInstance = this.provisionServiceInstanceByObject(
                    newServiceInstance, instanceId);
            servicesInstances.put(instanceId, newServiceInstance);
        } catch (Exception e) {
            if (newServiceInstance != null) {
                deprovisionServiceInstance(newServiceInstance.getInstanceId(),
                        servicesInstances);
            }
            throw new RuntimeException(
                    "Exception requesting an instance of service \"" + serviceId
                            + "\"",
                    e);
        }

        if (newServiceInstance == null) {
            throw new RuntimeException(
                    "Service with name \"" + serviceId + "\" not found in ESM");
        }
    }

    /* *** Provision With TJobExec *** */
    public void provisionTJobExecServiceInstance(String serviceId,
            TJobExecution tJobExec, String instanceId) throws RuntimeException {
        logger.info("Service id to provision: " + serviceId);
        SupportServiceInstance newServiceInstance = null;

        try {
            Long execId = tJobExec != null && tJobExec.getId() != null
                    ? tJobExec.getId()
                    : null;
            newServiceInstance = this.createNewServiceInstance(serviceId,
                    execId, instanceId);

            this.setTJobExecTSSFilesConfig(newServiceInstance, tJobExec);
            this.fillTJobExecEnvVariablesToTSS(newServiceInstance, tJobExec);
            boolean tJobIsExternal = false;
            if (tJobExec != null && tJobExec.getTjob() != null) {
                tJobIsExternal = tJobExec.getTjob().isExternal();

            }
            this.fillEMSMonitoringEnvVariablesToTSS(newServiceInstance,
                    tJobExec, tJobIsExternal, false);

            if (tJobExec != null && execId != null) {
                tJobServicesInstances.put(instanceId, newServiceInstance);
                List<String> tSSIByTJobExecAssociatedList = tSSIByTJobExecAssociated
                        .get(execId) == null ? new ArrayList<>()
                                : tSSIByTJobExecAssociated.get(execId);
                tSSIByTJobExecAssociatedList
                        .add(newServiceInstance.getInstanceId());
                tSSIByTJobExecAssociated.put(execId,
                        tSSIByTJobExecAssociatedList);

                createExecFilesFolder(newServiceInstance);
            } else {
                servicesInstances.put(instanceId, newServiceInstance);
            }

            this.provisionServiceInstanceByObject(newServiceInstance,
                    instanceId);

        } catch (Exception e) {
            if (newServiceInstance != null) {
                deprovisionServiceInstance(newServiceInstance.getInstanceId(),
                        tJobExec != null && tJobExec.getId() != null
                                ? tJobServicesInstances
                                : servicesInstances);
            }
            throw new RuntimeException(
                    "Exception requesting an instance of service \"" + serviceId
                            + "\"",
                    e);
        }

        if (newServiceInstance == null) {
            throw new RuntimeException(
                    "Service with name \"" + serviceId + "\" not found in ESM");
        }
    }

    private void setTJobExecTSSFilesConfig(
            SupportServiceInstance supportServiceInstance,
            TJobExecution tJobExec) {

        if (tJobExec != null && tJobExec.getTjob() != null) {
            String fileSeparator = "/";
            supportServiceInstance.getParameters().put("ET_FILES_PATH",
                    this.getTJobExecFolderPath(tJobExec)
                            + supportServiceInstance.getServiceName()
                                    .toLowerCase()
                            + fileSeparator);
        }

    }

    public String getTJobExecFolderPath(TJobExecution tJobExec) {
        String fileSeparator = "/";
        String path = etSharedFolder + fileSeparator + tJobsFolder
                + fileSeparator + tJobFolderPrefix + tJobExec.getTjob().getId()
                + fileSeparator + tJobExecFolderPefix + tJobExec.getId()
                + fileSeparator;
        logger.info("TJob Workspace: {}", path);
        return path;

    }

    private void fillTJobExecEnvVariablesToTSS(
            SupportServiceInstance supportServiceInstance,
            TJobExecution tJobExec) throws ServiceException {

        if (tJobExec != null && tJobExec.getTjob() != null) {
            Long tJobId = tJobExec.getTjob().getId();
            Long tJobExecId = tJobExec.getId();
            this.fillTSSConfigEnvVarsByTJob(tJobExec.getTjob(),
                    supportServiceInstance);
            supportServiceInstance.getParameters().put("ET_TJOB_ID",
                    tJobId.toString());
            supportServiceInstance.getParameters().put("ET_TJOBEXEC_ID",
                    tJobExecId.toString());
            supportServiceInstance.getParameters().put("ET_MON_EXEC",
                    tJobExecId.toString());
            switch (supportServiceInstance.getServiceName()) {
            case "ESS":
                logger.debug("Set timeout for {} container",
                        supportServiceInstance.getServiceName());
                supportServiceInstance.getParameters()
                        .put("ESM_DOCKER_DELETE_TIMEOUT", "3600");
                break;
            case "EUS":
                logger.debug("Set timeout for {} container",
                        supportServiceInstance.getServiceName());
                supportServiceInstance.getParameters()
                        .put("ESM_DOCKER_DELETE_TIMEOUT", "120");
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
            Map<String, String> vars = tJob.getTJobTSSConfigEnvVars(
                    supportServiceInstance.getServiceName());
            if (vars != null) {
                supportServiceInstance.getParameters().putAll(vars);
            }
        } catch (Exception e) {
        }
    }

    /* *** Provision With ExternalTJobExec *** */
    public void provisionExternalTJobExecServiceInstance(String serviceId,
            ExternalTJobExecution exTJobExec, String instanceId) {
        logger.info("Service id to provision: " + serviceId);
        SupportServiceInstance newServiceInstance = null;

        try {
            Long execId = exTJobExec != null && exTJobExec.getId() != null
                    ? exTJobExec.getId()
                    : null;
            newServiceInstance = this.createNewServiceInstance(serviceId,
                    execId, instanceId);

            this.setExternalTJobExecTSSFilesConfig(newServiceInstance,
                    exTJobExec);
            fillExternalTJobExecEnvVariablesToTSS(newServiceInstance,
                    exTJobExec);

            if (exTJobExec != null && execId != null) {
                externalTJobServicesInstances.put(instanceId,
                        newServiceInstance);
                List<String> tSSIByExternalTJobExecAssociatedList = tSSIByExternalTJobExecAssociated
                        .get(execId) == null ? new ArrayList<>()
                                : tSSIByExternalTJobExecAssociated.get(execId);
                tSSIByExternalTJobExecAssociatedList
                        .add(newServiceInstance.getInstanceId());
                tSSIByExternalTJobExecAssociated.put(execId,
                        tSSIByExternalTJobExecAssociatedList);

                createExecFilesFolder(newServiceInstance);
            } else {
                servicesInstances.put(instanceId, newServiceInstance);
            }

            this.provisionServiceInstanceByObject(newServiceInstance,
                    instanceId);

        } catch (Exception e) {
            if (newServiceInstance != null) {
                deprovisionServiceInstance(newServiceInstance.getInstanceId(),
                        exTJobExec != null && exTJobExec.getId() != null
                                ? externalTJobServicesInstances
                                : servicesInstances);
            }
            throw new RuntimeException(
                    "Exception requesting an instance of service \"" + serviceId
                            + "\"",
                    e);
        }

        if (newServiceInstance == null) {
            throw new RuntimeException(
                    "Service with name \"" + serviceId + "\" not found in ESM");
        }
    }

    public void registerExternalTJobExecutionInEus(String tssInstanceId,
            String serviceName, ExternalTJobExecution exTJobExec) {
        if (servicesInstances.containsKey(tssInstanceId)) {
            String folderPath = this.getExternalTJobExecFolderPath(exTJobExec)
                    + serviceName.toLowerCase() + "/";
            EusExecutionData eusExecutionData = new EusExecutionData(exTJobExec,
                    folderPath);
            String response = "";
            if (execMode.equals(ElastestConstants.MODE_NORMAL)) {
                response = eusWebDriverService
                        .registerExecution(
                                eusExecutionData.getAsExecutionData())
                        .getBody();
            } else {
                String eusApi = servicesInstances.get(tssInstanceId)
                        .getApiUrlIfExist();

                String url = eusApi.endsWith("/") ? eusApi : eusApi + "/";
                url += "execution/register";

                // Register execution in EUS
                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setAccept(
                        Collections.singletonList(MediaType.APPLICATION_JSON));

                HttpEntity<EusExecutionData> request = new HttpEntity<EusExecutionData>(
                        eusExecutionData, headers);

                response = restTemplate.postForObject(url, request,
                        String.class);
            }
            logger.debug("External TJob Execution {} registered in EUS",
                    response);
        }
    }

    public void unregisterExternalTJobExecutionInEus(String tssInstanceId,
            String serviceName, ExternalTJobExecution exTJobExec) {
        if (servicesInstances.containsKey(tssInstanceId)) {
            EusExecutionData eusExecutionData = new EusExecutionData(exTJobExec,
                    "");

            if (execMode.equals(ElastestConstants.MODE_NORMAL)) {
                eusWebDriverService
                        .unregisterExecution(eusExecutionData.getKey());
            } else {
                String eusApi = servicesInstances.get(tssInstanceId)
                        .getApiUrlIfExist();

                String url = eusApi.endsWith("/") ? eusApi : eusApi + "/";
                url += "execution/unregister/" + eusExecutionData.getKey();

                // Unregister execution in EUS
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.delete(url);
            }
        }
    }

    private void setExternalTJobExecTSSFilesConfig(
            SupportServiceInstance supportServiceInstance,
            ExternalTJobExecution exTJobExec) {
        if (exTJobExec != null && exTJobExec.getExTJob() != null) {
            String fileSeparator = "/";
            supportServiceInstance.getParameters().put("ET_FILES_PATH",
                    this.getExternalTJobExecFolderPath(exTJobExec)
                            + supportServiceInstance.getServiceName()
                                    .toLowerCase()
                            + fileSeparator);
        }
    }

    public String getExternalTJobExecFolderPath(
            ExternalTJobExecution exTJobExec) {
        String fileSeparator = "/";
        return etSharedFolder + fileSeparator + externalTJobsFolder
                + fileSeparator + externalTJobFolderPrefix
                + exTJobExec.getExTJob().getId() + fileSeparator
                + externalTJobExecFolderPefix + exTJobExec.getId()
                + fileSeparator;

    }

    private void fillExternalTJobExecEnvVariablesToTSS(
            SupportServiceInstance supportServiceInstance,
            ExternalTJobExecution exTJobExec) throws ServiceException {

        if (exTJobExec != null && exTJobExec.getExTJob() != null) {
            Long exTJobId = exTJobExec.getExTJob().getId();
            Long exTJobExecId = exTJobExec.getId();

            supportServiceInstance.getParameters().put("ET_TJOB_ID",
                    exTJobId.toString());
            supportServiceInstance.getParameters().put("ET_TJOBEXEC_ID",
                    exTJobExecId.toString());

            // Puts only Exec Monitoring index (without sut)
            supportServiceInstance.getParameters().put("ET_MON_EXEC",
                    exTJobExec.getExternalTJobExecMonitoringIndex());

            switch (supportServiceInstance.getServiceName()) {
            case "ESS":
                supportServiceInstance.getParameters()
                        .put("ESM_DOCKER_DELETE_TIMEOUT", "120");
                break;
            case "EUS":
                supportServiceInstance.getParameters()
                        .put("ESM_DOCKER_DELETE_TIMEOUT", "120");
                break;
            default:
                logger.debug("Container Timeout not defined");
            }
        }
        this.fillEnvVariablesToTSS(supportServiceInstance);
    }

    public SupportServiceInstance provisionServiceInstanceByObject(
            SupportServiceInstance newServiceInstance, String instanceId)
            throws Exception {
        supportServiceClient.provisionServiceInstance(newServiceInstance,
                instanceId, Boolean.toString(false));

        logger.info("Get registered all data of a service.");
        newServiceInstance = supportServiceClient
                .getServiceInstanceInfo(newServiceInstance);

        newServiceInstance = buildTssInstanceUrls(newServiceInstance);

        return newServiceInstance;
    }

    private SupportServiceInstance buildTssInstanceUrls(
            SupportServiceInstance serviceInstance) throws Exception {
        TssManifest manifest = supportServiceClient
                .getManifestById(serviceInstance.getManifestId());
        JsonNode manifestEndpoints = manifest.getEndpoints();

        Iterator<String> subServicesNames = manifestEndpoints.fieldNames();
        String serviceName = subServicesNames.next();

        JsonNode manifestEndpointService = manifestEndpoints.get(serviceName);
        JsonNode manifestEndpointServiceApi = manifestEndpointService
                .get("api");
        JsonNode manifestEndpointServiceGui = manifestEndpointService
                .get("gui");

        if (serviceInstance.getContainerIp() == null) {
            throw new Exception(
                    "Field ip not found for " + serviceName + " instance.");
        } else {

            String networkName = etDockerNetwork;
            logger.info("Network name: " + networkName);

            SupportServiceInstance auxServiceInstance = null;

            if (manifestEndpointService.get("main") != null
                    && manifestEndpointService.get("main").booleanValue()) {
                logger.info("Principal instance {}:" + serviceName);
                auxServiceInstance = serviceInstance;
            } else {
                auxServiceInstance = new SupportServiceInstance();
                auxServiceInstance.setEndpointName(serviceName);
                auxServiceInstance
                        .setContainerIp(serviceInstance.getContainerIp());
                auxServiceInstance.setServiceIp(serviceInstance.getServiceIp());
                auxServiceInstance
                        .setParameters(serviceInstance.getParameters());
                serviceInstance.getSubServices().add(auxServiceInstance);
            }

            auxServiceInstance.setEndpointName(serviceName);

            try {
                String tssContainerName = serviceInstance.getContainerName();

                if (manifestEndpointServiceApi != null) {
                    if (!manifestEndpointServiceApi.isArray()) {
                        getEndpointsInfo(auxServiceInstance,
                                manifestEndpointServiceApi, tssContainerName,
                                networkName, "api");
                    } else {
                        for (final JsonNode apiNode : manifestEndpointServiceApi) {
                            getEndpointsInfo(auxServiceInstance, apiNode,
                                    tssContainerName, networkName,
                                    apiNode.get("name") != null
                                            ? apiNode.get("name").toString()
                                                    .replaceAll("\"", "")
                                                    + "api"
                                            : "api");
                        }
                    }
                }

                if (manifestEndpointServiceGui != null) {
                    if (!manifestEndpointServiceGui.isArray()) {
                        getEndpointsInfo(auxServiceInstance,
                                manifestEndpointServiceGui, tssContainerName,
                                networkName, "gui");
                    } else {
                        for (final JsonNode guiNode : manifestEndpointServiceGui) {
                            getEndpointsInfo(auxServiceInstance, guiNode,
                                    tssContainerName, networkName,
                                    guiNode.get("name") != null
                                            ? guiNode.get("name").toString()
                                                    .replaceAll("\"", "")
                                                    + "gui"
                                            : "gui");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error building endpoints info: {}",
                        e.getMessage());
                throw new Exception(
                        "Error building endpoints info: " + e.getMessage());
            }
        }
        return serviceInstance;
    }

    private SupportServiceInstance getEndpointsInfo(
            SupportServiceInstance serviceInstance, JsonNode node,
            String tSSContainerName, String networkName, String nodeName)
            throws Exception {
        int auxPort = 37000;

        if (node != null) {
            if (!etPublicHost.equals("localhost")) {
                if (node.get("port") != null) {
                    String nodePort = node.get("port").toString()
                            .replaceAll("\"", "");
                    if (serviceInstance.getEndpointsBindingsPorts()
                            .containsKey(nodePort)) {
                        auxPort = Integer.parseInt(serviceInstance
                                .getEndpointsBindingsPorts().get(nodePort));
                    } else {
                        try {
                            SocatBindedPort socatBindedPortObj = dockerEtmService
                                    .bindingPort(
                                            serviceInstance.getContainerIp(),
                                            node.get("port").toString(),
                                            networkName,
                                            epmService.etMasterSlaveMode);
                            serviceInstance.getPortBindingContainers()
                                    .add(socatBindedPortObj.getBindedPort());
                            auxPort = Integer.parseInt(
                                    socatBindedPortObj.getListenPort());
                            serviceInstance.getEndpointsBindingsPorts()
                                    .put(nodePort, String.valueOf(auxPort));
                        } catch (Exception e) {
                            logger.error("Ports binding fails: {} ",
                                    e.getMessage());
                            throw new Exception(
                                    "Ports binding fails: " + e.getMessage());
                        }

                    }
                }

                ((ObjectNode) node).put("port", auxPort);

                if (node.get("protocol") != null && (node.get("protocol")
                        .toString().contains("http")
                        || node.get("protocol").toString().contains("https")
                        || node.get("protocol").toString().contains("ws"))) {
                    serviceInstance.setServicePort(auxPort);
                    serviceInstance.getUrls().put(nodeName,
                            createServiceInstanceUrl(node,
                                    serviceInstance.getServiceIp()));
                }
            } else if (node.get("port") != null && node.get("protocol") != null
                    && (node.get("protocol").toString().contains("http")
                            || node.get("protocol").toString().contains("https")
                            || node.get("protocol").toString()
                                    .contains("ws"))) {
                serviceInstance.setServicePort(Integer.parseInt(
                        node.get("port").toString().replaceAll("\"", "")));
                serviceInstance.getUrls().put(nodeName,
                        createServiceInstanceUrl(node,
                                serviceInstance.getServiceIp()));
            }
            serviceInstance.getEndpointsData().put(nodeName, node);
        }
        return serviceInstance;
    }

    private String createServiceInstanceUrl(JsonNode node, String ip) {
        String url = null;

        url = node.get("protocol").toString().replaceAll("\"", "") + "://" + ip
                + ":" + node.get("port").toString().replaceAll("\"", "")
                + (node.get("path") != null
                        ? node.get("path").toString().replaceAll("\"", "")
                        : "/");
        logger.info("New url: " + url);
        return url;
    }

    public void deprovisionServicesInstances() {
        tJobServicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            deprovisionServiceInstance(tSSInstanceId, tJobServicesInstances);
        });

        tJobServicesInstances = null;

        externalTJobServicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            deprovisionServiceInstance(tSSInstanceId,
                    externalTJobServicesInstances);
        });

        externalTJobServicesInstances = null;

        servicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            // If is not EUS in normal mode, deprovision
            if (!isIntegratedEUS(tSSInstance)) {
                deprovisionServiceInstance(tSSInstanceId, servicesInstances);
            }
        });

        servicesInstances = null;

    }

    public boolean isIntegratedEUS(String serviceName, String instanceId) {
        return execMode.equals(ElastestConstants.MODE_NORMAL)
                && "EUS".equals(serviceName)
                && tssLoadedOnInitMap.containsKey(serviceName)
                && tssLoadedOnInitMap.containsValue(instanceId);
    }

    public boolean isIntegratedEUS(SupportServiceInstance instance) {
        return this.isIntegratedEUS(instance.getServiceName(),
                instance.getInstanceId());
    }

    public String deprovisionTJobExecServiceInstance(String instanceId,
            Long tJobExecId) {
        tSSIByTJobExecAssociated.remove(tJobExecId);

        SupportServiceInstance tssInstance = tJobServicesInstances
                .get(instanceId);
        String serviceName = tssInstance != null
                ? tssInstance.getServiceName().toUpperCase()
                : null;
        // If mode normal and is shared tss
        if (serviceName != null
                && execMode.equals(ElastestConstants.MODE_NORMAL)
                && tssLoadedOnInitMap.containsKey(serviceName)) {

            if (serviceName.equals("EUS")) {
                TJobExecution tJobExec = tJobExecRepositoryImpl
                        .findById(tJobExecId).get();
                this.unregisterTJobExecutionInEus(instanceId, serviceName,
                        tJobExec);
                int index = tssInstance.gettJobExecIdList().indexOf(tJobExecId);
                if (index >= 0) {
                    tssInstance.gettJobExecIdList().remove(index);
                }
            }
            return "Instance not deleted: is shared. Finished!";
        } else {

            return deprovisionServiceInstance(instanceId,
                    tJobServicesInstances);
        }
    }

    public String deprovisionExternalTJobExecServiceInstance(String instanceId,
            Long externalTJobExecId) {
        tSSIByExternalTJobExecAssociated.remove(externalTJobExecId);

        SupportServiceInstance tssInstance = externalTJobServicesInstances
                .get(instanceId);

        String serviceName = tssInstance != null
                ? tssInstance.getServiceName().toUpperCase()
                : null;
        // If mode normal and is shared tss
        if (serviceName != null
                && execMode.equals(ElastestConstants.MODE_NORMAL)
                && tssLoadedOnInitMap.containsKey(serviceName)) {

            if (serviceName.equals("EUS")) {
                ExternalTJobExecution exTJobExec = externalTJobExecutionRepository
                        .findById(externalTJobExecId).get();
                this.unregisterExternalTJobExecutionInEus(instanceId,
                        serviceName, exTJobExec);
                int index = tssInstance.gettJobExecIdList()
                        .indexOf(externalTJobExecId);
                if (index >= 0) {
                    tssInstance.gettJobExecIdList().remove(index);
                }
            }
            return "Instance not deleted: is shared. Finished!";
        } else {
            return deprovisionServiceInstance(instanceId,
                    externalTJobServicesInstances);
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
            serviceInstance.setServiceReady(false);

            for (String containerId : serviceInstance
                    .getPortBindingContainers()) {
                try {
                    dockerEtmService.dockerService
                            .stopDockerContainer(containerId);
                    dockerEtmService.dockerService
                            .removeDockerContainer(containerId);
                } catch (Exception e) {
                    logger.error("Error on stop and remove container {}",
                            containerId, e);
                }
            }

            for (SupportServiceInstance subServiceInstance : serviceInstance
                    .getSubServices()) {
                for (String containerId : subServiceInstance
                        .getPortBindingContainers()) {
                    try {
                        dockerEtmService.dockerService
                                .stopDockerContainer(containerId);
                        dockerEtmService.dockerService
                                .removeDockerContainer(containerId);
                    } catch (Exception e) {
                        logger.error("Error on stop and remove container {}",
                                containerId, e);
                    }
                }
            }
            supportServiceClient.deprovisionServiceInstance(instanceId,
                    serviceInstance);
        }
        ssiMap.remove(instanceId);
        return result;
    }

    public boolean isInstanceUp(String instanceId) {
        boolean result = false;
        try {
            result = supportServiceClient.getServiceInstanceInfo(instanceId)
                    .toString().equals("{}") ? false : true;
            logger.info("Check instance status:{}", instanceId, ". Info: {}",
                    result);
            result = true;
        } catch (RuntimeException re) {
            logger.info("Error cause: {}", re.getCause().getMessage());
            if (re.getCause().getMessage().contains("404")) {
                logger.info("Check instance status:{}", instanceId,
                        ". Info: {}", result);
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

    public SupportServiceInstance getExternalTJobExecServiceInstance(
            String id) {
        return this.getServiceInstanceBySIMap(id,
                externalTJobServicesInstances);
    }

    public SupportServiceInstance getServiceInstanceBySIMap(String id,
            Map<String, SupportServiceInstance> ssiMap) {
        SupportServiceInstance tss = ssiMap.get(id);
        if (tss != null) {
            tss.setServiceReady(checkInstanceUrlIsUp(tss));
        }

        return tss;
    }

    public List<SupportServiceInstance> getServicesInstancesAsList() {
        ArrayList<SupportServiceInstance> servicesInstancesList = new ArrayList<>();
        servicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            if (!this.isIntegratedEUS(tSSInstance)) {
                boolean isUp = checkInstanceUrlIsUp(tSSInstance);
                tSSInstance.setServiceReady(isUp);

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

    public Map<String, SupportServiceInstance> getServicesInstances() {
        return servicesInstances;
    }

    public void setServicesInstances(
            Map<String, SupportServiceInstance> servicesInstances) {
        this.servicesInstances = servicesInstances;
    }

    public Map<String, SupportServiceInstance> gettJobServicesInstances() {
        return tJobServicesInstances;
    }

    public void settJobServicesInstances(
            Map<String, SupportServiceInstance> tJobsServicesInstances) {
        this.tJobServicesInstances = tJobsServicesInstances;
    }

    public List<SupportServiceInstance> getTSSInstByTJobExecId(
            Long tJobExecId) {
        logger.debug("Get ready TSS by TJobExecId {}", tJobExecId);
        List<SupportServiceInstance> tSSInstanceList = new ArrayList<>();
        tJobServicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            if (tSSInstance.gettJobExecIdList().contains(tJobExecId.longValue())
                    && checkInstanceUrlIsUp(tSSInstance)) {
                tSSInstanceList.add(tSSInstance);
            }
        });

        return tSSInstanceList;
    }

    public boolean checkInstanceUrlIsUp(SupportServiceInstance tSSInstance) {
        boolean up = false;
        int responseCode = 0;
        if (tSSInstance != null) {
            if (tSSInstance.getUrls() != null
                    && !tSSInstance.getUrls().isEmpty()) {
                // First check if api status url exist (for integrated EUS)
                String urlValue = tSSInstance.getApiStatusUrlIfExist();
                if (urlValue == null) {
                    tSSInstance.getApiUrlIfExist();
                }

                up = true;
                if (urlValue != null) {
                    try {
                        logger.debug(tSSInstance.getServiceName()
                                + " Service URL: " + urlValue);

                        up = up && UtilTools.checkIfUrlIsUp(urlValue);
                        logger.debug(tSSInstance.getServiceName()
                                + " Service response: " + responseCode);

                        if (!up) {
                            logger.debug(tSSInstance.getServiceName()
                                    + " Service is not ready.");
                            return up;
                        }
                    } catch (Exception e) {
                        logger.debug(tSSInstance.getServiceName()
                                + " Service is not ready by exception error.");
                        return false;
                    }
                }
            }
            String checklMessage = up
                    ? tSSInstance.getServiceName() + " Service is ready."
                    : tSSInstance.getServiceName()
                            + " Service is not ready yet.";

            logger.info(checklMessage);
        }

        return up;

    }

    public List<TJobExecutionFile> getTJobExecutionFilesUrls(Long tJobId,
            Long tJobExecId) throws InterruptedException {
        logger.info("Retrived the files generated by the TJob execution: {}",
                tJobExecId);

        String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
        String tJobExecFilePath = tJobsFolder + fileSeparator + tJobFolderPrefix
                + tJobId + fileSeparator + tJobExecFolderPefix + tJobExecId
                + fileSeparator;

        List<TJobExecutionFile> filesList = null;
        try {
            filesList = this.getFilesUrls(fileSeparator, tJobExecFilePath);
        } catch (IOException fnfe) {
            logger.warn("Error building the URLs of the execution files {}",
                    tJobExecId);
        }
        return filesList;
    }

    public List<TJobExecutionFile> getExternalTJobExecutionFilesUrls(
            Long exTJobId, Long exTJobExecId) throws InterruptedException {
        logger.info("Retrived the files generated by the TJob execution: {}",
                exTJobExecId);

        String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
        String tJobExecFilePath = externalTJobsFolder + fileSeparator
                + externalTJobFolderPrefix + exTJobId + fileSeparator
                + externalTJobExecFolderPefix + exTJobExecId + fileSeparator;

        List<TJobExecutionFile> filesList = null;
        try {
            filesList = this.getFilesUrls(fileSeparator, tJobExecFilePath);
        } catch (IOException fnfe) {
            logger.warn("Error building the URLs of the execution files {}",
                    exTJobExecId);
        }
        return filesList;
    }

    public List<TJobExecutionFile> getFilesUrls(String fileSeparator,
            String tJobExecFilePath) throws InterruptedException, IOException {
        List<TJobExecutionFile> filesList = new ArrayList<TJobExecutionFile>();

        String tJobExecFolder = sharedFolder.endsWith(fileSeparator)
                ? sharedFolder
                : sharedFolder + fileSeparator;
        tJobExecFolder += tJobExecFilePath;
        logger.debug("Shared folder: " + tJobExecFolder);

        File file = ResourceUtils.getFile(tJobExecFolder);

        if (file.exists()) {
            List<String> servicesFolders = new ArrayList<>(
                    Arrays.asList(file.list()));
            for (String serviceFolderName : servicesFolders) {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ie) {
                    logger.error("Thread sleep fail");
                    throw ie;
                }
                logger.debug("Files folder:" + serviceFolderName);
                String fullPathFolder = tJobExecFolder + serviceFolderName;
                logger.debug("Full path:" + fullPathFolder);
                File serviceFolder = ResourceUtils.getFile(fullPathFolder);

                filesList.addAll(this.getFilesByFolder(serviceFolder,
                        tJobExecFilePath + serviceFolderName + fileSeparator,
                        serviceFolderName, fileSeparator));
            }
        }

        return filesList;
    }

    public List<TJobExecutionFile> getFilesByFolder(File folder,
            String relativePath, String serviceName, String fileSeparator)
            throws IOException {
        String absolutePath = sharedFolder + relativePath;
        if (sharedFolder.endsWith("/") && relativePath.startsWith("/")) {
            absolutePath = sharedFolder + relativePath.replaceFirst("/", "");
        } else {
            if (!sharedFolder.endsWith("/") && !relativePath.startsWith("/")) {
                absolutePath = sharedFolder + "/" + relativePath;
            }
        }
        List<TJobExecutionFile> filesList = new ArrayList<TJobExecutionFile>();

        List<String> folderFilesNames = new ArrayList<>(
                Arrays.asList(folder.list()));

        for (String currentFileName : folderFilesNames) {
            String absoluteFilePath = absolutePath + currentFileName;
            String relativeFilePath = relativePath + currentFileName;
            File currentFile = ResourceUtils.getFile(absoluteFilePath);
            if (currentFile.isDirectory()) {
                filesList.addAll(this.getFilesByFolder(currentFile,
                        relativeFilePath + fileSeparator, serviceName,
                        fileSeparator));
            } else {
                filesList.add(new TJobExecutionFile(currentFileName,
                        getFileUrl(relativeFilePath), serviceName));
            }
        }
        return filesList;
    }

    public String getFileUrl(String serviceFilePath) throws IOException {
        String urlResponse = contextPath.replaceFirst("/", "")
                + registryContextPath + "/"
                + serviceFilePath.replace("\\\\", "/");
        return urlResponse;
    }

    private void createExecFilesFolder(
            SupportServiceInstance supportServiceInstance) {
        logger.info("try to create folder.");
        File folderStructure = new File(
                supportServiceInstance.getParameters().get("ET_FILES_PATH"));

        try {
            if (!folderStructure.exists()) {
                logger.info("creating folder at {}.",
                        folderStructure.getAbsolutePath());
                folderStructure.mkdirs();
                logger.info("Folder created.");
            }
        } catch (Exception e) {
            logger.error("File does not created.");
            throw e;
        }
    }

    private void setTSSFilesConfig(
            SupportServiceInstance supportServiceInstance) {
        if (execMode.equals(ElastestConstants.MODE_NORMAL)) {
            String fileSeparator = "/";
            supportServiceInstance.getParameters().put("ET_FILES_PATH",
                    etSharedFolder
                            + fileSeparator + supportServiceInstance
                                    .getServiceName().toLowerCase()
                            + fileSeparator);
        }
    }

    private void fillEnvVariablesToTSS(
            SupportServiceInstance supportServiceInstance)
            throws ServiceException {

        if (epmService.etMasterSlaveMode) {
            supportServiceInstance.getParameters().put("pop_name", epmService
                    .getPopName(epmService.getRe().getHostIp(), "compose"));
        }

        supportServiceInstance.getParameters()
                .putAll(etmContextAuxService.getMonitoringEnvVars(true));

        supportServiceInstance.getParameters().put("ET_ETM_LSTCP_HOST",
                etEtmLstcpHost);
        supportServiceInstance.getParameters().put("ET_ETM_LSTCP_PORT",
                etEtmLstcpPort);

        supportServiceInstance.getParameters().put("ET_CONTEXT_API",
                "http://" + UtilTools.getMyIp() + ":" + serverPort
                        + "/api/context/tss/"
                        + supportServiceInstance.getInstanceId());

        supportServiceInstance.getParameters().put("ET_INTERNET_DISABLED",
                etInternetDisabled);
        supportServiceInstance.getParameters().put("ET_PUBLIC_HOST",
                etPublicHost);
        supportServiceInstance.getParameters().put("ET_EDM_ALLUXIO_API",
                etEdmAlluxioApi);
        supportServiceInstance.getParameters().put("ET_EDM_MYSQL_HOST",
                etEdmMysqlHost);
        supportServiceInstance.getParameters().put("ET_EDM_MYSQL_PORT",
                etEdmMysqlPort);
        supportServiceInstance.getParameters().put("ET_EDM_ELASTICSEARCH_API",
                etEdmElasticsearchApi);
        supportServiceInstance.getParameters().put("ET_EDM_API", etEdmApi);
        supportServiceInstance.getParameters().put("ET_EPM_API", etEpmApi);
        supportServiceInstance.getParameters().put("ET_ETM_API", etEtmApi);
        supportServiceInstance.getParameters().put("ET_ESM_API", etEsmApi);
        supportServiceInstance.getParameters().put("ET_EIM_API", etEimApi);

        supportServiceInstance.getParameters().put("ET_ETM_LSBEATS_HOST",
                etEtmLsbeatsHost);
        supportServiceInstance.getParameters().put("ET_ETM_LSBEATS_PORT",
                etEtmLsbeatsPort);
        supportServiceInstance.getParameters().put("ET_ETM_LSHTTP_API",
                etEtmLshttpApi);

        supportServiceInstance.getParameters().put("ET_ETM_BINDED_LSBEATS_HOST",
                etEtmBindedLsBeatsHost);
        supportServiceInstance.getParameters().put("ET_ETM_BINDED_LSBEATS_PORT",
                etEtmBindedLsBeatsPort);
        supportServiceInstance.getParameters().put("ET_ETM_BINDED_LSTCP_HOST",
                etEtmBindedLsTcpHost);
        supportServiceInstance.getParameters().put("ET_ETM_BINDED_LSTCP_PORT",
                etEtmBindedLsTcpPort);

        supportServiceInstance.getParameters().put("ET_ETM_RABBIT_HOST",
                etEtmRabbitHost);
        supportServiceInstance.getParameters().put("ET_ETM_RABBIT_PORT",
                etEtmRabbitPort);
        supportServiceInstance.getParameters().put("ET_EMP_API", etEmpApi);
        supportServiceInstance.getParameters().put("ET_EMP_INFLUXDB_API",
                etEmpInfluxdbApi);
        supportServiceInstance.getParameters().put("ET_EMP_INFLUXDB_HOST",
                etEmpInfluxdbHost);
        supportServiceInstance.getParameters().put(
                "ET_EMP_INFLUXDB_GRAPHITE_PORT", etEmpInfluxdbGraphitePort);

        supportServiceInstance.getParameters().put("USE_TORM", "true");
    }

    private void fillEMSMonitoringEnvVariablesToTSS(
            SupportServiceInstance supportServiceInstance,
            TJobExecution tJobExec, boolean externalTJob,
            boolean withPublicPrefix) {
        try {
            if (tJobExec != null && tJobExec.getTjob() != null) {
                if (tJobExec.getTjob().isEmsTssSelected()) {
                    // IF EMS is started, TJobExec have EMS env vars
                    Map<String, String> emsEnvVars = tJobExec.getEnvVars();

                    supportServiceInstance.getParameters()
                            .putAll(etmContextAuxService
                                    .getMonitoringEnvVarsFromEms(emsEnvVars));
                }
            }
        } catch (Exception e) {
            logger.error("Error on fill EMS monitoring env vars to TSS", e);
        }
    }

    public Map<String, String> getTSSInstanceContext(String tSSInstanceId,
            boolean publicEnvVars, boolean withPublicPrefix) {
        Map<String, String> tSSInstanceContextMap = new HashMap<>();
        SupportServiceInstance ssi = null;
        if (servicesInstances.get(tSSInstanceId) != null) {
            ssi = servicesInstances.get(tSSInstanceId);
        } else if (tJobServicesInstances.get(tSSInstanceId) != null) {
            ssi = tJobServicesInstances.get(tSSInstanceId);
        } else {
            return null;
        }

        tSSInstanceContextMap.putAll(
                getTSSInstanceEnvVars(ssi, publicEnvVars, withPublicPrefix));
        tSSInstanceContextMap.putAll(ssi.getParameters());

        return tSSInstanceContextMap;
    }

    public Map<String, String> getTSSInstanceEnvVars(SupportServiceInstance ssi,
            boolean publicEnvVars, boolean withPublicPrefix) {
        Map<String, String> envVars = new HashMap<String, String>();
        String servicePrefix = ssi.getServiceName().toUpperCase()
                .replaceAll("-", "_");
        String envVarNamePrefix = withPublicPrefix ? "ET_PUBLIC" : "ET";

        for (Map.Entry<String, JsonNode> entry : ssi.getEndpointsData()
                .entrySet()) {
            String prefix = withPublicPrefix ? envVarNamePrefix
                    : envVarNamePrefix + "_" + servicePrefix;
            envVars.putAll(
                    setTssEnvVarByEndpoint(ssi, prefix, entry, publicEnvVars));
        }

        for (SupportServiceInstance subssi : ssi.getSubServices()) {
            String envNamePrefixSubSSI = envVarNamePrefix + "_" + servicePrefix
                    + "_" + subssi.getEndpointName().toUpperCase()
                            .replaceAll("-", "_");

            for (Map.Entry<String, JsonNode> entry : subssi.getEndpointsData()
                    .entrySet()) {
                envVars.putAll(setTssEnvVarByEndpoint(subssi,
                        envNamePrefixSubSSI, entry, publicEnvVars));
            }
        }

        return envVars;
    }

    private Map<String, String> setTssEnvVarByEndpoint(
            SupportServiceInstance ssi, String prefix,
            Map.Entry<String, JsonNode> entry, boolean publicEnvVars) {
        Map<String, String> envVars = new HashMap<>();
        if (!entry.getKey().toLowerCase().contains("gui")) {
            try {
                if (entry.getValue().get("name") != null) {
                    prefix += "_" + entry.getValue().get("name").toString()
                            .toUpperCase().replaceAll("\"", "")
                            .replaceAll("-", "_");
                }

                String envNameHost = prefix + "_HOST";
                String envValueHost = publicEnvVars ? ssi.getServiceIp()
                        : ssi.getContainerIp();

                String envNamePort = prefix + "_PORT";

                String envValuePort = "";
                if (etPublicHost.equals("localhost")) {
                    envValuePort = entry.getValue().get("port").toString();
                } else {
                    for (Map.Entry<String, String> endpointBindingPort : ssi
                            .getEndpointsBindingsPorts().entrySet()) {
                        logger.debug("Binding port Key: {}",
                                endpointBindingPort.getKey());
                        logger.debug("Binding port value: {}",
                                endpointBindingPort.getValue());
                        logger.debug("Node port: {}",
                                entry.getValue().get("port").toString());
                        if (endpointBindingPort.getValue().equals(
                                entry.getValue().get("port").toString())) {
                            envValuePort = !publicEnvVars
                                    ? endpointBindingPort.getKey()
                                    : endpointBindingPort.getValue();
                        }
                    }
                }

                String protocol = entry.getValue().findValue("protocol")
                        .toString().toLowerCase().replaceAll("\"", "");
                if (protocol.equals("http") || protocol.equals("https")
                        || protocol.equals("ws")) {
                    String envNameAPI = prefix + "_API";
                    JsonNode pathNode = entry.getValue().get("path");
                    String path = "/";
                    if (pathNode != null) {
                        path = pathNode.toString().replaceAll("\"", "");
                    }
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }

                    String envValueAPI = protocol + "://" + envValueHost + ":"
                            + envValuePort + path;
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

    public Map<Long, List<String>> gettSSIByTJobExecAssociated() {
        return tSSIByTJobExecAssociated;
    }

    public void settSSIByTJobExecAssociated(
            Map<Long, List<String>> tSSIByTJobExecAssociated) {
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
        // If mode normal and is shared tss
        if (serviceName != null
                && execMode.equals(ElastestConstants.MODE_NORMAL)
                && tssLoadedOnInitMap.containsKey(serviceName.toUpperCase())) {
            return tssLoadedOnInitMap.get(serviceName.toUpperCase());
        }
        return null;
    }

    public SupportServiceInstance getSharedTssInstance(String instanceId) {
        // If mode normal and is shared tss
        if (instanceId != null && execMode.equals(ElastestConstants.MODE_NORMAL)
                && servicesInstances.containsKey(instanceId)) {
            return servicesInstances.get(instanceId);
        }
        return null;
    }
}