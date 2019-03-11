package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.NotFoundException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.elastest.epm.client.json.DockerContainerInfo.DockerContainer;
import io.elastest.epm.client.json.DockerContainerInfo.PortInfo;
import io.elastest.epm.client.model.DockerPullImageProgress;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.epm.client.model.DockerServiceStatus.DockerServiceStatusEnum;
import io.elastest.epm.client.service.DockerComposeService;
import io.elastest.etm.model.EtPlugin;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.platform.service.DockerEtmService;
import io.elastest.etm.platform.service.PlatformService;
import io.elastest.etm.utils.PasswordFactory;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

@Service
public class EtPluginsService {
    final Logger logger = getLogger(lookup().lookupClass());

    private static final String TESTLINK_NAME = "testlink";
    private static final String TESTLINK_DISPLAY_NAME = "TestLink";

    private static final String ERE_NAME = "ere";
    private static final String ERE_DISPLAY_NAME = "Recommendation Engine";

    private static final String ERE_TRIAL_NAME = "eretrial";
    private static final String ERE_TRIAL_DISPLAY_NAME = "Recommendation Engine (Trial)";

    private static final String ECE_NAME = "ece";
    private static final String ECE_DISPLAY_NAME = "Cost Engine";

    private static final String EIM_NAME = "eim";
    private static final String EIM_DISPLAY_NAME = "Instrumentation Manager";

    private static final String JENKINS_NAME = "jenkins";
    private static final String JENKINS_DISPLAY_NAME = "Jenkins";

    private UtilsService utilsService;

    Map<String, EtPlugin> enginesMap = new HashMap<>();
    Map<String, EtPlugin> uniqueEtPluginsMap = new HashMap<>();
    Map<String, EtPlugin> tssInstancesMap = new HashMap<>();

    @Value("${et.compose.project.name}")
    String etComposeProjectName;

    @Value("${elastest.docker.network}")
    public String network;

    @Value("${et.test.engines.path}")
    public String ET_TEST_ENGINES_PATH;

    @Value("${exec.mode}")
    public String execmode;

    @Value("${et.user}")
    public String etUser;

    @Value("${et.pass}")
    public String etPass;

    @Value("${et.edm.mysql.host}")
    public String etEdmMysqlHost;

    @Value("${et.edm.mysql.port}")
    public String etEdmMysqlPort;

    @Value("${et.shared.folder}")
    private String sharedFolder;

    @Value("${et.etm.testlink.host}")
    public String etEtmTestLinkHost;

    @Value("${et.etm.testlink.port}")
    public String etEtmTestLinkPort;

    @Value("${et.etm.testlink.binded.port}")
    public String etEtmTestLinkBindedPort;

    @Value("${et.etm.testlink.container.name}")
    public String etEtmTestLinkContainerName;

    @Value("${et.etm.jenkins.host}")
    public String etEtmJenkinsHost;

    @Value("${et.etm.jenkins.port}")
    public String etEtmJenkinsPort;

    @Value("${et.etm.jenkins.container.name}")
    public String etEtmJenkinsContainerName;

    @Value("${et.etm.jenkins.binded.port}")
    public String etEtmJenkinsBindedPort;

    @Value("${et.test.engines.private.ere.enabled}")
    private boolean privateEreEnabled;

    @Value("${et.test.engines.private.ere.enabled}")
    private String privateEreEnabledString;

    private String tmpEnginesYmlFolder;
    private String uniqueEtPluginsYmlFolder;
    private String tmpTssInstancesYmlFolder;
    private PlatformService platformService;

    public EtPluginsService(PlatformService platformService,
            UtilsService utilsService) {
        this.utilsService = utilsService;
        this.platformService = platformService;
    }

    public void registerEngines() {
        this.enginesMap.put(ECE_NAME, new EtPlugin(ECE_NAME, ECE_DISPLAY_NAME));
        // It's necessary to auth:
        // https://docs.google.com/document/d/1RMMnJO3rA3KRg-q_LRgpmmvSTpaCPsmfAQjs9obVNeU

        if (privateEreEnabled || "true".equals(privateEreEnabledString)) {
            logger.debug("Private ERE is enabled");
            this.enginesMap.put(ERE_NAME,
                    new EtPlugin(ERE_NAME, ERE_DISPLAY_NAME));
        } else { // TRIAL
            logger.debug("Trial ERE is enabled");
            this.enginesMap.put(ERE_NAME, new EtPlugin(ERE_NAME,
                    ERE_TRIAL_DISPLAY_NAME, ERE_TRIAL_NAME));
        }

        this.uniqueEtPluginsMap.put(EIM_NAME,
                new EtPlugin(EIM_NAME, EIM_DISPLAY_NAME));
        this.uniqueEtPluginsMap.put(TESTLINK_NAME,
                new EtPlugin(TESTLINK_NAME, TESTLINK_DISPLAY_NAME));
        this.uniqueEtPluginsMap.put(JENKINS_NAME,
                new EtPlugin(JENKINS_NAME, JENKINS_DISPLAY_NAME));
    }

    @PostConstruct
    public void init() throws Exception {
        String path = sharedFolder.endsWith("/") ? sharedFolder
                : sharedFolder + "/";
        this.tmpEnginesYmlFolder = path + "tmp-engines-yml";
        this.tmpTssInstancesYmlFolder = path + "tmp-support-services-yml";
        this.uniqueEtPluginsYmlFolder = path + "tmp-unique-etplugins-yml";

        // Set credentials for ET Plugin
        if (etUser.equals("none") && etPass.equals("none")) {
            etUser = "elastest";
            etPass = PasswordFactory.generatePassword(8, PasswordFactory.ALPHA
                    + PasswordFactory.ALPHA_CAPS + PasswordFactory.NUMERIC);
        }

        registerEngines();
        for (String engine : this.enginesMap.keySet()) {
            createTestEngineProject(engine,
                    this.enginesMap.get(engine).getFileName());
        }

        for (String plugin : this.uniqueEtPluginsMap.keySet()) {
            createUniqueEtPluginProject(plugin,
                    this.uniqueEtPluginsMap.get(plugin).getFileName());
        }
    }

    @PreDestroy
    public void destroy() {
        for (String engine : this.enginesMap.keySet()) {
            stopAndRemoveProject(engine);
        }
        for (String uniqueEtPlugin : this.uniqueEtPluginsMap.keySet()) {
            stopAndRemoveProject(uniqueEtPlugin);
        }
        for (String tssInstance : this.tssInstancesMap.keySet()) {
            stopAndRemoveProject(tssInstance);
        }
    }

    /* ****************************** */
    /* *** Single Create Projects *** */
    /* ****************************** */

    public void createTestEngineProject(String name, String fileName) {
        String dockerComposeYml = getDockerCompose(fileName);
        this.createProject(name, dockerComposeYml, tmpEnginesYmlFolder);
    }

    public void createUniqueEtPluginProject(String name, String fileName)
            throws Exception {
        String dockerComposeYml = getDockerCompose(name);
        Map<String, String> envVars = new HashMap<>();

        if (name.equals(JENKINS_NAME)) {
            this.uniqueEtPluginsMap.get(name).setUser(etUser);
            this.uniqueEtPluginsMap.get(name).setPass(etPass);
            if (!utilsService.isDefaultEtPublicHost()) {
                envVars.put("JENKINS_LOCATION",
                        "http://" + utilsService.getEtPublicHostValue() + ":"
                                + etEtmJenkinsBindedPort);
            }
            envVars.put("ET_USER", etUser);
            envVars.put("ET_PASS", etPass);
            this.createProjectWithEnv(name, dockerComposeYml,
                    uniqueEtPluginsYmlFolder, envVars);
        } else if (name.equals(TESTLINK_NAME)) {
            this.uniqueEtPluginsMap.get(name).setUser(etUser);
            this.uniqueEtPluginsMap.get(name).setPass(etPass);
            envVars.put("TESTLINK_USERNAME", etUser);
            envVars.put("TESTLINK_PASSWORD", etPass);
            this.createProjectWithEnv(name, dockerComposeYml,
                    uniqueEtPluginsYmlFolder, envVars);
        } else {
            this.createProject(name, dockerComposeYml,
                    uniqueEtPluginsYmlFolder);
        }
    }

    public SupportServiceInstance createTssInstanceProject(String instanceId,
            String dockerComposeYml, SupportServiceInstance serviceInstance)
            throws Exception {
        platformService.createServiceDeploymentProject(instanceId,
                dockerComposeYml, tmpTssInstancesYmlFolder, true,
                serviceInstance.getParameters(), false, false);

        List<String> images = platformService
                .getServiceDeploymentImages(instanceId);
        serviceInstance.setImagesList(images);

        tssInstancesMap.put(instanceId, serviceInstance);

        return serviceInstance;
    }

    /* ******************************* */
    /* *** Generic Create Projects *** */
    /* ******************************* */

    public void createProject(String name, String dockerComposeYml,
            boolean withBindedExposedPortsToRandom, boolean withRemoveVolumes,
            String ymlPath) {
        try {
            platformService.createServiceDeploymentProject(name,
                    dockerComposeYml, ymlPath, true, null,
                    withBindedExposedPortsToRandom, withRemoveVolumes);
        } catch (Exception e) {
            logger.error("Exception creating project {}", name, e);
        }
    }

    public void createProject(String name, String dockerComposeYml,
            String ymlPath) {
        this.createProject(name, dockerComposeYml, false, false, ymlPath);
    }

    public void createProjectWithEnv(String name, String dockerComposeYml,
            boolean withBindedExposedPortsToRandom, boolean withRemoveVolumes,
            String ymlPath, Map<String, String> envs) {
        try {
            platformService.createServiceDeploymentProject(name,
                    dockerComposeYml, ymlPath, true, envs,
                    withBindedExposedPortsToRandom, withRemoveVolumes);
        } catch (Exception e) {
            logger.error("Exception creating project {}", name, e);
        }
    }

    public void createProjectWithEnv(String name, String dockerComposeYml,
            String ymlPath, Map<String, String> envs) {
        this.createProjectWithEnv(name, dockerComposeYml, false, false, ymlPath,
                envs);
    }

    /* **************************** */
    /* *** Stop/Remove Projects *** */
    /* **************************** */

    public EtPlugin stopEtPlugin(String projectName) {
        try {
            platformService.undeployService(projectName);
            this.getEtPlugin(projectName).initToDefault();
        } catch (IOException e) {
            logger.error("Error while stopping EtPlugin {}", projectName);
        }
        return this.getEtPlugin(projectName);
    }

    public boolean stopAndRemoveProject(String projectName) {
        boolean removed = platformService
                .undeployAndCleanDeployment(projectName);

        if (!removed) {
            return removed;
        }

        if (enginesMap.containsKey(projectName)) {
            enginesMap.remove(projectName);
        } else if (uniqueEtPluginsMap.containsKey(projectName)) {
            uniqueEtPluginsMap.remove(projectName);
        } else {
            tssInstancesMap.remove(projectName);
        }
        return removed;
    }

    /* ************************** */
    /* ***** Start Projects ***** */
    /* ************************** */

    @Async
    public void startEtPluginAsync(String projectName) {
        this.startEtPlugin(projectName);
    }

    public EtPlugin startEtPlugin(String projectName) {
        try {
            // Initialize
            this.updateStatus(projectName, DockerServiceStatusEnum.INITIALIZING,
                    "Initializing...");
            logger.debug("Initializing {} plugin...", projectName);

            // Pull
            this.pullProject(projectName);

            // Start
            this.updateStatus(projectName, DockerServiceStatusEnum.STARTING,
                    "Starting...");
            logger.debug("Starting {} plugin...", projectName);
            platformService.deployService(projectName, false);
            platformService.insertIntoETNetwork(projectName, network);
        } catch (Exception e) {
            logger.error("Cannot start {} plugin", projectName, e);
            logger.error("Stopping service {}", projectName);
            this.stopEtPlugin(projectName);
        }
        return this.getEtPlugin(projectName);
    }

    @Async
    public void startEngineOrUniquePluginAsync(String projectName) {
        this.startEngineOrUniquePlugin(projectName);
    }

    public EtPlugin startEngineOrUniquePlugin(String projectName) {
        String url = "";
        logger.debug("Checking if {} is not already running", projectName);
        if (!isRunning(projectName)) {
            this.startEtPlugin(projectName);
        }
        url = getEtPluginUrl(projectName);
        this.waitForReady(projectName, 2500);
        this.getEtPlugin(projectName).setUrl(url);
        return this.getEtPlugin(projectName);
    }

    /* *************************** */
    /* ****** Pull Projects ****** */
    /* *************************** */

    private void pullProject(String projectName) throws Exception {
        Map<String, EtPlugin> currentEtPluginMap;
        if (enginesMap.containsKey(projectName)) {
            currentEtPluginMap = enginesMap;
        } else if (uniqueEtPluginsMap.containsKey(projectName)) {
            currentEtPluginMap = uniqueEtPluginsMap;
        } else if (tssInstancesMap.containsKey(projectName)) {
            currentEtPluginMap = tssInstancesMap;
        } else {
            throw new Exception("Error on pulling images of " + projectName
                    + ": EtPlugin does not exists");
        }

        List<String> images = currentEtPluginMap.get(projectName)
                .getImagesList();

        if (images == null || images.isEmpty()) {
            images = platformService.getDeploymentImages(projectName);
            currentEtPluginMap.get(projectName).setImagesList(images);
        }

        Map<String, EtPlugin> map = getMapThatContainsEtPlugin(projectName);
        DockerServiceStatus serviceStatus = null;
        if (map != null) {
            serviceStatus = map.get(projectName);
        }

        platformService.pullDeploymentImages(projectName, serviceStatus, images,
                true);
    }

    /* ************************** */
    /* *** Wait/Check methods *** */
    /* ************************** */

    public String getEtPluginUrl(String serviceName) {
        if (serviceName == null) {
            return "";
        }
        return getEtPluginUrl(serviceName, serviceName + "_1");
    }

    public String getEtPluginUrl(String serviceName,
            String containerNameSuffix) {
        String url = "";
        if (serviceName != null && containerNameSuffix != null) {
            try {
                // Check first if is Unique EtPlugin started on init
                if (isUniqueEtPluginStartedOnInit(serviceName)) {
                    return this.getUniqueEtPlugin(serviceName).getUrl();
                }

                for (DockerContainer container : platformService
                        .getContainers(serviceName).getContainers()) {
                    String containerName = container.getName(); // example:
                                                                // ece_ece_1
                    if (containerName != null
                            && containerName.endsWith(containerNameSuffix)) {
                        logger.debug("Container info: {}", container);

                        String bindedIp = utilsService.getEtPublicHostValue();
                        String internalIp = "";
                        String ip = bindedIp;
                        boolean useBindedPort = true;

                        if (platformService.isContainerIntoNetwork(network,
                                containerName)) {
                            internalIp = platformService
                                    .getContainerIpByNetwork(containerName,
                                            network);
                        }

                        // If not server-address, use internal ip
                        if (utilsService.isDefaultEtPublicHost()) {
                            useBindedPort = false;
                            ip = internalIp;
                            if ("".equals(internalIp)) {
                                return "";
                            }
                        }

                        String port = "";
                        String internalPort = "";
                        String bindedPort = "";

                        switch (serviceName) {
                        case TESTLINK_NAME:
                            internalPort = "80";
                            bindedPort = "37071";
                            break;

                        case ERE_NAME:
                        case ERE_TRIAL_NAME:
                            internalPort = "9080";
                            bindedPort = "37007";
                            break;

                        case ECE_NAME:
                            internalPort = "8888";
                            bindedPort = "37008";
                            break;

                        case EIM_NAME:
                            internalPort = "8080";
                            bindedPort = "37004";
                            break;
                        case JENKINS_NAME:
                            internalPort = "8080";
                            bindedPort = "37092";
                            break;
                        default:
                            // TSS
                            if (isTssInstance(serviceName)) {
                                SupportServiceInstance tss = (SupportServiceInstance) getEtPlugin(
                                        serviceName);

                                if (tss.getInternalServicePort() != 0) {
                                    internalPort = ""
                                            + tss.getInternalServicePort();
                                    bindedPort = ""
                                            + tss.getBindedServicePort();
                                    break;
                                }

                            }

                            // Other
                            for (Entry<String, List<PortInfo>> portList : container
                                    .getPorts().entrySet()) {
                                if (portList.getValue() != null) {
                                    internalPort = portList.getKey()
                                            .split("/")[0];
                                    bindedPort = portList.getValue().get(0)
                                            .getHostPort();
                                    break;
                                }
                            }

                            break;
                        }

                        if (!useBindedPort) {
                            port = internalPort;
                        } else {
                            port = bindedPort;
                        }

                        if ("".equals(port)) {
                            throw new Exception("Port not found");
                        }

                        String protocol = "http";
                        if ("443".equals(port)) {
                            protocol = "https";
                        }

                        url = protocol + "://" + ip + ":" + port;
                        if (ERE_NAME.equals(serviceName)
                                || ERE_TRIAL_NAME.equals(serviceName)) {
                            url += "/ere-app";
                        }
                        logger.debug("Url: " + url);

                        // Update EtPlugin Urls
                        Map<String, EtPlugin> map = getMapThatContainsEtPlugin(
                                serviceName);
                        if (map != null && map.containsKey(serviceName)) {
                            map.get(serviceName).setUrl(url);

                            String internalUrl = "";
                            if (protocol != null && !"".equals(protocol)
                                    && internalIp != null
                                    && !"".equals(internalIp)
                                    && internalPort != null
                                    && !"".equals(internalPort)) {
                                internalUrl = protocol + "://" + internalIp
                                        + ":" + internalPort;
                            }

                            map.get(serviceName).setInternalUrl(internalUrl);

                            String bindedUrl = "";
                            if (protocol != null && !"".equals(protocol)
                                    && bindedIp != null && !"".equals(bindedIp)
                                    && internalPort != null
                                    && !"".equals(internalPort)) {
                                bindedUrl = protocol + "://" + bindedIp + ":"
                                        + bindedPort;
                            }
                            map.get(serviceName).setBindedUrl(bindedUrl);

                        }
                        break;
                    }
                }

            } catch (Exception e) {
                logger.error("Service url not exist {}", serviceName, e);
            }
        }
        return url;
    }

    public boolean checkIfEtPluginUrlIsUp(String serviceName) {
        EtPlugin plugin = getEtPlugin(serviceName);
        return this.checkIfEtPluginUrlIsUp(plugin);
    }

    public boolean checkIfEtPluginUrlIsUp(EtPlugin plugin) {
        String serviceName = plugin.getName();
        String url = plugin != null ? plugin.getInternalUrl() : "";

        logger.debug("Service {} url: {} ", serviceName, url);
        if (!"".equals(url)) {
            logger.debug("Service {} internal url: {} ", serviceName,
                    plugin.getInternalUrl());
            logger.debug("Service {} binded url: {} ", serviceName,
                    plugin.getBindedUrl());
        }

        boolean isUp = false;

        if (isTssInstance(plugin.getName())) {
            try {
                isUp = UtilTools.checkIfUrlIsUp(url);
            } catch (Exception e) {
            }
        } else {
            isUp = checkIfUrlIsUp(url);
        }

        if (isUp) {
            this.updateStatus(serviceName, DockerServiceStatusEnum.READY,
                    "Ready");
        }
        return isUp;
    }

    public boolean checkIfUrlIsUp(String etPluginUrl) {
        boolean up = false;
        URL url;
        try {
            url = new URL(etPluginUrl);
            logger.info("Service url to check: " + etPluginUrl);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            int responseCode = huc.getResponseCode();
            logger.info("Code returned: {}", responseCode);
            up = ((responseCode >= 200 && responseCode <= 299)
                    || responseCode == 403);
            if (!up) {
                logger.info("Service not ready at url: " + etPluginUrl);
                return up;
            }
        } catch (IOException e) {
            logger.warn("No url to check or not available yet");
            return false;
        }

        logger.info("Service ready at url: " + etPluginUrl);

        return up;
    }

    public boolean waitForReady(String projectName, int interval) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new Callable<String>() {

            @Override
            public String call() throws Exception {
                while (!getEtPlugin(projectName).getStatus()
                        .equals(DockerServiceStatusEnum.NOT_INITIALIZED)
                        && !checkIfEtPluginUrlIsUp(projectName)) {
                    // Wait
                    Thread.sleep(interval);
                }
                return "OK";
            }
        });
        try {
            System.out.println(future.get(2, TimeUnit.MINUTES)); // timeout is
                                                                 // in 2 seconds
        } catch (TimeoutException | InterruptedException
                | ExecutionException e) {
            System.err.println("Timeout waiting for a service to be ready");
            return false;
        } finally {
            executor.shutdownNow();
        }
        return true;
    }

    public Boolean isRunning(String serviceName) {
        try {
            checkIfEtPluginUrlIsUp(serviceName);
            // First check if is unique plugin started on init:
            if (isUniqueEtPluginStartedOnInit(serviceName)) {
                return true;
            }

            for (DockerContainer container : platformService
                    .getContainers(serviceName).getContainers()) {
                String containerName = serviceName + "_" + serviceName + "_1";
                if (container.getName().equals(containerName)
                        || container.getName().endsWith(serviceName + "_1")) {
                    return container.isRunning();
                }
            }

        } catch (Exception e) {
            logger.error("EtPlugin {} not started or not exist", serviceName,
                    e);
        }
        return false;
    }

    public boolean isUniqueEtPluginStartedOnInit(String serviceName) {
        switch (serviceName) {
        case JENKINS_NAME:
            return !etEtmJenkinsHost.equals("none");
        case TESTLINK_NAME:
            return !etEtmTestLinkHost.equals("none");
        default:
            return false;
        }
    }

    public EtPlugin getUniqueEtPlugin(String serviceName) {
        // TODO refactor (common code with getUrl)

        String protocol = "http://";
        String host = "";
        String port = "";
        String internalPort = "";
        String bindedPort = "";
        String containerName = "";

        switch (serviceName) {
        case JENKINS_NAME:
            host = etEtmJenkinsHost;
            internalPort = etEtmJenkinsPort;
            bindedPort = etEtmJenkinsBindedPort;
            containerName = etEtmJenkinsContainerName;
            break;
        case TESTLINK_NAME:
            host = etEtmTestLinkHost;
            internalPort = etEtmTestLinkPort;
            bindedPort = etEtmTestLinkBindedPort;
            containerName = etEtmTestLinkContainerName;
            break;
        default:
            return this.uniqueEtPluginsMap.get(serviceName);
        }

        port = utilsService.isDefaultEtPublicHost() ? internalPort : bindedPort;

        EtPlugin etPlugin = new EtPlugin(
                this.uniqueEtPluginsMap.get(serviceName));
        logger.debug("Get unique service: {}", serviceName);

        String internalHost = null;
        try {
            containerName = platformService.getContainerName(serviceName, network);
            internalHost = platformService.getContainerIpByNetwork(containerName, network);
        } catch (Exception e) {
            logger.error("Error on get {} internal url", serviceName);
        }
        
        String bindedHost = utilsService.getEtPublicHostValue();

        String internalUrl = "";
        if (protocol != null && !"".equals(protocol) && internalHost != null
                && !"".equals(internalHost) && internalPort != null
                && !"".equals(internalPort)) {
            internalUrl = protocol + internalHost + ":" + internalPort;
        }
        etPlugin.setInternalUrl(internalUrl);

        String bindedUrl = "";
        if (protocol != null && !"".equals(protocol) && bindedHost != null
                && !"".equals(bindedHost) && bindedPort != null
                && !"".equals(bindedPort)) {
            bindedUrl = protocol + bindedHost + ":" + bindedPort;
        }
        etPlugin.setBindedUrl(bindedUrl);

        // If started on init (platform)
        if (!host.equals("none")) {
            etPlugin = new EtPlugin(etPlugin);

            // Default or development
            String finalHost = internalHost;

            // If not development or default, start socat
            if (!utilsService.isDefaultEtPublicHost()) {
                finalHost = bindedHost;
            }

            if (protocol != null && !"".equals(protocol) && finalHost != null
                    && !"".equals(finalHost) && port != null
                    && !"".equals(port)) {
                String url = protocol + finalHost + ":" + port;
                etPlugin.setUrl(url);
            }

            etPlugin.setStatus(DockerServiceStatusEnum.READY);
            etPlugin.setStatusMsg("Ready");
            updateStatus(serviceName, DockerServiceStatusEnum.READY, "Ready");
        } else {
            etPlugin.setUrl(getEtPluginUrl(serviceName));
            // Check if ready and update
            if (!etPlugin.getStatus().equals(DockerServiceStatusEnum.READY)) {
                this.checkIfEtPluginUrlIsUp(etPlugin);
            }
        }

        return etPlugin;

    }

    /* ************************* */
    /* ****** Get Methods ****** */
    /* ************************* */

    public List<EtPlugin> getEngines() {
        return new ArrayList<>(enginesMap.values());
    }

    public List<EtPlugin> getUniqueEtPlugins() {
        return new ArrayList<>(uniqueEtPluginsMap.values());
    }

    public List<EtPlugin> getTssInstances() {
        return new ArrayList<>(tssInstancesMap.values());
    }

    public EtPlugin getEtPlugin(String name) {
        if (enginesMap.containsKey(name)) {
            return enginesMap.get(name);
        } else if (uniqueEtPluginsMap.containsKey(name)) {
            return this.getUniqueEtPlugin(name);
        } else {
            return tssInstancesMap.get(name);
        }
    }

    public boolean isTssInstance(String name) {
        return tssInstancesMap.containsKey(name);
    }

    public Map<String, EtPlugin> getMapThatContainsEtPlugin(String serviceName)
            throws NotFoundException {
        if (enginesMap.containsKey(serviceName)) {
            return enginesMap;
        } else if (uniqueEtPluginsMap.containsKey(serviceName)) {
            return uniqueEtPluginsMap;
        } else if (tssInstancesMap.containsKey(serviceName)) {
            return tssInstancesMap;
        }
        throw new NotFoundException(
                "The EtPlugin " + serviceName + " does not exist");
    }

    public String getUrlIfIsRunning(String engineName) {
        return getEtPluginUrl(engineName);
    }

    public String getDockerCompose(String engineFileName) {
        String content = "";
        try {
            InputStream inputStream = getClass().getResourceAsStream(
                    "/" + ET_TEST_ENGINES_PATH + engineFileName + ".yml");
            content = IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        content = this.replaceProjectNameMatchesByElastestProjectName(content);

        return content;
    }

    /* ******************** */
    /* ****** Others ****** */
    /* ******************** */

    public void updateStatus(String serviceName, DockerServiceStatusEnum status,
            String statusMsg) throws NotFoundException {
        Map<String, EtPlugin> map = getMapThatContainsEtPlugin(serviceName);
        if (map != null) {
            map.get(serviceName).setStatus(status);
            map.get(serviceName).setStatusMsg(statusMsg);
        }
    }

    private String replaceProjectNameMatchesByElastestProjectName(
            String content) {
        return content.replaceAll("projectnametoreplace", etComposeProjectName);
    }

}
