package io.elastest.epm.client.service;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableList;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DefaultDockerClient.Builder;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ExecCreateParam;
import com.spotify.docker.client.DockerClient.ExecStartParameter;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerMount;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;

import io.elastest.epm.client.DockerContainer;

@Service
public class DockerService {
    private static final Logger logger = LoggerFactory
            .getLogger(DockerService.class);

    @Value("${os.name}")
    private String osName;

    @Value("${docker.port}")
    private String dockerPort;

    @Value("${docker.default.socket}")
    private String dockerSock;

    @Value("${et.master.slave.mode}")
    private boolean etMasterSlaveMode;

    @Value("${elastest.docker.network}")
    private String elastestDockerNetwork;

    @Value("${docker.default.host.ip}")
    private String dockerDefaultHostIp;

    @Value("${docker.server.port}")
    private int dockerServerPort;

    @Value("${docker.wait.timeout.sec}")
    private int dockerWaitTimeoutSec;

    @Value("${docker.poll.time.ms}")
    private int dockerPollTimeMs;

    String dockerServerUrl;
    String latestTag = "latest";
    String remoteDockerServer;

    private String dockerServerIp;
    private boolean isRunningInContainer = false;
    private boolean containerCheked = false;

    @Autowired
    EpmService epmService;
    @Autowired
    ShellService shellService;

    @PostConstruct
    private void init() throws Exception {
        if (osName.toLowerCase().contains("win")) {
            logger.info("Executing on Windows.");
            dockerServerUrl = getDockerHostUrlOnWin();
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
            DockerContainer dockerContainer, boolean withPull)
            throws Exception {
        String imageId = dockerContainer.getImageId();

        if (withPull) {
            this.pullImage(imageId);
        }

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

        Optional<Long> shmSize = dockerContainer.getShmSize();
        if (shmSize.isPresent()) {
            logger.trace("Using shm size {}", shmSize.get());
            hostConfigBuilder.shmSize(shmSize.get());
        }

        Optional<List<String>> capAdd = dockerContainer.getCapAdd();
        if (capAdd.isPresent()) {
            logger.trace("Using capAdd: {}", capAdd.get());
            hostConfigBuilder.capAdd(capAdd.get());
        }

        Optional<Map<String, String>> labels = dockerContainer.getLabels();
        if (labels.isPresent()) {
            logger.trace("Using labels: {}", labels.get());
            containerConfigBuilder.labels(labels.get());
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

    public String createAndStartContainerWithPull(
            DockerContainer dockerContainer, boolean remotely, boolean withPull)
            throws Exception {
        DockerClient dockerClient = this.getDockerClient(remotely);
        return this.createAndStartContainer(dockerClient, dockerContainer,
                withPull);
    }

    public String createAndStartContainer(DockerContainer dockerContainer,
            boolean remotely) throws Exception {
        return this.createAndStartContainerWithPull(dockerContainer, remotely,
                false);
    }

    public void removeDockerContainer(String containerId, boolean removeVolumes)
            throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        List<ContainerMount> volumes = null;
        try {
            volumes = getContainerVolumes(containerId);
        } catch (Exception e) {
            logger.error(
                    "Error on get container {} volumes. Volumes have not been removed",
                    containerId);
        }
        // Remove container
        dockerClient.removeContainer(containerId);

        if (removeVolumes) {
            this.removeVolumes(volumes);
        }
    }

    public void removeDockerContainer(String containerId) throws Exception {
        removeDockerContainer(containerId, true);
    }

    public void stopDockerContainer(DockerClient dockerClient,
            String containerId) throws DockerException, InterruptedException {
        int killAfterSeconds = 60;
        dockerClient.stopContainer(containerId, killAfterSeconds);
    }

    public void stopDockerContainer(String containerId) throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        this.stopDockerContainer(dockerClient, containerId);
    }

    public void stopAndRemoveContainer(String containerId) throws Exception {
        this.stopDockerContainer(containerId);
        this.removeDockerContainer(containerId);
    }

    public void stopAndRemoveContainerWithKillTimeout(String containerId,
            int killAfterSeconds) throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        dockerClient.stopContainer(containerId, killAfterSeconds);
        this.removeDockerContainer(containerId);
    }

    /* ******************************* */
    /* **** End execution methods **** */
    /* ******************************* */

    public void endContainer(String containerName, boolean removeVolumes,
            int timeout) throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        if (existsContainer(containerName)) {
            String containerId = getContainerIdByName(containerName);
            // try {
            logger.info("Stopping " + containerName + " container");
            dockerClient.stopContainer(containerId, timeout);
            // Wait status code
            dockerClient.waitContainer(containerId);

            logger.info("Removing " + containerName + " container");
            this.removeDockerContainer(containerId, removeVolumes);

        } else {
            logger.info("Could not end " + containerName
                    + " container -> Not started.");
        }
    }

    public void endContainer(String containerName, boolean removeVolumes)
            throws Exception {
        endContainer(containerName, removeVolumes, 60);
    }

    public void endContainer(String containerName) throws Exception {
        endContainer(containerName, false);
    }

    /* ****************** */
    /* ***** Images ***** */
    /* ****************** */

    public void pullImageWithoutProgressHandler(DockerClient dockerClient,
            String imageId) throws DockerException, InterruptedException {
        logger.info("Pulling Docker image {}. Please wait...", imageId);

        // If no tag, set latest
        String finalImage = imageId.contains(":") ? imageId
                : imageId.concat(":" + latestTag);

        dockerClient.pull(finalImage);
        logger.info("Docker image {} downloaded", imageId);
    }

    public void pullImageWithoutProgressHandler(String imageId)
            throws Exception {
        this.pullImageWithoutProgressHandler(this.getDockerClient(true),
                imageId);
    }

    public void pullImageWithProgressHandler(DockerClient dockerClient,
            String imageId, ProgressHandler progressHandler)
            throws DockerException, InterruptedException {
        logger.info("Pulling Docker image {}. Please wait...", imageId);

        // If no tag, set latest
        String finalImage = imageId.contains(":") ? imageId
                : imageId.concat(":" + latestTag);

        synchronized (this) {
            if (progressHandler != null) {
                dockerClient.pull(finalImage, progressHandler);
            } else {
                dockerClient.pull(finalImage);
            }
        }
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
                getEmptyProgressHandler());
    }

    public void pullImageIfNotExist(DockerClient dockerClient, String imageId)
            throws DockerException, InterruptedException {
        if (!existsImage(imageId)) {
            this.pullImageWithProgressHandler(dockerClient, imageId,
                    getEmptyProgressHandler());
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

    public ProgressHandler getEmptyProgressHandler() {
        return new ProgressHandler() {
            @Override
            public void progress(ProgressMessage message)
                    throws DockerException {
            }
        };
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

    public ImageInfo getImageInfoByContainerId(String containerId)
            throws Exception {
        ContainerInfo container = getContainerInfoByName(containerId);
        String imageName = container.config().image();
        return getImageInfoByName(imageName);
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

    public String waitForContainerIpWith(String containerId, String network)
            throws Exception {
        return this.waitForContainerIpWithDockerClient(
                this.getDockerClient(true), containerId, network,
                dockerWaitTimeoutSec);
    }

    public String waitForContainerIpWith(String containerId) throws Exception {
        return this.waitForContainerIpWithDockerClient(
                this.getDockerClient(true), containerId, "bridge",
                dockerWaitTimeoutSec);
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

    public boolean waitForContainerCreated(String containerId)
            throws Exception {
        return this.waitForContainerCreated(containerId, dockerWaitTimeoutSec);
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

    public String getNetworkName(String containerId) throws Exception {
        return this.getNetworkName(containerId, this.getDockerClient(true));
    }

    public ImmutableList<String> getContainerNetworks(String containerId,
            DockerClient dockerClient)
            throws DockerException, InterruptedException {
        return dockerClient.inspectContainer(containerId).networkSettings()
                .networks().keySet().asList();
    }

    public ImmutableList<String> getContainerNetworks(String containerId)
            throws Exception {
        return this.getContainerNetworks(containerId,
                this.getDockerClient(true));
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
        List<Container> containers = getContainersByNamePrefix(prefix);

        return this.getContainersByNamePrefixByGivenList(containers, prefix,
                ContainersListActionEnum.NONE, network);
    }

    public List<Container> getContainersByNamePrefix(String prefix)
            throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        List<Container> allContainers = dockerClient
                .listContainers(ListContainersParam.allContainers());
        List<Container> matchPrefixContainers = new ArrayList<>();
        for (Container container : allContainers) {
            if (container.names() != null) {
                boolean match = false;
                for (String name : container.names()) {
                    if (prefix.equals(name)) {
                        match = true;
                        break;
                    }
                }

                if (match) {
                    matchPrefixContainers.add(container);
                }

            }
        }

        return matchPrefixContainers;
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

    public List<Container> getAllContainers() throws Exception {
        DockerClient dockerClient = getDockerClient(false);
        return dockerClient.listContainers(ListContainersParam.allContainers());
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
            String currentImageName = currentContainer.image();
            if (currentImageName != null
                    && currentImageName.equals(imageName.trim())) {
                imageContainers.add(currentContainer);
            }
        }
        return imageContainers;
    }

    public ImmutableList<ContainerMount> getContainerMounts(String containerId)
            throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        return dockerClient.inspectContainer(containerId).mounts();

    }

    /* ***************** */
    /* ***** Binds ***** */
    /* ***************** */
    public List<ContainerMount> getContainerBinds(String containerId)
            throws Exception {
        List<ContainerMount> binds = new ArrayList<>();
        ImmutableList<ContainerMount> mounts = getContainerMounts(containerId);
        for (ContainerMount mount : mounts) {
            if ("bind".equals(mount.type())) {
                binds.add(mount);
            }
        }
        return binds;
    }

    /* ******************* */
    /* ***** Volumes ***** */
    /* ******************* */

    public List<ContainerMount> getContainerVolumes(String containerId)
            throws Exception {
        List<ContainerMount> volumes = new ArrayList<>();
        ImmutableList<ContainerMount> mounts = getContainerMounts(containerId);
        for (ContainerMount mount : mounts) {
            if ("volume".equals(mount.type())) {
                volumes.add(mount);
            }
        }
        return volumes;
    }

    public void removeVolumes(List<ContainerMount> volumes) throws Exception {
        DockerClient dockerClient = this.getDockerClient(true);
        if (volumes != null) {
            for (ContainerMount volume : volumes) {
                try {
                    dockerClient.removeVolume(volume.name());
                } catch (DockerException | InterruptedException e) {
                    logger.error("Error on remove volume {}", volume.name());
                }
            }
        }
    }

    /* ****************** */
    /* ***** Others ***** */
    /* ****************** */

    public static String getDockerHostUrlOnWin() {
        BufferedReader reader = null;
        String dockerHostUrl = "";

        try {
            Process child = Runtime.getRuntime().exec("docker-machine url");
            reader = new BufferedReader(
                    new InputStreamReader(child.getInputStream()));
            dockerHostUrl = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return dockerHostUrl;
    }

    /* Methods From EMP-Client */
    public String generateContainerName(String prefix) {
        String randomSufix = new BigInteger(130, new SecureRandom())
                .toString(32);
        return prefix + randomSufix;
    }

    public String generateEUSBrowserContainerName(String prefix) {
        return prefix + randomUUID().toString();
    }

    public int findRandomOpenPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    public String findRandomOpenPortAsString() throws IOException {
        return Integer.toString(this.findRandomOpenPort());
    }

    public String getDockerServerIp() throws IOException {
        if (dockerServerIp == null) {
            if (IS_OS_WINDOWS) {
                dockerServerIp = getDockerMachineIp();
            } else {
                if (!containerCheked) {
                    isRunningInContainer = shellService.isRunningInContainer();
                    containerCheked = true;
                }
                if (isRunningInContainer) {
                    dockerServerIp = getContainerIp();

                } else {
                    dockerServerIp = dockerDefaultHostIp;
                }
            }
            logger.trace("Docker server IP: {}", dockerServerIp);
        }

        return dockerServerIp;
    }

    public String getDockerServerUrl() throws IOException {
        String out;
        if (dockerServerUrl != null && !dockerServerUrl.equals("")) {
            out = dockerServerUrl;
        } else {
            out = "tcp://" + getDockerServerIp() + ":" + dockerServerPort;
        }
        logger.debug("Docker server URL: {}", out);
        return out;
    }

    public String getDockerMachineIp() throws IOException {
        return shellService.runAndWait("docker-machine", "ip")
                .replaceAll("\\r", "").replaceAll("\\n", "");
    }

    public String getContainerIp() throws IOException {
        String ipRoute = shellService.runAndWait("sh", "-c", "/sbin/ip route");
        String[] tokens = ipRoute.split("\\s");
        return tokens[2];
    }

    private void waitUrl(String url, long timeoutMillis, long endTimeMillis,
            String errorMessage)
            throws IOException, InterruptedException, DockerException {
        int responseCode = 0;
        while (true) {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url)
                        .openConnection();
                connection.setConnectTimeout((int) timeoutMillis);
                connection.setReadTimeout((int) timeoutMillis);
                connection.setRequestMethod("GET");
                responseCode = connection.getResponseCode();

                if (responseCode == HTTP_OK) {
                    logger.debug("URL already reachable");
                    break;
                } else {
                    logger.trace(
                            "URL {} not reachable (response {}). Trying again in {} ms",
                            url, responseCode, dockerPollTimeMs);
                }

            } catch (SSLHandshakeException | SocketException e) {
                logger.trace("Error {} waiting URL {}, trying again in {} ms",
                        e.getMessage(), url, dockerPollTimeMs);

            } finally {
                // Polling to wait a consistent state
                sleep(dockerPollTimeMs);
            }

            if (currentTimeMillis() > endTimeMillis) {
                throw new DockerException(errorMessage);
            }
        }
    }

    public void waitForHostIsReachable(String url) throws DockerException {
        long timeoutMillis = MILLISECONDS.convert(dockerWaitTimeoutSec,
                SECONDS);
        long endTimeMillis = System.currentTimeMillis() + timeoutMillis;

        logger.debug("Waiting for {} to be reachable (timeout {} seconds)", url,
                dockerWaitTimeoutSec);
        String errorMessage = "URL " + url + " not reachable in "
                + dockerWaitTimeoutSec + " seconds";
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[] {};
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs,
                            String authType) {
                        // No actions required
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs,
                            String authType) {
                        // No actions required
                    }
                } };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            waitUrl(url, timeoutMillis, endTimeMillis, errorMessage);

        } catch (Exception e) {
            // Not propagating multiple exceptions (NoSuchAlgorithmException,
            // KeyManagementException, IOException, InterruptedException) to
            // improve readability
            throw new DockerException(errorMessage, e);
        }

    }

    public List<Container> getContainersByPrefix(String prefix)
            throws DockerException, InterruptedException, Exception {
        return getDockerClient(true)
                .listContainers(ListContainersParam.allContainers(true))
                .stream()
                .filter(container -> Arrays
                        .stream(container.names().toArray(new String[0]))
                        .anyMatch(name -> name.startsWith("/" + prefix)))
                .collect(toList());
    }

    public String execCommand(String containerName, boolean awaitCompletion,
            String... command) throws Exception {
        assert (command.length > 0);

        String output = null;
        String commandStr = Arrays.toString(command);

        logger.trace(
                "Executing command {} in container {} (await completion {})",
                commandStr, containerName, awaitCompletion);

        if (existsContainer(containerName)) {
            DockerClient dockerClient = getDockerClient(true);
            ExecCreation exec = dockerClient.execCreate(containerName, command,
                    ExecCreateParam.tty(), ExecCreateParam.attachStdin(true),
                    ExecCreateParam.attachStdout(true),
                    ExecCreateParam.attachStderr(true),
                    ExecCreateParam.detach(awaitCompletion));
            logger.debug("Command executed. Exec id: {}", exec.id());

            LogStream startResultCallback = dockerClient.execStart(exec.id(),
                    ExecStartParameter.TTY);

            if (awaitCompletion) {
                output = startResultCallback.readFully();
            }

            logger.trace("Callback terminated. Result: {}", output);

        }
        return output;
    }

    private void unTar(TarArchiveInputStream tis, File destFolder)
            throws IOException {
        TarArchiveEntry entry = null;
        while ((entry = tis.getNextTarEntry()) != null) {
            FileOutputStream fos = null;
            try {
                if (entry.isDirectory()) {
                    continue;
                }
                File curfile = new File(destFolder, entry.getName());
                File parent = curfile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                fos = new FileOutputStream(curfile);
                IOUtils.copy(tis, fos);
            } catch (Exception e) {
                logger.warn("Exception extracting recording {} to {}", tis,
                        destFolder, e);
            } finally {
                try {
                    if (fos != null) {
                        fos.flush();
                        fos.getFD().sync();
                        fos.close();
                    }
                } catch (IOException e) {
                    logger.warn("Exception closing {}", fos, e);
                }
            }
        }
    }
}
