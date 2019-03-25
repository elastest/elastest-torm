package io.elastest.etm.platform.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.spotify.docker.client.ProgressHandler;

import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TJobExecution;

public class K8ServiceImpl implements PlatformService {

    @Override
    public TJobExecution deployTJobExecution(TJobExecution tJobExecution) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TJobExecution unDeployTJobExecution(TJobExecution tJobExecution) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SupportServiceInstance deployTSS(SupportService supportService) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deployTSSs(TJobExecution tJobExecution) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deploySUT() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String undeploySUT() {
        // TODO Auto-generated method stub
        return null;
    }

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
            String containerSufix, String port, String networkName,
            boolean remotely) throws Exception {
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

}
