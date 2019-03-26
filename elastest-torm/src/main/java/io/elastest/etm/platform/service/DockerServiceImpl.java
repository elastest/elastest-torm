package io.elastest.etm.platform.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;

import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.epm.client.json.DockerComposeCreateProject;
import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.json.DockerContainerInfo.DockerContainer;
import io.elastest.epm.client.model.DockerPullImageProgress;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.epm.client.model.DockerServiceStatus.DockerServiceStatusEnum;
import io.elastest.epm.client.service.DockerComposeService;
import io.elastest.epm.client.service.DockerService;
import io.elastest.epm.client.service.DockerService.ContainersListActionEnum;
import io.elastest.epm.client.service.EpmService;
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
import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.VersionInfo;
import io.elastest.etm.service.exception.TJobStoppedException;
import io.elastest.etm.utils.EtmFilesService;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

public class DockerServiceImpl implements PlatformService {
    final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.etm.container.name}")
    private String etEtmContainerName;
    @Value("${elastest.docker.network}")
    private String elastestNetwork;
    @Value("${et.docker.img.socat}")
    public String etSocatImage;
    @Value("${et.etm.logstash.container.name}")
    private String etEtmLogstashContainerName;

    public DockerComposeService dockerComposeService;
    public DockerEtmService dockerEtmService;
    public DockerService dockerService;
    private EpmService epmService;
    private EtmFilesService etmFilesService;
    private UtilsService utilsService;

    public DockerServiceImpl(DockerComposeService dockerComposeService,
            DockerEtmService dockerEtmService, EpmService epmService,
            EtmFilesService etmFilesService, UtilsService utilsService,
            DockerService dockerService) {
        super();
        this.dockerComposeService = dockerComposeService;
        this.dockerEtmService = dockerEtmService;
        this.epmService = epmService;
        this.etmFilesService = etmFilesService;
        this.utilsService = utilsService;
        this.dockerService = dockerService;
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
            String containerSufix, String port, String networkName,
            boolean remotely) throws Exception {
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
                    .createAndStartContainer(dockerBuilder.build(), remotely);
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
            containers = dockerEtmService.dockerService
                    .getContainersByPrefix(projectName);
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
        return dockerEtmService.dockerService.isContainerIntoNetwork(networkId,
                containerId);
    }

    @Override
    public String getContainerIpByNetwork(String containerId, String network)
            throws Exception {
        return dockerEtmService.dockerService
                .getContainerIpByNetwork(containerId, network);
    }

    @Override
    public void insertIntoETNetwork(String engineName, String network)
            throws Exception {
        try {
            for (DockerContainer container : getContainers(engineName)
                    .getContainers()) {
                try {
                    dockerEtmService.dockerService.insertIntoNetwork(network,
                            container.getName());
                    try {
                        // Insert into bridge too
                        dockerEtmService.dockerService.insertIntoNetwork(
                                "bridge", container.getName());
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
        dockerEtmService.startDockbeat(execution);
    }

    @Override
    public void disableMetricMonitoring(Execution execution, boolean force)
            throws Exception {
        String containerName = dockerEtmService
                .getDockbeatContainerName(execution);
        if (force) {
            dockerEtmService.endContainer(containerName, 1);
        } else {
            dockerEtmService.endContainer(containerName);
        }
    }

    @Override
    public void deployAndRunTJobExecution(Execution execution)
            throws Exception {
        dockerEtmService.createAndRunTestContainer(execution);
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
        if (EpmService.etMasterSlaveMode) {
            logger.info("Sut main service name: {}", ("/"
                    + dockerEtmService.getSutName(execution).replaceAll("_", "")
                    + "_" + sut.getMainService() + "_1"));
            sutIp = epmService.getRemoteServiceIpByVduName(
                    "/" + dockerEtmService.getSutName(execution) + "_"
                            + sut.getMainService() + "_1");
            logger.info("Sut main service ip: {}", sutIp);
        } else {
            String sutContainerId = dockerEtmService.getSutContainerIdByExec(
                    execution.getExecutionId().toString());
            sutIp = dockerEtmService.getContainerIpWithDockerExecution(
                    sutContainerId, execution);
        }

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
            dockerEtmService.checkSut(execution, sutIp, sutPort);
            dockerEtmService
                    .endContainer(dockerEtmService.getCheckName(execution));
        }

        // Save SuT Url and Ip into sutexec
        String sutUrl = sut.getSutUrlByGivenIp(sutIp);
        sutExec.setUrl(sutUrl);
        sutExec.setIp(sutIp);
    }

    private void deploySutFromDockerImage(Execution execution)
            throws Exception {
        dockerEtmService.createAndStartSutContainer(execution);
    }

    public void deploySutFromDockerCompose(Execution execution)
            throws Exception {
        SutSpecification sut = execution.getSut();
        String mainService = sut.getMainService();
        logger.debug("The main service saved in DB is: {}", mainService);
        String composeProjectName = dockerEtmService.getSutName(execution);

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

        if (!EpmService.etMasterSlaveMode) {
            for (DockerContainer container : dockerComposeService
                    .getContainers(composeProjectName).getContainers()) {
                String containerId = dockerEtmService.dockerService
                        .getContainerIdByName(container.getName());

                // Insert container into containers list
                dockerEtmService.insertCreatedContainer(containerId,
                        container.getName());
                // If is main service container, set app id
                if (container.getName().equals(
                        composeProjectName + "_" + mainService + "_1")) {
                    dockerEtmService.addSutByExecution(
                            execution.getExecutionId().toString(), containerId);
                }

                if (dockerEtmService.getSutContainerIdByExec(
                        execution.getExecutionId().toString()) == null
                        || dockerEtmService
                                .getSutContainerIdByExec(
                                        execution.getExecutionId().toString())
                                .isEmpty()) {
                    throw new Exception(
                            "Main Sut service from docker compose not started");
                }
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String prepareElasTestConfigInDockerComposeYml(
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
                // Pull images in a local execution
                if (EpmService.etMasterSlaveMode) {
                    pullDockerComposeYmlService(service, execution);
                }

                // Set Logging
                service = setLoggingToDockerComposeYmlService(service,
                        composeProjectName, execution);

                // Set Elastest Network
                service = setNetworkToDockerComposeYmlService(service,
                        composeProjectName, execution);

                // Set Elastest Labels
                service = setETLabelsToDockerComposeYmlService(service,
                        composeProjectName, execution);

                // Binding port of the main service if ElasTest is running in
                // Master/Slave mode
                if (EpmService.etMasterSlaveMode
                        && service.getKey().equals(mainService)) {
                    service = setBindingPortYmlService(service,
                            composeProjectName);
                }
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
            dockerEtmService.pullETExecutionImage(execution, image,
                    service.getKey(), false);
        }
    }

    @Override
    public void undeploySut(Execution execution, boolean force)
            throws Exception {
        SutSpecification sut = execution.getSut();
        dockerEtmService.removeSutVolumeFolder(execution);
        // If it's Managed Sut, and container is created
        if (sut.getSutType() != SutTypeEnum.DEPLOYED) {
            updateSutExecDeployStatus(execution, DeployStatusEnum.UNDEPLOYING);
            try {
                if (sut.getManagedDockerType() != ManagedDockerType.COMPOSE) {
                    if (sut.isSutInNewContainer()) {
                        endSutInContainer(execution);
                    }
                    String sutContainerName = dockerEtmService
                            .getSutName(execution);
                    if (force) {
                        dockerEtmService.endContainer(sutContainerName, 1);
                    } else {
                        dockerEtmService.endContainer(sutContainerName);
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
                    dockerEtmService.endContainer(
                            "socat_sut_" + execution.getSutExec().getId());
                    dockerEtmService.removeSutByExecution(
                            execution.getExecutionId().toString());
                }
            }
        }
        endCheckSutExec(execution);
    }

    public void endComposedSutExec(Execution execution) throws Exception {
        String composeProjectName = dockerEtmService.getSutName(execution);
        dockerComposeService.stopAndRemoveProject(composeProjectName);
    }

    public void endCheckSutExec(Execution execution) throws Exception {
        dockerEtmService.endContainer(dockerEtmService.getCheckName(execution));
    }

    public void endSutInContainer(Execution execution) throws Exception {
        SutSpecification sut = execution.getSut();
        String containerName = null;
        String sutPrefix = null;
        boolean isDockerCompose = false;

        // If is Docker compose Sut
        if (sut.getCommandsOption() == CommandsOptionEnum.IN_DOCKER_COMPOSE) {
            containerName = this.getCurrentExecSutMainServiceName(sut,
                    execution);
            sutPrefix = this.dockerEtmService.getSutPrefix(execution);
            isDockerCompose = true;
        }
        // If is unique Docker image Sut
        else if (sut
                .getCommandsOption() == CommandsOptionEnum.IN_NEW_CONTAINER) {
            containerName = dockerEtmService.getSutPrefix(execution);
            sutPrefix = containerName;
        }

        // Add containers to dockerEtmService list
        if (isDockerCompose) {
            List<Container> containersList = this.dockerEtmService.dockerService
                    .getContainersCreatedSinceId(
                            dockerEtmService.getSutsByExecution().get(
                                    execution.getExecutionId().toString()));
            this.dockerEtmService.dockerService
                    .getContainersByNamePrefixByGivenList(containersList,
                            sutPrefix, ContainersListActionEnum.REMOVE,
                            dockerEtmService.getElastestNetwork());
        } else {
            this.dockerEtmService.endContainer(containerName);
        }
    }

    @Override
    public void undeployTJob(Execution execution, boolean force)
            throws Exception {
        String testContainerName = dockerEtmService.getTestName(execution);
        if (force) {
            dockerEtmService.endContainer(testContainerName, 1);
        } else {
            dockerEtmService.endContainer(testContainerName);
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
        if (EpmService.etMasterSlaveMode) {
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

    public void updateSutExecDeployStatus(Execution execution,
            DeployStatusEnum status) {
        SutExecution sutExec = execution.getSutExec();

        if (sutExec != null) {
            sutExec.setDeployStatus(status);
        }
        execution.setSutExec(sutExec);
    }

    public String waitForSutInContainer(Execution execution, long timeout)
            throws Exception {
        SutSpecification sut = execution.getSut();
        String containerName = null;
        String sutPrefix = null;
        boolean isDockerCompose = false;
        // If is Docker compose Sut
        if (sut.getCommandsOption() == CommandsOptionEnum.IN_DOCKER_COMPOSE) {
            containerName = this.getCurrentExecSutMainServiceName(sut,
                    execution);
            sutPrefix = this.dockerEtmService.getSutPrefix(execution);
            isDockerCompose = true;
            logger.debug(
                    "Is SuT in new container With Docker Compose. Main Service Container Name: {}",
                    containerName);
        }
        // If is unique Docker image Sut
        else if (sut
                .getCommandsOption() == CommandsOptionEnum.IN_NEW_CONTAINER) {
            containerName = dockerEtmService.getSutPrefix(execution);
            sutPrefix = containerName;
            logger.debug(
                    "Is SuT in new container With Docker Image. Container Name: {}",
                    containerName);
        }
        // Wait for created
        this.dockerEtmService.dockerService
                .waitForContainerCreated(containerName, timeout);

        String containerId = this.dockerEtmService.dockerService
                .getContainerIdByName(containerName);
        // Insert main sut/service into ET network if it's necessary
        this.dockerEtmService.dockerService.insertIntoNetwork(
                dockerEtmService.getElastestNetwork(), containerId);

        // Get Main sut/service ip from ET network
        String sutIp = dockerEtmService.waitForContainerIpWithDockerExecution(
                containerName, execution, timeout);

        // Add containers to dockerEtmService list
        if (isDockerCompose) {
            List<Container> containersList = this.dockerEtmService.dockerService
                    .getContainersCreatedSinceId(
                            dockerEtmService.getSutContainerIdByExec(
                                    execution.getExecutionId().toString()));
            this.dockerEtmService.dockerService
                    .getContainersByNamePrefixByGivenList(containersList,
                            sutPrefix, ContainersListActionEnum.ADD,
                            dockerEtmService.getElastestNetwork());
        } else {
            containerId = this.dockerEtmService.dockerService
                    .getContainerIdByName(containerName);
            this.dockerEtmService.insertCreatedContainer(containerId,
                    containerName);
        }
        return sutIp;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HashMap.Entry<String, HashMap> setLoggingToDockerComposeYmlService(
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
        String port = EpmService.etMasterSlaveMode
                ? dockerEtmService.bindedLsTcpPort
                : dockerEtmService.logstashTcpPort;

        if (dockerEtmService.isEMSSelected(execution)) {
            // ET_EMS env vars created in EsmService setTssEnvVarByEndpoint()
            host = execution.getTJobExec().getEnvVars()
                    .get("ET_EMS_TCP_SUTLOGS_HOST");
            port = execution.getTJobExec().getEnvVars()
                    .get("ET_EMS_TCP_SUTLOGS_PORT");
        } else {
            try {
                host = dockerEtmService.getLogstashHost();
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
    public HashMap.Entry<String, HashMap> setNetworkToDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName,
            Execution execution) {

        HashMap serviceContent = service.getValue();
        String networksKey = "networks";
        // If service has networks, remove it
        if (serviceContent.containsKey(networksKey)) {
            serviceContent.remove(networksKey);
        }

        List<String> networksList = new ArrayList<>();
        networksList.add(dockerEtmService.getElastestNetwork());
        serviceContent.put(networksKey, networksList);

        return service;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HashMap.Entry<String, HashMap> setETLabelsToDockerComposeYmlService(
            HashMap.Entry<String, HashMap> service, String composeProjectName,
            Execution execution) {

        HashMap serviceContent = service.getValue();
        String labelsKey = "labels";
        // If service has networks, remove it
        if (serviceContent.containsKey(labelsKey)) {
            serviceContent.remove(labelsKey);
        }

        Map<String, String> labelsMap = dockerEtmService.getEtLabels(execution,
                "sut", service.getKey());

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

        networkMap.put(dockerEtmService.getElastestNetwork(), networkOptions);
        dockerComposeMap.put(networksKey, networkMap);

        return dockerComposeMap;

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public HashMap.Entry<String, HashMap> setBindingPortYmlService(
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

    public String getCurrentExecSutMainServiceName(SutSpecification sut,
            Execution execution) {
        return dockerEtmService.getSutPrefix(execution) + "_"
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

    public void updateStatus(String serviceName, DockerServiceStatusEnum status,
            String statusMsg, DockerServiceStatus serviceToPull)
            throws NotFoundException {
        serviceToPull.setStatus(status);
        serviceToPull.setStatusMsg(statusMsg);
    }

}
