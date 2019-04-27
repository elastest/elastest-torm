package io.elastest.etm.platform.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spotify.docker.client.ProgressHandler;

import io.elastest.epm.client.DockerContainer;
import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.epm.client.service.K8Service;
import io.elastest.epm.client.service.K8Service.JobResult;
import io.elastest.etm.model.CoreServiceInfo;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.VersionInfo;
import io.elastest.etm.service.exception.TJobStoppedException;

@Service
public class K8ServiceImpl extends PlatformService {

    private K8Service k8Service;

    public K8ServiceImpl(K8Service k8Service) {
        super();
        this.k8Service = k8Service;
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
    public List<ReportTestSuite> deployAndRunTJobExecution(Execution execution)
            throws Exception {
        // TODO Auto-generated method stub
        List<ReportTestSuite> testResults = new ArrayList<ReportTestSuite>();
        TJobExecution tJobExec = execution.getTJobExec();
        try {
            // Create Container Object
            DockerContainer testContainer = createContainer(execution,
                    ContainerType.TJOB);

            String resultMsg = "Starting Test Execution";
            execution.updateTJobExecutionStatus(
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);
            execution.setStatusMsg(resultMsg);

            resultMsg = "Executing Test";
            execution.updateTJobExecutionStatus(
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);
            execution.setStatusMsg(resultMsg);

            // Create and start container
            JobResult result = k8Service.deployJob(testContainer);
            Thread.sleep(5000);

            // Test Results
            if (execution.gettJob().getResultsPath() != null
                    && !execution.gettJob().getResultsPath().isEmpty()) {
                resultMsg = "Waiting for Test Results";
                execution.updateTJobExecutionStatus(
                        TJobExecution.ResultEnum.WAITING, resultMsg);
                execution.setStatusMsg(resultMsg);
                String testResultsAsString = null;
//                String testResultsAsString = getFileFromContainer(
//                        result.getPodName(),
//                        execution.gettJob().getResultsPath());
                testResults = getTestSuitesByString(testResultsAsString);
            }
            
           k8Service.deleteJob(result.getJobName());

            tJobExec.setEndDate(new Date());
            logger.info("Ending Execution {}...", tJobExec.getId());
            saveFinishStatus(tJobExec, execution,
                    (result.getResult().equals("Succeeded") ? 0 : 1));
            return testResults;

        } catch (TJobStoppedException | InterruptedException e) {
            throw new TJobStoppedException(
                    "Error on create and start TJob container: Stopped", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error on create and start TJob container", e);
        }

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

    @Override
    protected String getFileFromContainer(String testContainer, String filePath)
            throws Exception {
        return k8Service.readFileFromContainer(testContainer, filePath);
    }

    @Override
    public Integer copyFilesFomContainer(String container, String originPath, String targetPath) {
        logger.info("Copy files in {}, from {} to {}.", container, originPath, targetPath);
        Integer result = 1;
        result = k8Service.copyFileFromContainer(container, originPath, targetPath);
        return result;
    }

}
