package io.elastest.etm.platform.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.apache.maven.plugins.surefire.report.TestSuiteXmlParser;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.HostConfig.Bind.Builder;
import com.spotify.docker.client.messages.LogConfig;

import io.elastest.epm.client.DockerContainer;
import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.etm.model.CoreServiceInfo;
import io.elastest.etm.model.EtPlugin;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.Parameter;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutExecution.DeployStatusEnum;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.VersionInfo;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.service.exception.TJobStoppedException;
import io.elastest.etm.utils.ElastestConstants;
import io.elastest.etm.utils.EtmFilesService;

public abstract class PlatformService {
    static final Logger logger = getLogger(lookup().lookupClass());
    public static String GET_CONTAINER_IP_COMMAND = "ip a | grep -m 1 global | grep -oE '([0-9]{1,3}\\.){3}[0-9]{1,3}\\/' | grep -oE '([0-9]{1,3}\\.){3}[0-9]{1,3}'";

    protected EtmFilesService etmFilesService;

    @Value("${elastest.docker.network}")
    public String elastestNetwork;
    @Value("${et.type.sut.label.value}")
    public String etTypeSutLabelValue;
    @Value("${et.type.test.label.value}")
    public String etTypeTestLabelValue;
    @Value("${et.tjob.exec.id.label}")
    public String etTJobExecIdLabel;
    @Value("${et.tjob.id.label}")
    public String etTJobIdLabel;
    @Value("${et.type.label}")
    public String etTypeLabel;
    @Value("${et.tjob.sut.service.name.label}")
    public String etTJobSutServiceNameLabel;
    @Value("${et.etm.lstcp.port}")
    public String logstashTcpPort;
    @Value("${docker.sock}")
    public String dockerSock;
    @Value("${et.shared.folder}")
    public String sharedFolder;
    @Value("${et.data.in.host}")
    public String etDataInHost;
    @Value("${logstash.host:#{null}}")
    public String logstashOrMiniHost;
    @Value("${et.enable.cloud.mode}")
    public boolean etEnableCloudMode;
    @Value("${et.proxy.port}")
    String etProxyPort;
    @Value("${et.etm.internal.host}")
    String etEtmInternalHost;
    @Value("${et.etm.internal.lsbeats.port}")
    private String lsInternalBeatsPort;
    @Value("${et.type.monitoring.label.value}")
    public String etTypeMonitoringLabelValue;
    @Value("${et.docker.img.dockbeat}")
    private String dockbeatImage;

    public enum ContainerPrefix {
        TEST("test_"), SUT("sut_"), CHECK("check_"), DOCK_BEAT(
                "elastest_dockbeat_");

        private String value;

        ContainerPrefix(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ContainerPrefix fromValue(String text) {
            for (ContainerPrefix b : ContainerPrefix.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public enum ContainerType {
        SUT("sut"), TJOB("tjob");

        private String value;

        ContainerType(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ContainerType fromValue(String text) {
            for (ContainerType b : ContainerType.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public abstract boolean createServiceDeploymentProject(String projectName,
            String serviceDescriptor, String targetPath, boolean override,
            Map<String, String> envs, boolean withBindedExposedPortsToRandom,
            boolean withRemoveVolumes) throws Exception;

    public abstract boolean createServiceDeploymentProject(String projectName,
            String serviceDescriptor, String targetPath, boolean override,
            Map<String, String> envs, boolean withBindedExposedPortsToRandom,
            boolean withRemoveVolumes, List<String> extraHosts,
            Map<String, String> labels) throws Exception;

    public abstract boolean deployService(String projectName, boolean withPull,
            String namespace) throws IOException, Exception;

    public abstract boolean undeployService(String projectName)
            throws IOException;

    public abstract boolean undeployAndCleanDeployment(String projectName,
            SupportServiceInstance serviceInstance);

    public abstract List<String> getDeploymentImages(String projectName)
            throws Exception;

    public abstract void pullProject(String projectName,
            Map<String, EtPlugin> currentEtPluginMap) throws Exception;

    public abstract void pullImageWithProgress(String projectName,
            ProgressHandler progressHandler, String image) throws Exception;

    public abstract void pullDeploymentImages(String projectName,
            DockerServiceStatus serviceStatus, List<String> images,
            boolean withProgress) throws Exception;

    public abstract List<String> getServiceDeploymentImages(String projectName)
            throws Exception;

    public abstract DockerContainerInfo getContainers(String projectName);

    public abstract boolean isContainerIntoNetwork(String networkId,
            String containerId) throws Exception;

    public abstract String getContainerIp(String containerId) throws Exception;

    public abstract String getContainerIp(String serviceId,
            EtPlugin serviceInstance) throws Exception;

    public abstract String getUniqPluginContainerName(String serviceName,
            String network);

    public abstract String getTSSInstanceContainerName(String... params);

    public abstract void disableMetricMonitoring(Execution execution,
            boolean force) throws Exception;

    public abstract void deployAndRunTJobExecution(Execution execution)
            throws Exception;

    public abstract String getPublicServiceIp(String serviceName, String port,
            String namespace);

    public String generateContainerName(ContainerPrefix prefix,
            Execution execution) {
        return this.generateContainerName(prefix, execution, false);
    }

    public String generateContainerName(ContainerPrefix prefix,
            Execution execution, boolean aux) {
        logger.info("Building container name with prefix: {}", prefix);
        String containerName = "";
        if (prefix == ContainerPrefix.SUT && execution.getSut() != null) {
            // Normal
            if (!execution.isExternal()) {
                containerName = prefix.value + execution.getExecutionId();

            } else { // External TJob (TL)
                containerName = ContainerPrefix.SUT
                        + execution.getExternalTJobExec()
                                .getExternalTJobExecMonitoringIndex();
            }
            SutSpecification sut = execution.getSut();
            containerName += (sut.isDockerCommandsSut()
                    && sut.isSutInNewContainer() && aux
                            ? "_" + sut.getSutInContainerAuxLabel()
                            : "");
        } else {
            containerName = prefix.value + (execution.isExternal()
                    ? execution.getExternalTJobExec()
                            .getExternalTJobExecMonitoringIndex()
                    : execution.getExecutionId().toString());
        }
        logger.debug("Generated container name: {}", containerName);
        return containerName;
    }

    public abstract void deploySut(Execution execution) throws Exception;

    public abstract void undeploySut(Execution execution, boolean force)
            throws Exception;

    public abstract void undeployTJob(Execution execution, boolean force)
            throws Exception;

    public abstract String getEtmHost() throws Exception;

    public abstract String getETPublicHost();

    public abstract String getLogstashHost() throws Exception;

    public abstract VersionInfo getImageInfo(String name) throws Exception;

    public abstract VersionInfo getVersionInfoFromContainer(String imageName,
            String version) throws Exception;

    public abstract String getImageTagFromImageName(String imageName);

    public abstract String getImageNameFromCompleteImageName(String imageName);

    public abstract void setCoreServiceInfoFromContainer(String imageName,
            String version, CoreServiceInfo coreServiceInfo) throws Exception;

    public abstract String getAllContainerLogs(String containerName,
            boolean withFollow) throws Exception;

    public abstract String getSomeContainerLogs(String containerName,
            int amount, boolean withFollow) throws Exception;

    public abstract String getContainerLogsFrom(String containerId, int from,
            boolean withFollow) throws Exception;

    public abstract void removeBindedPorts(
            SupportServiceInstance serviceInstance);

    public abstract void removeBindedPort(String portBindedId);

    public abstract void removeWorkEnvironment(String name);

    public abstract Integer copyFilesFomContainer(String container,
            String originPath, String targetPath);

    public abstract ServiceBindedPort getBindedPort(String serviceIdentifier,
            String containerSufix, String bindedPort, String port,
            String namespace) throws Exception;

    public abstract String getBindedServiceIp(
            SupportServiceInstance serviceInstance, String port);

    public abstract boolean isContainerByServiceName(String serviceName,
            DockerContainerInfo.DockerContainer container);

    protected abstract void startDockbeat(DockerContainer dockerContainer)
            throws Exception;

    public void enableServiceMetricMonitoring(Execution execution)
            throws Exception {
        try {
            Long executionId = execution.getExecutionId();

            String containerName = generateContainerName(
                    ContainerPrefix.DOCK_BEAT, execution);

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

            envVar = "FILTER_CONTAINERS"
                    + "=(^(test|sut|eus(_|-)browser(_|-).*_exec)(_)?(\\d*)(.*)?)|(^(k8s_test|k8s_sut|k8s_eus(_|-)browser(_|-).*(_|-)exec)(.*)?)";
            envList.add(envVar);

            if (isEMSSelected(execution)) {

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
            startDockbeat(dockerContainer);
        } catch (TJobStoppedException e) {
            throw e;
        } catch (Exception e) {
            new Exception("Exception on start Dockbeat", e);
        }
    }

    protected String getSutPath(Execution execution) {
        String sutPath;
        if (execution.isExternal()) {
            ExternalTJobExecution exTJobExec = execution.getExternalTJobExec();

            sutPath = etmFilesService.buildExternalTJobFilesPath(exTJobExec,
                    ElastestConstants.SUT_FOLDER);

        } else {
            TJobExecution tJobExec = execution.getTJobExec();

            logger.debug("etmFilesService {}", etmFilesService != null);
            logger.debug("tJobExec {}", tJobExec != null);
            logger.debug("ElastestConstants.SUT_FOLDER {}",
                    ElastestConstants.SUT_FOLDER != null);
            sutPath = etmFilesService.buildTJobFilesPath(tJobExec,
                    ElastestConstants.SUT_FOLDER);
        }
        return sutPath;
    }

    protected Map<String, String> getEtLabels(Execution execution,
            String type) {
        return this.getEtLabels(execution, type, null);
    }

    protected Map<String, String> getEtLabels(Execution execution, String type,
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

    protected boolean isEMSSelected(Execution execution) {
        return !execution.isExternal()
                && execution.gettJob().isSelectedService("ems");
    }

    protected LogConfig getEMSLogConfig(String type, String tagPrefix,
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

    /* ******************************* */
    /* ******* Logging methods ******* */
    /* ******************************* */
    protected LogConfig getLogConfig(String host, String port, String tagPrefix,
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

        logger.debug("Syslog address: {}", "tcp://" + host + ":" + port);
        configMap.put("syslog-address", "tcp://" + host + ":" + port);
        configMap.put("syslog-format", "rfc5424micro");

        logConfig = LogConfig.create("syslog", configMap);

        return logConfig;
    }

    protected void initLogstashHostIfNecessary() throws Exception {
        if (logstashOrMiniHost == null || logstashOrMiniHost.isEmpty()) {
            logstashOrMiniHost = this.getLogstashHost();
        }
    }

    protected LogConfig getDefaultLogConfig(String port, String tagPrefix,
            String tagSuffix, Execution execution) throws Exception {

        initLogstashHostIfNecessary();
        logger.info(
                "Logstash/Tcp Server Host to send logs from containers: {}. To port {}",
                logstashOrMiniHost, port);

        return this.getLogConfig(logstashOrMiniHost, port, tagPrefix, tagSuffix,
                execution);
    }

    protected LogConfig getLogstashOrMiniLogConfig(String tag)
            throws Exception {
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

    protected DockerContainer createContainer(Execution execution,
            ContainerType type) throws Exception {
        logger.debug("Creating new container");
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

        ArrayList<String> envList = new ArrayList<>();
        String envVar;

        if (ContainerType.SUT.equals(type)) {
            logger.debug("Creating SUT container");
            parametersList = sut.getParameters();
            commands = sut.getCommands();
            image = sut.getSpecification();
            prefix = ContainerPrefix.SUT.toString();
            if (sut.isSutInNewContainer()) {
                suffix = sut.getSutInContainerAuxLabel();
                containerName = generateContainerName(ContainerPrefix.SUT,
                        execution, true);
            } else {
                containerName = generateContainerName(ContainerPrefix.SUT,
                        execution);
                envList.add(
                        "ET_K8S_SUT_NAME=" + containerName.replace("_", "-"));
            }

            sutPath = getSutPath(execution);

            etmFilesService.createFolderIfNotExists(sutPath);
        } else if (ContainerType.TJOB.equals(type)) {
            logger.debug("Creating TJob container");
            TJob tJob = execution.gettJob();
            TJobExecution tJobExec = execution.getTJobExec();

            parametersList = tJobExec.getParameters();
            commands = tJob.getCommands();
            image = tJob.getImageName();
            prefix = ContainerPrefix.TEST.toString();
            containerName = generateContainerName(ContainerPrefix.TEST,
                    execution);

            envVar = "ET_TEST_CONTAINER_NAME=" + containerName;
            envList.add(envVar);

            if (execution.isWithSut()) {
                sutHost = execution.getSutExec().getIp();
                sutPort = sut.getPort();
                sutProtocol = sut.getProtocol().toString();
            }

        }
        /* ********* Environment variables (optional) ********* */

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

        /* ********** Commands (optional) ********** */

        ArrayList<String> cmdList = new ArrayList<>();
        ArrayList<String> entrypointList = null;

        logger.debug("Commands to execute: {}", commands);

        if (commands != null && !commands.isEmpty()) {
            cmdList = new ArrayList<>();
            entrypointList = new ArrayList<>();
            cmdList.add("-c");
            logger.debug("Filling cmdList");
            if (sut != null) {
                if (sut.isSutInNewContainer()) {
                    commands = sutPath != null
                            ? ("cd " + sutPath + ";" + commands)
                            : commands;
                }
            } else {
                commands = "export ET_SUT_HOST=$(" + GET_CONTAINER_IP_COMMAND
                        + ") || echo;" + commands;
            }

            commands = commands + getCommandsByPlatform();
            cmdList.add(commands);
            entrypointList.add("/bin/sh");
            cmdList.forEach((command) -> {
                logger.debug("Commands to execute from cmdList: {}", command);
            });
            if (etEnableCloudMode) {
                cmdList.add(0, entrypointList.get(0));

            }
            cmdList.forEach((command) -> {
                logger.debug("Commands to execute from cmdList: {}", command);
            });
        }

        /* ********** Load Log Config ********** */

        LogConfig logConfig = null;
        if (isEMSSelected(execution)) {
            logger.debug("EMS selected.");
            try {
                logConfig = getEMSLogConfig(type.toString(), prefix, suffix,
                        execution);
            } catch (Exception e) {
                logger.error("Cannot get Ems Log config", e);
            }
        } else {
            logger.debug("Setting syslog");
            logConfig = getDefaultLogConfig((logstashTcpPort), prefix, suffix,
                    execution);
        }

        /* ********** ElasTest labels ********** */
        Map<String, String> labels = this.getEtLabels(execution,
                type.toString());

        /* ******************************************************** */
        DockerBuilder dockerBuilder = new DockerBuilder(image);
        dockerBuilder.envs(envList).logConfig(logConfig)
                .containerName(containerName).cmd(cmdList)
                .entryPoint(entrypointList).network(elastestNetwork)
                .labels(labels);

        List<Bind> volumes = new ArrayList<>();

        /* ********** Docker Sock ********** */

        logger.info("Docker sock volume: {}", dockerSock);
        Builder dockerSockVolumeBuilder = Bind.builder();
        dockerSockVolumeBuilder.from(dockerSock);
        dockerSockVolumeBuilder.to(dockerSock);
        volumes.add(dockerSockVolumeBuilder.build());

        /* ********** Shared data in sutInNewContainer ********** */
        if (sut != null && sut.isSutInNewContainer()) {
            Builder sharedDataVolumeBuilder = Bind.builder();
            sharedDataVolumeBuilder.from(etDataInHost);
            sharedDataVolumeBuilder.to(sharedFolder);

            volumes.add(sharedDataVolumeBuilder.build());
        }

        dockerBuilder.volumeBindList(volumes);

        /* ********** Create DockerContainer object ********** */
        return dockerBuilder.build();
    }

    /* ************************* */
    /* **** Get TestResults **** */
    /* ************************* */

    public List<ReportTestSuite> getTestResultsFromContainer(
            String testContainer, String filePath) throws Exception {
        List<ReportTestSuite> results = new ArrayList<>();
        List<String> files = getFilesContentFromContainer(testContainer,
                filePath, Arrays.asList("xml"));

        if (files != null) {
            for (String fileContent : files) {
                List<ReportTestSuite> testResults = getTestSuitesByString(
                        fileContent);
                if (testResults != null) {
                    results.addAll(testResults);
                }
            }
        }

        return results;
    }

    public abstract List<String> getFilesContentFromContainer(
            String testContainer, String filePath,
            List<String> filterExtensions) throws Exception;

    public List<ReportTestSuite> getTestSuitesByString(String result) {
        List<ReportTestSuite> results = new ArrayList<>();
        String head = "<testsuite ";
        String foot = "</testsuite>";

        if (result != null && !result.isEmpty()) {
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
        }

        return results;
    }

    protected List<ReportTestSuite> testSuiteStringToReportTestSuite(
            String testSuiteStr) throws UnsupportedEncodingException,
            ParserConfigurationException, SAXException, IOException {
        TestSuiteXmlParser testSuiteXmlParser = new TestSuiteXmlParser(null);
        InputStream byteArrayIs = new ByteArrayInputStream(
                testSuiteStr.getBytes());

        // normally, a single test suite, but in some cases returns more than 1
        return testSuiteXmlParser
                .parse(new InputStreamReader(byteArrayIs, "UTF-8"));
    }

    protected void removeSutVolumeFolder(Execution execution) {
        String sutPath = getSutPath(execution);

        try {
            etmFilesService.removeFolder(sutPath);
        } catch (Exception e) {
            logger.debug("The SuT folder could not be deleted: {}",
                    e.getMessage());
        }
    }

    protected void updateSutExecDeployStatus(Execution execution,
            DeployStatusEnum status) {
        SutExecution sutExec = execution.getSutExec();

        if (sutExec != null) {
            sutExec.setDeployStatus(status);
        }
        execution.setSutExec(sutExec);
    }

    protected abstract void endContainer(String containerName, int timeout)
            throws Exception;

    protected abstract void endContainer(String containerName) throws Exception;

    protected abstract String getCommandsByPlatform();
}
