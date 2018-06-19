package io.elastest.etm.service;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.InternalServerErrorException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;

@Service
public class DockerService2 {

    private static final Logger logger = LoggerFactory
            .getLogger(DockerService2.class);

    @Value("${docker.sock}")
    private String dockerSock;

    String latestTag = "latest";

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

    /**************************/
    /***** Config Methods *****/
    /**************************/

    public DockerClient getDockerClient() {
        DockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder().build();
        return DockerClientBuilder.getInstance(config).build();
    }

    /*****************************/
    /***** Container Methods *****/
    /*****************************/

    public String runDockerContainer(DockerClient dockerClient,
            String imageName, List<String> envs, String containerName,
            String networkName, Ports portBindings, Integer listenPort)
            throws DockerClientException {
        try {
            this.doPull(dockerClient, imageName);
        } catch (DockerClientException e) {
            throw new DockerClientException("Error on Pulling " + imageName, e);
        }
        CreateContainerCmd createContainer = dockerClient
                .createContainerCmd(imageName);
        if (containerName != null && !"".equals(containerName)) {
            createContainer = createContainer.withName(containerName);
        }

        if (envs != null) {
            createContainer = createContainer.withEnv(envs);
        }

        if (networkName != null && !"".equals(networkName)) {
            createContainer = createContainer.withNetworkMode(networkName);
        }

        if (listenPort != null) {
            createContainer = createContainer
                    .withExposedPorts(ExposedPort.tcp(listenPort));
        }

        if (portBindings != null) {
            createContainer = createContainer.withPortBindings(portBindings);
        }

        createContainer = createContainer.withPublishAllPorts(true);
        CreateContainerResponse container = createContainer.exec();

        dockerClient.startContainerCmd(container.getId()).exec();

        logger.info("Container Id: " + container.getId());

        return container.getId();
    }

    public String runDockerContainer(String imageName, List<String> envs,
            String containerName, String networkName, Ports portBindings,
            Integer listenPort) throws DockerClientException {
        DockerClient dockerClient = this.getDockerClient();
        return this.runDockerContainer(dockerClient, imageName, envs,
                containerName, networkName, portBindings, listenPort);
    }

    public void removeDockerContainer(String containerId) {
        DockerClient dockerClient = this.getDockerClient();
        dockerClient.removeContainerCmd(containerId).exec();
    }

    public void stopDockerContainer(String containerId) {
        DockerClient dockerClient = this.getDockerClient();
        this.stopDockerContainer(dockerClient, containerId);
    }

    public void stopDockerContainer(DockerClient dockerClient,
            String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    /*********************************/
    /***** End execution methods *****/
    /*********************************/

    public void endContainer(String containerName) {
        DockerClient dockerClient = this.getDockerClient();
        if (existsContainer(containerName)) {
            String containerId = getContainerIdByName(containerName);
            int timeout = 60;
            try {
                logger.info("Stopping " + containerName + " container");
                dockerClient.stopContainerCmd(containerId).withTimeout(timeout)
                        .exec();
                // Wait
                dockerClient.waitContainerCmd(containerId)
                        .exec(new WaitContainerResultCallback())
                        .awaitStatusCode();
            } catch (DockerClientException e) {
            } catch (NotModifiedException e) {
                logger.info(
                        "Container " + containerName + " is already stopped");
            } catch (Exception e) {
                logger.info(
                        "Error during stop " + containerName + " container {}",
                        e);
            } finally {
                try {
                    logger.info("Removing " + containerName + " container");
                    dockerClient.removeContainerCmd(containerId).exec();
                } catch (DockerClientException e) {
                } catch (Exception e) {
                    logger.info("Error during remove " + containerName
                            + " container");
                }
            }
        } else {
            logger.info("Could not end " + containerName
                    + " container -> Not started.");
        }
    }

    /* ************* */
    /* *** Utils *** */
    /* ************* */

    public void doPull(DockerClient dockerClient, String image)
            throws DockerClientException {
        image = image.contains(":") ? image : image.concat(":" + latestTag);
        try {
            dockerClient.pullImageCmd(image).exec(new PullImageResultCallback())
                    .awaitSuccess();
        } catch (InternalServerErrorException | NotFoundException ie) {
            if (imageExistsLocally(image, dockerClient)) {
                logger.info("Docker image exits locally.");
            } else {
                logger.error("Error pulling the image: {}", ie.getMessage());
                throw ie;
            }
        } catch (DockerClientException e) {
            throw e;
        }
    }

    public void doPull(String image) throws DockerClientException {
        DockerClient dockerClient = this.getDockerClient();
        this.doPull(dockerClient, image);
    }

    public String getContainerIp(String containerId, String network)
            throws Exception {
        return this.getContainerIpWithDockerClient(this.getDockerClient(),
                containerId, network);
    }

    public String getContainerIpWithDockerClient(DockerClient dockerClient,
            String containerId, String network) throws Exception {
        String ip = dockerClient.inspectContainerCmd(containerId).exec()
                .getNetworkSettings().getNetworks().get(network).getIpAddress();
        return ip.split("/")[0];

    }

    public String waitForContainerIpWith(String containerId, String network,
            long timeout) throws Exception {
        return this.waitForContainerIpWithDockerClient(this.getDockerClient(),
                containerId, network, timeout);
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

    public String getHostIpByNetwork(String network) {
        return this.getDockerClient().inspectNetworkCmd().withNetworkId(network)
                .exec().getIpam().getConfig().get(0).getGateway();
    }

    public boolean imageExist(String imageName) {
        return !this.getDockerClient().searchImagesCmd(imageName).exec()
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
        boolean isAreadyInNetwork = this.isContainerIntoNetwork(networkId,
                containerId);
        if (!isAreadyInNetwork) {
            DockerClient client = getDockerClient();
            client.connectToNetworkCmd().withNetworkId(networkId)
                    .withContainerId(containerId).exec();
        }
    }

    public boolean isContainerIntoNetwork(String networkId,
            String containerId) {
        DockerClient client = getDockerClient();
        Map<String, ContainerNetwork> networksMap = client
                .inspectContainerCmd(containerId).exec().getNetworkSettings()
                .getNetworks();
        return networksMap.get(networkId) != null;
    }

    public List<Container> getBridgeContainersByNamePrefix(String prefix) {
        return this.getContainersByNamePrefix(prefix, "bridge");
    }

    public List<Container> getContainersByNamePrefix(String prefix,
            String network) {
        DockerClient dockerClient = this.getDockerClient();
        List<Container> containers = dockerClient.listContainersCmd()
                .withShowAll(true).exec();

        return this.getContainersByNamePrefixByGivenList(containers, prefix,
                ContainersListActionEnum.NONE, network);
    }

    public List<Container> getContainersCreatedSinceId(String startId) {
        DockerClient dockerClient = this.getDockerClient();
        return dockerClient.listContainersCmd().withShowAll(true)
                .withSince(startId).exec();
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
                        try {
                            this.insertIntoNetwork(network,
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

    public String getAllContainerLogs(String containerId, boolean withFollow) {
        DockerClient dockerClient = this.getDockerClient();
        LogContainerCmd logContainerCmd = dockerClient
                .logContainerCmd(containerId);

        if (withFollow) {
            logContainerCmd = logContainerCmd.withFollowStream(true);
        }

        return this.getContainerLogsByGivenLogContainerCmd(logContainerCmd);
    }

    public String getSomeContainerLogs(String containerId, int amount,
            boolean withFollow) {
        DockerClient dockerClient = this.getDockerClient();
        LogContainerCmd logContainerCmd = dockerClient
                .logContainerCmd(containerId).withTail(amount);

        if (withFollow) {
            logContainerCmd = logContainerCmd.withFollowStream(true);
        }

        return this.getContainerLogsByGivenLogContainerCmd(logContainerCmd);
    }

    /*
     * since time in seconds
     */
    public String getContainerLogsSinceDate(String containerId, int since,
            boolean withFollow) {
        DockerClient dockerClient = this.getDockerClient();
        LogContainerCmd logContainerCmd = dockerClient
                .logContainerCmd(containerId).withSince(since).withTailAll();
        if (withFollow) {
            logContainerCmd = logContainerCmd.withFollowStream(true);
        }

        return this.getContainerLogsByGivenLogContainerCmd(logContainerCmd);
    }

    public String getContainerLogsByGivenLogContainerCmd(
            LogContainerCmd logContainerCmd) {
        StringBuilder logs = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(1);

        logContainerCmd = logContainerCmd.withStdOut(true).withStdErr(true)
                .withTimestamps(true);
        logContainerCmd.exec(getLogsResultCallback(latch, logs));

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(
                    "Interrupted when waiting for complete result callback on docker logs",
                    e);
        }
        return logs.toString();
    }

    public ResultCallback<Frame> getLogsResultCallback(CountDownLatch latch,
            StringBuilder logs) {
        return new ResultCallback<Frame>() {

            @Override
            public void close() throws IOException {

            }

            @Override
            public void onStart(Closeable closeable) {
            }

            @Override
            public void onNext(Frame f) {
                logs.append(new String(f.getPayload()));
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("Error on get container logs: {}",
                        throwable.getMessage());

            }

            @Override
            public void onComplete() {
                latch.countDown();
            }

        };
    }

    public InputStream getFileFromContainer(String containerNameOrId,
            String fileName) {
        InputStream inputStream = null;
        if (existsContainer(containerNameOrId)) {
            inputStream = this.getDockerClient()
                    .copyArchiveFromContainerCmd(containerNameOrId, fileName)
                    .exec();
        }
        return inputStream;
    }

    public boolean existsContainer(String containerName) {
        DockerClient dockerClient = this.getDockerClient();
        return this.existsContainer(containerName, dockerClient);
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

    public String getContainerIdByName(String containerName) {
        DockerClient dockerClient = this.getDockerClient();
        String id = "";
        if (existsContainer(containerName)) {
            try {

                InspectContainerResponse response = dockerClient
                        .inspectContainerCmd(containerName).exec();
                id = response.getId();
            } catch (Exception e) {

            }
        }
        return id;
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

    public List<Container> getRunningContainersByImageName(String imageName) {
        imageName += ":";
        DockerClient dockerClient = getDockerClient();
        List<Container> allContainers = dockerClient.listContainersCmd()
                .withShowAll(true).exec();
        List<Container> imageContainers = new ArrayList<>();
        for (Container currentContainer : allContainers) {
            if (currentContainer.getImage().startsWith(imageName)) {
                imageContainers.add(currentContainer);
            }
        }
        return imageContainers;
    }

    public List<Container> getRunningContainersByImageNameAndVersion(
            String imageName, String version) {
        DockerClient dockerClient = getDockerClient();
        List<Container> allContainers = dockerClient.listContainersCmd()
                .withShowAll(true).exec();
        List<Container> imageContainers = new ArrayList<>();
        for (Container currentContainer : allContainers) {
            if (currentContainer.getImage().equals(imageName)) {
                imageContainers.add(currentContainer);
            }
        }
        return imageContainers;
    }

}
