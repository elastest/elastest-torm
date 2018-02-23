package io.elastest.etm.service;

import static org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.model.SocatBindedPort;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.service.client.EsmServiceClient;
import io.elastest.etm.utils.ElastestConstants;
import io.elastest.etm.utils.FilesService;
import io.elastest.etm.utils.ParserService;
import io.elastest.etm.utils.UtilTools;

@Service
public class EsmService {
    private static final Logger logger = LoggerFactory
            .getLogger(EsmService.class);

    @Value("${et.esm.ss.desc.files.path}")
    public String etEsmSsDescFilesPath;
    @Value("${et.shared.folder}")
    public String etSharedFolder;
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
    @Value("${et.etm.lstcp.host}")
    public String etEtmLstcpHost;
    @Value("${et.etm.lstcp.port}")
    public String etEtmLstcpPort;

    @Value("${exec.mode}")
    public String execMode;

    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Value("${registry.contextPath}")
    private String registryContextPath;
    @Value("${et.shared.folder}")
    private String sharedFolder;

    public EsmServiceClient esmServiceClient;
    public DockerService2 dockerService;
    public UtilTools utilTools;
    public FilesService filesServices;
    private Map<String, SupportServiceInstance> servicesInstances;
    private Map<String, SupportServiceInstance> tJobServicesInstances;
    private Map<String, SupportServiceInstance> externalTJobServicesInstances;
    private Map<Long, List<String>> tSSIByTJobExecAssociated;
    private Map<Long, List<String>> tSSIByExternalTJobExecAssociated;
    private List<String> tSSIdLoadedOnInit;
    private List<String> tSSNameLoadedOnInit;

    private String tJobsFolder = "tjobs";
    private String tJobFolderPrefix = "tjob_";
    private String tJobExecFolderPefix = "exec_";

    private String externalTJobsFolder = "external_tjobs";
    private String externalTJobFolderPrefix = "external_tjob_";
    private String externalTJobExecFolderPefix = "external_exec_";

    public EsmService(EsmServiceClient esmServiceClient, UtilTools utilTools,
            DockerService2 dockerService, FilesService filesServices) {
        logger.info("EsmService constructor.");
        this.esmServiceClient = esmServiceClient;
        this.utilTools = utilTools;
        this.servicesInstances = new ConcurrentHashMap<>();
        this.tJobServicesInstances = new HashMap<>();
        this.externalTJobServicesInstances = new HashMap<>();
        this.tSSIByTJobExecAssociated = new HashMap<>();
        this.tSSIByExternalTJobExecAssociated = new HashMap<>();
        this.dockerService = dockerService;
        this.tSSIdLoadedOnInit = new ArrayList<>();
        this.tSSNameLoadedOnInit = new ArrayList<>(Arrays.asList("EUS"));
        this.filesServices = filesServices;
    }

    @PostConstruct
    public void init() {
        logger.info("EsmService initialization.");
        try {
            registerElastestServices();
            if (execMode.equals(ElastestConstants.MODE_NORMAL)) {
                tSSIdLoadedOnInit.forEach((serviceId) -> {
                    provisionServiceInstanceSync(serviceId);
                    logger.debug("EUS is started from ElasTest in normal mode");
                });

            }
        } catch (Exception e) {
            logger.warn("Error during the services registry. ");
        }
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
                if (execMode.equals("normal")) {
                    logger.debug("TSS file {}", serviceFile.getName());
                    if (tSSNameLoadedOnInit
                            .contains(serviceFile.getName().split("-")[0]
                                    .toUpperCase())) {
                        logger.debug(
                                "TSS {} will be registered in normal mode.",
                                serviceFile.getName().split("-")[0]
                                        .toUpperCase());
                        registerElasTestService(serviceDefJson);
                        tSSIdLoadedOnInit.add(serviceDefJson.get("register")
                                .get("id").toString().replaceAll("\"", ""));
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
        JsonNode configJson = serviceDefJson.get("manifest") != null
                ? serviceDefJson.get("manifest").get("config") : null;
        String config = configJson != null ? serviceDefJson.toString() : "";

        esmServiceClient
                .registerService(serviceDefJson.get("register").toString());
        esmServiceClient.registerManifest("{ " + "\"id\": "
                + serviceDefJson.get("manifest").get("id").toString()
                + ", \"manifest_content\": "
                + serviceDefJson.get("manifest").get("manifest_content")
                        .toString()
                + ", \"manifest_type\": "
                + serviceDefJson.get("manifest").get("manifest_type").toString()
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
        JsonNode objs = esmServiceClient.getRegisteredServices();
        for (JsonNode esmService : objs) {
            JsonNode configJson = esmService.get("manifest") != null
                    ? esmService.get("manifest").get("config") : null;
            String config = configJson != null ? configJson.toString() : "";
            services.add(new SupportService(
                    esmService.get("id").toString().replaceAll("\"", ""),
                    esmService.get("name").toString().replaceAll("\"", ""),
                    // esmService.get("short_name").toString().replaceAll("\"",
                    // "")
                    "", config));
        }
        return services;
    }

    public JsonNode getRawRegisteredServices() throws IOException {
        logger.info("Get registered all data of a service.");
        return esmServiceClient.getRegisteredServices();
    }

    @Async
    public void provisionServiceInstanceAsync(String serviceId,
            String instanceId) {
        provisionServiceInstance(serviceId, instanceId);
    }

    public String provisionServiceInstanceSync(String serviceId) {
        String instanceId = utilTools.generateUniqueId();
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
        String instanceId = utilTools.generateUniqueId();
        provisionTJobExecServiceInstance(serviceId, tJobExec, instanceId);
        return instanceId;
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
        String instanceId = utilTools.generateUniqueId();
        provisionExternalTJobExecServiceInstance(serviceId, exTJobExec,
                instanceId);
        return instanceId;
    }

    public JsonNode getRawServiceById(String serviceId) throws IOException {
        JsonNode service = null;
        JsonNode services = getRawRegisteredServices();
        for (JsonNode currentService : services) {
            if (currentService.get("id").toString().replaceAll("\"", "")
                    .equals(serviceId)) {
                service = currentService;
                break;
            }
        }
        return service;
    }

    public SupportServiceInstance createNewServiceInstance(String serviceId,
            Long executionId, String instanceId) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode service = this.getRawServiceById(serviceId);
        logger.info("Service instance: " + instanceId);
        List<ObjectNode> plans = Arrays.asList(mapper.readValue(
                service.get("plans").toString(), ObjectNode[].class));

        return new SupportServiceInstance(instanceId,
                service.get("id").toString().replaceAll("\"", ""),
                service.get("name").toString().replaceAll("\"", ""),
                // service.get("short_name").toString().replaceAll("\"",
                // ""),
                "", plans.get(0).get("id").toString().replaceAll("\"", ""),
                executionId != null ? executionId : null);
    }

    public void provisionServiceInstance(String serviceId, String instanceId) {
        logger.info("Service id to provision: " + serviceId);
        SupportServiceInstance newServiceInstance = null;

        try {
            newServiceInstance = this.createNewServiceInstance(serviceId, null,
                    instanceId);

            this.setTSSFilesConfig(newServiceInstance);
            this.fillEnvVariablesToTSS(newServiceInstance);

            servicesInstances.put(instanceId, newServiceInstance);

            this.provisionServiceInstanceByObject(newServiceInstance,
                    instanceId);
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
                    ? tJobExec.getId() : null;
            newServiceInstance = this.createNewServiceInstance(serviceId,
                    execId, instanceId);

            this.setTJobExecTSSFilesConfig(newServiceInstance, tJobExec);
            this.fillTJobExecEnvVariablesToTSS(newServiceInstance, tJobExec);

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
                                ? tJobServicesInstances : servicesInstances);
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
            Long tJobId = tJobExec.getTjob().getId();
            Long tJobExecId = tJobExec.getId();
            // etmcontextService.getMonitoringEnvVars
            String fileSeparator = "/";
            supportServiceInstance.getParameters().put("ET_FILES_PATH",
                    etSharedFolder + fileSeparator + tJobsFolder + fileSeparator
                            + tJobFolderPrefix + tJobId + fileSeparator
                            + tJobExecFolderPefix + tJobExecId
                            + fileSeparator + supportServiceInstance
                                    .getServiceName().toLowerCase()
                            + fileSeparator);
        }

    }

    private void fillTJobExecEnvVariablesToTSS(
            SupportServiceInstance supportServiceInstance,
            TJobExecution tJobExec) {

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
                    tJobExecId.toString());// TODO refactor -> Use

        }
        this.fillEnvVariablesToTSS(supportServiceInstance);
    }

    public void fillTSSConfigEnvVarsByTJob(TJob tJob,
            SupportServiceInstance supportServiceInstance) {
        try {
            List<ObjectNode> tJobTssList = tJob.getSupportServicesObj();
            for (ObjectNode tJobSuportService : tJobTssList) {
                JsonNode tJobTssNameObj = tJobSuportService.get("name");
                if (tJobTssNameObj != null
                        && supportServiceInstance.getServiceName()
                                .equals(stringFromJsonNode(tJobTssNameObj))) {
                    JsonNode selectedObj = tJobSuportService.get("selected");
                    if (selectedObj != null && selectedObj.asBoolean()) {
                        JsonNode configObj = tJobSuportService.get("config");
                        if (configObj != null) {
                            Iterable<String> singleConfigNamesIterator = () -> configObj
                                    .fieldNames();
                            for (String singleConfigName : singleConfigNamesIterator) {
                                JsonNode singleConfig = configObj
                                        .get(singleConfigName);
                                String envName = singleConfigName.replaceAll(
                                        "([a-z])_?([A-Z])", "$1_$2");
                                envName = "ET_CONFIG_" + envName.toUpperCase();
                                JsonNode valueObj = singleConfig.get("value");
                                if (valueObj != null) {
                                    supportServiceInstance.getParameters()
                                            .put(envName, valueObj.toString());
                                }

                            }
                        }
                    }
                }
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
                    ? exTJobExec.getId() : null;
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

    private void setExternalTJobExecTSSFilesConfig(
            SupportServiceInstance supportServiceInstance,
            ExternalTJobExecution exTJobExec) {
        if (exTJobExec != null && exTJobExec.getExTJob() != null) {
            Long exTJobId = exTJobExec.getExTJob().getId();
            Long exTJobExecId = exTJobExec.getId();
            String fileSeparator = "/";
            supportServiceInstance.getParameters().put("ET_FILES_PATH",
                    etSharedFolder + fileSeparator + externalTJobsFolder
                            + fileSeparator + externalTJobFolderPrefix
                            + exTJobId + fileSeparator
                            + externalTJobExecFolderPefix + exTJobExecId
                            + fileSeparator + supportServiceInstance
                                    .getServiceName().toLowerCase()
                            + fileSeparator);
        }
    }

    private void fillExternalTJobExecEnvVariablesToTSS(
            SupportServiceInstance supportServiceInstance,
            ExternalTJobExecution exTJobExec) {

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
            // TODO refactor -> Use etmcontextService.getMonitoringEnvVars
        }
        this.fillEnvVariablesToTSS(supportServiceInstance);
    }

    public void provisionServiceInstanceByObject(
            SupportServiceInstance newServiceInstance, String instanceId)
            throws Exception {
        esmServiceClient.provisionServiceInstance(newServiceInstance,
                instanceId, Boolean.toString(false));
        ObjectNode serviceInstanceDetail = getServiceInstanceInfo(instanceId);
        newServiceInstance.setManifestId(serviceInstanceDetail.get("context")
                .get("manifest_id").toString().replaceAll("\"", ""));
        buildSrvInstancesUrls(newServiceInstance, serviceInstanceDetail);
    }

    private void buildSrvInstancesUrls(SupportServiceInstance serviceInstance,
            ObjectNode serviceInstanceDetail) throws Exception {
        ObjectNode manifest = esmServiceClient
                .getManifestById(serviceInstance.getManifestId());
        Iterator<String> subServicesNames = manifest.get("endpoints")
                .fieldNames();
        Iterator<String> itEsmRespContextFields = serviceInstanceDetail
                .get("context").fieldNames();

        while (subServicesNames.hasNext()) {
            String serviceName = subServicesNames.next();
            logger.info("Manifest services {}:" + serviceName);
            String baseRegex = "[0-9a-f]{32}_" + serviceName + "_Ip";
            Pattern pattern = Pattern.compile(baseRegex);
            String serviceIp = null;
            boolean ipFound = false;

            while (itEsmRespContextFields.hasNext() && !ipFound) {
                String fieldName = itEsmRespContextFields.next();
                logger.info("Instance data fields {}:" + fieldName);
                Matcher matcher = pattern.matcher(fieldName);
                if (matcher.matches()) {

                    String ssrvContainerName = fieldName.substring(0,
                            fieldName.indexOf("_Ip"));
                    String networkName = etDockerNetwork;
                    logger.info("Network name: " + networkName);
                    String containerIp = serviceInstanceDetail.get("context")
                            .get(fieldName).toString().replaceAll("\"", "");
                    logger.info("ET_PUBLIC_HOST value: " + etPublicHost);
                    serviceIp = !etPublicHost.equals("localhost") ? etPublicHost
                            : containerIp;
                    serviceInstance.setContainerIp(containerIp);
                    serviceInstance.setServiceIp(serviceIp);
                    logger.info(
                            "Service Ip {}:" + serviceInstance.getServiceIp());

                    SupportServiceInstance auxServiceInstance = null;

                    if (manifest.get("endpoints").get(serviceName)
                            .get("main") != null
                            && manifest.get("endpoints").get(serviceName)
                                    .get("main").booleanValue()) {
                        logger.info("Principal instance {}:" + serviceName);
                        auxServiceInstance = serviceInstance;
                    } else {
                        auxServiceInstance = new SupportServiceInstance();
                        auxServiceInstance.setEndpointName(serviceName);
                        auxServiceInstance.setContainerIp(containerIp);
                        auxServiceInstance.setServiceIp(serviceIp);
                        auxServiceInstance
                                .setParameters(serviceInstance.getParameters());
                        serviceInstance.getSubServices()
                                .add(auxServiceInstance);
                    }

                    auxServiceInstance.setEndpointName(serviceName);

                    try {
                        if (manifest.get("endpoints").get(serviceName)
                                .get("api") != null) {
                            if (!manifest.get("endpoints").get(serviceName)
                                    .get("api").isArray()) {
                                getEndpointsInfo(auxServiceInstance,
                                        manifest.get("endpoints")
                                                .get(serviceName).get("api"),
                                        ssrvContainerName, networkName, "api");
                            } else {
                                for (final JsonNode apiNode : manifest
                                        .get("endpoints").get(serviceName)
                                        .get("api")) {
                                    getEndpointsInfo(auxServiceInstance,
                                            apiNode, ssrvContainerName,
                                            networkName,
                                            apiNode.get("name") != null
                                                    ? apiNode.get("name")
                                                            .toString()
                                                            .replaceAll("\"",
                                                                    "")
                                                            + "api"
                                                    : "api");
                                }
                            }
                        }
                        if (manifest.get("endpoints").get(serviceName)
                                .get("gui") != null) {
                            if (!manifest.get("endpoints").get(serviceName)
                                    .get("gui").isArray()) {
                                getEndpointsInfo(auxServiceInstance,
                                        manifest.get("endpoints")
                                                .get(serviceName).get("gui"),
                                        ssrvContainerName, networkName, "gui");
                            } else {
                                for (final JsonNode guiNode : manifest
                                        .get("endpoints").get(serviceName)
                                        .get("gui")) {
                                    getEndpointsInfo(auxServiceInstance,
                                            guiNode, ssrvContainerName,
                                            networkName,
                                            guiNode.get("name") != null
                                                    ? guiNode.get("name")
                                                            .toString()
                                                            .replaceAll("\"",
                                                                    "")
                                                            + "gui"
                                                    : "gui");
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error building endpoints info: {}",
                                e.getMessage());
                        throw new Exception("Error building endpoints info: "
                                + e.getMessage());
                    }
                    ipFound = true;
                }
            }

            if (!ipFound) {
                throw new Exception(
                        "Field ip not found for " + serviceName + " instance.");
            }
        }
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
                            SocatBindedPort socatBindedPortObj = dockerService
                                    .bindingPort(
                                            serviceInstance.getContainerIp(),
                                            node.get("port").toString(),
                                            networkName);
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

                if (node.get("protocol") != null
                        && (node.get("protocol").toString().contains("http"))
                        || node.get("protocol").toString().contains("ws")) {
                    serviceInstance.setServicePort(auxPort);
                    serviceInstance.getUrls().put(nodeName,
                            createServiceInstanceUrl(node,
                                    serviceInstance.getServiceIp()));
                }
            } else if (node.get("port") != null && node.get("protocol") != null
                    && (node.get("protocol").toString().contains("http"))
                    || node.get("protocol").toString().contains("ws")) {
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
            deprovisionServiceInstance(tSSInstanceId, servicesInstances);
        });

        servicesInstances = null;
    }

    public String deprovisionTJobExecServiceInstance(String instanceId,
            Long tJobExecId) {
        tSSIByTJobExecAssociated.remove(tJobExecId);
        return deprovisionServiceInstance(instanceId, tJobServicesInstances);
    }

    public String deprovisionExternalTJobExecServiceInstance(String instanceId,
            Long externalTJobExecId) {
        tSSIByExternalTJobExecAssociated.remove(externalTJobExecId);
        return deprovisionServiceInstance(instanceId,
                externalTJobServicesInstances);
    }

    /**
     * 
     * 
     * @param instanceId
     */
    public String deprovisionServiceInstance(String instanceId,
            Map<String, SupportServiceInstance> ssiMap) {
        if (ssiMap == null) {
            ssiMap = this.servicesInstances;
        }
        String result = "Instance deleted.";

        SupportServiceInstance serviceInstance = ssiMap.get(instanceId);
        if (serviceInstance != null) {
            serviceInstance.setServiceReady(false);

            for (String containerId : serviceInstance
                    .getPortBindingContainers()) {
                dockerService.stopDockerContainer(containerId);
                dockerService.removeDockerContainer(containerId);
            }

            for (SupportServiceInstance subServiceInstance : serviceInstance
                    .getSubServices()) {
                for (String containerId : subServiceInstance
                        .getPortBindingContainers()) {
                    dockerService.stopDockerContainer(containerId);
                    dockerService.removeDockerContainer(containerId);
                }
            }

            esmServiceClient.deprovisionServiceInstance(instanceId,
                    serviceInstance);
        }
        ssiMap.remove(instanceId);
        return result;
    }

    public boolean isInstanceUp(String instanceId) {
        boolean result = false;
        try {
            result = esmServiceClient.getServiceInstanceInfo(instanceId)
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

    /**
     * 
     * @return
     * @throws IOException
     */
    public ObjectNode getServiceInstanceInfo(String instanceId)
            throws IOException {
        logger.info("Get registered all data of a service.");
        return esmServiceClient.getServiceInstanceInfo(instanceId);
    }

    public List<SupportServiceInstance> getServicesInstancesAsList() {
        servicesInstances.forEach((tSSInstanceId, tSSInstance) -> {
            tSSInstance.setServiceReady(checkInstanceUrlIsUp(tSSInstance));
        });
        return new ArrayList<SupportServiceInstance>(
                servicesInstances.values());
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
            if (tSSInstance.gettJobExecId().longValue() == tJobExecId
                    .longValue() && checkInstanceUrlIsUp(tSSInstance)) {
                tSSInstanceList.add(tSSInstance);
            }
        });

        return tSSInstanceList;
    }

    public boolean checkInstanceUrlIsUp(SupportServiceInstance tSSInstance) {
        boolean up = false;
        int responseCode = 0;
        if (tSSInstance != null) {
            if (tSSInstance.getUrls() != null) {
                for (Map.Entry<String, String> urlHash : tSSInstance.getUrls()
                        .entrySet()) {
                    up = true;
                    if (urlHash.getValue().contains("http")) {
                        URL url;

                        try {
                            url = new URL(urlHash.getValue());
                            logger.debug(tSSInstance.getServiceName()
                                    + " Service URL: " + urlHash.getValue());
                            HttpURLConnection huc = (HttpURLConnection) url
                                    .openConnection();
                            huc.setConnectTimeout(2000);
                            responseCode = huc.getResponseCode();
                            up = up && ((responseCode >= 200
                                    && responseCode <= 299)
                                    || (responseCode >= 400
                                            && responseCode <= 415));
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
            }
            String checklMessage = up
                    ? tSSInstance.getServiceName() + "Service is ready."
                    : tSSInstance.getServiceName() + " Service is not ready.";

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

        String tJobExecFolder = sharedFolder + tJobExecFilePath;
        logger.debug("Shared folder:" + tJobExecFolder);

        File file = ResourceUtils.getFile(tJobExecFolder);
        // If not in dev mode
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
                logger.debug("Full path:" + tJobExecFolder + serviceFolderName);
                File serviceFolder = ResourceUtils
                        .getFile(tJobExecFolder + serviceFolderName);
                List<String> servicesFilesNames = new ArrayList<>(
                        Arrays.asList(serviceFolder.list()));
                for (String serviceFileName : servicesFilesNames) {
                    filesList.add(new TJobExecutionFile(serviceFileName,
                            getFileUrl(tJobExecFilePath + serviceFolderName
                                    + fileSeparator + serviceFileName),
                            serviceFolderName));
                }
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
        if (execMode.equals("normal")) {
            String fileSeparator = "/";
            supportServiceInstance.getParameters().put("ET_FILES_PATH",
                    etSharedFolder
                            + fileSeparator + supportServiceInstance
                                    .getServiceName().toLowerCase()
                            + fileSeparator);
        }
    }

    private void fillEnvVariablesToTSS(
            SupportServiceInstance supportServiceInstance) {

        supportServiceInstance.getParameters().put("ET_MON_LSHTTP_API",
                etEtmLshttpApi); // TODO refactor -> Use
                                 // etmcontextService.getMonitoringEnvVars
        supportServiceInstance.getParameters().put("ET_LSBEATS_HOST",
                etEtmLsbeatsHost);
        supportServiceInstance.getParameters().put("ET_LSBEATS_PORT",
                etEtmLsbeatsPort);
        supportServiceInstance.getParameters().put("ET_LSTCP_HOST",
                etEtmLstcpHost);
        supportServiceInstance.getParameters().put("ET_LSTCP_PORT",
                etEtmLstcpPort);
        supportServiceInstance.getParameters().put("ET_ETM_LSTCP_HOST",
                etEtmLstcpHost);
        supportServiceInstance.getParameters().put("ET_ETM_LSTCP_PORT",
                etEtmLstcpPort);

        supportServiceInstance.getParameters().put("ET_CONTEXT_API",
                "http://" + utilTools.getMyIp() + ":" + serverPort
                        + "/api/context/tss/"
                        + supportServiceInstance.getInstanceId());
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
                if (protocol.equals("http") || protocol.equals("ws")) {
                    String envNameAPI = prefix + "_API";
                    String path = entry.getValue().get("path").toString()
                            .replaceAll("\"", "");
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

    public String stringFromJsonNode(JsonNode toStringObj) {
        String string = null;
        if (toStringObj != null) {
            string = toStringObj.toString().replaceAll("\"", "");
        }
        return string;
    }
}
