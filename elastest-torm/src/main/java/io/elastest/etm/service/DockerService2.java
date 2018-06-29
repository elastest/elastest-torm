package io.elastest.etm.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DefaultDockerClient.Builder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;

import io.elastest.etm.model.DockerContainer;
import io.elastest.etm.utils.UtilTools;

@Service
public class DockerService2 {
    private static final Logger logger = LoggerFactory
            .getLogger(DockerService2.class);

    @Value("${os.name}")
    private String osName;

    @Value("${docker.port}")
    private String dockerPort;

    @Value("${docker.sock}")
    private String dockerSock;

    @Value("${et.master.slave.mode}")
    private boolean etMasterSlaveMode;

    @Value("${elastest.docker.network}")
    private String elastestDockerNetwork;

    String dockerServerUrl;
    String latestTag = "latest";
    String remoteDockerServer;

    @Autowired
    EpmService epmService;

    @PostConstruct
    private void init() throws Exception {
        if (osName.toLowerCase().contains("win")) {
            logger.info("Executing on Windows.");
            dockerServerUrl = UtilTools.getDockerHostUrlOnWin();
        } else {
            logger.info("Executing on Linux.");
            // dockerServerUrl = "tcp://" + UtilTools.getHostIp() + ":"
            // + dockerPort;
            dockerServerUrl = "unix:///var/run/docker.sock";
        }

        // Set remote docker server
        if (epmService.etMasterSlaveMode) {
            logger.info("Remote ip: {}", epmService.getRe().getHostIp());
            logger.info("Remote Docker port: {}", dockerPort);
            remoteDockerServer = "http://" + epmService.getRe().getHostIp()
                    + ":" + dockerPort;
            NetworkConfig networkConfig = NetworkConfig.builder()
                    .checkDuplicate(true).attachable(true)
                    .name(elastestDockerNetwork).build();
            getDockerClient(true).createNetwork(networkConfig);

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

    /* ************************ */
    /* **** Config Methods **** */
    /* ************************ */

    public DockerClient getDockerClient(boolean remote) throws Exception {
        Builder dockerClientBuilder = DefaultDockerClient.fromEnv();
        if (remote && remoteDockerServer != null) {
            dockerClientBuilder.uri(remoteDockerServer);
        } else {
            dockerClientBuilder.uri(dockerServerUrl);
        }

        return dockerClientBuilder.build();
    }

    /* *************************** */
    /* **** Container Methods **** */
    /* *************************** */

    public String createAndStartContainer(DockerClient dockerClient,
            DockerContainer dockerContainer) throws DockerException,
            InterruptedException, DockerCertificateException {
        String imageId = dockerContainer.getImageId();
        logger.info("Starting Docker container {}", imageId);

        HostConfig.Builder hostConfigBuilder = HostConfig.builder();
        ContainerConfig.Builder containerConfigBuilder = ContainerConfig
                .builder();

        boolean privileged = dockerContainer.isPrivileged();
        if (privileged) {
            logger.trace("Using privileged mode");
            hostConfigBuilder.privileged(true);
        }

        Optional<String> network = dockerContainer.getNetwork();
        if (network.isPresent()) {
            logger.trace("Using network: {}", network.get());
            hostConfigBuilder.networkMode(network.get());
        }
        Optional<Map<String, List<PortBinding>>> portBindings = dockerContainer
                .getPortBindings();
        if (portBindings.isPresent()) {
            logger.trace("Using port bindings: {}", portBindings.get());
            hostConfigBuilder.portBindings(portBindings.get());
            containerConfigBuilder.exposedPorts(portBindings.get().keySet());
        }
        Optional<List<String>> binds = dockerContainer.getBinds();
        if (binds.isPresent()) {
            logger.trace("Using binds: {}", binds.get());
            hostConfigBuilder.binds(binds.get());
        }

        Optional<LogConfig> logConfig = dockerContainer.getLogConfig();
        if (logConfig.isPresent()) {
            logger.trace("Using log config: {}", logConfig.get());
            hostConfigBuilder.logConfig(logConfig.get());
        }

        Optional<List<Bind>> volumeBindList = dockerContainer
                .getVolumeBindList();
        if (volumeBindList.isPresent()) {
            logger.trace("Using volumeBindList: {}", volumeBindList.get());
            hostConfigBuilder.appendBinds(volumeBindList.get()
                    .toArray(new Bind[volumeBindList.get().size()]));
        }

        Optional<List<String>> exposedPorts = dockerContainer.getExposedPorts();
        if (exposedPorts.isPresent()) {
            logger.trace("Using exposed Ports: {}", exposedPorts.get());
            containerConfigBuilder
                    .exposedPorts(new HashSet<String>(exposedPorts.get()));
        }
        Optional<List<String>> envs = dockerContainer.getEnvs();
        if (envs.isPresent()) {
            logger.trace("Using envs: {}", envs.get());
            containerConfigBuilder.env(envs.get());
        }
        Optional<List<String>> cmd = dockerContainer.getCmd();
        if (cmd.isPresent()) {
            logger.trace("Using cmd: {}", cmd.get());
            containerConfigBuilder.cmd(cmd.get());
        }
        Optional<List<String>> entryPoint = dockerContainer.getEntryPoint();
        if (entryPoint.isPresent()) {
            logger.trace("Using entryPoint: {}", entryPoint.get());
            containerConfigBuilder.entrypoint(entryPoint.get());
        }

        ContainerConfig createContainer = containerConfigBuilder.image(imageId)
                .hostConfig(hostConfigBuilder.build()).build();

        String containerId = null;

        Optional<String> containerName = dockerContainer.getContainerName();
        if (containerName.isPresent()) {
            logger.trace("Using container name: {}", containerName.get());
            containerId = dockerClient
                    .createContainer(createContainer, containerName.get()).id();
        } else {
            containerId = dockerClient.createContainer(createContainer).id();
        }
        dockerClient.startContainer(containerId);
        logger.info("Started Docker container of {} with id {}", imageId,
                containerId);

        return containerId;
    }

    public String createAndStartContainer(DockerContainer dockerContainer)
            throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        return this.createAndStartContainer(dockerClient, dockerContainer);
    }

    public void removeDockerContainer(String containerId) throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        dockerClient.removeContainer(containerId);
    }

    public void stopDockerContainer(String containerId) throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        this.stopDockerContainer(dockerClient, containerId);
    }

    public void stopDockerContainer(DockerClient dockerClient,
            String containerId) throws DockerException, InterruptedException {
        int killAfterSeconds = 60;
        dockerClient.stopContainer(containerId, killAfterSeconds);
    }

    /* ******************************* */
    /* **** End execution methods **** */
    /* ******************************* */

    public void endContainer(String containerName) throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        if (existsContainer(containerName)) {
            String containerId = getContainerIdByName(containerName);
            int timeout = 60;
            // try {
            logger.info("Stopping " + containerName + " container");
            dockerClient.stopContainer(containerId, timeout);
            // Wait status code
            dockerClient.waitContainer(containerId);

            // } catch (DockerException e) {
            // } catch (NotModifiedException e) {
            // logger.info(
            // "Container " + containerName + " is already stopped");
            // } catch (Exception e) {
            // logger.info(
            // "Error during stop " + containerName + " container {}",
            // e);
            // } finally {
            // try {
            logger.info("Removing " + containerName + " container");
            this.removeDockerContainer(containerId);
            // } catch (DockerClientException e) {
            // } catch (Exception e) {
            // logger.info(
            // "Error during remove " + containerName + " container");
            // }
            // }
        } else {
            logger.info("Could not end " + containerName
                    + " container -> Not started.");
        }
    }

    /* ****************** */
    /* ***** Images ***** */
    /* ****************** */

    public void pullImageWithProgressHandler(DockerClient dockerClient,
            String imageId, ProgressHandler progressHandler)
            throws DockerException, InterruptedException {
        logger.info("Pulling Docker image {}. Please wait...", imageId);

        // If no tag, set latest
        String finalImage = imageId.contains(":") ? imageId
                : imageId.concat(":" + latestTag);

        dockerClient.pull(finalImage, progressHandler);
        logger.info("Docker image {} downloaded", imageId);
    }

    public void pullImageWithProgressHandler(String imageId,
            ProgressHandler progressHandler)
            throws DockerException, InterruptedException, Exception {
        this.pullImageWithProgressHandler(this.getDockerClient(true), imageId,
                progressHandler);
    }

    public void pullImage(String imageId)
            throws DockerException, InterruptedException, Exception {
        this.pullImageWithProgressHandler(this.getDockerClient(true), imageId,
                new ProgressHandler() {
                    @Override
                    public void progress(ProgressMessage message)
                            throws DockerException {
                    }
                });
    }

    public void pullImageIfNotExist(DockerClient dockerClient, String imageId)
            throws DockerException, InterruptedException {
        if (!existsImage(imageId)) {
            this.pullImageWithProgressHandler(dockerClient, imageId,
                    new ProgressHandler() {
                        @Override
                        public void progress(ProgressMessage message)
                                throws DockerException {
                        }
                    });
        }
    }

    public void pullImageIfNotExistWithProgressHandler(
            DockerClient dockerClient, String imageId,
            ProgressHandler progressHandler)
            throws DockerException, InterruptedException {
        if (!existsImage(imageId)) {
            this.pullImageWithProgressHandler(dockerClient, imageId,
                    progressHandler);
        }
    }

    public void pullImageIfNotExist(String imageId)
            throws DockerException, InterruptedException, Exception {
        this.pullImageIfNotExist(this.getDockerClient(true), imageId);
    }

    public void pullImageIfNotExistWithProgressHandler(String imageId,
            ProgressHandler progressHandler)
            throws DockerException, InterruptedException, Exception {
        this.pullImageIfNotExistWithProgressHandler(this.getDockerClient(true),
                imageId, progressHandler);
    }

    public boolean existsImage(String imageId) {
        boolean exists = true;
        try {
            this.getDockerClient(true).inspectImage(imageId);
            logger.trace("Docker image {} already exists", imageId);

        } catch (Exception e) {
            logger.trace("Image {} does not exist", imageId);
            exists = false;
        }
        return exists;
    }

    public String getTagByCompleteImageName(String imageName) {
        if (imageName == null) {
            return imageName;
        }
        String[] imageNameSplitted = imageName.split(":");
        String tag = null;

        if (imageNameSplitted != null && imageNameSplitted.length > 1
                && imageNameSplitted[1] != null) {
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

    public ImageInfo getImageInfoByName(String imageName) throws Exception {
        ImageInfo response = null;
        DockerClient dockerClient = getDockerClient(false);
        try {
            if (existsImage(imageName)) {
                response = dockerClient.inspectImage(imageName);
            }
        } catch (Exception e) {
            logger.error("Error loading image \"{}\" information.", imageName);
            throw e;
        }
        return response;
    }

    /* ****************** */
    /* *** Containers *** */
    /* ****************** */

    public String getContainerIp(String containerId, String network)
            throws Exception {
        return this.getContainerIpWithDockerClient(this.getDockerClient(true),
                containerId, network);
    }

    public String getContainerIpWithDockerClient(DockerClient dockerClient,
            String containerId, String network) throws Exception {
        String ip = dockerClient.inspectContainer(containerId).networkSettings()
                .networks().get(network).ipAddress();
        return ip.split("/")[0];

    }

    public String waitForContainerIpWith(String containerId, String network,
            long timeout) throws Exception {
        return this.waitForContainerIpWithDockerClient(
                this.getDockerClient(true), containerId, network, timeout);
    }

    public String waitForContainerIpWithDockerClient(DockerClient dockerClient,
            String containerId, String network, long timeout) throws Exception {
        long start_time = System.currentTimeMillis();
        long end_time = start_time + timeout;

        String containerIp = null;

        while (containerIp == null && System.currentTimeMillis() < end_time) {
            try {
                containerIp = this.getContainerIpWithDockerClient(dockerClient,
                        containerId, network);
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

    public boolean waitForContainerCreated(String containerId, long timeout)
            throws Exception {
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

    public String getContainerIpByNetwork(String containerId, String network)
            throws Exception {
        DockerClient client = this.getDockerClient(false);
        ContainerInfo info = client.inspectContainer(containerId);
        String ip = info.networkSettings().networks().get(network).ipAddress();
        return ip.split("/")[0];
    }

    public String getNetworkName(String containerId, DockerClient dockerClient)
            throws DockerException, InterruptedException {
        return (String) dockerClient.inspectContainer(containerId)
                .networkSettings().networks().keySet().toArray()[0];
    }

    public String getHostIpByNetwork(String network) throws Exception {
        return this.getDockerClient(false).inspectNetwork(network).ipam()
                .config().get(0).gateway();
    }

    public void insertIntoNetwork(String networkId, String containerId)
            throws Exception {
        boolean isAreadyInNetwork = this.isContainerIntoNetwork(networkId,
                containerId);
        if (!isAreadyInNetwork) {
            DockerClient client = getDockerClient(true);
            client.connectToNetwork(containerId, networkId);
        }
    }

    public boolean isContainerIntoNetwork(String networkId, String containerId)
            throws Exception {
        DockerClient client = getDockerClient(true);
        Map<String, AttachedNetwork> networksMap = client
                .inspectContainer(containerId).networkSettings().networks();
        return networksMap.get(networkId) != null;
    }

    public List<Container> getBridgeContainersByNamePrefix(String prefix)
            throws Exception {
        return this.getContainersByNamePrefix(prefix, "bridge");
    }

    public List<Container> getContainersByNamePrefix(String prefix,
            String network) throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        List<Container> containers = dockerClient
                .listContainers(ListContainersParam.allContainers());

        return this.getContainersByNamePrefixByGivenList(containers, prefix,
                ContainersListActionEnum.NONE, network);
    }

    public List<Container> getContainersCreatedSinceId(String startId)
            throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        return dockerClient.listContainers(ListContainersParam.allContainers(),
                ListContainersParam.containersCreatedSince(startId));
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
                        try {
                            this.insertIntoNetwork(network,
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

    public String getAllContainerLogs(String containerId, boolean withFollow)
            throws Exception {
        List<LogsParam> params = new ArrayList<>();

        if (withFollow) {
            params.add(LogsParam.follow(true));
        }

        return this.getContainerLogsByGivenLogContainerCmd(containerId, params);
    }

    public String getSomeContainerLogs(String containerId, int amount,
            boolean withFollow) throws Exception {
        List<LogsParam> params = new ArrayList<>();
        params.add(LogsParam.tail(amount));

        if (withFollow) {
            params.add(LogsParam.follow(true));
        }

        return this.getContainerLogsByGivenLogContainerCmd(containerId, params);
    }

    /*
     * since time in seconds
     */
    public String getContainerLogsSinceDate(String containerId, int since,
            boolean withFollow) throws Exception {
        List<LogsParam> params = new ArrayList<>();
        params.add(LogsParam.since(since));
        // params.add(LogsParam.tail(All))); TODO check if gets all

        if (withFollow) {
            params.add(LogsParam.follow(true));
        }

        return this.getContainerLogsByGivenLogContainerCmd(containerId, params);
    }

    public String getContainerLogsByGivenLogContainerCmd(String containerId,
            List<LogsParam> params) throws Exception {

        params.add(LogsParam.stdout(true));
        params.add(LogsParam.stderr(true));
        params.add(LogsParam.timestamps(true));

        DockerClient dockerClient = this.getDockerClient(false);
        LogStream logStream = dockerClient.logs(containerId,
                params.toArray(new LogsParam[params.size()]));

        return logStream.readFully();
    }

    public InputStream getFileFromContainer(String containerNameOrId,
            String fileName) throws Exception {
        InputStream inputStream = null;
        if (existsContainer(containerNameOrId)) {
            inputStream = this.getDockerClient(true)
                    .archiveContainer(containerNameOrId, fileName);
        }
        return inputStream;
    }

    public boolean existsContainer(String containerName) throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        return this.existsContainer(containerName, dockerClient);
    }

    public boolean existsContainer(String containerName,
            DockerClient dockerClient) throws InterruptedException {
        boolean exists = true;
        try {
            dockerClient.inspectContainer(containerName);
        } catch (DockerException e) {
            exists = false;
        }
        return exists;
    }

    public String getContainerIdByName(String containerName) throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        String id = "";
        try {
            if (existsContainer(containerName)) {

                ContainerInfo response = dockerClient
                        .inspectContainer(containerName);
                id = response.id();
            }
        } catch (Exception e) {
        }
        return id;
    }

    public ContainerInfo getContainerInfoByName(String containerName)
            throws Exception {
        ContainerInfo response = null;
        DockerClient dockerClient = getDockerClient(true);
        try {
            if (existsContainer(containerName, dockerClient)) {
                response = dockerClient.inspectContainer(containerName);
            }
        } catch (InterruptedException | DockerException e) {

        }
        return response;
    }

    public List<Container> getRunningContainersByImageName(String imageName)
            throws Exception {
        imageName += ":";
        DockerClient dockerClient = getDockerClient(false);
        List<Container> allContainers = dockerClient
                .listContainers(ListContainersParam.allContainers());
        List<Container> imageContainers = new ArrayList<>();
        for (Container currentContainer : allContainers) {
            if (currentContainer.image().startsWith(imageName)) {
                imageContainers.add(currentContainer);
            }
        }
        return imageContainers;
    }

    public List<Container> getRunningContainersByImageNameAndVersion(
            String imageName, String version) throws Exception {
        DockerClient dockerClient = getDockerClient(false);
        List<Container> allContainers = dockerClient
                .listContainers(ListContainersParam.allContainers());
        List<Container> imageContainers = new ArrayList<>();
        for (Container currentContainer : allContainers) {
            if (currentContainer.image().equals(imageName)) {
                imageContainers.add(currentContainer);
            }
        }
        return imageContainers;
    }

}
