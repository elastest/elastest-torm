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
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.SocatBindedPort;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.external.ExternalTJobExecution;
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
    private String logstashTcpPort;

    @Value("${et.etm.internal.lsbeats.port}")
    private String lsInternalBeatsPort;

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

    public DockerService dockerService;
    public EtmFilesService filesService;
    public TJobExecRepository tJobExecRepositoryImpl;
    public ExternalTJobExecutionRepository externalTJobExecutionRepository;
    public UtilsService utilsService;

    @Autowired
    public DockerEtmService(DockerService dockerService,
            EtmFilesService filesService,
            TJobExecRepository tJobExecRepositoryImpl,
            UtilsService utilsService,
            ExternalTJobExecutionRepository externalTJobExecutionRepository) {
        this.dockerService = dockerService;
        this.filesService = filesService;
        this.tJobExecRepositoryImpl = tJobExecRepositoryImpl;
        this.utilsService = utilsService;
        this.externalTJobExecutionRepository = externalTJobExecutionRepository;
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
        try {
            this.configureDocker(dockerExec);
            dockerExec.setNetwork(elastestNetwork);
        } catch (Exception e) {
            throw new Exception("Exception on load basic docker services", e);
        }
    }

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
    /* *************************** */
    /* **** Container Methods **** */
    /* *************************** */

    public DockerContainer createContainer(DockerExecution dockerExec,
            String type) throws Exception {
        SutSpecification sut = dockerExec.getSut();

        String image = "";
        String commands = null;
        List<Parameter> parametersList = new ArrayList<Parameter>();
        String prefix = "";
        String suffix = "";
        String containerName = "";
        String sutHost = null;
        String sutPort = null;

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

            sutPath = getSutPath(dockerExec);

            filesService.createExecFilesFolder(sutPath);
        } else if ("tjob".equals(type.toLowerCase())) {
            TJob tJob = dockerExec.gettJob();
            TJobExecution tJobExec = dockerExec.getTJobExec();

            parametersList = tJobExec.getParameters();
            commands = tJob.getCommands();
            image = tJob.getImageName();
            prefix = "test_";
            containerName = getTestName(dockerExec);
            if (dockerExec.isWithSut()) {
                sutHost = dockerExec.getSutExec().getIp();
                sutPort = sut.getPort();
            }
        }

        // Environment variables (optional)
        ArrayList<String> envList = new ArrayList<>();
        String envVar;

        // Get (External)TJob Exec Env Vars
        Map<String, String> tJobEnvVars;
        if (dockerExec.isExternal()) {
            // tJobEnvVars = dockerExec.getExternalTJob().getEnvVars(); TODO
            tJobEnvVars = new HashMap<>();
        } else {
            tJobEnvVars = dockerExec.getTJobExec().getEnvVars();
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
        if (isEMSSelected(dockerExec)) {
            try {
                logConfig = getEMSLogConfig(type, prefix, suffix, dockerExec);
            } catch (Exception e) {
                logger.error("Cannot get Ems Log config", e);
            }
        } else {
            logConfig = getDefaultLogConfig(
                    (EpmService.etMasterSlaveMode ? bindedLsTcpPort
                            : logstashTcpPort),
                    prefix, suffix, dockerExec);
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

    private String getSutPath(DockerExecution dockerExec) {
        String sutPath;
        if (dockerExec.isExternal()) {
            ExternalTJobExecution exTJobExec = dockerExec.getExternalTJobExec();

            sutPath = filesService.buildExternalTJobFilesPath(exTJobExec,
                    ElastestConstants.SUT_FOLDER);

        } else {
            TJobExecution tJobExec = dockerExec.getTJobExec();

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
                dockerPullImageProgress.processNewMessage(message);
                String msg = "Pulling " + name + " image (" + image + "): "
                        + dockerPullImageProgress.getCurrentPercentage() + "%";

                if (dockerExec.isExternal()) {
                    // TODO External status
                } else {
                    TJobExecution tJobExec = dockerExec.getTJobExec();
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

    public String getDockbeatContainerName(DockerExecution dockerExec) {
        String prefix = "elastest_dockbeat_";

        // elastest_dockbeat_X | elastest_dockbeat_extX_eX
        String suffix = dockerExec.isExternal()
                ? dockerExec.getExternalTJobExec()
                        .getExternalTJobExecMonitoringIndex()
                : dockerExec.getExecutionId().toString();

        return prefix + suffix;
    }

    public void startDockbeat(DockerExecution dockerExec) throws Exception {
        try {
            Long execution = dockerExec.getExecutionId();

            String containerName = getDockbeatContainerName(dockerExec);

            // Environment variables
            ArrayList<String> envList = new ArrayList<>();
            String envVar;

            // Get Parameters and insert into Env Vars¡
            String lsHostEnvVar = "LOGSTASHHOST" + "=" + logstashOrMiniHost;
            String lsInternalBeatsPortEnvVar = "LOGSTASHPORT" + "="
                    + lsInternalBeatsPort;

            if (isEMSSelected(dockerExec)) {
                TJobExecution tJobExec = dockerExec.getTJobExec();

                String regexSuffix = "_?(" + execution + ")(_([^_]*(_\\d*)?))?";
                String testRegex = "^test" + regexSuffix;
                String sutRegex = "^sut" + regexSuffix;
                envVar = "FILTER_CONTAINERS" + "=" + testRegex + "|" + sutRegex;
                envList.add(envVar);

                // envVar = "FILTER_EXCLUDE" + "=" + "\"\"";
                // envList.add(envVar);

                lsInternalBeatsPortEnvVar = "LOGSTASHPORT" + "="
                        + tJobExec.getEnvVars().get("ET_EMS_LSBEATS_PORT");

                lsHostEnvVar = "LOGSTASHHOST" + "="
                        + tJobExec.getEnvVars().get("ET_EMS_LSBEATS_HOST");
            }
            envList.add(lsHostEnvVar);
            envList.add(lsInternalBeatsPortEnvVar);

            // dockerSock
            Bind dockerSockVolumeBind = Bind.from(dockerSock).to(dockerSock)
                    .build();

            // Pull Image
            this.pullETExecImage(dockbeatImage, "Dockbeat", false);

            // Create Container
            logger.debug("Creating Dockbeat Container...");

            /* ******************************************************** */
            DockerBuilder dockerBuilder = new DockerBuilder(dockbeatImage);
            dockerBuilder.envs(envList);
            dockerBuilder.containerName(containerName);
            dockerBuilder.network(dockerExec.getNetwork());

            dockerBuilder.volumeBindList(Arrays.asList(dockerSockVolumeBind));

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

    public String getSutName(DockerExecution dockerExec) {
        SutSpecification sut = dockerExec.getSut();
        return this.getSutPrefix(dockerExec)
                + (sut.isDockerCommandsSut() && sut.isSutInNewContainer()
                        ? "_" + sut.getSutInContainerAuxLabel()
                        : "");
    }

    // Hardcoded in EUS, if you make changes here, make them too in EUS
    public String getSutPrefix(DockerExecution dockerExec) {
        String suffix = dockerExec.isExternal()
                ? dockerExec.getExternalTJobExec()
                        .getExternalTJobExecMonitoringIndex()
                : dockerExec.getExecutionId().toString();

        return getSutPrefixBySuffix(suffix);
    }

    public String getSutPrefixBySuffix(String suffix) {
        String prefix = "sut";
        return prefix + "_" + suffix;
    }

    public void createAndStartSutContainer(DockerExecution dockerExec)
            throws Exception {
        try {
            // Create Container Object
            dockerExec.setAppContainer(createContainer(dockerExec, "sut"));

            String resultMsg = "Starting dockerized SuT";
            updateExecutionResultStatus(dockerExec,
                    TJobExecution.ResultEnum.EXECUTING_SUT, resultMsg);
            logger.info(resultMsg + " " + dockerExec.getExecutionId());

            // Create and start container
            String sutContainerId = dockerService.createAndStartContainer(
                    dockerExec.getAppContainer(), EpmService.etMasterSlaveMode);
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
            throws DockerException, Exception {
        String envVar = "IP=" + ip;
        String envVar2 = "PORT=" + port;
        ArrayList<String> envList = new ArrayList<>();
        envList.add(envVar);
        envList.add(envVar2);

        try {
            dockerService.pullImageIfNotExist(checkImage);

            String checkName = getCheckName(dockerExec);

            DockerBuilder dockerBuilder = new DockerBuilder(checkImage);
            dockerBuilder.envs(envList);
            dockerBuilder.containerName(checkName);
            dockerBuilder.network(dockerExec.getNetwork());

            DockerContainer dockerContainer = dockerBuilder.build();
            String checkContainerId = dockerService.createAndStartContainer(
                    dockerContainer, EpmService.etMasterSlaveMode);

            this.insertCreatedContainer(checkContainerId, checkName);

            int statusCode = dockerExec.getDockerClient()
                    .waitContainer(checkContainerId).statusCode();
            if (statusCode == 0) {
                logger.info("Sut is ready " + dockerExec.getExecutionId());

            } else { // TODO timeout or catch stop execution
                throw new TJobStoppedException(
                        "Error on Waiting for CheckSut. Probably because the user has stopped the execution");
            }

        } catch (InterruptedException e) {
            throw new TJobStoppedException(
                    "Error on Waiting for CheckSut. Probably because the user has stopped the execution");
        }
    }

    public void removeSutVolumeFolder(DockerExecution dockerExec) {
        String sutPath = getSutPath(dockerExec);

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

            String resultMsg = "Starting Test Execution";
            updateExecutionResultStatus(dockerExec,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);

            // Create and start container
            String testContainerId = dockerService.createAndStartContainer(
                    dockerExec.getTestcontainer(),
                    EpmService.etMasterSlaveMode);
            dockerExec.setTestContainerId(testContainerId);

            resultMsg = "Executing Test";
            updateExecutionResultStatus(dockerExec,
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);

            String testName = getTestName(dockerExec);
            this.insertCreatedContainer(testContainerId, testName);

            int code = dockerExec.getDockerClient()
                    .waitContainer(testContainerId).statusCode();

            dockerExec.setTestContainerExitCode(code);
            logger.info("Test container ends with code " + code);
        } catch (TJobStoppedException | InterruptedException e) {
            throw new TJobStoppedException(
                    "Error on create and start TJob container: Stopped", e);
        } catch (Exception e) {
            throw new Exception("Error on create and start TJob container", e);
        }
        return getTestResults(dockerExec);
    }

    public void updateExecutionResultStatus(DockerExecution dockerExec,
            ResultEnum result, String msg) {
        if (dockerExec.isExternal()) {
            ExternalTJobExecution externalTJobExec = dockerExec
                    .getExternalTJobExec();
            updateExternalTJobExecResultStatus(externalTJobExec, result, msg);
        } else {
            TJobExecution tJobExec = dockerExec.getTJobExec();
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

    public boolean isEMSSelected(DockerExecution dockerExec) {
        return !dockerExec.isExternal()
                && dockerExec.gettJob().isSelectedService("ems");
    }

    /* ******************************* */
    /* ******* Logging methods ******* */
    /* ******************************* */
    public LogConfig getLogConfig(String host, String port, String tagPrefix,
            String tagSuffix, DockerExecution dockerExec) {
        Map<String, String> configMap = new HashMap<String, String>();

        String monitoringIndex = "";

        if (dockerExec.isExternal()) {
            monitoringIndex = dockerExec.getExternalTJobExec()
                    .getExternalTJobExecMonitoringIndex();
        } else {
            monitoringIndex = dockerExec.getExecutionId().toString();
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
            String tagSuffix, DockerExecution dockerExec) throws Exception {

        initLogstashHostIfNecessary();
        port = masterSlavemode ? bindedLsTcpPort : port;
        logger.info(
                "Logstash/Tcp Server Host to send logs from containers: {}. To port {}",
                logstashOrMiniHost, port);

        return this.getLogConfig(logstashOrMiniHost, port, tagPrefix, tagSuffix,
                dockerExec);
    }

    public LogConfig getEMSLogConfig(String type, String tagPrefix,
            String tagSuffix, DockerExecution dockerExec) throws Exception {
        TJobExecution tJobExec = dockerExec.getTJobExec();
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

    public SocatBindedPort bindingPort(String containerIp, String port,
            String networkName, boolean remotely) throws Exception {
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
            portBindings.put(listenPortAsString, Arrays
                    .asList(PortBinding.of("0.0.0.0", listenPortAsString)));
            dockerBuilder.portBindings(portBindings);

            dockerService.pullImage(etSocatImage);

            bindedPort = dockerService
                    .createAndStartContainer(dockerBuilder.build(), remotely);

        } catch (Exception e) {
            throw new Exception("Error on bindingPort (start socat container)",
                    e);
        }

        SocatBindedPort bindedPortObj = new SocatBindedPort(
                Integer.toString(listenPort), bindedPort);

        return bindedPortObj;
    }

    /* ************************* */
    /* **** Get TestResults **** */
    /* ************************* */

    private List<ReportTestSuite> getTestResults(DockerExecution dockerExec)
            throws Exception {
        try {
            List<ReportTestSuite> testSuites = null;
            String resultsPath = dockerExec.gettJob().getResultsPath();

            if (resultsPath != null && !resultsPath.isEmpty()) {
                try {
                    InputStream inputStream = dockerService
                            .getFileFromContainer(
                                    dockerExec.getTestContainerId(),
                                    resultsPath);

                    String result = IOUtils.toString(inputStream,
                            StandardCharsets.UTF_8);
                    testSuites = getTestSuitesByString(result);
                } catch (IOException e) {
                }
            }
            return testSuites;
        } catch (Exception e) {
            throw new Exception(
                    "Error on get test results. Probably the specified path is incorrect.",
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
