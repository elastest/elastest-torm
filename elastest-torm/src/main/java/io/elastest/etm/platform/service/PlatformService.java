package io.elastest.etm.platform.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spotify.docker.client.ProgressHandler;

import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.etm.model.CoreServiceInfo;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.VersionInfo;

public abstract class PlatformService {
    static final Logger logger = getLogger(lookup().lookupClass());

    public enum ContainerPrefix {
        TEST("test_"), SUT("sut_"), CHECK("check_"), SUT_EXT("sut_ext_");

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

    public abstract boolean createServiceDeploymentProject(String projectName,
            String serviceDescriptor, String targetPath, boolean override,
            Map<String, String> envs, boolean withBindedExposedPortsToRandom,
            boolean withRemoveVolumes) throws Exception;

    public abstract boolean deployService(String projectName, boolean withPull)
            throws IOException;

    public abstract boolean undeployService(String projectName) throws IOException;

    public abstract boolean undeployAndCleanDeployment(String projectName);

    public abstract List<String> getDeploymentImages(String projectName)
            throws Exception;

    public abstract void pullImageWithProgress(String projectName,
            ProgressHandler progressHandler, String image) throws Exception;

    public abstract void pullDeploymentImages(String projectName,
            DockerServiceStatus serviceStatus, List<String> images,
            boolean withProgress) throws Exception;

    public abstract List<String> getServiceDeploymentImages(String projectName)
            throws Exception;

    public abstract DockerContainerInfo getContainers(String projectName);

    public abstract boolean isContainerIntoNetwork(String networkId, String containerId)
            throws Exception;

    public abstract String getContainerIpByNetwork(String containerId, String network)
            throws Exception;

    public abstract void insertIntoETNetwork(String engineName, String network)
            throws Exception;

    public abstract String getContainerName(String serviceName, String network);

    public abstract void enableServiceMetricMonitoring(Execution execution)
            throws Exception;

    public abstract void disableMetricMonitoring(Execution execution, boolean force)
            throws Exception;

    public abstract void deployAndRunTJobExecution(Execution execution) throws Exception;

    public String generateContainerName(ContainerPrefix prefix,
            Execution execution) {
        logger.info("Building container name with prefix: {}", prefix);
        String containerName = prefix.value + execution.getExecutionId();
        if (prefix == ContainerPrefix.SUT && execution.getSut() != null) {
            SutSpecification sut = execution.getSut();
            containerName += (sut.isDockerCommandsSut()
                    && sut.isSutInNewContainer()
                            ? "_" + sut.getSutInContainerAuxLabel()
                            : "");
        }
        return containerName;
    }

    public abstract void deploySut(Execution execution) throws Exception;

    public abstract void undeploySut(Execution execution, boolean force)
            throws Exception;

    public abstract void undeployTJob(Execution execution, boolean force)
            throws Exception;

    public abstract ServiceBindedPort getBindingPort(String containerIp,
            String containerSufix, String port, String networkName) throws Exception;

    public abstract String getEtmHost() throws Exception;

    public abstract String getLogstashHost() throws Exception;

    public abstract VersionInfo getImageInfo(String name) throws Exception;

    public abstract VersionInfo getVersionInfoFromContainer(String imageName,
            String version) throws Exception;

    public abstract String getImageTagFromImageName(String imageName);

    public abstract String getImageNameFromCompleteImageName(String imageName);

    public abstract void setCoreServiceInfoFromContainer(String imageName,
            String version, CoreServiceInfo coreServiceInfo) throws Exception;

    public abstract String getAllContainerLogs(String containerName, boolean withFollow)
            throws Exception;
    
    public abstract String getSomeContainerLogs(String containerName, int amount, boolean withFollow) throws Exception;
    
    public abstract String getContainerLogsFrom(String containerId, int from, boolean withFollow) throws Exception;

    public abstract String undeployTSSByContainerId(String containerId);


}
