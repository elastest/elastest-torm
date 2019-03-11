package io.elastest.etm.platform.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.spotify.docker.client.ProgressHandler;

import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TJobExecution;

public interface PlatformService {

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
    /* ********************** */

    public TJobExecution deployTJobExecution(TJobExecution tJobExecution);

    public TJobExecution unDeployTJobExecution(TJobExecution tJobExecution);

    public SupportServiceInstance deployTSS(SupportService supportService);

    public String deployTSSs(TJobExecution tJobExecution);

    public String undeployTSS();

    public String deploySUT();

    public String undeploySUT();

}
