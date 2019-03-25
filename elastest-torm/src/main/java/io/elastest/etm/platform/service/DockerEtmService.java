package io.elastest.etm.platform.service;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.HostConfig.Bind.Builder;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;

import io.elastest.epm.client.DockerContainer;
import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.epm.client.model.DockerPullImageProgress;
import io.elastest.epm.client.service.DockerService;
import io.elastest.epm.client.service.EpmService;
import io.elastest.etm.dao.TJobExecRepository;
import io.elastest.etm.dao.external.ExternalTJobExecutionRepository;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.service.EtmTestResultService;
import io.elastest.etm.service.exception.TJobStoppedException;
import io.elastest.etm.utils.ElastestConstants;
import io.elastest.etm.utils.EtmFilesService;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

@Service
public class DockerEtmService {

    final Logger logger = getLogger(lookup().lookupClass());

    private static String checkImage = "elastest/etm-check-service-up";
    private static final Map<String, String> createdContainers = new HashMap<>();

    @Value("${logstash.host:#{null}}")
    private String logstashOrMiniHost;

    @Value("${et.etm.lstcp.port}")
    public String logstashTcpPort;

    @Value("${et.etm.internal.lsbeats.port}")
    private String lsInternalBeatsPort;

    @Value("${elastest.docker.network}")
    private String elastestNetwork;

    @Value("${et.docker.img.dockbeat}")
    private String dockbeatImage;

    @Value("${docker.sock}")
    private String dockerSock;

    @Value("${et.shared.folder}")
    private String sharedFolder;

    @Value("${et.etm.logstash.container.name}")
    private String etEtmLogstashContainerName;

    @Value("${et.etm.container.name}")
    private String etEtmContainerName;

    @Value("${et.edm.mysql.container.name}")
    private String etEdmMysqlContainerName;

    @Value("${et.master.slave.mode}")
    private boolean masterSlavemode;

    @Value("${et.etm.binded.lsbeats.port)")
    private String etEtmBindedLsbeatsPort;

    @Value("${et.etm.binded.lstcp.host}")
    public String bindedLsTcpHost;

    @Value("${et.etm.binded.lstcp.port}")
    public String bindedLsTcpPort;

    /* *** ET container labels *** */
    @Value("${et.type.label}")
    public String etTypeLabel;

    @Value("${et.tjob.id.label}")
    public String etTJobIdLabel;

    @Value("${et.tjob.exec.id.label}")
    public String etTJobExecIdLabel;

    @Value("${et.tjob.sut.service.name.label}")
    public String etTJobSutServiceNameLabel;

    @Value("${et.tjob.tss.id.label}")
    public String etTJobTSSIdLabel;

    @Value("${et.tjob.tss.type.label}")
    public String etTJobTssTypeLabel;

    @Value("${et.type.test.label.value}")
    public String etTypeTestLabelValue;

    @Value("${et.type.sut.label.value}")
    public String etTypeSutLabelValue;

    @Value("${et.type.tss.label.value}")
    public String etTypeTSSLabelValue;

    @Value("${et.type.core.label.value}")
    public String etTypeCoreLabelValue;

    @Value("${et.type.te.label.value}")
    public String etTypeTELabelValue;

    @Value("${et.type.monitoring.label.value}")
    public String etTypeMonitoringLabelValue;

    @Value("${et.type.tool.label.value}")
    public String etTypeToolLabelValue;
    /* *** END of ET container labels *** */

    public DockerService dockerService;
    public EtmFilesService filesService;
    public TJobExecRepository tJobExecRepositoryImpl;
    public ExternalTJobExecutionRepository externalTJobExecutionRepository;
    public UtilsService utilsService;
    private EtmTestResultService etmTestResultService;
    private Map<String, String> sutsByExecution;

    @Autowired
    public DockerEtmService(DockerService dockerService,
            EtmFilesService filesService,
            TJobExecRepository tJobExecRepositoryImpl,
            UtilsService utilsService,
            ExternalTJobExecutionRepository externalTJobExecutionRepository,
            EtmTestResultService etmTestResultService) {
        this.dockerService = dockerService;
        this.filesService = filesService;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.utilsService = utilsService;
        this.externalTJobExecutionRepository = externalTJobExecutionRepository;
        this.etmTestResultService = etmTestResultService;
        sutsByExecution = new ConcurrentHashMap<String, String>();
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

    public void insertCreatedContainer(String containerId,
            String containerName) {
        createdContainers.put(containerId, containerName);
    }

    private void initLogstashHostIfNecessary() throws Exception {
        if (logstashOrMiniHost == null || "".equals(logstashOrMiniHost)) {
            logstashOrMiniHost = this.getLogstashHost();
        }
    }

    public String getLogstashHost() throws Exception {
        if (masterSlavemode) {
            return utilsService.getEtPublicHostValue();
        } else {
            if (utilsService.isElastestMini()) {
                return getEtmHost();
            } else {
                return dockerService.getContainerIpByNetwork(
                        etEtmLogstashContainerName, elastestNetwork);
            }
        }
    }
    
    public String getEtmHost() throws Exception {
        if (utilsService.isEtmInContainer()) {
            return dockerService.getContainerIpByNetwork(etEtmContainerName,
                    elastestNetwork);
        } else {
            return dockerService.getHostIpByNetwork(elastestNetwork);
        }
    }

    public String getEdmMySqlHost() throws Exception {
        return dockerService.getContainerIpByNetwork(etEdmMysqlContainerName,
                elastestNetwork);
    }

    public Map<String, String> getEtLabels(Execution execution, String type,
            String sutServiceName) {
        String etTypeLabelValue = "";
        if ("sut".equals(type.toLowerCase())) {
            etTypeLabelValue = etTypeSutLabelValue;
        } else if ("tjob".equals(type.toLowerCase())) {
            etTypeLabelValue = etTypeTestLabelValue;
        }

        Map<String, String> labels = new HashMap<>();
        labels.put(etTypeLabel, etTypeLabelValue);
        String execId = null;
        String tJobId = null;
        if (execution.isExternal()) {
            execId = execution.getExternalTJobExec().getId().toString();
            tJobId = execution.getExternalTJob().getId().toString();
        } else {
            execId = execution.getTJobExec().getId().toString();
            tJobId = execution.gettJob().getId().toString();
        }

        labels.put(etTJobExecIdLabel, execId);
        labels.put(etTJobIdLabel, tJobId);

        if (sutServiceName != null) {
            labels.put(etTJobSutServiceNameLabel, sutServiceName);
        }

        return labels;
    }

    public Map<String, String> getEtLabels(Execution execution, String type) {
        return this.getEtLabels(execution, type, null);
    }

    /* *************************** */
    /* **** Container Methods **** */
    /* *************************** */

    public DockerContainer createContainer(Execution execution, String type)
            throws Exception {
        SutSpecification sut = execution.getSut();

        String image = "";
        String commands = null;
        List<Parameter> parametersList = new ArrayList<Parameter>();
        String prefix = "";
        String suffix = "";
        String containerName = "";
        String sutHost = null;
        String sutPort = null;
        String sutProtocol = null;

        String sutPath = null;

        if ("sut".equals(type.toLowerCase())) {
            parametersList = sut.getParameters();
            commands = sut.getCommands();
            image = sut.getSpecification();
            prefix = "sut_";
            if (sut.isSutInNewContainer()) {
                suffix = sut.getSutInContainerAuxLabel();
            }
            containerName = getSutName(execution);

            sutPath = getSutPath(execution);

            filesService.createExecFilesFolder(sutPath);
        } else if ("tjob".equals(type.toLowerCase())) {
            TJob tJob = execution.gettJob();
            TJobExecution tJobExec = execution.getTJobExec();

            parametersList = tJobExec.getParameters();
            commands = tJob.getCommands();
            image = tJob.getImageName();
            prefix = "test_";
            containerName = getTestName(execution);
            if (execution.isWithSut()) {
                sutHost = execution.getSutExec().getIp();
                sutPort = sut.getPort();
                sutProtocol = sut.getProtocol().toString();
            }

        }

        // Environment variables (optional)
        ArrayList<String> envList = new ArrayList<>();
        String envVar;

        // Get (External)TJob Exec Env Vars
        Map<String, String> tJobEnvVars;
        if (execution.isExternal()) {
            // tJobEnvVars = execution.getExternalTJob().getEnvVars(); TODO
            tJobEnvVars = new HashMap<>();
        } else {
            tJobEnvVars = execution.getTJobExec().getEnvVars();
        }

        for (Map.Entry<String, String> entry : tJobEnvVars.entrySet()) {
            envVar = entry.getKey() + "=" + entry.getValue();
            envList.add(envVar);
        }

        // Get Parameters and insert into Env Vars
        for (Parameter parameter : parametersList) {
            envVar = parameter.getName() + "=" + parameter.getValue();
            envList.add(envVar);
        }

        if (sutHost != null) {
            envList.add("ET_SUT_HOST=" + sutHost);
        }

        if (sutPort != null) {
            envList.add("ET_SUT_PORT=" + sutPort);
        }

        if (sutProtocol != null) {
            envList.add("ET_SUT_PROTOCOL=" + sutProtocol);
        }

        envList.add("ET_NETWORK=" + elastestNetwork);

        // Commands (optional)
        ArrayList<String> cmdList = null;
        ArrayList<String> entrypointList = null;
        if (commands != null && !commands.isEmpty()) {
            cmdList = new ArrayList<>();
            entrypointList = new ArrayList<>();
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
        if (isEMSSelected(execution)) {
            try {
                logConfig = getEMSLogConfig(type, prefix, suffix, execution);
            } catch (Exception e) {
                logger.error("Cannot get Ems Log config", e);
            }
        } else {
            logConfig = getDefaultLogConfig(
                    (EpmService.etMasterSlaveMode ? bindedLsTcpPort
                            : logstashTcpPort),
                    prefix, suffix, execution);
        }

        // ElasTest labels
        Map<String, String> labels = this.getEtLabels(execution, type);

        // Pull Image
        this.pullETExecutionImage(execution, image, type, false);

        /* ******************************************************** */
        DockerBuilder dockerBuilder = new DockerBuilder(image);
        dockerBuilder.envs(envList);
        dockerBuilder.logConfig(logConfig);
        dockerBuilder.containerName(containerName);
        dockerBuilder.cmd(cmdList);
        dockerBuilder.entryPoint(entrypointList);
        dockerBuilder.network(elastestNetwork);
        dockerBuilder.labels(labels);

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

    private String getSutPath(Execution execution) {
        String sutPath;
        if (execution.isExternal()) {
            ExternalTJobExecution exTJobExec = execution.getExternalTJobExec();

            sutPath = filesService.buildExternalTJobFilesPath(exTJobExec,
                    ElastestConstants.SUT_FOLDER);

        } else {
            TJobExecution tJobExec = execution.getTJobExec();

            sutPath = filesService.buildTJobFilesPath(tJobExec,
                    ElastestConstants.SUT_FOLDER);
        }
        return sutPath;
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

    public void pullETExecutionImage(Execution execution, String image,
            String name, boolean forcePull)
            throws DockerException, InterruptedException, Exception {
        DockerPullImageProgress dockerPullImageProgress = new DockerPullImageProgress();
        dockerPullImageProgress.setImage(image);
        dockerPullImageProgress.setCurrentPercentage(0);

        ProgressHandler progressHandler = new ProgressHandler() {
            @Override
            public void progress(ProgressMessage message)
                    throws DockerException {
                dockerPullImageProgress.processNewMessage(message);
                String msg = "Pulling " + name + " image (" + image + "): "
                        + dockerPullImageProgress.getCurrentPercentage() + "%";

                if (execution.isExternal()) {
                    // TODO External status
                } else {
                    TJobExecution tJobExec = execution.getTJobExec();
                    // tJobExec.setResult(result);
                    tJobExec.setResultMsg(msg);
                    tJobExecRepositoryImpl.save(tJobExec);
                }
            }

        };

        this.pullETExecImageWithProgressHandler(image, name, forcePull,
                progressHandler);
    }

    /* ****************** */
    /* **** Dockbeat **** */
    /* ****************** */

    public String getDockbeatContainerName(Execution execution) {
        String prefix = "elastest_dockbeat_";

        // elastest_dockbeat_X | elastest_dockbeat_extX_eX
        String suffix = execution.isExternal()
                ? execution.getExternalTJobExec()
                        .getExternalTJobExecMonitoringIndex()
                : execution.getExecutionId().toString();

        return prefix + suffix;
    }

    public void startDockbeat(Execution execution) throws Exception {
        try {
            Long executionId = execution.getExecutionId();

            String containerName = getDockbeatContainerName(execution);

            // Environment variables
            ArrayList<String> envList = new ArrayList<>();
            String envVar;

            // Get Parameters and insert into Env Vars¡
            String lsHostEnvVar = "LOGSTASHHOST" + "=" + logstashOrMiniHost;
            String lsInternalBeatsPortEnvVar = "LOGSTASHPORT" + "="
                    + lsInternalBeatsPort;

            Map<String, String> execEnvVars = new HashMap<>();
            String execId = "";
            String tJobId = "";

            if (execution.isExternal()) {
                execEnvVars = execution.getExternalTJobExec().getEnvVars();
                execId = execution.getExternalTJobExec().getId().toString();
                tJobId = execution.getExternalTJob().getId().toString();

            } else {
                execEnvVars = execution.getTJobExec().getEnvVars();
                execId = execution.getTJobExec().getId().toString();
                tJobId = execution.gettJob().getId().toString();
            }

            if (isEMSSelected(execution)) {

                String regexSuffix = "_?(" + executionId + ")(_([^_]*(_\\d*)?))?";
                String testRegex = "^test" + regexSuffix;
                String sutRegex = "^sut" + regexSuffix;
                envVar = "FILTER_CONTAINERS" + "=" + testRegex + "|" + sutRegex;
                envList.add(envVar);

                // envVar = "FILTER_EXCLUDE" + "=" + "\"\"";
                // envList.add(envVar);

                lsInternalBeatsPortEnvVar = "LOGSTASHPORT" + "="
                        + execEnvVars.get("ET_EMS_LSBEATS_PORT");

                lsHostEnvVar = "LOGSTASHHOST" + "="
                        + execEnvVars.get("ET_EMS_LSBEATS_HOST");
            }
            envList.add(lsHostEnvVar);
            envList.add(lsInternalBeatsPortEnvVar);
            // dockerSock volume bind
            Bind dockerSockVolumeBind = Bind.from(dockerSock).to(dockerSock)
                    .build();

            // ElasTest labels
            Map<String, String> labels = new HashMap<>();
            labels.put(etTypeLabel, etTypeMonitoringLabelValue);
            labels.put(etTJobExecIdLabel, execId);
            labels.put(etTJobIdLabel, tJobId);

            // Pull Image
            this.pullETExecImage(dockbeatImage, "Dockbeat", false);

            // Create Container
            logger.debug("Creating Dockbeat Container...");

            /* ******************************************************** */
            DockerBuilder dockerBuilder = new DockerBuilder(dockbeatImage);
            dockerBuilder.envs(envList);
            dockerBuilder.containerName(containerName);
            logger.debug("Adding dockbeat to network: {}", elastestNetwork);
            dockerBuilder.network(elastestNetwork);

            dockerBuilder.volumeBindList(Arrays.asList(dockerSockVolumeBind));

            dockerBuilder.labels(labels);

            DockerContainer dockerContainer = dockerBuilder.build();
            String containerId = dockerService.createAndStartContainer(
                    dockerContainer, EpmService.etMasterSlaveMode);
            this.insertCreatedContainer(containerId, containerName);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        } catch (TJobStoppedException e) {
            throw e;
        } catch (Exception e) {
            new Exception("Exception on start Dockbeat", e);
        }
    }

    /* ********************* */
    /* **** Sut Methods **** */
    /* ********************* */

    public String getSutName(Execution execution) {
        SutSpecification sut = execution.getSut();
        return this.getSutPrefix(execution)
                + (sut.isDockerCommandsSut() && sut.isSutInNewContainer()
                        ? "_" + sut.getSutInContainerAuxLabel()
                        : "");
    }

    // Hardcoded in EUS, if you make changes here, make them too in EUS
    public String getSutPrefix(Execution execution) {
        String suffix = execution.isExternal()
                ? execution.getExternalTJobExec()
                        .getExternalTJobExecMonitoringIndex()
                : execution.getExecutionId().toString();

        return getSutPrefixBySuffix(suffix);
    }

    public String getSutPrefixBySuffix(String suffix) {
        String prefix = "sut";
        return prefix + "_" + suffix;
    }

    public void createAndStartSutContainer(Execution execution)
            throws Exception {
        try {
            // Create Container Object
            DockerContainer sutContainer = createContainer(execution, "sut");

            String resultMsg = "Starting dockerized SuT";
            updateExecutionResultStatus(execution,
                    TJobExecution.ResultEnum.EXECUTING_SUT, resultMsg);
            logger.info(resultMsg + " " + execution.getExecutionId());

            // Create and start container
            String sutContainerId = dockerService.createAndStartContainer(
                    sutContainer, EpmService.etMasterSlaveMode);
            sutsByExecution.put(execution.getExecutionId().toString(),
                    sutContainerId);

            String sutName = getSutName(execution);
            this.insertCreatedContainer(sutContainerId, sutName);
        } catch (TJobStoppedException e) {
            throw new TJobStoppedException(
                    "Error on create and start Sut container", e);
        } catch (Exception e) {
            throw new Exception("Error on create and start Sut container", e);
        }
    }

    public String getCheckName(Execution execution) {
        return "check_" + execution.getExecutionId();
    }

    public void checkSut(Execution execution, String ip, String port)
            throws DockerException, Exception {
        String envVar = "IP=" + ip;
        String envVar2 = "PORT=" + port;
        ArrayList<String> envList = new ArrayList<>();
        envList.add(envVar);
        envList.add(envVar2);

        try {
            dockerService.pullImageIfNotExist(checkImage);

            String checkName = getCheckName(execution);

            DockerBuilder dockerBuilder = new DockerBuilder(checkImage);
            dockerBuilder.envs(envList);
            dockerBuilder.containerName(checkName);
            dockerBuilder.network(elastestNetwork);

            DockerContainer dockerContainer = dockerBuilder.build();
            String checkContainerId = dockerService.createAndStartContainer(
                    dockerContainer, EpmService.etMasterSlaveMode);

            this.insertCreatedContainer(checkContainerId, checkName);

            int statusCode = dockerService
                    .getDockerClient(EpmService.etMasterSlaveMode)
                    .waitContainer(checkContainerId).statusCode();
            if (statusCode == 0) {
                logger.info("Sut is ready " + execution.getExecutionId());

            } else { // TODO timeout or catch stop execution
                throw new TJobStoppedException(
                        "Error on Waiting for CheckSut. Probably because the user has stopped the execution");
            }

        } catch (InterruptedException e) {
            throw new TJobStoppedException(
                    "Error on Waiting for CheckSut. Probably because the user has stopped the execution");
        }
    }

    public void removeSutVolumeFolder(Execution execution) {
        String sutPath = getSutPath(execution);

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

    public String getTestName(Execution execution) {
        return "test_" + execution.getExecutionId();
    }

    public void createAndRunTestContainer(Execution execution)
            throws Exception {
        TJobExecution tJobExec = execution.getTJobExec();
        try {
            // Create Container Object
            DockerContainer testContainer = createContainer(execution, "tjob");

            String resultMsg = "Starting Test Execution";
            updateExecutionResultStatus(execution,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);

            // Create and start container
            String testContainerId = dockerService.createAndStartContainer(
                    testContainer, EpmService.etMasterSlaveMode);

            resultMsg = "Executing Test";
            updateExecutionResultStatus(execution,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);

            String testName = getTestName(execution);
            this.insertCreatedContainer(testContainerId, testName);

            int exitCode = dockerService
                    .getDockerClient(EpmService.etMasterSlaveMode)
                    .waitContainer(testContainerId).statusCode();
            logger.info("Test container ends with code " + exitCode);

            // Test Results
            resultMsg = "Waiting for Test Results";
            updateExecutionResultStatus(execution, ResultEnum.WAITING,
                    resultMsg);
            etmTestResultService.saveTestResults(
                    getTestResults(execution, testContainerId), tJobExec);

            tJobExec.setEndDate(new Date());
            logger.info("Ending Execution {}...", tJobExec.getId());
            saveFinishStatus(tJobExec, execution, exitCode);

        } catch (TJobStoppedException | InterruptedException e) {
            throw new TJobStoppedException(
                    "Error on create and start TJob container: Stopped", e);
        } catch (Exception e) {
            throw new Exception("Error on create and start TJob container", e);
        }
    }

//    public void updateExecutionResultStatus(Execution execution,
//            ResultEnum result, String msg) {
//        if (execution.isExternal()) {
//            ExternalTJobExecution externalTJobExec = execution
//                    .getExternalTJobExec();
//            updateExternalTJobExecResultStatus(externalTJobExec, result, msg);
//        } else {
//            TJobExecution tJobExec = execution.getTJobExec();
//            updateTJobExecResultStatus(tJobExec, result, msg);
//        }
//    }

//    public void updateTJobExecResultStatus(TJobExecution tJobExec,
//            ResultEnum result, String msg) {
//        tJobExec.setResult(result);
//        tJobExec.setResultMsg(msg);
//        tJobExecRepositoryImpl.save(tJobExec);
//    }

//    public void updateExternalTJobExecResultStatus(
//            ExternalTJobExecution externalTJobExec, ResultEnum result,
//            String msg) {
//        externalTJobExec.setResult(result);
//        externalTJobExec.setResultMsg(msg);
//        externalTJobExecutionRepository.save(externalTJobExec);
//    }

    public boolean isEMSSelected(Execution execution) {
        return !execution.isExternal()
                && execution.gettJob().isSelectedService("ems");
    }

    public void saveFinishStatus(TJobExecution tJobExec, Execution execution,
            int exitCode) {
        String resultMsg = "";
        ResultEnum finishStatus = ResultEnum.SUCCESS;

        if (tJobExec.getTestSuites() != null
                && tJobExec.getTestSuites().size() > 0) {
            for (TestSuite testSuite : tJobExec.getTestSuites()) {
                if (testSuite.getFinalStatus() == ResultEnum.FAIL) {
                    finishStatus = testSuite.getFinalStatus();
                    break;
                }
            }

        } else {
            if (exitCode != 0) {
                finishStatus = ResultEnum.FAIL;
            }
        }

        resultMsg = "Finished: " + finishStatus;
        updateExecutionResultStatus(execution, finishStatus, resultMsg);
    }

    /* ******************************* */
    /* ******* Logging methods ******* */
    /* ******************************* */
    public LogConfig getLogConfig(String host, String port, String tagPrefix,
            String tagSuffix, Execution execution) {
        Map<String, String> configMap = new HashMap<String, String>();

        String monitoringIndex = "";

        if (execution.isExternal()) {
            monitoringIndex = execution.getExternalTJobExec()
                    .getExternalTJobExecMonitoringIndex();
        } else {
            monitoringIndex = execution.getExecutionId().toString();
        }

        if (tagSuffix != null && !tagSuffix.equals("")) {
            tagSuffix = "_" + tagSuffix;
        }
        configMap.put("tag", tagPrefix + monitoringIndex + tagSuffix + "_exec");

        LogConfig logConfig = null;

        configMap.put("syslog-address", "tcp://" + host + ":" + port);
        configMap.put("syslog-format", "rfc5424micro");

        logConfig = LogConfig.create("syslog", configMap);

        return logConfig;
    }

    public LogConfig getLogstashOrMiniLogConfig(String tag) throws Exception {
        this.initLogstashHostIfNecessary();

        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("tag", tag);

        LogConfig logConfig = null;

        configMap.put("syslog-address",
                "tcp://" + logstashOrMiniHost + ":" + logstashTcpPort);
        configMap.put("syslog-format", "rfc5424micro");

        logConfig = LogConfig.create("syslog", configMap);

        return logConfig;
    }

    public LogConfig getDefaultLogConfig(String port, String tagPrefix,
            String tagSuffix, Execution execution) throws Exception {

        initLogstashHostIfNecessary();
        port = masterSlavemode ? bindedLsTcpPort : port;
        logger.info(
                "Logstash/Tcp Server Host to send logs from containers: {}. To port {}",
                logstashOrMiniHost, port);

        return this.getLogConfig(logstashOrMiniHost, port, tagPrefix, tagSuffix,
                execution);
    }

    public LogConfig getEMSLogConfig(String type, String tagPrefix,
            String tagSuffix, Execution execution) throws Exception {
        TJobExecution tJobExec = execution.getTJobExec();
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
                    execution);
        } else {
            throw new Exception("Error on get EMS Log config");
        }
    }

    /* *************** */
    /* **** Utils **** */
    /* *************** */

    public String getContainerIpWithDockerExecution(String containerId,
            Execution execution) throws Exception {
        return dockerService.getContainerIpWithDockerClient(
                dockerService.getDockerClient(EpmService.etMasterSlaveMode),
                containerId, elastestNetwork);
    }

    public String waitForContainerIpWithDockerExecution(String containerId,
            Execution execution, long timeout) throws Exception {
        return dockerService.waitForContainerIpWithDockerClient(
                dockerService.getDockerClient(EpmService.etMasterSlaveMode),
                containerId, elastestNetwork, timeout);
    }

    public String getHostIp(Execution execution) throws Exception {
        return dockerService.getDockerClient(EpmService.etMasterSlaveMode)
                .inspectNetwork(elastestNetwork).ipam().config().get(0)
                .gateway();
    }

    public void removeDockerContainer(String containerId) throws Exception {
        dockerService.removeDockerContainer(containerId);
        createdContainers.remove(containerId);
    }

    public void endContainer(String containerName) throws Exception {
        dockerService.endContainer(containerName, true);
        String containerId = dockerService.getContainerIdByName(containerName);

        createdContainers.remove(containerId);
    }

    public void endContainer(String containerName, int timeout)
            throws Exception {
        dockerService.endContainer(containerName, true, timeout);
        String containerId = dockerService.getContainerIdByName(containerName);

        createdContainers.remove(containerId);
    }



    /* ************************* */
    /* **** Get TestResults **** */
    /* ************************* */

    public List<ReportTestSuite> getTestResults(Execution execution,
            String testContainerId) throws Exception {
        try {
            List<ReportTestSuite> testSuites = null;
            String resultsPath = execution.gettJob().getResultsPath();

            if (resultsPath != null && !resultsPath.isEmpty()) {
                try {
                    InputStream inputStream = dockerService
                            .getFileFromContainer(testContainerId, resultsPath);

                    String result = IOUtils.toString(inputStream,
                            StandardCharsets.UTF_8);
                    testSuites = getTestSuitesByString(result);
                } catch (IOException e) {
                }
            }
            return testSuites;
        } catch (Exception e) {
            throw new Exception(
                    "Error on get test results. Maybe the specified path is incorrect.",
                    e);
        }
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

                List<ReportTestSuite> testSuites = null;

                try {
                    // normally, a single test suite
                    testSuites = this
                            .testSuiteStringToReportTestSuite(newResult);
                } catch (ParserConfigurationException | SAXException
                        | IOException e) {
                    logger.error("Error on parse testSuite {}", e);
                }

                if (testSuites != null) {
                    results.addAll(testSuites);
                }
            }
        }

        return results;
    }

    public String getElastestNetwork() {
        return elastestNetwork;
    }

    public void setElastestNetwork(String elastestNetwork) {
        this.elastestNetwork = elastestNetwork;
    }

    public Map<String, String> getSutsByExecution() {
        return sutsByExecution;
    }

    public void setSutsByExecution(Map<String, String> sutsByExecution) {
        this.sutsByExecution = sutsByExecution;
    }

    public String getSutContainerIdByExec(String execId) {
        return sutsByExecution.get(execId);
    }

    public void removeSutByExecution(String execId) {
        sutsByExecution.remove(execId);
    }

    public void addSutByExecution(String executionId, String sutContainerId) {
        sutsByExecution.put(executionId, sutContainerId);
    }

    private List<ReportTestSuite> testSuiteStringToReportTestSuite(
            String testSuiteStr) throws UnsupportedEncodingException,
            ParserConfigurationException, SAXException, IOException {
        TestSuiteXmlParser testSuiteXmlParser = new TestSuiteXmlParser(null);
        InputStream byteArrayIs = new ByteArrayInputStream(
                testSuiteStr.getBytes());

        // normally, a single test suite, but in some cases returns more than 1
        return testSuiteXmlParser
                .parse(new InputStreamReader(byteArrayIs, "UTF-8"));
    }

    public void updateExecutionResultStatus(Execution execution,
            ResultEnum result, String msg) {
        if (execution.isExternal()) {
            ExternalTJobExecution externalTJobExec = execution
                    .getExternalTJobExec();
            updateExternalTJobExecResultStatus(externalTJobExec, result, msg);
        } else {
            TJobExecution tJobExec = execution.getTJobExec();
            updateTJobExecResultStatus(tJobExec, result, msg);
        }
    }

    public void updateTJobExecResultStatus(TJobExecution tJobExec,
            ResultEnum result, String msg) {
        tJobExec.setResult(result);
        tJobExec.setResultMsg(msg);
        tJobExecRepositoryImpl.save(tJobExec);
    }

    public void updateExternalTJobExecResultStatus(
            ExternalTJobExecution externalTJobExec, ResultEnum result,
            String msg) {
        externalTJobExec.setResult(result);
        externalTJobExec.setResultMsg(msg);
        externalTJobExecutionRepository.save(externalTJobExec);
    }
    
}
