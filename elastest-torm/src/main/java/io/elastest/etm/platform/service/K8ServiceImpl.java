package io.elastest.etm.platform.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.spotify.docker.client.ProgressHandler;

import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.etm.model.CoreServiceInfo;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.VersionInfo;

public class K8ServiceImpl extends PlatformService {

    @Override
    public boolean createServiceDeploymentProject(String projectName,
            String serviceDescriptor, String targetPath, boolean override,
            Map<String, String> envs, boolean withBindedExposedPortsToRandom,
            boolean withRemoveVolumes) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deployService(String projectName, boolean withPull)
            throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean undeployService(String projectName) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean undeployAndCleanDeployment(String projectName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getDeploymentImages(String projectName)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void pullImageWithProgress(String projectName,
            ProgressHandler progressHandler, String image) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void pullDeploymentImages(String projectName,
            DockerServiceStatus serviceStatus, List<String> images,
            boolean withProgress) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getServiceDeploymentImages(String projectName)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DockerContainerInfo getContainers(String projectName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isContainerIntoNetwork(String networkId, String containerId)
            throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getContainerIpByNetwork(String containerId, String network)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void insertIntoETNetwork(String engineName, String network)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public String getContainerName(String serviceName, String network) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void enableServiceMetricMonitoring(Execution execution)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void disableMetricMonitoring(Execution execution, boolean force)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void deployAndRunTJobExecution(Execution execution)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void deploySut(Execution execution) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void undeploySut(Execution execution, boolean force)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void undeployTJob(Execution execution, boolean force)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public ServiceBindedPort getBindingPort(String containerIp,
            String containerSufix, String port, String networkName)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getEtmHost() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String undeployTSSByContainerId(String containerId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLogstashHost() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VersionInfo getImageInfo(String name) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public VersionInfo getVersionInfoFromContainer(String version,
            String imageName) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getImageTagFromImageName(String imageName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getImageNameFromCompleteImageName(String imageName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCoreServiceInfoFromContainer(String version,
            String imageName, CoreServiceInfo coreServiceInfo)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public String getAllContainerLogs(String containerName, boolean withFollow)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSomeContainerLogs(String containerName, int amount,
            boolean withFollow) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContainerLogsFrom(String containerId, int from,
            boolean withFollow) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
