package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.exceptions.ImagePullFailedException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.HostConfig.Bind.Builder;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;

import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.model.DockerContainer;
import io.elastest.etm.model.DockerContainer.DockerBuilder;
import io.elastest.etm.model.TJobExecution.ResultEnum;
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

    final Logger logger = getLogger(lookup().lookupClass());

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

    @Value("${et.master.slave.mode}")
    private boolean masterSlavemode;

    @Value("${et.public.host}")
    private String etPublicHost;

    @Value("${et.etm.binded.lsbeats.port)")
    private String etEtmBindedLsbeatsPort;

    @Value("${et.etm.binded.lstcp.host}")
    public String bindedLsTcpHost;

    @Value("${et.etm.binded.lstcp.port}")
    public String bindedLsTcpPort;

    public DockerService2 dockerService;
    public FilesService filesService;
    public TJobExecRepository tJobExecRepositoryImpl;

    @Autowired
    public DockerEtmService(DockerService2 dockerService,
            FilesService filesService,
            TJobExecRepository tJobExecRepositoryImpl) {
        this.dockerService = dockerService;
        this.filesService = filesService;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
    }

    public String getThisContainerIpCmd = "ip a | grep -m 1 global | grep -oE '([0-9]{1,3}\\.){3}[0-9]{1,3}\\/' | grep -oE '([0-9]{1,3}\\.){3}[0-9]{1,3}'";

    @PostConstruct
    public void initialize() throws Exception {
        initLogstashHostIfNecessary();

        logger.info("Pulling dockbeat image...");
        try {
            this.pullETExecImage(dockbeatImage, "Dockbeat", true);
        } catch (TJobStoppedException | DockerCertificateException
                | DockerException | InterruptedException e) {
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
                logger.error("Error on stop or remove container {}: {}",
                        containerId, e.getMessage());
            }
        }
    }

    /* ************************ */
    /* **** Config Methods **** */
    /* ************************ */

    public void configureDocker(DockerExecution dockerExec) throws Exception {
        DockerClient client = dockerService.getDockerClient(true);
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

    private void initLogstashHostIfNecessary() throws Exception {
        if (logstashHost == null) {
            logstashHost = masterSlavemode ? etPublicHost
                    : dockerService.getContainerIpByNetwork(
                            etEtmLogstashContainerName, elastestNetwork);
        }
    }

    /* *************************** */
    /* **** Container Methods **** */
    /* *************************** */

    public DockerContainer createContainer(DockerExecution dockerExec,
            String type) throws Exception {
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
        this.pullETExecutionImage(dockerExec, image, type, false);

        /* ******************************************************** */
        DockerBuilder dockerBuilder = new DockerBuilder(image);
        dockerBuilder.envs(envList);
        dockerBuilder.logConfig(logConfig);
        dockerBuilder.containerName(containerName);
        dockerBuilder.cmd(cmdList);
        dockerBuilder.entryPoint(entrypointList);
        dockerBuilder.network(dockerExec.getNetwork());

        boolean sharedDataVolume = false;
        if (sut != null && sut.isSutInNewContainer()) {
            sharedDataVolume = true;
        }

        List<Bind> volumes = new ArrayList<>();

        Builder dockerSockVolumeBuilder = Bind.builder();
        dockerSockVolumeBuilder.from(dockerSock);
        dockerSockVolumeBuilder.to(dockerSock);

        volumes.add(dockerSockVolumeBuilder.build());
        if (sharedDataVolume) {
            Builder sharedDataVolumeBuilder = Bind.builder();
            sharedDataVolumeBuilder.from(sharedFolder);
            sharedDataVolumeBuilder.to(sharedFolder);

            volumes.add(sharedDataVolumeBuilder.build());
        }

        dockerBuilder.volumeBindList(volumes);

        // Create DockerContainer object
        return dockerBuilder.build();
    }

    public void pullETExecImageWithProgressHandler(String image, String name,
            boolean forcePull, ProgressHandler progressHandler)
            throws DockerException, InterruptedException, Exception {
        logger.debug("Try to Pulling {} Image ({})", name, image);
        // try {
        if (forcePull) {
            dockerService.pullImageWithProgressHandler(image, progressHandler);
        } else {
            dockerService.pullImageIfNotExistWithProgressHandler(image,
                    progressHandler);
        }
        // } catch (DockerClientException e) {
        //
        // logger.info(
        // "Error probably because the user has stopped the execution",
        // e);
        // throw new TJobStoppedException();
        // }
        logger.debug("{} image pulled succesfully!", name);

    }

    public void pullETExecImage(String image, String name, boolean forcePull)
            throws DockerException, InterruptedException, Exception {
        this.pullETExecImageWithProgressHandler(image, name, forcePull,
                new ProgressHandler() {
                    @Override
                    public void progress(ProgressMessage message)
                            throws DockerException {
                    }
                });
    }

    public void pullETExecutionImage(DockerExecution dockerExec, String image,
            String name, boolean forcePull)
            throws DockerException, InterruptedException, Exception {
        DockerPullImageProgress dockerPullImageProgress = new DockerPullImageProgress();
        dockerPullImageProgress.setImage(image);
        dockerPullImageProgress.setCurrentPercentage(0);

        ProgressHandler progressHandler = new ProgressHandler() {
            @Override
            public void progress(ProgressMessage message)
                    throws DockerException {
                if (message.error() != null) {
                    if (message.error().contains("404")
                            || message.error().contains("not found")) {
                        throw new ImageNotFoundException(image,
                                message.toString());
                    } else {
                        throw new ImagePullFailedException(image,
                                message.toString());
                    }
                }
                dockerPullImageProgress.processNewMessage(message);

                TJobExecution tJobExec = dockerExec.gettJobexec();
                // tJobExec.setResult(result);
                String msg = "Pulling " + name + " image (" + image + "): "
                        + dockerPullImageProgress.getCurrentPercentage() + "%";
                tJobExec.setResultMsg(msg);
                tJobExecRepositoryImpl.save(tJobExec);
            }

        };

        this.pullETExecImageWithProgressHandler(image, name, forcePull,
                progressHandler);
    }

    /* ****************** */
    /* **** Dockbeat **** */
    /* ****************** */

    public String getDockbeatContainerName(DockerExecution dockerExec) {
        return "elastest_dockbeat_" + dockerExec.getExecutionId();

    }

    public void startDockbeat(DockerExecution dockerExec) throws Exception {
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
        Builder dockerSockVolumeBuilder = Bind.builder();
        dockerSockVolumeBuilder.from(dockerSock);
        dockerSockVolumeBuilder.to(dockerSock);

        // Pull Image
        this.pullETExecImage(dockbeatImage, "Dockbeat", false);

        // Create Container
        logger.debug("Creating Dockbeat Container...");

        /* ******************************************************** */
        DockerBuilder dockerBuilder = new DockerBuilder(dockbeatImage);
        dockerBuilder.envs(envList);
        dockerBuilder.containerName(containerName);
        dockerBuilder.network(dockerExec.getNetwork());

        dockerBuilder
                .volumeBindList(Arrays.asList(dockerSockVolumeBuilder.build()));

        DockerContainer dockerContainer = dockerBuilder.build();
        String containerId = dockerService
                .createAndStartContainer(dockerContainer);
        this.insertCreatedContainer(containerId, containerName);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    /* ********************* */
    /* **** Sut Methods **** */
    /* ********************* */

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

    public void createAndStartSutContainer(DockerExecution dockerExec)
            throws Exception {
        try {
            // Create Container Object
            dockerExec.setAppContainer(createContainer(dockerExec, "sut"));

            TJobExecution tJobExec = dockerExec.gettJobexec();
            String resultMsg = "Starting dockerized SuT";
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.EXECUTING_SUT, resultMsg);
            logger.info(resultMsg + " " + dockerExec.getExecutionId());

            // Create and start container
            String sutContainerId = dockerService
                    .createAndStartContainer(dockerExec.getAppContainer());
            dockerExec.setAppContainerId(sutContainerId);

            String sutName = getSutName(dockerExec);
            this.insertCreatedContainer(sutContainerId, sutName);
        } catch (TJobStoppedException e) {
            throw new TJobStoppedException(
                    "Error on create and start Sut container", e);
        } catch (Exception e) {
            throw new Exception("Error on create and start Sut container", e);
        }
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

        dockerService.pullImageIfNotExist(checkImage);

        String checkName = getCheckName(dockerExec);

        DockerBuilder dockerBuilder = new DockerBuilder(checkImage);
        dockerBuilder.envs(envList);
        dockerBuilder.containerName(checkName);
        dockerBuilder.network(dockerExec.getNetwork());

        DockerContainer dockerContainer = dockerBuilder.build();
        String checkContainerId = dockerService
                .createAndStartContainer(dockerContainer);

        this.insertCreatedContainer(checkContainerId, checkName);

        int statusCode = dockerExec.getDockerClient()
                .waitContainer(checkContainerId).statusCode();
        if (statusCode == 0) {
            logger.info("Sut is ready " + dockerExec.getExecutionId());

        } else { // TODO timeout or catch stop execution
            throw new TJobStoppedException(
                    "Error on Waiting for CheckSut. Probably because the user has stopped the execution");
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

    /* ************** */
    /* **** Test **** */
    /* ************** */

    public String getTestName(DockerExecution dockerExec) {
        return "test_" + dockerExec.getExecutionId();
    }

    public List<ReportTestSuite> createAndStartTestContainer(
            DockerExecution dockerExec) throws Exception {

        try {
            // Create Container Object
            dockerExec.setTestcontainer(createContainer(dockerExec, "tjob"));

            TJobExecution tJobExec = dockerExec.gettJobexec();
            String resultMsg = "Starting Test Execution";
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);

            // Create and start container
            String testContainerId = dockerService
                    .createAndStartContainer(dockerExec.getTestcontainer());
            dockerExec.setTestContainerId(testContainerId);

            resultMsg = "Executing Test";
            updateTJobExecResultStatus(tJobExec,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);

            String testName = getTestName(dockerExec);
            this.insertCreatedContainer(testContainerId, testName);

            int code = dockerExec.getDockerClient()
                    .waitContainer(testContainerId).statusCode();

            dockerExec.setTestContainerExitCode(code);
            logger.info("Test container ends with code " + code);

            return getTestResults(dockerExec);
        } catch (TJobStoppedException | InterruptedException e) {
            throw new TJobStoppedException(
                    "Error on create and start TJob container: Stopped", e);
        } catch (Exception e) {
            throw new Exception("Error on create and start TJob container", e);
        }
    }

    public LogConfig getLogConfig(String host, String port, String tagPrefix,
            String tagSuffix, DockerExecution dockerExec) {
        Map<String, String> configMap = new HashMap<String, String>();

        if (tagSuffix != null && !tagSuffix.equals("")) {
            tagSuffix = "_" + tagSuffix;
        }
        configMap.put("tag",
                tagPrefix + dockerExec.getExecutionId() + tagSuffix + "_exec");

        LogConfig logConfig = null;

        configMap.put("syslog-address", "tcp://" + host + ":" + port);
        logConfig = LogConfig.create("syslog", configMap);

        return logConfig;
    }

    public LogConfig getDefaultLogConfig(String port, String tagPrefix,
            String tagSuffix, DockerExecution dockerExec) throws Exception {

        initLogstashHostIfNecessary();
        port = masterSlavemode ? bindedLsTcpPort : port;
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

    public void updateTJobExecResultStatus(TJobExecution tJobExec,
            ResultEnum result, String msg) {
        tJobExec.setResult(result);
        tJobExec.setResultMsg(msg);
        tJobExecRepositoryImpl.save(tJobExec);
    }

    /* ******************************* */
    /* **** End execution methods **** */
    /* ******************************* */

    /* *************** */
    /* **** Utils **** */
    /* *************** */

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

    public String getHostIp(DockerExecution dockerExec)
            throws DockerException, InterruptedException {
        return dockerExec.getDockerClient()
                .inspectNetwork(dockerExec.getNetwork()).ipam().config().get(0)
                .gateway();
    }

    public String getLogstashHost(DockerExecution dockerExec)
            throws DockerException, InterruptedException {
        if (logstashHost == null) {
            return this.getHostIp(dockerExec);
        }
        return logstashHost;
    }

    public void removeDockerContainer(String containerId) throws Exception {
        dockerService.removeDockerContainer(containerId);
        createdContainers.remove(containerId);
    }

    public void endContainer(String containerName) throws Exception {
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
            String listenPortAsString = String.valueOf(listenPort);

            DockerBuilder dockerBuilder = new DockerBuilder(etSocatImage);
            dockerBuilder.envs(envVariables);
            dockerBuilder.containerName("container" + listenPortAsString);
            dockerBuilder.network(networkName);
            dockerBuilder
                    .exposedPorts(Arrays.asList(String.valueOf(listenPort)));

            // portBindings
            Map<String, List<PortBinding>> portBindings = new HashMap<>();
            portBindings.put(listenPortAsString,
                    Arrays.asList(PortBinding.of("", listenPortAsString)));
            dockerBuilder.portBindings(portBindings);

            bindedPort = dockerService
                    .createAndStartContainer(dockerBuilder.build());

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
            ContainersListActionEnum action, String network) throws Exception {
        List<Container> filteredList = new ArrayList<>();
        for (Container currentContainer : containersList) {
            // Get name (name start with slash, we remove it)
            String containerName = currentContainer.names().get(0)
                    .replaceFirst("/", "");
            if (containerName != null && containerName.startsWith(prefix)) {
                filteredList.add(currentContainer);
                if (action != ContainersListActionEnum.NONE) {
                    if (action == ContainersListActionEnum.ADD) {
                        this.insertCreatedContainer(currentContainer.id(),
                                containerName);
                        try {
                            dockerService.insertIntoNetwork(network,
                                    currentContainer.id());
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

    /* ************************* */
    /* **** Get TestResults **** */
    /* ************************* */

    private List<ReportTestSuite> getTestResults(DockerExecution dockerExec)
            throws Exception {
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
