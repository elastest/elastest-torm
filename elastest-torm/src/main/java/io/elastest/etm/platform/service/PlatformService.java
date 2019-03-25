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
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.TJobExecution;

public interface PlatformService {
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

    public boolean createServiceDeploymentProject(String projectName,
            String serviceDescriptor, String targetPath, boolean override,
            Map<String, String> envs, boolean withBindedExposedPortsToRandom,
            boolean withRemoveVolumes) throws Exception;

    public boolean deployService(String projectName, boolean withPull)
            throws IOException;

    public boolean undeployService(String projectName) throws IOException;

    public boolean undeployAndCleanDeployment(String projectName);

    public List<String> getDeploymentImages(String projectName)
            throws Exception;

    public void pullImageWithProgress(String projectName,
            ProgressHandler progressHandler, String image) throws Exception;

    public void pullDeploymentImages(String projectName,
            DockerServiceStatus serviceStatus, List<String> images,
            boolean withProgress) throws Exception;

    public List<String> getServiceDeploymentImages(String projectName)
            throws Exception;

    public DockerContainerInfo getContainers(String projectName);

    public boolean isContainerIntoNetwork(String networkId, String containerId)
            throws Exception;

    public String getContainerIpByNetwork(String containerId, String network)
            throws Exception;

    public void insertIntoETNetwork(String engineName, String network)
            throws Exception;

    public String getContainerName(String serviceName, String network);

    public void enableServiceMetricMonitoring(Execution execution)
            throws Exception;

    public void disableMetricMonitoring(Execution execution, boolean force)
            throws Exception;

    public void deployAndRunTJobExecution(Execution execution) throws Exception;

    public default String generateContainerName(ContainerPrefix prefix,
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

    public void deploySut(Execution execution) throws Exception;

    public void undeploySut(Execution execution, boolean force)
            throws Exception;

    public void undeployTJob(Execution execution, boolean force)
            throws Exception;

    public ServiceBindedPort getBindingPort(String containerIp,
            String containerSufix, String port, String networkName,
            boolean remotely) throws Exception;
    
    public String getEtmHost() throws Exception;
    
    public String getLogstashHost() throws Exception;
    /* ********************** */

    public TJobExecution deployTJobExecution(TJobExecution tJobExecution);

    public TJobExecution unDeployTJobExecution(TJobExecution tJobExecution);

    public SupportServiceInstance deployTSS(SupportService supportService);

    public String deployTSSs(TJobExecution tJobExecution);

    public String undeployTSSByContainerId(String containerId);

    public String deploySUT();

    public String undeploySUT();

}
