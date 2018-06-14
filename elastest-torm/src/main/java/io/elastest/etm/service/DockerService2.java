package io.elastest.etm.service;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.TestSuiteXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.LogConfig.LoggingType;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SocatBindedPort;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.utils.ElastestConstants;
import io.elastest.etm.utils.FilesService;
import io.elastest.etm.utils.UtilTools;

@Service
public class DockerService2 {

    private static final Logger logger = LoggerFactory
            .getLogger(DockerService2.class);

    private static String checkImage = "elastest/etm-check-service-up";
    private static final Map<String, String> createdContainers = new HashMap<>();

    @Value("${logstash.host:#{null}}")
    private String logstashHost;

    @Value("${et.etm.lstcp.port}")
    private String logstashTcpPort;

    @Value("${elastest.docker.network}")
    private String elastestNetwork;

    @Value("${et.docker.img.dockbeat}")
    private String dockbeatImage;

    @Value("${docker.sock}")
    private String dockerSock;

    @Value("${et.docker.img.socat}")
    public String etSocatImage;

    @Value("${et.shared.folder}")
    private String sharedFolder;

    @Value("${et.etm.logstash.container.name}")
    private String etEtmLogstashContainerName;

    @Autowired
    public FilesService filesService;

    @Autowired
    public UtilTools utilTools;

    public String getThisContainerIpCmd = "ip a | grep -m 1 global | grep -oE '([0-9]{1,3}\\.){3}[0-9]{1,3}\\/' | grep -oE '([0-9]{1,3}\\.){3}[0-9]{1,3}'";

    @PostConstruct
    public void initialize() {
        try {
            logger.info("Pulling dockbeat image...");
            this.pullETExecImage(dockbeatImage, "Dockbeat");
        } catch (TJobStoppedException e) {
            logger.error("Error on pulling Dockbeat Image", e);
        }
    }

    @PreDestroy
    public void removeAllContainers() {
        logger.info("Stopping started containers...");
        for (Map.Entry<String, String> entry : createdContainers.entrySet()) {
            String containerId = entry.getKey();
            String containerName = entry.getValue();
            try {
                stopDockerContainer(containerId);
                removeDockerContainer(containerId);
                logger.info("Container {} removed", containerName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public enum ContainersListActionEnum {
        ADD("ADD"), REMOVE("REMOVE"), NONE("NONE");

        private String value;

        ContainersListActionEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ContainersListActionEnum fromValue(String text) {
            for (ContainersListActionEnum b : ContainersListActionEnum
                    .values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    /**************************/
    /***** Config Methods *****/
    /**************************/

    public void loadBasicServices(DockerExecution dockerExec) throws Exception {
        configureDocker(dockerExec);
        dockerExec.setNetwork(elastestNetwork);
    }

    public DockerClient getDockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder().build();
        return DockerClientBuilder.getInstance(config).build();
    }

    public void insertCreatedContainer(String containerId,
            String containerName) {
        createdContainers.put(containerId, containerName);
    }

    public void configureDocker(DockerExecution dockerExec) {
        DockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder().build();
        dockerExec.setDockerClient(
                DockerClientBuilder.getInstance(config).build());
    }

    /*****************************/
    /***** Container Methods *****/
    /*****************************/

    public String runDockerContainer(DockerClient dockerClient,
            String imageName, List<String> envs, String containerName,
            String networkName, Ports portBindings, Integer listenPort)
            throws TJobStoppedException {

        this.doPull(dockerClient, imageName);

        CreateContainerCmd createContainer = dockerClient
                .createContainerCmd(imageName);
        if (containerName != null && !"".equals(containerName)) {
            createContainer = createContainer.withName(containerName);
        }

        if (envs != null) {
            createContainer = createContainer.withEnv(envs);
        }

        if (networkName != null && !"".equals(networkName)) {
            createContainer = createContainer.withNetworkMode(networkName);
        }

        if (listenPort != null) {
            createContainer = createContainer
                    .withExposedPorts(ExposedPort.tcp(listenPort));
        }

        if (portBindings != null) {
            createContainer = createContainer.withPortBindings(portBindings);
        }

        createContainer = createContainer.withPublishAllPorts(true);
        CreateContainerResponse container = createContainer.exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        this.insertCreatedContainer(container.getId(), containerName);

        logger.info("Id del contenedor:" + container.getId());

        return container.getId();
    }

    public String runDockerContainer(String imageName, List<String> envs,
            String containerName, String networkName, Ports portBindings,
            Integer listenPort) throws TJobStoppedException {
        DockerClient dockerClient = this.getDockerClient();
        return this.runDockerContainer(dockerClient, imageName, envs,
                containerName, networkName, portBindings, listenPort);
    }

    public void removeDockerContainer(String containerId) {
        DockerClient dockerClient = this.getDockerClient();
        dockerClient.removeContainerCmd(containerId).exec();
        createdContainers.remove(containerId);
    }

    public void stopDockerContainer(String containerId) {
        DockerClient dockerClient = this.getDockerClient();
        this.stopDockerContainer(dockerClient, containerId);
    }

    public void stopDockerContainer(DockerClient dockerClient,
            String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    public CreateContainerResponse createContainer(DockerExecution dockerExec,
            String type) throws TJobStoppedException {
        TJobExecution tJobExec = dockerExec.gettJobexec();
        TJob tJob = tJobExec.getTjob();
        SutSpecification sut = tJob.getSut();

        String image = "";
        String commands = null;
        List<Parameter> parametersList = new ArrayList<Parameter>();
        String prefix = "";
        String suffix = "";
        String containerName = "";
        String sutHost = null;

        String sutPath = null;

        if ("sut".equals(type.toLowerCase())) {
            parametersList = sut.getParameters();
            commands = sut.getCommands();
            image = sut.getSpecification();
            prefix = "sut_";
            if (sut.isSutInNewContainer()) {
                suffix = sut.getSutInContainerAuxLabel();
            }
            containerName = getSutName(dockerExec);
            sutPath = filesService.buildFilesPath(tJobExec,
                    ElastestConstants.SUT_FOLDER);
            filesService.createExecFilesFolder(sutPath);
        } else if ("tjob".equals(type.toLowerCase())) {
            parametersList = tJobExec.getParameters();
            commands = tJob.getCommands();
            image = tJob.getImageName();
            prefix = "test_";
            containerName = getTestName(dockerExec);
            if (dockerExec.isWithSut()) {
                sutHost = dockerExec.getSutExec().getIp();
            }
        }

        // Environment variables (optional)
        ArrayList<String> envList = new ArrayList<>();
        String envVar;

        // Get TJob Exec Env Vars
        for (Map.Entry<String, String> entry : dockerExec.gettJobexec()
                .getEnvVars().entrySet()) {
            envVar = entry.getKey() + "=" + entry.getValue();
            envList.add(envVar);
        }

        // Get Parameters and insert into Env Vars
        for (Parameter parameter : parametersList) {
            envVar = parameter.getName() + "=" + parameter.getValue();
            envList.add(envVar);
        }
        if (sutHost != null) {
            envList.add("ET_SUT_HOST=" + dockerExec.getSutExec().getIp());
        }

        // Commands (optional)
        ArrayList<String> cmdList = new ArrayList<>();
        ArrayList<String> entrypointList = new ArrayList<>();
        if (commands != null && !commands.isEmpty()) {
            cmdList.add("-c");
            if (sut != null) {
                if (sut.isSutInNewContainer()) {
                    commands = sutPath != null
                            ? ("cd " + sutPath + ";" + commands)
                            : commands;
                }
            } else {
                commands = "export ET_SUT_HOST=$(" + this.getThisContainerIpCmd
                        + ") || echo;" + commands;
            }
            cmdList.add(commands);

            entrypointList.add("/bin/sh");
        }

        // Load Log Config
        LogConfig logConfig = null;
        if (tJob.isSelectedService("ems")) {
            try {
                logConfig = getEMSLogConfig(type, prefix, suffix, dockerExec);
            } catch (Exception e) {
                logger.error("Cannot get Ems Log config", e);
            }
        } else {
            logConfig = getDefaultLogConfig(logstashTcpPort, prefix, suffix,
                    dockerExec);
        }

        // Pull Image
        this.pullETExecImage(image, type);

        // Create docker sock volume
        Volume dockerSockVolume = new Volume(dockerSock);

        CreateContainerCmd containerCmd = dockerExec.getDockerClient()
                .createContainerCmd(image).withEnv(envList)
                .withLogConfig(logConfig).withName(containerName)
                .withCmd(cmdList).withEntrypoint(entrypointList)
                .withNetworkMode(dockerExec.getNetwork());

        Volume sharedDataVolume = null;
        if (sut != null && sut.isSutInNewContainer()) {
            sharedDataVolume = new Volume(sharedFolder);
        }

        if (sharedDataVolume != null) {
            containerCmd = containerCmd
                    .withVolumes(dockerSockVolume, sharedDataVolume)
                    .withBinds(new Bind(dockerSock, dockerSockVolume),
                            new Bind(sharedFolder, sharedDataVolume));
        } else {
            containerCmd = containerCmd.withVolumes(dockerSockVolume)
                    .withBinds(new Bind(dockerSock, dockerSockVolume));
        }

        // Create Container
        return containerCmd.exec();
    }

    public void pullETExecImage(String image, String name)
            throws TJobStoppedException {
        logger.debug("Try to Pulling {} Image ({})", name, image);
        this.doPull(image);
        logger.debug("{} image pulled succesfully!", name);

    }

    /********************/
    /***** Dockbeat *****/
    /********************/

    public String getDockbeatContainerName(DockerExecution dockerExec) {
        return "elastest_dockbeat_" + dockerExec.getExecutionId();

    }

    public void startDockbeat(DockerExecution dockerExec)
            throws TJobStoppedException {
        TJobExecution tJobExec = dockerExec.gettJobexec();
        TJob tJob = tJobExec.getTjob();
        Long execution = dockerExec.getExecutionId();

        String containerName = getDockbeatContainerName(dockerExec);

        // Environment variables
        ArrayList<String> envList = new ArrayList<>();
        String envVar;

        // Get Parameters and insert into Env Vars¡
        String lsHostEnvVar = "LOGSTASHHOST" + "=" + logstashHost;
        if (tJob.isSelectedService("ems")) {
            String regexSuffix = "_?(" + execution + ")(_([^_]*(_\\d*)?))?";
            String testRegex = "^test" + regexSuffix;
            String sutRegex = "^sut" + regexSuffix;
            envVar = "FILTER_CONTAINERS" + "=" + testRegex + "|" + sutRegex;
            envList.add(envVar);

            // envVar = "FILTER_EXCLUDE" + "=" + "\"\"";
            // envList.add(envVar);

            envVar = "LOGSTASHPORT" + "="
                    + tJobExec.getEnvVars().get("ET_EMS_LSBEATS_PORT");
            envList.add(envVar);

            lsHostEnvVar = "LOGSTASHHOST" + "="
                    + tJobExec.getEnvVars().get("ET_EMS_LSBEATS_HOST");
        }
        envList.add(lsHostEnvVar);

        // dockerSock
        Volume volume1 = new Volume(dockerSock);

        // Pull Image
        this.pullETExecImage(dockbeatImage, "Dockbeat");

        // Create Container
        logger.debug("Creating Dockbeat Container...");
        CreateContainerResponse container = dockerExec.getDockerClient()
                .createContainerCmd(dockbeatImage).withEnv(envList)
                .withName(containerName)
                .withBinds(new Bind(dockerSock, volume1))
                .withNetworkMode(dockerExec.getNetwork()).exec();

        dockerExec.getDockerClient().startContainerCmd(container.getId())
                .exec();
        this.insertCreatedContainer(container.getId(), containerName);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    /***********************/
    /***** Sut Methods *****/
    /***********************/

    public String getSutName(DockerExecution dockerExec) {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
        return this.getSutPrefix(dockerExec)
                + (sut.isDockerCommandsSut() && sut.isSutInNewContainer()
                        ? "_" + sut.getSutInContainerAuxLabel()
                        : "");
    }

    public String getSutPrefix(DockerExecution dockerExec) {
        SutSpecification sut = dockerExec.gettJobexec().getTjob().getSut();
        String prefix = "sut_" + dockerExec.getExecutionId();

        if (sut.isDockerCommandsSut() && sut.isSutInNewContainer()) {
            // If is Docker compose Sut
            if (sut.getMainService() != null
                    && !"".equals(sut.getMainService())) {
                prefix = "sut" + dockerExec.getExecutionId();
            }
        }

        return prefix;

    }

    public void createSutContainer(DockerExecution dockerExec)
            throws TJobStoppedException {
        // Create Container
        dockerExec.setAppContainer(createContainer(dockerExec, "sut"));

        String appContainerId = dockerExec.getAppContainer().getId();
        dockerExec.setAppContainerId(appContainerId);
    }

    public void startSutcontainer(DockerExecution dockerExec) {
        String sutName = getSutName(dockerExec);
        String sutContainerId = dockerExec.getAppContainerId();

        dockerExec.getDockerClient().startContainerCmd(sutContainerId).exec();
        this.insertCreatedContainer(sutContainerId, sutName);
    }

    public String getCheckName(DockerExecution dockerExec) {
        return "check_" + dockerExec.getExecutionId();
    }

    public void checkSut(DockerExecution dockerExec, String ip, String port)
            throws Exception {
        String envVar = "IP=" + ip;
        String envVar2 = "PORT=" + port;
        ArrayList<String> envList = new ArrayList<>();
        envList.add(envVar);
        envList.add(envVar2);

        this.doPull(dockerExec.getDockerClient(), checkImage);

        String checkName = getCheckName(dockerExec);
        String checkContainerId = dockerExec.getDockerClient()
                .createContainerCmd(checkImage).withEnv(envList)
                .withName(checkName).withNetworkMode(dockerExec.getNetwork())
                .exec().getId();
        dockerExec.getDockerClient().startContainerCmd(checkContainerId).exec();
        this.insertCreatedContainer(checkContainerId, checkName);

        try {
            dockerExec.getDockerClient().waitContainerCmd(checkContainerId)
                    .exec(new WaitContainerResultCallback()).awaitStatusCode();
            logger.info("Sut is ready " + dockerExec.getExecutionId());

        } catch (DockerClientException e) {
            logger.info(
                    "Error on Waiting for CheckSut. Probably because the user has stopped the execution");
            throw new TJobStoppedException();
        } catch (Exception e) {
        }
    }

    /****************/
    /***** Test *****/
    /****************/

    public String getTestName(DockerExecution dockerExec) {
        return "test_" + dockerExec.getExecutionId();
    }

    public void createTestContainer(DockerExecution dockerExec)
            throws TJobStoppedException {
        try {
            CreateContainerResponse testContainer = createContainer(dockerExec,
                    "tjob");
            String testContainerId = testContainer.getId();

            dockerExec.setTestcontainer(testContainer);
            dockerExec.setTestContainerId(testContainerId);
        } catch (DockerClientException dce) {
            throw new TJobStoppedException();
        } catch (TJobStoppedException dce) {
            throw new TJobStoppedException();
        }
    }

    public List<ReportTestSuite> startTestContainer(DockerExecution dockerExec)
            throws TJobStoppedException {
        try {
            String testContainerId = dockerExec.getTestContainerId();
            String testName = getTestName(dockerExec);

            dockerExec.getDockerClient().startContainerCmd(testContainerId)
                    .exec();
            this.insertCreatedContainer(testContainerId, testName);

            int code = dockerExec.getDockerClient()
                    .waitContainerCmd(testContainerId)
                    .exec(new WaitContainerResultCallback()).awaitStatusCode();
            dockerExec.setTestContainerExitCode(code);
            logger.info("Test container ends with code " + code);

            return getTestResults(dockerExec);

        } catch (DockerClientException dce) {
            throw new TJobStoppedException();
        }
    }

    public LogConfig getLogConfig(String host, String port, String tagPrefix,
            String tagSuffix, DockerExecution dockerExec) {
        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("syslog-address", "tcp://" + host + ":" + port);
        if (tagSuffix != null && !tagSuffix.equals("")) {
            tagSuffix = "_" + tagSuffix;
        }
        configMap.put("tag",
                tagPrefix + dockerExec.getExecutionId() + tagSuffix + "_exec");

        LogConfig logConfig = new LogConfig();
        logConfig.setType(LoggingType.SYSLOG);
        logConfig.setConfig(configMap);

        return logConfig;
    }

    public LogConfig getDefaultLogConfig(String port, String tagPrefix,
            String tagSuffix, DockerExecution dockerExec) {
        logstashHost = getContainerIpByNetwork(etEtmLogstashContainerName,
                elastestNetwork);
        logger.info(
                "Logstash Host to send logs from containers: {}. To port {}",
                logstashHost, port);

        return this.getLogConfig(logstashHost, port, tagPrefix, tagSuffix,
                dockerExec);
    }

    public LogConfig getEMSLogConfig(String type, String tagPrefix,
            String tagSuffix, DockerExecution dockerExec) throws Exception {
        TJobExecution tJobExec = dockerExec.gettJobexec();
        String host = null;
        String port = null;
        // ET_EMS env vars created in EsmService setTssEnvVarByEndpoint()
        if ("tjob".equals(type.toLowerCase())) {
            host = tJobExec.getEnvVars().get("ET_EMS_TCP_TESTLOGS_HOST");
            port = tJobExec.getEnvVars().get("ET_EMS_TCP_TESTLOGS_PORT");
        } else if ("sut".equals(type.toLowerCase())) {
            host = tJobExec.getEnvVars().get("ET_EMS_TCP_SUTLOGS_HOST");
            port = tJobExec.getEnvVars().get("ET_EMS_TCP_SUTLOGS_PORT");
        }

        if (host != null && port != null) {
            logger.info(
                    "EMS Host to send logs from {} container: {}. To port {}",
                    type, host, port);
            return this.getLogConfig(host, port, tagPrefix, tagSuffix,
                    dockerExec);
        } else {
            throw new Exception("Error on get EMS Log config");
        }
    }

    /*********************************/
    /***** End execution methods *****/
    /*********************************/

    public void endContainer(String containerName) {
        DockerClient dockerClient = this.getDockerClient();
        if (existsContainer(containerName)) {
            String containerId = getContainerIdByName(containerName);
            int timeout = 60;
            try {
                logger.info("Stopping " + containerName + " container");
                dockerClient.stopContainerCmd(containerId).withTimeout(timeout)
                        .exec();
                // Wait
                dockerClient.waitContainerCmd(containerId)
                        .exec(new WaitContainerResultCallback())
                        .awaitStatusCode();
            } catch (DockerClientException e) {
                // throw new TJobStoppedException();
            } catch (NotModifiedException e) {
                logger.info(
                        "Container " + containerName + " is already stopped");
            } catch (Exception e) {
                logger.info(
                        "Error during stop " + containerName + " container {}",
                        e);
            } finally {
                try {
                    logger.info("Removing " + containerName + " container");
                    dockerClient.removeContainerCmd(containerId).exec();
                } catch (DockerClientException e) {
                    // throw new TJobStoppedException();
                } catch (Exception e) {
                    logger.info("Error during remove " + containerName
                            + " container");
                }
                createdContainers.remove(containerId);
            }
        } else {
            logger.info("Could not end " + containerName
                    + " container -> Not started.");
        }
    }

    public void removeSutVolumeFolder(DockerExecution dockerExec) {
        TJobExecution tJobExec = dockerExec.gettJobexec();
        String sutPath = filesService.buildFilesPath(tJobExec,
                ElastestConstants.SUT_FOLDER);
        try {
            filesService.removeExecFilesFolder(sutPath);
        } catch (Exception e) {
            logger.debug("The SuT folder could not be deleted: {}",
                    e.getMessage());
        }
    }

    /*****************/
    /***** Utils *****/
    /**
     * @throws TJobStoppedException
     ***************/

    public void doPull(DockerClient dockerClient, String image)
            throws TJobStoppedException {
        image = image.contains(":") ? image
                : image.concat(ElastestConstants.TAG_LATEST);
        try {
            dockerClient.pullImageCmd(image).exec(new PullImageResultCallback())
                    .awaitSuccess();
        } catch (InternalServerErrorException | NotFoundException ie) {
            if (imageExistsLocally(image, dockerClient)) {
                logger.info("Docker image exits locally.");
            } else {
                logger.error("Error pulling the image: {}", ie.getMessage());
                throw ie;
            }
        } catch (DockerClientException e) {
            logger.info(
                    "Error on Pulling {}. Probably because the user has stopped the execution",
                    image);
            throw new TJobStoppedException();
        }
    }

    public void doPull(String image) throws TJobStoppedException {
        DockerClient dockerClient = this.getDockerClient();
        this.doPull(dockerClient, image);
    }

    public String getContainerIpWithDockerExecution(String containerId,
            DockerExecution dockerExec) throws Exception {
        String ip = dockerExec.getDockerClient()
                .inspectContainerCmd(containerId).exec().getNetworkSettings()
                .getNetworks().get(dockerExec.getNetwork()).getIpAddress();
        return ip.split("/")[0];

    }

    public String waitForContainerIpWithDockerExecution(String containerId,
            DockerExecution dockerExec, long timeout) throws Exception {
        long start_time = System.currentTimeMillis();
        long end_time = start_time + timeout;

        String containerIp = null;

        while (containerIp == null && System.currentTimeMillis() < end_time) {
            try {
                containerIp = this.getContainerIpWithDockerExecution(
                        containerId, dockerExec);
            } catch (Exception e) {
                logger.info(
                        "Container with id {} is no reachable yet. Retrying...",
                        containerId);
            }
            Thread.sleep(1500);
        }

        if (containerIp == null) {
            throw new Exception("Container with id " + containerId
                    + " non reachable. Timeout!");
        }
        return containerIp;
    }

    public boolean waitForContainerCreated(String containerId,
            DockerExecution dockerExec, long timeout) throws Exception {
        long start_time = System.currentTimeMillis();
        long end_time = start_time + timeout;

        boolean created = false;

        while (!created && System.currentTimeMillis() < end_time) {
            try {
                created = this.existsContainer(containerId);
            } catch (Exception e) {
                logger.info(
                        "Container with id {} is not created yet. Retrying...",
                        containerId);
            }
            Thread.sleep(1500);
        }

        if (!created) {
            throw new Exception("Container with id " + containerId
                    + " not created. Timeout!");
        } else {
            return created;
        }
    }

    public String getContainerIpByNetwork(String containerId, String network) {
        DockerClient client = getDockerClient();

        String ip = client.inspectContainerCmd(containerId).exec()
                .getNetworkSettings().getNetworks().get(network).getIpAddress();
        return ip.split("/")[0];
    }

    public String getNetworkName(String containerId,
            DockerClient dockerClient) {
        return (String) dockerClient.inspectContainerCmd(containerId).exec()
                .getNetworkSettings().getNetworks().keySet().toArray()[0];
    }

    public String getHostIp(DockerExecution dockerExec) {
        return dockerExec.getDockerClient().inspectNetworkCmd()
                .withNetworkId(dockerExec.getNetwork()).exec().getIpam()
                .getConfig().get(0).getGateway();
    }

    public String getHostIpByNetwork(String network) {
        return this.getDockerClient().inspectNetworkCmd().withNetworkId(network)
                .exec().getIpam().getConfig().get(0).getGateway();
    }

    public boolean imageExist(String imageName, DockerExecution dockerExec) {
        return !dockerExec.getDockerClient().searchImagesCmd(imageName).exec()
                .isEmpty();
    }

    public boolean imageExistsLocally(String imageName,
            DockerClient dockerClient) {
        boolean imageExists = false;
        try {
            dockerClient.inspectImageCmd(imageName).exec();
            imageExists = true;
        } catch (NotFoundException nfe) {
            imageExists = false;
        }
        return imageExists;
    }

    public void insertIntoNetwork(String networkId, String containerId) {
        boolean isAreadyInNetwork = this.isContainerIntoNetwork(networkId,
                containerId);
        if (!isAreadyInNetwork) {
            DockerClient client = getDockerClient();
            client.connectToNetworkCmd().withNetworkId(networkId)
                    .withContainerId(containerId).exec();
        }
    }

    public boolean isContainerIntoNetwork(String networkId,
            String containerId) {
        DockerClient client = getDockerClient();
        Map<String, ContainerNetwork> networksMap = client
                .inspectContainerCmd(containerId).exec().getNetworkSettings()
                .getNetworks();
        return networksMap.get(networkId) != null;
    }

    public String getLogstashHost(DockerExecution dockerExec) {
        if (logstashHost == null) {
            return getHostIpByNetwork(dockerExec.getNetwork());
        }
        return logstashHost;
    }

    public SocatBindedPort bindingPort(String containerIp, String port,
            String networkName) throws Exception {
        int listenPort = 37000;
        String bindedPort = null;
        try {
            listenPort = UtilTools.findRandomOpenPort();
            List<String> envVariables = new ArrayList<>();
            envVariables.add("LISTEN_PORT=" + listenPort);
            envVariables.add("FORWARD_PORT=" + port);
            envVariables.add("TARGET_SERVICE_IP=" + containerIp);
            Ports portBindings = new Ports();
            ExposedPort exposedListenPort = ExposedPort.tcp(listenPort);

            portBindings.bind(exposedListenPort,
                    Ports.Binding.bindPort(listenPort));

            bindedPort = this.runDockerContainer(etSocatImage, envVariables,
                    "container" + listenPort, networkName, portBindings,
                    listenPort);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }

        SocatBindedPort bindedPortObj = new SocatBindedPort(
                Integer.toString(listenPort), bindedPort);

        return bindedPortObj;
    }

    public List<Container> getContainersByNamePrefix(String prefix) {
        DockerClient dockerClient = this.getDockerClient();
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true).exec();

        return this.getContainersByNamePrefixByGivenList(containers, prefix,
                ContainersListActionEnum.NONE);
    }

    public List<Container> getContainersCreatedSinceId(String startId) {
        DockerClient dockerClient = this.getDockerClient();
        return dockerClient.listContainersCmd().withShowAll(true)
                .withSince(startId).exec();
    }

    public List<Container> getContainersByNamePrefixByGivenList(
            List<Container> containersList, String prefix,
            ContainersListActionEnum action) {
        List<Container> filteredList = new ArrayList<>();
        for (Container currentContainer : containersList) {
            // Get name (name start with slash, we remove it)
            String containerName = currentContainer.getNames()[0]
                    .replaceFirst("/", "");
            if (containerName != null && containerName.startsWith(prefix)) {
                filteredList.add(currentContainer);
                if (action != ContainersListActionEnum.NONE) {
                    if (action == ContainersListActionEnum.ADD) {
                        this.insertCreatedContainer(currentContainer.getId(),
                                containerName);
                        try {
                            this.insertIntoNetwork(this.elastestNetwork,
                                    currentContainer.getId());
                        } catch (Exception e) {
                            // Already added
                        }
                    } else {
                        this.endContainer(containerName);
                    }
                }
            }
        }

        return filteredList;
    }

    public String getTagByCompleteImageName(String imageName) {
        if (imageName == null) {
            return imageName;
        }
        String[] imageNameSplitted = imageName.split(":");
        String tag = null;
        
        if (imageNameSplitted[1] != null) {
            tag = imageNameSplitted[1];
        } else {
            tag = "latest";
        }
        return tag;
    }

    public String getImageNameByCompleteImageName(String imageName) {
        if (imageName == null) {
            return imageName;
        }
        return imageName.split(":")[0];
    }

    public String getAllContainerLogs(String containerId, boolean withFollow) {
        DockerClient dockerClient = this.getDockerClient();
        LogContainerCmd logContainerCmd = dockerClient
                .logContainerCmd(containerId);

        if (withFollow) {
            logContainerCmd = logContainerCmd.withFollowStream(true);
        }

        return this.getContainerLogsByGivenLogContainerCmd(logContainerCmd);
    }

    public String getSomeContainerLogs(String containerId, int amount,
            boolean withFollow) {
        DockerClient dockerClient = this.getDockerClient();
        LogContainerCmd logContainerCmd = dockerClient
                .logContainerCmd(containerId).withTail(amount);

        if (withFollow) {
            logContainerCmd = logContainerCmd.withFollowStream(true);
        }

        return this.getContainerLogsByGivenLogContainerCmd(logContainerCmd);
    }

    /*
     * since time in seconds
     */
    public String getContainerLogsSinceDate(String containerId, int since,
            boolean withFollow) {
        DockerClient dockerClient = this.getDockerClient();
        LogContainerCmd logContainerCmd = dockerClient
                .logContainerCmd(containerId).withSince(since).withTailAll();
        if (withFollow) {
            logContainerCmd = logContainerCmd.withFollowStream(true);
        }

        return this.getContainerLogsByGivenLogContainerCmd(logContainerCmd);
    }

    public String getContainerLogsByGivenLogContainerCmd(
            LogContainerCmd logContainerCmd) {
        StringBuilder logs = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(1);

        logContainerCmd = logContainerCmd.withStdOut(true).withStdErr(true)
                .withTimestamps(true);
        logContainerCmd.exec(getLogsResultCallback(latch, logs));

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(
                    "Interrupted when waiting for complete result callback on docker logs",
                    e);
        }
        return logs.toString();
    }

    public ResultCallback<Frame> getLogsResultCallback(CountDownLatch latch,
            StringBuilder logs) {
        return new ResultCallback<Frame>() {

            @Override
            public void close() throws IOException {

            }

            @Override
            public void onStart(Closeable closeable) {
            }

            @Override
            public void onNext(Frame f) {
                logs.append(new String(f.getPayload()));
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("Error on get container logs: {}",
                        throwable.getMessage());

            }

            @Override
            public void onComplete() {
                latch.countDown();
            }

        };
    }

    /***************************/
    /***** Get TestResults *****/
    /***************************/

    public InputStream getFileFromContainer(String containerName,
            String fileName, DockerExecution dockerExec) {
        InputStream inputStream = null;
        if (existsContainer(getTestName(dockerExec))) {
            inputStream = dockerExec.getDockerClient()
                    .copyArchiveFromContainerCmd(containerName, fileName)
                    .exec();
        }
        return inputStream;
    }

    public boolean existsContainer(String containerName) {
        DockerClient dockerClient = this.getDockerClient();
        return this.existsContainer(containerName, dockerClient);
    }

    public boolean existsContainer(String containerName,
            DockerClient dockerClient) {
        boolean exists = true;
        try {
            dockerClient.inspectContainerCmd(containerName).exec();
        } catch (NotFoundException e) {
            exists = false;
        }
        return exists;
    }

    public String getContainerIdByName(String containerName) {
        DockerClient dockerClient = this.getDockerClient();
        String id = "";
        if (existsContainer(containerName)) {
            try {

                InspectContainerResponse response = dockerClient
                        .inspectContainerCmd(containerName).exec();
                id = response.getId();
            } catch (Exception e) {

            }
        }
        return id;
    }

    public InspectContainerResponse getContainerInfoByName(
            String containerName) {
        InspectContainerResponse response = null;
        DockerClient dockerClient = getDockerClient();
        if (existsContainer(containerName, dockerClient)) {
            try {
                response = dockerClient.inspectContainerCmd(containerName)
                        .exec();
            } catch (Exception e) {

            }
        }
        return response;
    }

    public InspectImageResponse getImageInfoByName(String imageName) {
        InspectImageResponse response = null;
        DockerClient dockerClient = getDockerClient();
        if (imageExistsLocally(imageName, dockerClient)) {
            try {
                response = dockerClient.inspectImageCmd(imageName).exec();
            } catch (Exception e) {
                logger.error("Error loading image \"{}\" information.",
                        imageName);
                throw e;
            }
        }
        return response;
    }

    public List<Container> getRunningContainersByImageName(String imageName) {
        imageName += ":";
        DockerClient dockerClient = getDockerClient();
        List<Container> allContainers = dockerClient.listContainersCmd()
                .withShowAll(true).exec();
        List<Container> imageContainers = new ArrayList<>();
        for (Container currentContainer : allContainers) {
            if (currentContainer.getImage().startsWith(imageName)) {
                imageContainers.add(currentContainer);
            }
        }
        return imageContainers;
    }

    public List<Container> getRunningContainersByImageNameAndVersion(
            String imageName, String version) {
        DockerClient dockerClient = getDockerClient();
        List<Container> allContainers = dockerClient.listContainersCmd()
                .withShowAll(true).exec();
        List<Container> imageContainers = new ArrayList<>();
        for (Container currentContainer : allContainers) {
            if (currentContainer.getImage().equals(imageName)) {
                imageContainers.add(currentContainer);
            }
        }
        return imageContainers;
    }

    private List<ReportTestSuite> getTestResults(DockerExecution dockerExec) {
        List<ReportTestSuite> testSuites = null;
        String resultsPath = dockerExec.gettJobexec().getTjob()
                .getResultsPath();

        if (resultsPath != null && !resultsPath.isEmpty()) {
            try {
                InputStream inputStream = getFileFromContainer(
                        dockerExec.getTestContainerId(), resultsPath,
                        dockerExec);

                String result = IOUtils.toString(inputStream,
                        StandardCharsets.UTF_8);
                testSuites = getTestSuitesByString(result);
            } catch (IOException e) {
            }
        }
        return testSuites;
    }

    private List<ReportTestSuite> getTestSuitesByString(String result) {
        List<ReportTestSuite> results = new ArrayList<>();
        String head = "<testsuite ";
        String foot = "</testsuite>";

        List<String> splitedHeadResult = new ArrayList<String>(
                Arrays.asList(result.split(head)));
        if (splitedHeadResult != null) {
            if (!result.startsWith(head)) { // delete non-deseable string
                                            // (surefire-reports/)
                splitedHeadResult.remove(0);
            }
            for (String piece : splitedHeadResult) {
                List<String> splitedFootResult = new ArrayList<String>(
                        Arrays.asList(piece.split(foot)));
                String newResult = head + splitedFootResult.get(0) + foot;

                ReportTestSuite testSuite = null;

                try {
                    testSuite = this
                            .testSuiteStringToReportTestSuite(newResult);
                } catch (ParserConfigurationException | SAXException
                        | IOException e) {
                    logger.error("Error on parse testSuite {}", e);
                }

                if (testSuite != null) {
                    results.add(testSuite);
                }
            }
        }

        return results;
    }

    private ReportTestSuite testSuiteStringToReportTestSuite(
            String testSuiteStr) throws UnsupportedEncodingException,
            ParserConfigurationException, SAXException, IOException {
        TestSuiteXmlParser testSuiteXmlParser = new TestSuiteXmlParser(null);
        InputStream byteArrayIs = new ByteArrayInputStream(
                testSuiteStr.getBytes());
        return testSuiteXmlParser
                .parse(new InputStreamReader(byteArrayIs, "UTF-8")).get(0);
    }
}
