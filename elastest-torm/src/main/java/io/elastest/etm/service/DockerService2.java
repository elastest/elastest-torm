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
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.ExposedPort;
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
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.utils.UtilTools;

@Service
public class DockerService2 {

    private static final Logger logger = LoggerFactory
            .getLogger(DockerService2.class);

    private static String checkImage = "elastest/etm-check-service-up";
    private static final Map<String, String> createdContainers = new HashMap<>();

    @Value("${logstash.host:#{null}}")
    private String logstashHost;

    @Value("${elastest.docker.network}")
    private String elastestNetwork;

    @Value("${et.dockbeat.image}")
    private String dockbeatImage;

    @Value("${docker.sock}")
    private String dockerSock;

    @Autowired
    public UtilTools utilTools;

    @PreDestroy
    public void removeAllContainers() {
        DockerClient dockerClient = getDockerClient();
        logger.info("Stopping started containers...");
        for (Map.Entry<String, String> entry : createdContainers.entrySet()) {
            String containerId = entry.getKey();
            String containerName = entry.getValue();
            try {
                stopDockerContainer(containerId, dockerClient);
                removeDockerContainer(containerId, dockerClient);
                logger.info(containerName + " removed");

            } catch (Exception e) {
                e.printStackTrace();
            }
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
            String targetContainerName, String networkName, Ports portBindings,
            int listenPort) throws TJobStoppedException {
        try {
            dockerClient.pullImageCmd(imageName)
                    .exec(new PullImageResultCallback()).awaitSuccess();
        } catch (InternalServerErrorException | NotFoundException ie) {
            if (imageExistsLocally(imageName, dockerClient)) {
                logger.info("Docker image exits locally.");
            } else {
                throw ie;
            }
        } catch (DockerClientException e) {
            logger.info("Error on Pulling " + imageName
                    + " image. Probably because the user has stopped the execution");
            throw new TJobStoppedException();
        }
        CreateContainerResponse container = dockerClient
                .createContainerCmd(imageName).withName(containerName)
                .withEnv(envs).withNetworkMode(networkName)
                .withExposedPorts(ExposedPort.tcp(listenPort))
                .withPortBindings(portBindings).withPublishAllPorts(true)
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
        createdContainers.put(container.getId(), containerName);

        logger.info("Id del contenedor:" + container.getId());

        return container.getId();
    }

    public void removeDockerContainer(String containerId,
            DockerClient dockerClient) {
        dockerClient.removeContainerCmd(containerId).exec();
        createdContainers.remove(containerId);
    }

    public void stopDockerContainer(String containerId,
            DockerClient dockerClient) {
        dockerClient.stopContainerCmd(containerId).exec();
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
        String containerName = "";
        int logPort = 5000;

        String sutHost = null;

        if ("sut".equals(type.toLowerCase())) {
            parametersList = sut.getParameters();
            commands = sut.getCommands();
            image = sut.getSpecification();
            prefix = "sut_";
            containerName = getSutName(dockerExec);
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
        if (commands != null && !commands.isEmpty()) {
            cmdList.add("sh");
            cmdList.add("-c");
            cmdList.add(commands);
        }

        // Load Log Config
        LogConfig logConfig = getLogConfig(logPort, prefix, dockerExec);

        // Pull Image
        try {
            dockerExec.getDockerClient().pullImageCmd(image)
                    .exec(new PullImageResultCallback()).awaitSuccess();
        } catch (InternalServerErrorException | NotFoundException ie) {
            if (imageExistsLocally(image, dockerExec.getDockerClient())) {
                logger.info("Docker image exits locally.");
            } else {
                logger.error("Error pulling the image: {}", ie.getMessage());
                throw ie;
            }
        } catch (DockerClientException e) {
            logger.info("Error on Pulling " + type
                    + " image. Probably because the user has stopped the execution");
            throw new TJobStoppedException();
        }

        // Create Container
        return dockerExec.getDockerClient().createContainerCmd(image)
                .withEnv(envList).withLogConfig(logConfig)
                .withName(containerName).withCmd(cmdList)
                .withNetworkMode(dockerExec.getNetwork()).exec();
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
            envVar = "FILTER_CONTAINERS" + "=" + "\"^sut\\d*_.*_" + execution
                    + "|^test\\d*_.*_" + execution + "\"";
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
        try {
            logger.debug("Try to Pulling Dockbeat Image ({})", dockbeatImage);
            dockerExec.getDockerClient().pullImageCmd(dockbeatImage)
                    .exec(new PullImageResultCallback()).awaitSuccess();
        } catch (InternalServerErrorException | NotFoundException ie) {
            if (imageExistsLocally(dockbeatImage,
                    dockerExec.getDockerClient())) {
                logger.info("Docker image exits locally.");
            } else {
                logger.error("Error pulling the image: {}", ie.getMessage());
                throw ie;
            }
        } catch (DockerClientException e) {
            logger.info("Error on Pulling " + dockbeatImage
                    + " image. Probably because the user has stopped the execution");
            throw new TJobStoppedException();
        }

        // Create Container
        logger.debug("Creating Dockbeat Container...");
        CreateContainerResponse container = dockerExec.getDockerClient()
                .createContainerCmd(dockbeatImage).withEnv(envList)
                .withName(containerName)
                .withBinds(new Bind(dockerSock, volume1))
                .withNetworkMode(dockerExec.getNetwork()).exec();

        dockerExec.getDockerClient().startContainerCmd(container.getId())
                .exec();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        createdContainers.put(container.getId(), containerName);

    }

    /***********************/
    /***** Sut Methods *****/
    /***********************/

    public String getSutName(DockerExecution dockerExec) {
        return "sut_" + dockerExec.getExecutionId();
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
        createdContainers.put(sutContainerId, sutName);
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

        try {
            dockerExec.getDockerClient().pullImageCmd(checkImage)
                    .exec(new PullImageResultCallback()).awaitSuccess();
        } catch (InternalServerErrorException | NotFoundException ie) {
            if (imageExistsLocally(checkImage, dockerExec.getDockerClient())) {
                logger.info("Docker image exits locally.");
            } else {
                logger.error("Error pulling the image: {}", ie.getMessage());
                throw ie;
            }
        } catch (DockerClientException e) {
            logger.info(
                    "Error on Pulling CheckSut. Probably because the user has stopped the execution");
            throw new TJobStoppedException();
        }
        String checkName = getCheckName(dockerExec);
        String checkContainerId = dockerExec.getDockerClient()
                .createContainerCmd(checkImage).withEnv(envList)
                .withName(checkName).withNetworkMode(dockerExec.getNetwork())
                .exec().getId();
        dockerExec.getDockerClient().startContainerCmd(checkContainerId).exec();
        createdContainers.put(checkContainerId, checkName);

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
            createdContainers.put(testContainerId, testName);

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

    public LogConfig getLogConfig(int port, String tagPrefix,
            DockerExecution dockerExec) {

        logstashHost = getLogstashHost(dockerExec);

        logger.info(
                "Logstash Host to send logs from containers: {}. To port {}",
                logstashHost, port);

        Map<String, String> configMap = new HashMap<String, String>();
        configMap.put("syslog-address", "tcp://" + logstashHost + ":" + port);
        configMap.put("tag", tagPrefix + dockerExec.getExecutionId() + "_exec");

        LogConfig logConfig = new LogConfig();
        logConfig.setType(LoggingType.SYSLOG);
        logConfig.setConfig(configMap);

        return logConfig;
    }

    /*********************************/
    /***** End execution methods *****/
    /*********************************/

    public void endContainer(DockerExecution dockerExec, String containerName) {
        if (existsContainer(containerName, dockerExec)) {
            String containerId = getContainerIdByName(containerName,
                    dockerExec);
            try {
                logger.info("Stopping " + containerName + " container");
                dockerExec.getDockerClient().stopContainerCmd(containerId)
                        .exec();
            } catch (DockerClientException e) {
                // throw new TJobStoppedException();
            } catch (NotModifiedException e) {
                logger.info(
                        "Container " + containerName + " is already stopped");
            } catch (Exception e) {
                logger.info(
                        "Error during stop " + containerName + " container");
            } finally {
                try {
                    logger.info("Removing " + containerName + " container");
                    dockerExec.getDockerClient().removeContainerCmd(containerId)
                            .exec();
                } catch (DockerClientException e) {
                    // throw new TJobStoppedException();
                } catch (Exception e) {
                    logger.info("Error during remove " + containerName
                            + "container");
                }
                createdContainers.remove(containerId);
            }
        } else {
            logger.info("Could not end " + containerName
                    + " container -> Not started.");
        }
    }

    /*****************/
    /***** Utils *****/
    /*****************/

    public String getContainerIp(String containerId,
            DockerExecution dockerExec) {
        String ip = dockerExec.getDockerClient()
                .inspectContainerCmd(containerId).exec().getNetworkSettings()
                .getNetworks().get(dockerExec.getNetwork()).getIpAddress();
        return ip.split("/")[0];
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

    public String getHostIpByNetwork(DockerExecution dockerExec,
            String network) {
        return dockerExec.getDockerClient().inspectNetworkCmd()
                .withNetworkId(network).exec().getIpam().getConfig().get(0)
                .getGateway();
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
        DockerClient client = getDockerClient();
        client.connectToNetworkCmd().withNetworkId(networkId)
                .withContainerId(containerId).exec();
    }

    public String getLogstashHost(DockerExecution dockerExec) {
        if (logstashHost == null) {
            return getHostIpByNetwork(dockerExec, dockerExec.getNetwork());
        }
        return logstashHost;
    }

    /***************************/
    /***** Get TestResults *****/
    /***************************/

    public InputStream getFileFromContainer(String containerName,
            String fileName, DockerExecution dockerExec) {
        InputStream inputStream = null;
        if (existsContainer(getTestName(dockerExec), dockerExec)) {
            inputStream = dockerExec.getDockerClient()
                    .copyArchiveFromContainerCmd(containerName, fileName)
                    .exec();
        }
        return inputStream;
    }

    public boolean existsContainer(String containerName,
            DockerExecution dockerExec) {
        boolean exists = true;
        try {
            dockerExec.getDockerClient().inspectContainerCmd(containerName)
                    .exec();
        } catch (NotFoundException e) {
            exists = false;
        }
        return exists;
    }

    public String getContainerIdByName(String containerName,
            DockerExecution dockerExec) {
        String id = "";
        if (existsContainer(containerName, dockerExec)) {
            try {

                InspectContainerResponse response = dockerExec.getDockerClient()
                        .inspectContainerCmd(containerName).exec();
                id = response.getId();
            } catch (Exception e) {

            }
        }
        return id;
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
