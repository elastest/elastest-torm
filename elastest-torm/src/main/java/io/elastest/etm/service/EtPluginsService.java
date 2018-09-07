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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.NotFoundException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ProgressMessage;

import io.elastest.epm.client.json.DockerContainerInfo.DockerContainer;
import io.elastest.epm.client.json.DockerContainerInfo.PortInfo;
import io.elastest.epm.client.model.DockerPullImageProgress;
import io.elastest.epm.client.model.DockerServiceStatus.DockerServiceStatusEnum;
import io.elastest.epm.client.service.DockerComposeService;
import io.elastest.etm.model.EtPlugin;
import io.elastest.etm.model.SupportServiceInstance;

@Service
public class EtPluginsService {
    private static final String TESTLINK_NAME = "testlink";

    private static final String ERE_NAME = "ere";

    private static final String ECE_NAME = "ece";

    private static final String EIM_NAME = "eim";

    private static final String JENKINS_NAME = "jenkins";

    final Logger logger = getLogger(lookup().lookupClass());

    public DockerComposeService dockerComposeService;
    public DockerEtmService dockerEtmService;

    Map<String, EtPlugin> enginesMap = new HashMap<>();
    Map<String, EtPlugin> uniqueEtPluginsMap = new HashMap<>();
    Map<String, EtPlugin> tssInstancesMap = new HashMap<>();

    @Value("${et.compose.project.name}")
    String etComposeProjectName;

    @Value("${elastest.docker.network}")
    public String network;

    @Value("${et.test.engines.path}")
    public String ET_TEST_ENGINES_PATH;

    @Value("${exec.mode")
    public String execmode;

    @Value("${et.public.host}")
    public String etPublicHost;

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

    @Value("${et.etm.testlink.container.name}")
    public String etEtmTestLinkContainerName;

    @Value("${et.etm.jenkins.host}")
    public String etEtmJenkinsHost;

    @Value("${et.etm.jenkins.port}")
    public String etEtmJenkinsPort;

    @Value("${et.etm.jenkins.container.name}")
    public String etEtmJenkinsContainerName;

    private String tmpEnginesYmlFolder;
    private String uniqueEtPluginsYmlFolder;
    private String tmpTssInstancesYmlFolder;

    public EtPluginsService(DockerComposeService dockerComposeService,
            DockerEtmService dockerEtmService) {
        this.dockerComposeService = dockerComposeService;
        this.dockerEtmService = dockerEtmService;
    }

    public void registerEngines() {
        this.enginesMap.put(ECE_NAME, new EtPlugin(ECE_NAME));
        // It's necessary to auth:
        // https://docs.google.com/document/d/1RMMnJO3rA3KRg-q_LRgpmmvSTpaCPsmfAQjs9obVNeU
        this.enginesMap.put(ERE_NAME, new EtPlugin(ERE_NAME));

        this.uniqueEtPluginsMap.put(EIM_NAME, new EtPlugin(EIM_NAME));
        this.uniqueEtPluginsMap.put(TESTLINK_NAME, new EtPlugin(TESTLINK_NAME));
        this.uniqueEtPluginsMap.put(JENKINS_NAME, new EtPlugin(JENKINS_NAME));
    }

    @PostConstruct
    public void init() throws Exception {
        String path = sharedFolder.endsWith("/") ? sharedFolder
                : sharedFolder + "/";
        this.tmpEnginesYmlFolder = path + "tmp-engines-yml";
        this.tmpTssInstancesYmlFolder = path + "tmp-support-services-yml";
        this.uniqueEtPluginsYmlFolder = path + "tmp-unique-etplugins-yml";

        registerEngines();
        for (String engine : this.enginesMap.keySet()) {
            createTestEngineProject(engine);
        }

        for (String plugin : this.uniqueEtPluginsMap.keySet()) {
            createUniqueEtPluginProject(plugin);
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

    public void createTestEngineProject(String name) {
        String dockerComposeYml = getDockerCompose(name);
        this.createProject(name, dockerComposeYml, tmpEnginesYmlFolder);
    }

    public void createUniqueEtPluginProject(String name) throws Exception {
        String dockerComposeYml = getDockerCompose(name);
        if (EIM_NAME.equals(name) || TESTLINK_NAME.equals(name)) {
            try {
                String mysqlHost = dockerEtmService.getEdmMySqlHost();
                dockerComposeYml = dockerComposeYml.replace("edm-mysql",
                        mysqlHost);
            } catch (Exception e) {
                throw new Exception("Error on get MySQL host", e);
            }
        }

        this.createProject(name, dockerComposeYml, uniqueEtPluginsYmlFolder);

    }

    public SupportServiceInstance createTssInstanceProject(String instanceId,
            String dockerComposeYml, SupportServiceInstance serviceInstance)
            throws Exception {
        dockerComposeService.createProjectWithEnv(instanceId, dockerComposeYml,
                tmpTssInstancesYmlFolder, true, serviceInstance.getParameters(),
                false, false);

        List<String> images = dockerComposeService.getProjectImages(instanceId);
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
            dockerComposeService.createProject(name, dockerComposeYml, ymlPath,
                    true, withBindedExposedPortsToRandom, withRemoveVolumes);
        } catch (Exception e) {
            logger.error("Exception creating project {}", name, e);
        }
    }

    public void createProject(String name, String dockerComposeYml,
            String ymlPath) {
        this.createProject(name, dockerComposeYml, false, false, ymlPath);
    }

    /* **************************** */
    /* *** Stop/Remove Projects *** */
    /* **************************** */

    public EtPlugin stopEtPlugin(String projectName) {
        try {
            dockerComposeService.stopProject(projectName);
            this.getEtPlugin(projectName).initToDefault();
        } catch (IOException e) {
            logger.error("Error while stopping EtPlugin {}", projectName);
        }
        return this.getEtPlugin(projectName);
    }

    public boolean stopAndRemoveProject(String projectName) {
        boolean removed = dockerComposeService
                .stopAndRemoveProject(projectName);

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
//            this.pullProject(projectName);

            // Start
            this.updateStatus(projectName, DockerServiceStatusEnum.STARTING,
                    "Starting...");
            logger.debug("Starting {} plugin...", projectName);
            dockerComposeService.startProject(projectName, false);
            insertIntoETNetwork(projectName);
            this.checkIfEtPluginUrlIsUp(projectName);
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

        this.waitForReady(projectName, 2500);
        url = getEtPluginUrl(projectName);
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
            images = dockerComposeService.getProjectImages(projectName);
            currentEtPluginMap.get(projectName).setImagesList(images);
        }

        for (String image : images) {
            ProgressHandler progressHandler = this
                    .getEtPluginProgressHandler(projectName, image);

            dockerComposeService.pullImageWithProgressHandler(projectName,
                    progressHandler, image);
        }
    }

    public ProgressHandler getEtPluginProgressHandler(String projectName,
            String image) {
        DockerPullImageProgress dockerPullImageProgress = new DockerPullImageProgress();
        dockerPullImageProgress.setImage(image);
        dockerPullImageProgress.setCurrentPercentage(0);

        this.updateStatus(projectName, DockerServiceStatusEnum.PULLING,
                "Pulling " + image + " image");
        return new ProgressHandler() {
            @Override
            public void progress(ProgressMessage message)
                    throws DockerException {
                dockerPullImageProgress.processNewMessage(message);
                String msg = "Pulling image " + image + " from " + projectName
                        + " ET Plugin: "
                        + dockerPullImageProgress.getCurrentPercentage() + "%";

                updateStatus(projectName, DockerServiceStatusEnum.PULLING, msg);
            }

        };

    }

    /* ************************** */
    /* *** Wait/Check methods *** */
    /* ************************** */

    public String getEtPluginUrl(String serviceName) {
        String url = "";
        try {
            // Check first if is Unique EtPlugin started on init
            if (isUniqueEtPluginStartedOnInit(serviceName)) {
                return this.getUniqueEtPlugin(serviceName).getUrl();
            }

            for (DockerContainer container : dockerComposeService
                    .getContainers(serviceName).getContainers()) {
                String containerName = container.getName(); // example:
                                                            // ece_ece_1
                if (containerName != null
                        && containerName.endsWith(serviceName + "_1")) {
                    logger.debug("Container info: {}", container);

                    String ip = etPublicHost;

                    boolean useBindedPort = true;
                    if (ip.equals("localhost")) {
                        useBindedPort = false;
                        ip = dockerEtmService.dockerService
                                .getContainerIpByNetwork(containerName,
                                        network);
                    }

                    String port = "";

                    switch (serviceName) {
                    case TESTLINK_NAME:
                        if (!useBindedPort) {
                            port = "80";
                        } else {
                            port = "37071";
                        }
                        break;

                    case ERE_NAME:
                        if (!useBindedPort) {
                            port = "9080";
                        } else {
                            port = "37007";
                        }
                        break;

                    case ECE_NAME:
                        if (!useBindedPort) {
                            port = "8888";
                        } else {
                            port = "37008";
                        }
                        break;

                    case EIM_NAME:
                        if (!useBindedPort) {
                            port = "8080";
                        } else {
                            port = "37004";
                        }
                        break;
                    case JENKINS_NAME:
                        if (!useBindedPort) {
                            port = "8080";
                        } else {
                            port = "37092";
                        }
                        break;
                    default:
                        for (Entry<String, List<PortInfo>> portList : container
                                .getPorts().entrySet()) {
                            if (portList.getValue() != null) {
                                if (!useBindedPort) {
                                    port = portList.getKey().split("/")[0];
                                } else {
                                    port = portList.getValue().get(0)
                                            .getHostPort();
                                }
                                break;

                            }
                        }
                        break;
                    }

                    if ("".equals(port)) {
                        throw new Exception("Port not found");
                    }

                    String protocol = "http";
                    if ("443".equals(port)) {
                        protocol = "https";
                    }

                    url = protocol + "://" + ip + ":" + port;
                    if (ERE_NAME.equals(serviceName)) {
                        url += "/ere-app";
                    }
                    logger.debug("Url: " + url);
                    break;
                }
            }

        } catch (Exception e) {
            logger.error("Service url not exist {}", serviceName, e);
        }
        return url;
    }

    public boolean checkIfEtPluginUrlIsUp(String serviceName) {
        String url = getEtPluginUrl(serviceName);
        boolean isUp = checkIfUrlIsUp(url);
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
            up = (responseCode >= 200 && responseCode <= 299);
            if (!up) {
                logger.info("Service not ready at url: " + etPluginUrl);
                return up;
            }
        } catch (IOException e) {
            return false;
        }

        logger.info("Service ready at url: " + etPluginUrl);

        return up;
    }

    public boolean waitForReady(String projectName, int interval) {
        while (!this.getEtPlugin(projectName).getStatus()
                .equals(DockerServiceStatusEnum.NOT_INITIALIZED)
                && !this.checkIfEtPluginUrlIsUp(projectName)) {
            // Wait
            try { // TODO timeout
                Thread.sleep(interval);
            } catch (InterruptedException e) {
            }
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

            for (DockerContainer container : dockerComposeService
                    .getContainers(serviceName).getContainers()) {
                String containerName = serviceName + "_" + serviceName + "_1";
                if (container.getName().equals(containerName)
                        || container.getName().endsWith(serviceName + "_1")) {
                    return container.isRunning();
                }
            }
            return false;

        } catch (Exception e) {
            logger.error("EtPlugin {} not started or not exist", serviceName,
                    e);
            return false;
        }
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
        String host = "";
        String port = "";
        String containerName = "";

        switch (serviceName) {
        case JENKINS_NAME:
            host = etEtmJenkinsHost;
            port = etEtmJenkinsPort;
            containerName = etEtmJenkinsContainerName;
            break;
        case TESTLINK_NAME:
            host = etEtmTestLinkHost;
            port = etEtmTestLinkPort;
            containerName = etEtmTestLinkContainerName;
            break;
        default:
            return this.uniqueEtPluginsMap.get(serviceName);
        }

        EtPlugin etPlugin = new EtPlugin(
                this.uniqueEtPluginsMap.get(serviceName));

        if (!host.equals("none")) {
            etPlugin = new EtPlugin(etPlugin);
            try {
                String url = "http://" + dockerEtmService.dockerService
                        .getContainerIp(containerName, network) + ":" + port;
                etPlugin.setUrl(url);
            } catch (Exception e) {
                logger.error("Error on get {} url", serviceName, e);
            }

            etPlugin.setStatus(DockerServiceStatusEnum.READY);
            etPlugin.setStatusMsg("Ready");
            updateStatus(serviceName, DockerServiceStatusEnum.READY, "Ready");
        } else {
            // Check if ready and update
            if (!etPlugin.getStatus().equals(DockerServiceStatusEnum.READY)) {
                this.checkIfEtPluginUrlIsUp(serviceName);
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

    public String getDockerCompose(String engineName) {
        String content = "";
        try {
            InputStream inputStream = getClass().getResourceAsStream(
                    "/" + ET_TEST_ENGINES_PATH + engineName + ".yml");
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

    public void insertIntoETNetwork(String engineName) throws Exception {
        try {
            for (DockerContainer container : dockerComposeService
                    .getContainers(engineName).getContainers()) {
                try {
                    dockerEtmService.dockerService.insertIntoNetwork(network,
                            container.getName());
                } catch (DockerException | InterruptedException
                        | DockerCertificateException e) {
                    throw new Exception(
                            "Error on insert container " + container.getName()
                                    + " into " + network + " network");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String replaceProjectNameMatchesByElastestProjectName(
            String content) {
        return content.replaceAll("projectnametoreplace", etComposeProjectName);
    }

}
