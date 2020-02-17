package io.elastest.etm.platform.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.maven.plugins.surefire.report.ReportTestSuite;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.spotify.docker.client.ProgressHandler;

import io.elastest.epm.client.DockerContainer;
import io.elastest.epm.client.json.DockerContainerInfo;
import io.elastest.epm.client.model.DockerServiceStatus;
import io.elastest.epm.client.service.K8sService;
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
import io.fabric8.kubernetes.api.model.Pod;

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
    public boolean createServiceDeploymentProject(String projectName, String serviceDescriptor,
            String targetPath, boolean override, Map<String, String> envs,
            boolean withBindedExposedPortsToRandom, boolean withRemoveVolumes) throws Exception {

        return k8sService.createk8sProject(projectName, serviceDescriptor, envs) != null ? true
                : false;
    }

    @Override
    public boolean createServiceDeploymentProject(String projectName, String serviceDescriptor,
            String targetPath, boolean override, Map<String, String> envs,
            boolean withBindedExposedPortsToRandom, boolean withRemoveVolumes,
            List<String> extraHosts, Map<String, String> labels) throws Exception {
        return k8sService.createk8sProject(projectName, serviceDescriptor, envs, extraHosts,
                labels) != null ? true : false;
    }

    @Override
    public boolean deployService(String projectName, boolean withPull, String namespace)
            throws Exception {
        if (namespace != null && !namespace.isEmpty()) {
            k8sService.createNamespace(projectName);
        }
        return k8sService.deployResourcesFromProject(projectName, namespace) != null ? true : false;
    }

    @Override
    public boolean undeployService(String projectName) throws IOException {
        return k8sService.deleteResources(projectName);
    }

    @Override
    public boolean undeployAndCleanDeployment(String projectName,
            SupportServiceInstance serviceInstance) {
        boolean result = false;
        try {
            if (serviceInstance != null) {
                List<Pod> pods = k8sService.getPodsFromNamespace(serviceInstance.getInstanceId());
                pods.forEach(pod -> {
                    k8sService.copyFileFromContainer(pod.getMetadata().getName(),
                            serviceInstance.getParameters().get("ET_SHARED_FOLDER"),
                            serviceInstance.getParameters().get("ET_FILES_PATH"),
                            serviceInstance.getInstanceId());
                });
            }
        } catch (Exception e) {
            logger.error("Error copying files from {} to {}",
                    serviceInstance.getParameters().get("ET_FILES_PATH"),
                    serviceInstance.getParameters().get("ET_FILES_PATH_IN_HOST"));
        } finally {
            result = k8sService.deleteResources(projectName);
        }
        return result;
    }

    @Override
    public List<String> getDeploymentImages(String projectName) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void pullProject(String projectName, Map<String, EtPlugin> currentEtPluginMap)
            throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void pullImageWithProgress(String projectName, ProgressHandler progressHandler,
            String image) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void pullDeploymentImages(String projectName, DockerServiceStatus serviceStatus,
            List<String> images, boolean withProgress) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getServiceDeploymentImages(String projectName) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DockerContainerInfo getContainers(String projectName) {
        logger.debug("Retriving containers for the service {}", projectName);
        DockerContainerInfo containersInfo = new DockerContainerInfo();
        getServicesPods(projectName, null).forEach(pod -> {
            logger.debug("Pod {} retrived", pod.getMetadata().getName());
            logger.debug("Pod status {}", pod.getStatus().getPhase());
            DockerContainerInfo.DockerContainer containerInfo = new DockerContainerInfo.DockerContainer();
            containerInfo.initFromPod(pod);
            containersInfo.getContainers().add(containerInfo);
        });

        return containersInfo;
    }

    private List<Pod> getServicesPods(String projectName, String namespace) {
        logger.debug("Retriving pods for the service {}", projectName);
        List<Pod> pods = new ArrayList<>();
        Map<String, String> labels = new HashMap<>();
        labels.put("io.elastest.service", projectName);
        pods.addAll(k8sService.getPodsByLabels(labels, namespace));
        return pods;

    }

    @Override
    public boolean isContainerIntoNetwork(String networkId, String containerId) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getContainerIp(String containerId) throws Exception {
        return k8sService.getPodIpByPodName(containerId, null);
    }

    @Override
    public String getContainerIp(String serviceName, EtPlugin serviceInstance) throws Exception {

        if (serviceInstance != null) {
            if (serviceInstance instanceof SupportServiceInstance) {
                logger.debug("Get Container ip for the service {}-{} in the namespace {}",
                        serviceName, ((SupportServiceInstance) serviceInstance).getInstanceId());
                return k8sService.getPodIpByLabel("io.elastest.tjob.tss.id", serviceName,
                        ((SupportServiceInstance) serviceInstance).getInstanceId());
            } else {
                return k8sService.getPodIpByPodName(serviceName, serviceInstance.getName());
            }
        } else {
            return k8sService.getPodIpByPodName(serviceName);
        }
    }

    @Override
    public String getUniquePluginContainerName(String serviceName, String network) {
        Map<String, String> labels = new HashMap<>();
        labels.put("io.elastest.service", serviceName);
        if (!k8sService.getPodsByLabels(labels, null).isEmpty()) {
            return k8sService.getPodsByLabels(labels, null).get(0).getMetadata().getName();
        } else {
            return "";
        }
    }

    @Override
    protected void startDockbeat(DockerContainer dockerContainer) throws Exception {
        try {
            k8sService.createDaemonSetFromContainerInfo(dockerContainer);
        } catch (Exception e) {
            new Exception("Exception on start Dockbeat", e);
            throw e;
        }
    }

    @Override
    public void disableMetricMonitoring(Execution execution, boolean force) throws Exception {
        String containerName = generateContainerName(ContainerPrefix.DOCK_BEAT, execution);
        k8sService.deleteDaemonByName(containerName, null);
    }

    @Override
    public void deployAndRunTJobExecution(Execution execution) throws Exception {
        List<ReportTestSuite> testResults = new ArrayList<ReportTestSuite>();
        TJobExecution tJobExec = execution.getTJobExec();
        JobResult result = null;
        String resultMsg = "";
        try {
            // Create Container Object
            DockerContainer testContainer = createContainer(execution, ContainerType.TJOB);

            resultMsg = "Executing Test";
            execution.updateTJobExecutionStatus(TJobExecution.ResultEnum.EXECUTING_TEST, resultMsg);
            execution.setStatusMsg(resultMsg);

            // Create and start container
            result = k8sService.deployJob(testContainer);
            execution.setExitCode(result.getResult());

        } catch (TJobStoppedException | InterruptedException e) {
            throw new TJobStoppedException("Error on create and start TJob container: Stopped", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error running the TJob", e);
        } finally {
            if (result != null && k8sService.existJobByName(result.getJobName())) {
                k8sService.deleteJob(result.getJobName());
            }
        }

    }

    @Override
    public void deploySut(Execution execution) throws Exception {
        PodInfo podInfo = null;
        try {
            // Create Container Object
            DockerContainer sutContainer = createContainer(execution, ContainerType.SUT);

            String resultMsg = "Starting dockerized SuT";
            execution.updateTJobExecutionStatus(TJobExecution.ResultEnum.EXECUTING_SUT, resultMsg);
            logger.info(resultMsg + " " + execution.getExecutionId());

            // Create and start container
            podInfo = k8sService.deployPod(sutContainer);
            execution.getSutExec()
                    .setUrl(execution.getSut().getSutUrlByGivenIp(podInfo.getPodIp()));
            logger.info("Waiting for sut {} deployed in {}", execution.getSut().getName(),
                    execution.getSutExec().getUrl());
            checkIfServiceIsUp(execution.getSutExec().getUrl(), 480000);
            sutsByExecution.put(execution.getExecutionId().toString(), podInfo.getPodName());

            String sutName = generateContainerName(ContainerPrefix.SUT, execution);
            this.insertCreatedContainer(podInfo.getPodName(), sutName);
            execution.getSutExec().setIp(podInfo.getPodIp());
            logger.debug("Sut Ip stored in the sutExec {}", execution.getSutExec().getIp());
            logger.debug(" Url stored in the sutExec {}", execution.getSutExec().getUrl());

        } catch (Exception e) {
            if (podInfo.getPodName() != null) {
                k8sService.deletePod(podInfo.getPodName());
            }
            throw new Exception("Error on create and start Sut container", e);
        }

    }

    private boolean checkIfServiceIsUp(String url, long timeout) throws TimeoutException {
        logger.debug("Checking connection to {}", url);

        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout;

        boolean created = false;

        while (!created && System.currentTimeMillis() < endTime) {
            try {
                ResponseEntity<String> response = prepareRestTemplate().exchange(url,
                        HttpMethod.GET, null, String.class);
                int responseCode = response.getStatusCode().value();
                created = ((responseCode >= 200 && responseCode <= 299)
                        || (responseCode >= 400 && responseCode <= 415));
                if (created) {
                    return created;
                } else {
                    UtilTools.sleep(2);
                }
            } catch (Exception e) {
                logger.trace("Service is not up yet at {}: {}", url, e.getMessage());
                UtilTools.sleep(2);
            }
        }

        throw new TimeoutException("Timeout connecting to " + url);

    }

    private RestTemplate prepareRestTemplate()
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("https", sslsf)
                .register("http", new PlainConnectionSocketFactory()).build();

        BasicHttpClientConnectionManager connectionManager = new BasicHttpClientConnectionManager(
                socketFactoryRegistry);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf)
                .setConnectionManager(connectionManager).build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
                httpClient);
        return new RestTemplate(requestFactory);
    }

    @Override
    public void undeploySut(Execution execution, boolean force) throws Exception {
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
                    String sutContainerName = generateContainerName(ContainerPrefix.SUT, execution);
                    k8sService.deleteServiceAssociatedWithAPOD(sutContainerName, null);

                    endContainer(sutContainerName);
                } else {
                    // endComposedSutExec(execution);
                }
                updateSutExecDeployStatus(execution, DeployStatusEnum.UNDEPLOYED);
            } finally {

            }
        }
    }

    @Override
    public void undeployTJob(Execution execution, boolean force) throws Exception {
        DockerContainer testContainer = createContainer(execution, ContainerType.TJOB);
        k8sService.deleteJob(testContainer.getContainerName().get().replace("_", "-"));
    }

    @Override
    public String getEtmHost() throws Exception {
        return utilsService.getEtPublicHostValue();
    }

    @Override
    public String getETPublicHost() {
        return utilsService.getEtPublicHostValue();
    }

    @Override
    public void removeBindedPorts(SupportServiceInstance serviceInstance) {
        for (String serviceName : serviceInstance.getPortBindingContainers()) {
            logger.debug("Deleteing the k8s service that bind the port of the service: {}",
                    serviceName);
            removeBindedPort(serviceName, serviceInstance.getInstanceId());
        }
    }

    public void removeBindedPort(String serviceName, String namespace) {
        k8sService.deleteServiceAssociatedWithAPOD(serviceName, namespace);
    }

    @Override
    public void removeBindedPort(String bindedPortId) {
        removeBindedPort(bindedPortId, null);
    }

    @Override
    public void removeWorkEnvironment(String namespace) {
        k8sService.deleteNamespace(namespace);
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
    public VersionInfo getVersionInfoFromContainer(String version, String imageName)
            throws Exception {
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
    public void setCoreServiceInfoFromContainer(String version, String imageName,
            CoreServiceInfo coreServiceInfo) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public String getAllContainerLogs(String containerName, boolean withFollow) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSomeContainerLogs(String containerName, int amount, boolean withFollow)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContainerLogsFrom(String containerId, int from, boolean withFollow)
            throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getFilesContentFromContainer(String testContainer, String filePath,
            List<String> filterExtensions) throws Exception {
        return k8sService.readFilesFromContainer(testContainer, filePath, filterExtensions);
    }

    @Override
    public Integer copyFilesFomContainer(String container, String originPath, String targetPath) {
        logger.info("Copy files in {}, from {} to {}.", container, originPath, targetPath);
        Integer result = 1;
        result = k8sService.copyFileFromContainer(container, originPath, targetPath, null);
        return result;
    }

    @Override
    protected void endContainer(String containerName) throws Exception {
        k8sService.deletePod(containerName);
        createdContainers.remove(containerName);
    }

    public void insertCreatedContainer(String containerId, String containerName) {
        createdContainers.put(containerId, containerName);
    }

    @Override
    protected void endContainer(String containerName, int timeout) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public ServiceBindedPort getBindedPort(String serviceName, String containerSufix,
            String bindedPort, String port, String namespace) throws Exception {
        logger.debug("Binding port for the service {}", serviceName);
        if (namespace != null && !namespace.isEmpty()) {
            k8sService.createNamespace(namespace);
        }
        ServiceInfo serviceInfo = k8sService.createService(serviceName, serviceName,
                bindedPort != null ? Integer.valueOf(bindedPort) : null, Integer.valueOf(port),
                "http", namespace, K8sService.LABEL_COMPONENT);
        logger.debug("Getting binding port for the service {}", serviceName);

        ServiceBindedPort bindedPortObj = new ServiceBindedPort(port, serviceInfo.getServicePort(),
                serviceInfo.getServiceName());
        logger.debug("Port for service {} binded against: {}", serviceName,
                serviceInfo.getServicePort());
        return bindedPortObj;

    }

    @Override
    public String getBindedServiceIp(SupportServiceInstance serviceInstance, String port) {
        logger.debug("Getting binded service ip for the service: {}",
                serviceInstance.getServiceName());
        return k8sService.getServiceIp(serviceInstance.getEndpointName().toLowerCase(), port,
                serviceInstance.getInstanceId());
    }

    @Override
    public String getTSSInstanceContainerName(String... params) {
        logger.debug("Check if the TSS {} exist as a pod in the namespace {}", params[1],
                params[0]);
        return params[1];
    }

    @Override
    public String getPublicServiceIp(String serviceName, String port, String namespace) {
        logger.debug("Getting binded service ip for the service: {}", serviceName);
        return k8sService.getServiceIp(serviceName.toLowerCase(), port, namespace);
    }

    @Override
    public boolean isContainerByServiceName(String serviceName,
            io.elastest.epm.client.json.DockerContainerInfo.DockerContainer container) {
        for (Entry<String, String> label : container.getLabels().getAllLabels().entrySet()) {
            if (label.getValue().equals(serviceName) || label.getValue().contains(serviceName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected String getCommandsByPlatform() {
        return "curl -X POST http://etm:8091/api/tjob/exec/pod/$HOSTNAME/results";
    }

    @Override
    public void manageTSSInstanceIfNecessary(SupportServiceInstance instance, boolean isCreation) {
        if (!utilsService.isElastestMini()
                && "EUS".equals(instance.getServiceName().toUpperCase())) {
            if (isCreation) {
                try {
                    k8sService.createClusterRoleBindingAdmin(instance.getInstanceId(),
                            instance.getInstanceId());
                } catch (Exception e) {
                    logger.error("Error on create cluster role binding for EUS instance with id {}",
                            instance.getInstanceId());
                    e.printStackTrace();
                }
            } else {
                try {
                    k8sService.deleteClusterRoleBindingAdmin(instance.getInstanceId(),
                            instance.getInstanceId());
                } catch (Exception e) {
                    logger.error("Error on delete cluster role binding for EUS instance with id {}",
                            instance.getInstanceId());
                    e.printStackTrace();
                }
            }
        }
    }

}
