package io.elastest.etm.platform.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.spotify.docker.client.ProgressHandler;

import io.elastest.epm.client.DockerContainer;
import io.elastest.epm.client.DockerException;
import io.elastest.epm.client.json.DockerComposeProjectMessage;
import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.json.DockerProject;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.epm.client.service.K8sService;
import io.elastest.epm.client.service.ServiceException;
import io.elastest.epm.client.service.K8sService.JobResult;
import io.elastest.epm.client.service.K8sService.PodInfo;
import io.elastest.epm.client.service.K8sService.ServiceInfo;
import io.elastest.etm.model.CoreServiceInfo;
import io.elastest.etm.model.EtPlugin;
import io.elastest.etm.model.Execution;
import io.elastest.etm.model.ServiceBindedPort;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SutExecution.DeployStatusEnum;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.ManagedDockerType;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.VersionInfo;
import io.elastest.etm.service.exception.TJobStoppedException;
import io.elastest.etm.utils.EtmFilesService;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;
import io.fabric8.kubernetes.api.model.Service;

@org.springframework.stereotype.Service
public class K8ServiceImpl extends PlatformService {
    private static final Logger logger = getLogger(lookup().lookupClass());
    private static final Map<String, String> createdContainers = new HashMap<>();
    private Map<String, String> sutsByExecution;

    @Value("${et.etm.internal.host}")
    private String etEtmInternalHost;
    @Value("${hostname}")
    private String hostname;

    private K8sService k8sService;
    private UtilsService utilsService;

    public K8ServiceImpl(K8sService k8sService, EtmFilesService etmFilesService,
            UtilsService utilsService) {
        super();
        this.utilsService = utilsService;
        if (utilsService.isKubernetes()) {
            logger.info("******* ElasTest on K8s *******");
        }
        this.k8sService = k8sService;
        sutsByExecution = new ConcurrentHashMap<String, String>();
        this.etmFilesService = etmFilesService;
    }
    

    @Override
    public boolean createServiceDeploymentProject(String projectName,
            String serviceDescriptor, String targetPath, boolean override,
            Map<String, String> envs, boolean withBindedExposedPortsToRandom,
            boolean withRemoveVolumes) throws Exception {

        return k8sService.createk8sProject(projectName, serviceDescriptor,
                envs) != null ? true : false;
    }

    @Override
    public boolean deployService(String projectName, boolean withPull)
            throws IOException {
        k8sService.createNamespace(projectName);
        return k8sService.deployResourcesFromProject(projectName) != null ? true : false;
    }

    @Override
    public boolean undeployService(String projectName) throws IOException {
        return k8sService.deleteResourcesFromYmlString(projectName);
    }

    @Override
    public boolean undeployAndCleanDeployment(String projectName) {
        return k8sService.deleteResourcesFromYmlString(projectName);
    }

    @Override
    public List<String> getDeploymentImages(String projectName)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void pullProject(String projectName,
            Map<String, EtPlugin> currentEtPluginMap) throws Exception {
        // TODO Auto-generated method stub
        
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
    public String getContainerIp(String containerId)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public String getContainerIp(String serviceName,
            SupportServiceInstance serviceInstance) {
        k8sService.getServiceIpByName(serviceName,
                serviceInstance.getInstanceId());
        return "";
    }

    @Override
    public String getUniqPluginContainerName(String serviceName, String network) {
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
        List<ReportTestSuite> testResults = new ArrayList<ReportTestSuite>();
        TJobExecution tJobExec = execution.getTJobExec();
        JobResult result = null;
        String resultMsg = "";
        try {
            // Create Container Object
            DockerContainer testContainer = createContainer(execution,
                    ContainerType.TJOB);

            resultMsg = "Executing Test";
            execution.updateTJobExecutionStatus(
                    TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);
            execution.setStatusMsg(resultMsg);

            // Create and start container
            result = k8sService.deployJob(testContainer);
            execution.setExitCode(result.getResult());

        } catch (TJobStoppedException | InterruptedException e) {
            throw new TJobStoppedException(
                    "Error on create and start TJob container: Stopped", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error running the TJob", e);
        } finally {
            if (result != null
                    && k8sService.existJobByName(result.getJobName())) {
                k8sService.deleteJob(result.getJobName());
            }
        }

    }

    @Override
    public void deploySut(Execution execution) throws Exception {
        PodInfo podInfo = null;
        try {
            // Create Container Object
            DockerContainer sutContainer = createContainer(execution,
                    ContainerType.SUT);

            String resultMsg = "Starting dockerized SuT";
            execution.updateTJobExecutionStatus(
                    TJobExecution.ResultEnum.EXECUTING_SUT, resultMsg);
            logger.info(resultMsg + " " + execution.getExecutionId());

            // Create and start container
            podInfo = k8sService.deployPod(sutContainer);
            execution.getSutExec().setUrl(
                    execution.getSut().getSutUrlByGivenIp(podInfo.getPodIp()));
            logger.info("Waiting for sut {}", execution.getSut().getName());
            while (!UtilTools.checkIfUrlIsUp(execution.getSutExec().getUrl())) {
                logger.trace("SUT {} is not ready yet",
                        execution.getSut().getName());
            }
            ;

            sutsByExecution.put(execution.getExecutionId().toString(),
                    podInfo.getPodName());

            String sutName = generateContainerName(ContainerPrefix.SUT,
                    execution);
            this.insertCreatedContainer(podInfo.getPodName(), sutName);
            execution.getSutExec().setIp(podInfo.getPodIp());
            logger.debug("Sut Ip stored in the sutExec {}",
                    execution.getSutExec().getIp());
            logger.debug(" Url stored in the sutExec {}",
                    execution.getSutExec().getUrl());

        } catch (Exception e) {
            if (podInfo.getPodName() != null) {
                k8sService.deletePod(podInfo.getPodName());
            }
            throw new Exception("Error on create and start Sut container", e);
        }

    }

    @Override
    public void undeploySut(Execution execution, boolean force)
            throws Exception {
        SutSpecification sut = execution.getSut();
        removeSutVolumeFolder(execution);
        // If it's Managed Sut, and container is created
        if (sut.getSutType() != SutTypeEnum.DEPLOYED) {
            updateSutExecDeployStatus(execution, DeployStatusEnum.UNDEPLOYING);
            try {
                if (sut.getManagedDockerType() != ManagedDockerType.COMPOSE) {
                    if (sut.isSutInNewContainer()) {
                        // endSutInContainer(execution);
                    }
                    String sutContainerName = generateContainerName(
                            ContainerPrefix.SUT, execution);
                    endContainer(sutContainerName);
                } else {
                    // endComposedSutExec(execution);
                }
                updateSutExecDeployStatus(execution,
                        DeployStatusEnum.UNDEPLOYED);
            } finally {

            }
        }
    }

    @Override
    public void undeployTJob(Execution execution, boolean force)
            throws Exception {
        // TODO Auto-generated method stub

    }


    @Override
    public String getEtmHost() throws Exception {
        if (utilsService.isEtmInDevelopment()) {
            return utilsService.getEtPublicHostValue();
        } else {
            return k8sService.getPodIpByPodName(hostname);
        }
    }

    @Override
    public void removeBindedPort(String bindedPortId) {
        k8sService.deleteService(bindedPortId);        
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
    public List<String> getFilesContentFromContainer(String testContainer,
            String filePath, List<String> filterExtensions) throws Exception {
        return k8sService.readFilesFromContainer(testContainer, filePath,
                filterExtensions);
    }

    @Override
    public Integer copyFilesFomContainer(String container, String originPath,
            String targetPath) {
        logger.info("Copy files in {}, from {} to {}.", container, originPath,
                targetPath);
        Integer result = 1;
        result = k8sService.copyFileFromContainer(container, originPath,
                targetPath);
        return result;
    }

    @Override
    protected void endContainer(String containerName) throws Exception {
        k8sService.deletePod(containerName);
        createdContainers.remove(containerName);
    }

    public void insertCreatedContainer(String containerId,
            String containerName) {
        createdContainers.put(containerId, containerName);
    }

    @Override
    protected void endContainer(String containerName, int timeout)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public ServiceBindedPort getBindedPort(String serviceName,
            String containerSufix, String port, String namespace) throws Exception {        
        logger.debug("Init binding port with k8s");
        k8sService.createNamespace(namespace);
        ServiceInfo serviceInfo = k8sService.createService(serviceName,
                Integer.valueOf(port), "http", namespace);
        logger.debug("Getting binding port");
        ServiceBindedPort bindedPortObj = new ServiceBindedPort(port,
                serviceInfo.getServicePort(), serviceInfo.getServiceName());
        logger.debug("Port binded against: {}", serviceInfo.getServicePort());
        return bindedPortObj;

    }
    
    @Override
    public String getBindedServiceIp(SupportServiceInstance serviceInstance,
            String port) {
        logger.debug("Getting binded service ip for the service: {}",
                serviceInstance.getServiceName());
        return k8sService.getServiceIp(
                serviceInstance.getServiceName().toLowerCase(), port,
                serviceInstance.getInstanceId());
    }
    
    @Override
    public String getTSSInstanceContainerName(String... params) {
        String name = "";
        Map<String, String> labels = new HashMap<>();
        labels.put("io.elastest.tjob.tss.subservice.id", params[1]);
        if (k8sService.getPodsByLabels(labels).size() > 0) {
            name = params[1];
        }
        return name;
    }

}
