package io.elastest.etm.platform.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
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
import javax.ws.rs.NotFoundException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.TestSuiteXmlParser;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.HostConfig.Bind.Builder;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;

import io.elastest.epm.client.DockerContainer;
import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.epm.client.json.DockerComposeCreateProject;
import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.model.DockerPullImageProgress;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.epm.client.model.DockerServiceStatus.DockerServiceStatusEnum;
import io.elastest.epm.client.service.DockerComposeService;
import io.elastest.epm.client.service.DockerService;
import io.elastest.epm.client.service.DockerService.ContainersListActionEnum;
import io.elastest.etm.model.CoreServiceInfo;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutExecution.DeployStatusEnum;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.CommandsOptionEnum;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.TestSuite;
import io.elastest.etm.model.VersionInfo;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.service.exception.TJobStoppedException;
import io.elastest.etm.utils.ElastestConstants;
import io.elastest.etm.utils.EtmFilesService;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

public class DockerServiceImpl extends PlatformService {
    final Logger logger = getLogger(lookup().lookupClass());
    private static String checkImage = "elastest/etm-check-service-up";
    private static final Map<String, String> createdContainers = new HashMap<>();
    public String getThisContainerIpCmd = "ip a | grep -m 1 global | grep -oE '([0-9]{1,3}\\.){3}[0-9]{1,3}\\/' | grep -oE '([0-9]{1,3}\\.){3}[0-9]{1,3}'";

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
    @Value("${et.docker.img.socat}")
    public String etSocatImage;

    private DockerComposeService dockerComposeService;
    private DockerService dockerService;
    private EtmFilesService etmFilesService;
    private UtilsService utilsService;
    private Map<String, String> sutsByExecution;

    public DockerServiceImpl(DockerComposeService dockerComposeService,
            EtmFilesService etmFilesService, UtilsService utilsService,
            DockerService dockerService) {
        super();
        this.dockerComposeService = dockerComposeService;
        this.etmFilesService = etmFilesService;
        this.utilsService = utilsService;
        this.dockerService = dockerService;
        sutsByExecution = new ConcurrentHashMap<String, String>();
    }

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

    public void insertCreatedContainer(String containerId,
            String containerName) {
        createdContainers.put(containerId, containerName);
    }

    private void initLogstashHostIfNecessary() throws Exception {
        if (logstashOrMiniHost == null || logstashOrMiniHost.isEmpty()) {
            logstashOrMiniHost = this.getLogstashHost();
        }
    }

    private Map<String, String> getEtLabels(Execution execution, String type,
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

    private Map<String, String> getEtLabels(Execution execution, String type) {
        return this.getEtLabels(execution, type, null);
    }

    private DockerContainer createContainer(Execution execution, String type)
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
                containerName = generateContainerName(ContainerPrefix.SUT,
                        execution, true);
            } else {
                containerName = generateContainerName(ContainerPrefix.SUT,
                        execution);
            }

            sutPath = getSutPath(execution);

            etmFilesService.createFolderIfNotExists(sutPath);
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
            logConfig = getDefaultLogConfig((logstashTcpPort), prefix, suffix,
                    execution);
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

            sutPath = etmFilesService.buildExternalTJobFilesPath(exTJobExec,
                    ElastestConstants.SUT_FOLDER);

        } else {
            TJobExecution tJobExec = execution.getTJobExec();

            sutPath = etmFilesService.buildTJobFilesPath(tJobExec,
                    ElastestConstants.SUT_FOLDER);
        }
        return sutPath;
    }

    private void pullETExecImageWithProgressHandler(String image, String name,
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

    private void pullETExecutionImage(Execution execution, String image,
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
                    execution.setStatusMsg(msg);

                }
            }

        };

        this.pullETExecImageWithProgressHandler(image, name, forcePull,
                progressHandler);
    }

    private String getDockbeatContainerName(Execution execution) {
        String prefix = "elastest_dockbeat_";

        // elastest_dockbeat_X | elastest_dockbeat_extX_eX
        String suffix = execution.isExternal()
                ? execution.getExternalTJobExec()
                        .getExternalTJobExecMonitoringIndex()
                : execution.getExecutionId().toString();

        return prefix + suffix;
    }

    private void startDockbeat(Execution execution) throws Exception {
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

                String regexSuffix = "_?(" + executionId
                        + ")(_([^_]*(_\\d*)?))?";
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
            String containerId = dockerService
                    .createAndStartContainer(dockerContainer);
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

    private void createAndStartSutContainer(Execution execution)
            throws Exception {
        try {
            // Create Container Object
            DockerContainer sutContainer = createContainer(execution, "sut");

            String resultMsg = "Starting dockerized SuT";
            execution.updateTJobExecutionStatus(
                    TJobExecution.ResultEnum.EXECUTING_SUT, resultMsg);
            logger.info(resultMsg + " " + execution.getExecutionId());

            // Create and start container
            String sutContainerId = dockerService
                    .createAndStartContainer(sutContainer);
            sutsByExecution.put(execution.getExecutionId().toString(),
                    sutContainerId);

            String sutName = generateContainerName(ContainerPrefix.SUT,
                    execution);
            this.insertCreatedContainer(sutContainerId, sutName);
        } catch (TJobStoppedException e) {
            throw new TJobStoppedException(
                    "Error on create and start Sut container", e);
        } catch (Exception e) {
            throw new Exception("Error on create and start Sut container", e);
        }
    }

    private String getCheckName(Execution execution) {
        return "check_" + execution.getExecutionId();
    }

    private void checkSut(Execution execution, String ip, String port)
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
            String checkContainerId = dockerService
                    .createAndStartContainer(dockerContainer);

            this.insertCreatedContainer(checkContainerId, checkName);

            int statusCode = dockerService.getDockerClient()
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

    private void removeSutVolumeFolder(Execution execution) {
        String sutPath = getSutPath(execution);

        try {
            etmFilesService.removeFolder(sutPath);
        } catch (Exception e) {
            logger.debug("The SuT folder could not be deleted: {}",
                    e.getMessage());
        }
    }

    /* ************** */
    /* **** Test **** */
    /* ************** */

    private String getTestName(Execution execution) {
        return "test_" + execution.getExecutionId();
    }

    @Override
    public List<ReportTestSuite> deployAndRunTJobExecution(Execution execution)
            throws Exception {
        TJobExecution tJobExec = execution.getTJobExec();
        try {
            // Create Container Object
            DockerContainer testContainer = createContainer(execution, "tjob");

            String resultMsg = "Starting Test Execution";
            execution.updateTJobExecutionStatus(
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);
            execution.setStatusMsg(resultMsg);

            // Create and start container
            String testContainerId = dockerService
                    .createAndStartContainer(testContainer);

            resultMsg = "Executing Test";
            execution.updateTJobExecutionStatus(
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);
            execution.setStatusMsg(resultMsg);

            String testName = getTestName(execution);
            this.insertCreatedContainer(testContainerId, testName);

            int exitCode = dockerService.getDockerClient()
                    .waitContainer(testContainerId).statusCode();
            logger.info("Test container ends with code " + exitCode);

            // Test Results
            resultMsg = "Waiting for Test Results";
            execution.updateTJobExecutionStatus(ResultEnum.WAITING, resultMsg);
            execution.setStatusMsg(resultMsg);
            List<ReportTestSuite> testResults = getTestResults(execution,
                    testContainerId);

            tJobExec.setEndDate(new Date());
            logger.info("Ending Execution {}...", tJobExec.getId());
            saveFinishStatus(tJobExec, execution, exitCode);
            return testResults;

        } catch (TJobStoppedException | InterruptedException e) {
            throw new TJobStoppedException(
                    "Error on create and start TJob container: Stopped", e);
        } catch (Exception e) {
            throw new Exception("Error on create and start TJob container", e);
        }
    }

    private boolean isEMSSelected(Execution execution) {
        return !execution.isExternal()
                && execution.gettJob().isSelectedService("ems");
    }

    private void saveFinishStatus(TJobExecution tJobExec, Execution execution,
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
        execution.updateTJobExecutionStatus(finishStatus, resultMsg);
    }

    /* ******************************* */
    /* ******* Logging methods ******* */
    /* ******************************* */
    private LogConfig getLogConfig(String host, String port, String tagPrefix,
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

    private LogConfig getDefaultLogConfig(String port, String tagPrefix,
            String tagSuffix, Execution execution) throws Exception {

        initLogstashHostIfNecessary();
        port = masterSlavemode ? bindedLsTcpPort : port;
        logger.info(
                "Logstash/Tcp Server Host to send logs from containers: {}. To port {}",
                logstashOrMiniHost, port);

        return this.getLogConfig(logstashOrMiniHost, port, tagPrefix, tagSuffix,
                execution);
    }

    private LogConfig getEMSLogConfig(String type, String tagPrefix,
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

    private String getContainerIpWithDockerExecution(String containerId,
            Execution execution) throws Exception {
        return dockerService.getContainerIpWithDockerClient(
                dockerService.getDockerClient(), containerId, elastestNetwork);
    }

    private String waitForContainerIpWithDockerExecution(String containerId,
            Execution execution, long timeout) throws Exception {
        return dockerService.waitForContainerIpWithDockerClient(
                dockerService.getDockerClient(), containerId, elastestNetwork,
                timeout);
    }

    private String getHostIp(Execution execution) throws Exception {
        return dockerService.getDockerClient().inspectNetwork(elastestNetwork)
                .ipam().config().get(0).gateway();
    }

    public void removeDockerContainer(String containerId) throws Exception {
        dockerService.removeDockerContainer(containerId);
        createdContainers.remove(containerId);
    }

    private void endContainer(String containerName) throws Exception {
        dockerService.endContainer(containerName, true);
        String containerId = dockerService.getContainerIdByName(containerName);

        createdContainers.remove(containerId);
    }

    private void endContainer(String containerName, int timeout)
            throws Exception {
        dockerService.endContainer(containerName, true, timeout);
        String containerId = dockerService.getContainerIdByName(containerName);

        createdContainers.remove(containerId);
    }

    /* ************************* */
    /* **** Get TestResults **** */
    /* ************************* */

    private List<ReportTestSuite> getTestResults(Execution execution,
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

    private String getElastestNetwork() {
        return elastestNetwork;
    }

    private void setElastestNetwork(String elastestNetwork) {
        this.elastestNetwork = elastestNetwork;
    }

    private Map<String, String> getSutsByExecution() {
        return sutsByExecution;
    }

    private void setSutsByExecution(Map<String, String> sutsByExecution) {
        this.sutsByExecution = sutsByExecution;
    }

    private String getSutContainerIdByExec(String execId) {
        return sutsByExecution.get(execId);
    }

    private void removeSutByExecution(String execId) {
        sutsByExecution.remove(execId);
    }

    private void addSutByExecution(String executionId, String sutContainerId) {
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

    private void deploySutFromDockerImage(Execution execution)
            throws Exception {
        createAndStartSutContainer(execution);
    }

    private void deploySutFromDockerCompose(Execution execution)
            throws Exception {
        SutSpecification sut = execution.getSut();
        String mainService = sut.getMainService();
        logger.debug("The main service saved in DB is: {}", mainService);
        String composeProjectName = generateContainerName(ContainerPrefix.SUT,
                execution);

        // TMP replace sut exec and logstash sut tcp
        String dockerComposeYml = sut.getSpecification();

        // Set logging, network, labels and do pull of images
        dockerComposeYml = prepareElasTestConfigInDockerComposeYml(
                dockerComposeYml, composeProjectName, execution, mainService);

        // Environment variables (optional)
        ArrayList<String> envList = new ArrayList<>();
        String envVar;

        // Get Parameters and insert into Env Vars
        for (Parameter parameter : sut.getParameters()) {
            envVar = parameter.getName() + "=" + parameter.getValue();
            envList.add(envVar);
        }

        DockerComposeCreateProject project = new DockerComposeCreateProject(
                composeProjectName, dockerComposeYml, envList);

        String resultMsg = "Starting dockerized SuT";
        execution.updateTJobExecutionStatus(ResultEnum.EXECUTING_SUT,
                resultMsg);
        logger.info(resultMsg + " " + execution.getExecutionId());

        // Create Containers
        String pathToSaveTmpYml = "";
        if (execution.isExternal()) {
            pathToSaveTmpYml = etmFilesService.getExternalTJobExecFolderPath(
                    execution.getExternalTJobExec());
        } else {
            pathToSaveTmpYml = etmFilesService
                    .getTJobExecFolderPath(execution.getTJobExec());
        }
        boolean created = dockerComposeService.createProject(project,
                pathToSaveTmpYml, false, false, false);

        // Start Containers
        if (!created) {
            throw new Exception(
                    "Sut docker compose containers are not created");
        }

        dockerComposeService.startProject(composeProjectName, false);

        for (io.elastest.epm.client.json.DockerContainerInfo.DockerContainer container : dockerComposeService
                .getContainers(composeProjectName).getContainers()) {
            String containerId = dockerService
                    .getContainerIdByName(container.getName());

            // Insert container into containers list
            insertCreatedContainer(containerId, container.getName());
            // If is main service container, set app id
            if (container.getName()
                    .equals(composeProjectName + "_" + mainService + "_1")) {
                addSutByExecution(execution.getExecutionId().toString(),
                        containerId);
            }

            if (getSutContainerIdByExec(
                    execution.getExecutionId().toString()) == null
                    || getSutContainerIdByExec(
                            execution.getExecutionId().toString()).isEmpty()) {
                throw new Exception(
                        "Main Sut service from docker compose not started");
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String prepareElasTestConfigInDockerComposeYml(
            String dockerComposeYml, String composeProjectName,
            Execution execution, String mainService) throws Exception {
        YAMLFactory yf = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yf);
        Object object;
        try {
            object = mapper.readValue(dockerComposeYml, Object.class);

            Map<String, HashMap<String, HashMap>> dockerComposeMap = (HashMap) object;
            Map<String, HashMap> servicesMap = dockerComposeMap.get("services");
            for (HashMap.Entry<String, HashMap> service : servicesMap
                    .entrySet()) {

                // Set Logging
                service = setLoggingToDockerComposeYmlService(service,
                        composeProjectName, execution);

                // Set Elastest Network
                service = setNetworkToDockerComposeYmlService(service,
                        composeProjectName, execution);

                // Set Elastest Labels
                service = setETLabelsToDockerComposeYmlService(service,
                        composeProjectName, execution);
            }

            dockerComposeMap = setNetworkToDockerComposeYmlRoot(
                    dockerComposeMap, composeProjectName, execution);

            StringWriter writer = new StringWriter();

            yf.createGenerator(writer).writeObject(object);
            dockerComposeYml = writer.toString();

            writer.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new Exception("Error modifying the docker-compose file");
        }

        return dockerComposeYml;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void pullDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, Execution execution)
            throws DockerException, InterruptedException, Exception {
        HashMap<String, String> serviceContent = service.getValue();

        String imageKey = "image";
        // If service has image, pull
        if (serviceContent.containsKey(imageKey)) {
            String image = serviceContent.get(imageKey);
            pullETExecutionImage(execution, image, service.getKey(), false);
        }
    }

    private void endComposedSutExec(Execution execution) throws Exception {
        String composeProjectName = generateContainerName(ContainerPrefix.SUT,
                execution);
        ;
        dockerComposeService.stopAndRemoveProject(composeProjectName);
    }

    private void endCheckSutExec(Execution execution) throws Exception {
        endContainer(getCheckName(execution));
    }

    private void endSutInContainer(Execution execution) throws Exception {
        SutSpecification sut = execution.getSut();
        String containerName = null;
        String sutPrefix = null;
        boolean isDockerCompose = false;

        // If is Docker compose Sut
        if (sut.getCommandsOption() == CommandsOptionEnum.IN_DOCKER_COMPOSE) {
            containerName = this.getCurrentExecSutMainServiceName(sut,
                    execution);
            sutPrefix = generateContainerName(ContainerPrefix.SUT, execution);
            isDockerCompose = true;
        }
        // If is unique Docker image Sut
        else if (sut
                .getCommandsOption() == CommandsOptionEnum.IN_NEW_CONTAINER) {
            containerName = generateContainerName(ContainerPrefix.SUT,
                    execution);
            sutPrefix = containerName;
        }

        // Add containers to the containers list
        if (isDockerCompose) {
            List<Container> containersList = this.dockerService
                    .getContainersCreatedSinceId(getSutsByExecution()
                            .get(execution.getExecutionId().toString()));
            this.dockerService.getContainersByNamePrefixByGivenList(
                    containersList, sutPrefix, ContainersListActionEnum.REMOVE,
                    getElastestNetwork());
        } else {
            this.endContainer(containerName);
        }
    }

    private Container getContainerFromImage(String imageName, String version)
            throws Exception {
        Container container;
        if (version.equals("unspecified")) {
            container = dockerService.getRunningContainersByImageName(imageName)
                    .get(0);
        } else {
            container = dockerService.getRunningContainersByImageNameAndVersion(
                    imageName, version).get(0);
        }
        return container;
    }

    private void updateSutExecDeployStatus(Execution execution,
            DeployStatusEnum status) {
        SutExecution sutExec = execution.getSutExec();

        if (sutExec != null) {
            sutExec.setDeployStatus(status);
        }
        execution.setSutExec(sutExec);
    }

    private String waitForSutInContainer(Execution execution, long timeout)
            throws Exception {
        SutSpecification sut = execution.getSut();
        String containerName = null;
        String sutPrefix = null;
        boolean isDockerCompose = false;
        // If is Docker compose Sut
        if (sut.getCommandsOption() == CommandsOptionEnum.IN_DOCKER_COMPOSE) {
            containerName = this.getCurrentExecSutMainServiceName(sut,
                    execution);
            sutPrefix = generateContainerName(ContainerPrefix.SUT, execution);
            isDockerCompose = true;
            logger.debug(
                    "Is SuT in new container With Docker Compose. Main Service Container Name: {}",
                    containerName);
        }
        // If is unique Docker image Sut
        else if (sut
                .getCommandsOption() == CommandsOptionEnum.IN_NEW_CONTAINER) {
            containerName = generateContainerName(ContainerPrefix.SUT,
                    execution);
            sutPrefix = containerName;
            logger.debug(
                    "Is SuT in new container With Docker Image. Container Name: {}",
                    containerName);
        }
        // Wait for created
        this.dockerService.waitForContainerCreated(containerName, timeout);

        String containerId = this.dockerService
                .getContainerIdByName(containerName);
        // Insert main sut/service into ET network if it's necessary
        this.dockerService.insertIntoNetwork(getElastestNetwork(), containerId);

        // Get Main sut/service ip from ET network
        String sutIp = waitForContainerIpWithDockerExecution(containerName,
                execution, timeout);

        // Add containers to the containers list
        if (isDockerCompose) {
            List<Container> containersList = this.dockerService
                    .getContainersCreatedSinceId(getSutContainerIdByExec(
                            execution.getExecutionId().toString()));
            this.dockerService.getContainersByNamePrefixByGivenList(
                    containersList, sutPrefix, ContainersListActionEnum.ADD,
                    getElastestNetwork());
        } else {
            containerId = this.dockerService
                    .getContainerIdByName(containerName);
            this.insertCreatedContainer(containerId, containerName);
        }
        return sutIp;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private HashMap.Entry<String, HashMap> setLoggingToDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName,
            Execution execution) throws Exception {
        HashMap<String, HashMap> serviceContent = service.getValue();
        String loggingKey = "logging";
        // If service has logging, remove it
        if (serviceContent.containsKey(loggingKey)) {
            serviceContent.remove(loggingKey);
        }
        HashMap<String, Object> loggingContent = new HashMap<String, Object>();
        loggingContent.put("driver", "syslog");

        HashMap<String, Object> loggingOptionsContent = new HashMap<String, Object>();

        String host = "";
        String port = logstashTcpPort;

        if (isEMSSelected(execution)) {
            // ET_EMS env vars created in EsmService setTssEnvVarByEndpoint()
            host = execution.getTJobExec().getEnvVars()
                    .get("ET_EMS_TCP_SUTLOGS_HOST");
            port = execution.getTJobExec().getEnvVars()
                    .get("ET_EMS_TCP_SUTLOGS_PORT");
        } else {
            try {
                host = getLogstashHost();
            } catch (Exception e) {
                throw new TJobStoppedException(
                        "Error on set Logging to Service of docker compose yml:"
                                + e);
            }
        }

        if (host != null && !"".equals(host) && port != null
                && !"".equals(port)) {

            loggingOptionsContent.put("syslog-address",
                    "tcp://" + host + ":" + port);
            loggingOptionsContent.put("syslog-format", "rfc5424micro");

            loggingOptionsContent.put("tag",
                    composeProjectName + "_" + service.getKey() + "_exec");

            loggingContent.put("options", loggingOptionsContent);

            serviceContent.put(loggingKey, loggingContent);

            return service;
        } else {
            throw new Exception("Error on get Logging config. Host(" + host
                    + ") or Port(" + port + ") are null");
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private HashMap.Entry<String, HashMap> setNetworkToDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName,
            Execution execution) {

        HashMap serviceContent = service.getValue();
        String networksKey = "networks";
        // If service has networks, remove it
        if (serviceContent.containsKey(networksKey)) {
            serviceContent.remove(networksKey);
        }

        List<String> networksList = new ArrayList<>();
        networksList.add(getElastestNetwork());
        serviceContent.put(networksKey, networksList);

        return service;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private HashMap.Entry<String, HashMap> setETLabelsToDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName,
            Execution execution) {

        HashMap serviceContent = service.getValue();
        String labelsKey = "labels";
        // If service has networks, remove it
        if (serviceContent.containsKey(labelsKey)) {
            serviceContent.remove(labelsKey);
        }

        Map<String, String> labelsMap = getEtLabels(execution, "sut",
                service.getKey());

        serviceContent.put(labelsKey,
                dockerComposeService.mapAsList(labelsMap));

        return service;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Map<String, HashMap<String, HashMap>> setNetworkToDockerComposeYmlRoot(
            Map<String, HashMap<String, HashMap>> dockerComposeMap,
            String composeProjectName, Execution execution) {

        String networksKey = "networks";
        // If service has networks, remove it
        if (dockerComposeMap.containsKey(networksKey)) {
            dockerComposeMap.remove(networksKey);
        }

        HashMap<String, HashMap> networkMap = new HashMap();
        HashMap<String, Boolean> networkOptions = new HashMap<>();
        networkOptions.put("external", true);

        networkMap.put(getElastestNetwork(), networkOptions);
        dockerComposeMap.put(networksKey, networkMap);

        return dockerComposeMap;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private HashMap.Entry<String, HashMap> setBindingPortYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName)
            throws TJobStoppedException {
        logger.info("Binding the port of the SUT");
        HashMap serviceContent = service.getValue();
        String portsKey = "ports";
        String exposeKey = "expose";

        List<String> bindingPorts = new ArrayList<>();
        String servicePort = ((List<Integer>) serviceContent.get(exposeKey))
                .get(0).toString();
        logger.info("Service port: {}", servicePort);
        bindingPorts.add(servicePort + ":" + servicePort);
        serviceContent.put(portsKey, bindingPorts);

        return service;
    }

    private String getCurrentExecSutMainServiceName(SutSpecification sut,
            Execution execution) {
        return generateContainerName(ContainerPrefix.SUT, execution) + "_"
                + sut.getMainService() + "_1";
    }

    private ProgressHandler getEtPluginProgressHandler(String projectName,
            String image, DockerServiceStatus serviceToPull) {
        DockerPullImageProgress dockerPullImageProgress = new DockerPullImageProgress();
        dockerPullImageProgress.setImage(image);
        dockerPullImageProgress.setCurrentPercentage(0);

        updateStatus(projectName, DockerServiceStatusEnum.PULLING,
                "Pulling " + image + " image", serviceToPull);
        return new ProgressHandler() {
            @Override
            public void progress(ProgressMessage message)
                    throws DockerException {
                dockerPullImageProgress.processNewMessage(message);
                String msg = "Pulling image " + image + " from " + projectName
                        + " ET Plugin: "
                        + dockerPullImageProgress.getCurrentPercentage() + "%";

                updateStatus(projectName, DockerServiceStatusEnum.PULLING, msg,
                        serviceToPull);
            }

        };

    }

    private void updateStatus(String serviceName,
            DockerServiceStatusEnum status, String statusMsg,
            DockerServiceStatus serviceToPull) throws NotFoundException {
        serviceToPull.setStatus(status);
        serviceToPull.setStatusMsg(statusMsg);
    }

    @Override
    public String undeployTSSByContainerId(String containerId) {
        try {
            dockerService.stopDockerContainer(containerId);
            dockerService.removeDockerContainer(containerId);
        } catch (Exception e) {
            logger.error("Error on stop and remove container {}", containerId,
                    e);
        }
        return null;
    }

    @Override
    public ServiceBindedPort getBindingPort(String containerIp,
            String containerSufix, String port, String networkName)
            throws Exception {
        String bindedPort = "37000";
        String socatContainerId = null;
        try {
            bindedPort = String.valueOf(UtilTools.findRandomOpenPort());
            List<String> envVariables = new ArrayList<>();
            envVariables.add("LISTEN_PORT=" + bindedPort);
            envVariables.add("FORWARD_PORT=" + port);
            envVariables.add("TARGET_SERVICE_IP=" + containerIp);
            // String listenPortAsString = String.valueOf(bindedPort);

            DockerBuilder dockerBuilder = new DockerBuilder(etSocatImage);
            dockerBuilder.envs(envVariables);
            dockerBuilder.containerName("socat_"
                    + (containerSufix != null && !containerSufix.isEmpty()
                            ? containerSufix
                            : bindedPort));
            dockerBuilder.network(networkName);
            dockerBuilder.exposedPorts(Arrays.asList(bindedPort));

            // portBindings
            Map<String, List<PortBinding>> portBindings = new HashMap<>();
            portBindings.put(bindedPort,
                    Arrays.asList(PortBinding.of("0.0.0.0", bindedPort)));
            dockerBuilder.portBindings(portBindings);

            dockerService.pullImage(etSocatImage);

            socatContainerId = dockerService
                    .createAndStartContainer(dockerBuilder.build());
            logger.info("Socat container id: {} ", socatContainerId);

        } catch (Exception e) {
            throw new Exception("Error on bindingPort (start socat container)",
                    e);
        }

        ServiceBindedPort bindedPortObj = new ServiceBindedPort(port,
                bindedPort, socatContainerId);

        return bindedPortObj;
    }

    @Override
    public boolean createServiceDeploymentProject(String projectName,
            String serviceDescriptor, String targetPath, boolean override,
            Map<String, String> envs, boolean withBindedExposedPortsToRandom,
            boolean withRemoveVolumes) throws Exception {
        return dockerComposeService.createProjectWithEnv(projectName,
                serviceDescriptor, targetPath, true, envs, false, false);
    }

    @Override
    public List<String> getServiceDeploymentImages(String projectName)
            throws Exception {
        List<String> images = dockerComposeService
                .getProjectImages(projectName);
        return images;
    }

    @Override
    public boolean undeployService(String projectName) throws IOException {
        return dockerComposeService.stopProject(projectName);
    }

    @Override
    public boolean undeployAndCleanDeployment(String projectName) {
        return dockerComposeService.stopAndRemoveProject(projectName);
    }

    @Override
    public boolean deployService(String projectName, boolean withPull)
            throws IOException {
        return dockerComposeService.startProject(projectName, false);
    }

    @Override
    public List<String> getDeploymentImages(String projectName)
            throws Exception {
        return dockerComposeService.getProjectImages(projectName);
    }

    @Override
    public void pullImageWithProgress(String projectName,
            ProgressHandler progressHandler, String image) throws Exception {
        dockerComposeService.pullImageWithProgressHandler(projectName,
                progressHandler, image);
    }

    @Override
    public void pullDeploymentImages(String projectName,
            DockerServiceStatus serviceStatus, List<String> images,
            boolean withProgress) throws Exception {
        for (String image : images) {
            ProgressHandler progressHandler = getEtPluginProgressHandler(
                    projectName, image, serviceStatus);

            pullImageWithProgress(projectName, progressHandler, image);
        }
    }

    @Override
    public DockerContainerInfo getContainers(String projectName) {
        List<Container> containers;
        DockerContainerInfo dockerContainerInfo = new DockerContainerInfo();
        try {
            containers = dockerService.getContainersByPrefix(projectName);
            for (Container container : containers) {
                io.elastest.epm.client.json.DockerContainerInfo.DockerContainer dockerContainer = new io.elastest.epm.client.json.DockerContainerInfo.DockerContainer();
                dockerContainer.initFromContainer(container);
                dockerContainerInfo.getContainers().add(dockerContainer);
            }

        } catch (Exception e) {
            logger.error("Error on get containers of project {}", projectName);
        }
        return dockerContainerInfo;
    }

    @Override
    public boolean isContainerIntoNetwork(String networkId, String containerId)
            throws Exception {
        return dockerService.isContainerIntoNetwork(networkId, containerId);
    }

    @Override
    public String getContainerIpByNetwork(String containerId, String network)
            throws Exception {
        return dockerService.getContainerIpByNetwork(containerId, network);
    }

    @Override
    public void insertIntoETNetwork(String engineName, String network)
            throws Exception {
        try {
            for (io.elastest.epm.client.json.DockerContainerInfo.DockerContainer container : getContainers(
                    engineName).getContainers()) {
                try {
                    dockerService.insertIntoNetwork(network,
                            container.getName());
                    try {
                        // Insert into bridge too
                        dockerService.insertIntoNetwork("bridge",
                                container.getName());
                    } catch (Exception e) {
                        logger.error("Error on insert container "
                                + container.getName() + " into bridge network");
                    }
                } catch (InterruptedException | DockerCertificateException e) {
                    throw new Exception(
                            "Error on insert container " + container.getName()
                                    + " into " + network + " network");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getContainerName(String serviceName, String network) {
        String currentContainerName = null;
        try {
            for (Container container : dockerComposeService.dockerService
                    .getAllContainers()) {
                currentContainerName = container.names().get(0); // example:

                if (currentContainerName != null
                        && currentContainerName.endsWith(serviceName + "_1")
                        && isContainerIntoNetwork(network,
                                currentContainerName)) {
                    return currentContainerName;

                }
            }
        } catch (Exception e) {
            logger.error("Error on get {} internal url", serviceName);
        }
        return currentContainerName;
    }

    @Override
    public void enableServiceMetricMonitoring(Execution execution)
            throws Exception {
        // Start Dockbeat
        startDockbeat(execution);
    }

    @Override
    public void disableMetricMonitoring(Execution execution, boolean force)
            throws Exception {
        String containerName = getDockbeatContainerName(execution);
        if (force) {
            endContainer(containerName, 1);
        } else {
            endContainer(containerName);
        }
    }

    @Override
    public void deploySut(Execution execution) throws Exception {
        SutSpecification sut = execution.getSut();
        SutExecution sutExec = execution.getSutExec();
        // By Docker Image
        if (sut.getManagedDockerType() != ManagedDockerType.COMPOSE) {
            logger.debug("Is Sut By Docker Image");
            deploySutFromDockerImage(execution);
        }
        // By Docker Compose
        else {
            logger.debug("Is Sut By Docker Compose");
            deploySutFromDockerCompose(execution);
        }
        execution.getSutExec()
                .setDeployStatus(SutExecution.DeployStatusEnum.DEPLOYED);

        String sutIp;
        String sutContainerId = getSutContainerIdByExec(
                execution.getExecutionId().toString());
        sutIp = getContainerIpWithDockerExecution(sutContainerId, execution);

        // If port is defined, wait for SuT ready
        if (sut.getPort() != null && !"".equals(sut.getPort())) {
            String sutPort = sut.getPort();
            String resultMsg = "Waiting for dockerized SuT";
            logger.info(resultMsg);
            execution.updateTJobExecutionStatus(ResultEnum.WAITING_SUT,
                    resultMsg);

            // If is Sut In new Container
            if (sut.isSutInNewContainer()) {
                sutIp = this.waitForSutInContainer(execution, 480000); // 8min
            }

            // Wait for SuT started
            resultMsg = "Waiting for SuT service ready at ip " + sutIp
                    + " and port " + sutPort;
            logger.debug(resultMsg);
            checkSut(execution, sutIp, sutPort);
            endContainer(getCheckName(execution));
        }

        // Save SuT Url and Ip into sutexec
        String sutUrl = sut.getSutUrlByGivenIp(sutIp);
        sutExec.setUrl(sutUrl);
        sutExec.setIp(sutIp);
    }

    @Override
    public void undeploySut(Execution execution, boolean force)
            throws Exception {
        SutSpecification sut = execution.getSut();
        removeSutVolumeFolder(execution);
        // If it's Managed Sut, and container is created
        if (sut.getSutType() != SutTypeEnum.DEPLOYED) {
            updateSutExecDeployStatus(execution, DeployStatusEnum.UNDEPLOYING);
            try {
                if (sut.getManagedDockerType() != ManagedDockerType.COMPOSE) {
                    if (sut.isSutInNewContainer()) {
                        endSutInContainer(execution);
                    }
                    String sutContainerName = generateContainerName(
                            ContainerPrefix.SUT, execution);
                    if (force) {
                        endContainer(sutContainerName, 1);
                    } else {
                        endContainer(sutContainerName);
                    }
                } else {
                    endComposedSutExec(execution);
                }
                updateSutExecDeployStatus(execution,
                        DeployStatusEnum.UNDEPLOYED);
            } finally {
                if (execution.getSutExec() != null
                        && execution.getSutExec().getPublicPort() != null) {
                    logger.debug("Removing sut socat container: {}",
                            "socat_sut_" + execution.getSutExec().getId());
                    endContainer("socat_sut_" + execution.getSutExec().getId());
                    removeSutByExecution(execution.getExecutionId().toString());
                }
            }
        }
        endCheckSutExec(execution);
    }

    @Override
    public void undeployTJob(Execution execution, boolean force)
            throws Exception {
        String testContainerName = getTestName(execution);
        if (force) {
            endContainer(testContainerName, 1);
        } else {
            endContainer(testContainerName);
        }
    }

    @Override
    public String getEtmHost() throws Exception {
        if (utilsService.isEtmInContainer()) {
            return dockerService.getContainerIpByNetwork(etEtmContainerName,
                    elastestNetwork);
        } else {
            return dockerService.getHostIpByNetwork(elastestNetwork);
        }
    }

    @Override
    public String getLogstashHost() throws Exception {
        if (utilsService.isElastestMini()) {
            return getEtmHost();
        } else {
            return dockerService.getContainerIpByNetwork(
                    etEtmLogstashContainerName, elastestNetwork);
        }
    }

    @Override
    public VersionInfo getImageInfo(String name) throws Exception {
        return new VersionInfo(dockerService.getImageInfoByName(name));
    }

    @Override
    public VersionInfo getVersionInfoFromContainer(String imageName,
            String version) throws Exception {
        Container container = getContainerFromImage(imageName, version);
        return new VersionInfo(
                dockerService.getImageInfoByContainerId(container.id()));
    }

    @Override
    public String getImageTagFromImageName(String imageName) {
        return dockerService.getTagByCompleteImageName(imageName);
    }

    @Override
    public String getImageNameFromCompleteImageName(String imageName) {
        return dockerService.getImageNameByCompleteImageName(imageName);
    }

    @Override
    public void setCoreServiceInfoFromContainer(String imageName,
            String version, CoreServiceInfo coreServiceInfo) throws Exception {
        coreServiceInfo
                .setDataByContainer(getContainerFromImage(version, imageName));
    }

    @Override
    public String getAllContainerLogs(String containerName, boolean withFollow)
            throws Exception {
        return dockerService.getAllContainerLogs(containerName, withFollow);
    }

    @Override
    public String getSomeContainerLogs(String containerName, int amount,
            boolean withFollow) throws Exception {
        return dockerService.getSomeContainerLogs(containerName, amount,
                withFollow);
    }

    @Override
    public String getContainerLogsFrom(String containerId, int from,
            boolean withFollow) throws Exception {
        return dockerService.getContainerLogsSinceDate(containerId, from,
                withFollow);
    }

}
