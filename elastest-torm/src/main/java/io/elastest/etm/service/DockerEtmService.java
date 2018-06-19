package io.elastest.etm.service;

import java.io.ByteArrayInputStream;
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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.LogConfig;
import com.github.dockerjava.api.model.LogConfig.LoggingType;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SocatBindedPort;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.service.DockerService2.ContainersListActionEnum;
import io.elastest.etm.utils.ElastestConstants;
import io.elastest.etm.utils.FilesService;
import io.elastest.etm.utils.UtilTools;

@Service
public class DockerEtmService {

    private static final Logger logger = LoggerFactory
            .getLogger(DockerEtmService.class);

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
    public DockerService2 dockerService;

    @Autowired
    public FilesService filesService;

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
                dockerService.stopDockerContainer(containerId);
                this.removeDockerContainer(containerId);
                logger.info("Container {} removed", containerName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**************************/
    /***** Config Methods *****/
    /**************************/

    public void configureDocker(DockerExecution dockerExec) {
        DockerClient client = dockerService.getDockerClient();
        dockerExec.setDockerClient(client);
    }

    public void loadBasicServices(DockerExecution dockerExec) throws Exception {
        this.configureDocker(dockerExec);
        dockerExec.setNetwork(elastestNetwork);
    }

    public void insertCreatedContainer(String containerId,
            String containerName) {
        createdContainers.put(containerId, containerName);
    }

    /*****************************/
    /***** Container Methods *****/
    /*****************************/

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
        try {
            dockerService.doPull(image);
        } catch (DockerClientException e) {

            logger.info(
                    "Error probably because the user has stopped the execution",
                    e);
            throw new TJobStoppedException();
        }
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

        dockerService.doPull(dockerExec.getDockerClient(), checkImage);

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

    /*****************/
    /***** Utils *****/
    /**
     * @throws TJobStoppedException
     ***************/

    public String getContainerIpWithDockerExecution(String containerId,
            DockerExecution dockerExec) throws Exception {
        return dockerService.getContainerIpWithDockerClient(
                dockerExec.getDockerClient(), containerId,
                dockerExec.getNetwork());
    }

    public String waitForContainerIpWithDockerExecution(String containerId,
            DockerExecution dockerExec, long timeout) throws Exception {
        return dockerService.waitForContainerIpWithDockerClient(
                dockerExec.getDockerClient(), containerId,
                dockerExec.getNetwork(), timeout);
    }

    public String getContainerIpByNetwork(String containerId, String network) {
        DockerClient client = dockerService.getDockerClient();

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

    public String getLogstashHost(DockerExecution dockerExec) {
        if (logstashHost == null) {
            return this.getHostIp(dockerExec);
        }
        return logstashHost;
    }

    public String runDockerContainer(DockerClient dockerClient,
            String imageName, List<String> envs, String containerName,
            String networkName, Ports portBindings, Integer listenPort)
            throws TJobStoppedException {
        try {
            String containerId = dockerService.runDockerContainer(dockerClient,
                    imageName, envs, containerName, networkName, portBindings,
                    listenPort);
            this.insertCreatedContainer(containerId, containerName);
            return containerId;
        } catch (DockerClientException e) {
            logger.info(
                    "Error probably because the user has stopped the execution",
                    e);
            throw new TJobStoppedException();
        }
    }

    public String runDockerContainer(String imageName, List<String> envs,
            String containerName, String networkName, Ports portBindings,
            Integer listenPort) throws TJobStoppedException {
        return this.runDockerContainer(dockerService.getDockerClient(),
                imageName, envs, containerName, networkName, portBindings,
                listenPort);
    }

    public void removeDockerContainer(String containerId) {
        dockerService.removeDockerContainer(containerId);
        createdContainers.remove(containerId);
    }

    public void endContainer(String containerName) {
        dockerService.endContainer(containerName);
        String containerId = dockerService.getContainerIdByName(containerName);

        createdContainers.remove(containerId);
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

    public List<Container> getContainersByNamePrefixByGivenList(
            List<Container> containersList, String prefix,
            ContainersListActionEnum action, String network) {
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
                            dockerService.insertIntoNetwork(network,
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

    /***************************/
    /***** Get TestResults *****/
    /***************************/

    private List<ReportTestSuite> getTestResults(DockerExecution dockerExec) {
        List<ReportTestSuite> testSuites = null;
        String resultsPath = dockerExec.gettJobexec().getTjob()
                .getResultsPath();

        if (resultsPath != null && !resultsPath.isEmpty()) {
            try {
                InputStream inputStream = dockerService.getFileFromContainer(
                        dockerExec.getTestContainerId(), resultsPath);

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
