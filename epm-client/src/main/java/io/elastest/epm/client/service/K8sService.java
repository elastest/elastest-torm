package io.elastest.epm.client.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.elastest.epm.client.DockerContainer;
import io.elastest.epm.client.json.DockerProject;
import io.elastest.epm.client.utils.UtilTools;
import io.fabric8.kubernetes.api.model.Capabilities;
import io.fabric8.kubernetes.api.model.CapabilitiesBuilder;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.DoneableService;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.HostPathVolumeSource;
import io.fabric8.kubernetes.api.model.HostPathVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.LabelSelector;
import io.fabric8.kubernetes.api.model.LabelSelectorBuilder;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.SecurityContextBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import okhttp3.Response;

@org.springframework.stereotype.Service
public class K8sService {
    private static final Logger logger = LoggerFactory
            .getLogger(K8sService.class);

    @Value("${et.enable.cloud.mode}")
    public boolean enableCloudMode;

    private static final String DEFAULT_NAMESPACE = "default";
    public static final String LABEL_JOB_NAME = "job-name";
    public static final String LABEL_POD_NAME = "pod-name";
    public static final String LABEL_APP_NAME = "app";
    private static final String SUT_PORT_NAME = "sut-port";
    private static final String BINDING_PORT_SUFIX = "-host-port";
    private static final String CLUSTER_DOMAIN = "svc.cluster.local";
    public static final String LABEL_TSS_NAME = "io.elastest.tjob.tss.id";
    public static final String LABEL_UNIQUE_PLUGIN_NAME = "io.elastest.service";

    @Value("${et.data.in.host}")
    public String etDataInHost;

    @Value("${et.tools.resource.folder.path}")
    public String etToolsResourceFolderPath;

    @Value("${et.shared.folder}")
    public String etSharedFolder;

    public HostPathVolumeSource etToolsVolume;
    public static final String etToolsInternalPath = "et_tools";
    public KubernetesClient client;
    private FilesService filesService;
    private Map<String, List<String>> servicesAssociatedWithAPod;

    public K8sService(FilesService filesService) {
        this.filesService = filesService;
        this.servicesAssociatedWithAPod = new ConcurrentHashMap<>();
    }

    public Map<String, List<String>> getServicesAssociatedWithAPod() {
        return servicesAssociatedWithAPod;
    }

    public void setServicesAssociatedWithAPod(
            Map<String, List<String>> servicesAssociatedWithAPod) {
        this.servicesAssociatedWithAPod = servicesAssociatedWithAPod;
    }

    private static final Map<String, DockerProject> projects = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        if (enableCloudMode) {
            logger.debug("Default K8s");
            client = new DefaultKubernetesClient();
        }
    }

    @PreDestroy
    public void finish() {
        client.close();
    }

    public enum PodsStatusEnum {
        PENDING("Pending"), RUNNING("Running"), SUCCEEDED("Succeeded"),
        FAILED("Failed"), UNKNOWN("Unknown");

        private String value;

        PodsStatusEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static PodsStatusEnum fromValue(String text) {
            for (PodsStatusEnum b : PodsStatusEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

    }

    public enum ServicesType {
        NODE_PORT("NodePort");

        private String value;

        ServicesType(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ServicesType fromValue(String text) {
            for (ServicesType b : ServicesType.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

    }

    public enum ServiceProtocolEnum {
        TCP("TCP"), UDP("UDP"), SCTP("SCTP");

        private String value;

        ServiceProtocolEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ServiceProtocolEnum fromValue(String text) {
            for (ServiceProtocolEnum b : ServiceProtocolEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

    }

    public JobResult deployJob(DockerContainer container) throws Exception {
        return deployJob(container, DEFAULT_NAMESPACE);
    }

    public JobResult deployJob(DockerContainer container, String namespace)
            throws Exception {
        final JobResult result = new JobResult();
        container.getCmd().get().forEach((command) -> {
            logger.debug("Commands to execute: {}", command);
        });
        try {
            logger.info("Container name: {}",
                    container.getContainerName().get());
            logger.info(String.join(",", container.getCmd().get()));

            Map<String, String> k8sJobLabels = container.getLabels().get();
            String containerNameWithoutUnderscore = container.getContainerName()
                    .get().replace("_", "-");

            k8sJobLabels.put(LABEL_JOB_NAME, containerNameWithoutUnderscore);

            String etToolsVolumeName = "et-tools";
            final Job job = new JobBuilder(Boolean.FALSE)
                    .withApiVersion("batch/v1").withNewMetadata()
                    .withName(containerNameWithoutUnderscore)
                    .withLabels(k8sJobLabels).endMetadata().withNewSpec()
                    .withNewTemplate().withNewSpec().addNewContainer()
                    .withName(containerNameWithoutUnderscore)
                    .withImage(container.getImageId())
                    .withArgs(container.getCmd().get())
                    .withEnv(getEnvVarListFromStringList(
                            container.getEnvs().get()))
                    .endContainer().withRestartPolicy("Never").endSpec()
                    .endTemplate().endSpec().build();

            logger.info("Creating job: {}.",
                    job.getMetadata().getLabels().get(LABEL_JOB_NAME));
            client.batch().jobs().inNamespace(namespace).create(job);

            result.setResult(1);
            result.setJobName(job.getMetadata().getName());
            result.setPodName("");

            final CountDownLatch watchLatch = new CountDownLatch(1);
            try (final Watch ignored = client.pods().inNamespace(namespace)
                    .withLabel(LABEL_JOB_NAME, job.getMetadata().getName())
                    .watch(new Watcher<Pod>() {
                        @Override
                        public void eventReceived(final Action action,
                                Pod pod) {
                            job.getMetadata().getLabels()
                                    .forEach((label, value) -> {
                                        logger.debug("Label: {}-{}", label,
                                                value);
                                    });
                            logger.debug("Job {} receives an event",
                                    job.getMetadata().getLabels()
                                            .get(LABEL_JOB_NAME));
                            logger.debug("Event received: {}",
                                    pod.getStatus().getPhase());
                            logger.debug("Action: {}", action.toString());

                            logger.debug("Enum Pending: {}",
                                    PodsStatusEnum.PENDING.toString());

                            if (result.getPodName().isEmpty()) {
                                result.setPodName(pod.getMetadata().getName());
                            }

                            if (!(pod.getStatus().getPhase()
                                    .equals(PodsStatusEnum.PENDING.toString()))
                                    && !(pod.getStatus().getPhase()
                                            .equals(PodsStatusEnum.RUNNING
                                                    .toString()))) {
                                logger.info("Pod executed with result: {}",
                                        pod.getStatus().getPhase());
                                result.setResult(pod.getStatus().getPhase()
                                        .equals(PodsStatusEnum.SUCCEEDED
                                                .toString()) ? 0 : 1);
                                logger.info("Job {} is completed!",
                                        pod.getMetadata().getName());
                                watchLatch.countDown();
                            }
                        }

                        @Override
                        public void onClose(final KubernetesClientException e) {

                        }
                    })) {
                watchLatch.await();
            } catch (final KubernetesClientException e) {
                logger.error("Could not watch pod", e);

            }
        } catch (final KubernetesClientException e) {
            String msg = "Unable to create job";
            logger.error(msg, e);
            throw new Exception(msg, e);
        }

        return result;
    }

    public PodInfo deployPod(DockerContainer container) throws Exception {
        return deployPod(container, DEFAULT_NAMESPACE);
    }

    public PodInfo deployPod(DockerContainer container, String namespace)
            throws Exception {
        PodInfo podInfo = new PodInfo();
        Pod pod = null;

        try {
            logger.info("Container name: {}",
                    container.getContainerName().get());
            if (container.getCmd().isPresent()) {
                logger.info(String.join(",", container.getCmd().get()));
            }

            Map<String, String> k8sPobLabels = container.getLabels().get();
            k8sPobLabels.put(LABEL_POD_NAME,
                    container.getContainerName().get());

            String containerNameWithoutUnderscore = container.getContainerName()
                    .get().replace("_", "-");

            // Create Container
            ContainerBuilder containerBuilder = new ContainerBuilder();
            containerBuilder.withName(containerNameWithoutUnderscore)
                    .withImage(container.getImageId())
                    .withEnv(getEnvVarListFromStringList(
                            container.getEnvs().get()));

            // Add ports
            if (container.getExposedPorts().isPresent()
                    && !container.getExposedPorts().get().isEmpty()) {
                List<ContainerPort> ports = new ArrayList<>();
                container.getExposedPorts().get().forEach(port -> {
                    ContainerPort containerPort = new ContainerPort();
                    containerPort.setContainerPort(new Integer(port));
                    ports.add(containerPort);
                });
                containerBuilder.withPorts(ports);
            }

            if (container.getCapAdd().isPresent()
                    && !container.getCapAdd().get().isEmpty()) {
                SecurityContextBuilder securityContextBuilder = new SecurityContextBuilder();
                List<String> stringCapabilities = new ArrayList<>();
                container.getCapAdd().get().forEach(cap -> {
                    stringCapabilities.add(cap);
                });
                Capabilities capabilities = new CapabilitiesBuilder()
                        .withAdd(stringCapabilities).build();

                securityContextBuilder.withCapabilities(capabilities);
                containerBuilder
                        .withSecurityContext(securityContextBuilder.build());
            }

            PodBuilder podBuilder = new PodBuilder();
            podBuilder.withNewMetadata()
                    .withName(containerNameWithoutUnderscore).endMetadata()
                    .withNewSpec().addNewContainerLike(containerBuilder.build())
                    .endContainer().endSpec();

            // Set Labels if there are
            if (container.getLabels().isPresent()
                    && container.getLabels().get().size() > 0) {
                podBuilder.buildMetadata()
                        .setLabels(container.getLabels().get());
            }

            if (podBuilder.buildMetadata().getLabels() != null) {
                podBuilder.buildMetadata().getLabels().put(LABEL_POD_NAME,
                        container.getContainerName().get());
            } else {
                podBuilder.buildMetadata().setLabels(k8sPobLabels);
            }

            podBuilder.buildSpec().getContainers().get(0);
            pod = client.pods().inNamespace(namespace)
                    .createOrReplace(podBuilder.build());

            while (!isReady(containerNameWithoutUnderscore, null)) {
                UtilTools.sleep(1);
            }
            pod = client.pods().inNamespace(DEFAULT_NAMESPACE)
                    .withName(containerNameWithoutUnderscore).get();
            logger.debug("Sut Pod ip: {}", pod.getStatus().getPodIP());

        } catch (final KubernetesClientException e) {
            logger.error("Unable to create job", e);
            throw e;
        }
        podInfo.setPodIp(pod.getStatus().getPodIP());
        podInfo.setPodName(pod.getMetadata().getName());

        return podInfo;
    }

    public List<PodInfo> deployResourcesFromProject(String projectName,
            String namespace) throws Exception {
        PodInfo podInfo = new PodInfo();
        List<PodInfo> podsInfoList = new ArrayList<>();
        DockerProject project = projects.get(projectName);
        logger.debug("Project to deploy: {}", projectName);
        logger.debug("Resources as string: {}", project.getYml());
        namespace = namespace == null || namespace.isEmpty() ? DEFAULT_NAMESPACE
                : namespace;

        try (InputStream is = IOUtils.toInputStream(project.getYml(),
                CharEncoding.UTF_8)) {
            List<HasMetadata> resourcesMetadata = client.load(is)
                    .inNamespace(namespace).get();

            project.getEnv().put("ETM_K8S_API_TOKEN", getServiceAccountToken());
            project.getEnv().put("ETM_K8S_API_URL",
                    client.getConfiguration().getMasterUrl());
            logger.debug("Add these environment variables:");
            project.getEnv().forEach((key, value) -> {
                logger.debug("Env var {} with value {}", key, value);
            });

            logger.debug("Number of deployments loaded: {}",
                    resourcesMetadata.size());

            for (HasMetadata metadata : resourcesMetadata) {
                switch (metadata.getKind()) {
                case "Deployment":
                    String deploymentName = ((Deployment) metadata)
                            .getMetadata().getName();
                    logger.debug("Editing and deploying service {}:",
                            deploymentName);
                    DeploymentBuilder dpB = new DeploymentBuilder(
                            ((Deployment) metadata));
                    Deployment dp = dpB.editSpec().editTemplate().editSpec()
                            .editContainer(0)
                            .addAllToEnv(getEnvVarListFromMap(project.getEnv()))
                            .endContainer().endSpec().endTemplate().endSpec()
                            .build();
                    logger.debug("Deployment {} content: {}", deploymentName,
                            dp.toString());

                    client.apps().deployments().inNamespace(namespace)
                            .createOrReplace(dp);
                    client.apps().deployments().inNamespace(namespace)
                            .withName(deploymentName)
                            .waitUntilReady(5, TimeUnit.MINUTES);

                    break;

                case "PersistentVolumeClaim":
                    PersistentVolumeClaimBuilder pVCBuilder = new PersistentVolumeClaimBuilder(
                            (PersistentVolumeClaim) metadata);
                    if (client.persistentVolumeClaims().inNamespace(namespace)
                            .withName("elastest-" + projectName + "-data-claim")
                            .get() == null) {
                        client.persistentVolumeClaims().inNamespace(namespace)
                                .create(pVCBuilder.build());
                    }
                    break;

                case "Service":
                    logger.debug("Creating k8s service \"{}\"",
                            ((Service) metadata).getMetadata().getName());
                    ServiceBuilder serviceBuilder = new ServiceBuilder(
                            (Service) metadata);
                    if (client.services().inNamespace(namespace)
                            .withName(projectName).get() == null) {
                        client.services().inNamespace(namespace)
                                .create(serviceBuilder.build());
                    }
                default:
                    break;
                }
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error loading deployment from a yaml string");
            e.printStackTrace();
            throw e;
        }

        return podsInfoList;
    }

    public boolean deleteResources(String projectName) {
        DockerProject project = projects.get(projectName);
        logger.debug("Remove resources described in this yml: {}",
                project.getYml());
        if (checkIfExistNamespace(projectName)) {
            return deleteNamespace(projectName);
        } else {
            return deleteResourcesFromYmlString(project.getYml(), null);
        }
    }

    public boolean deleteResourcesFromYmlString(String manifest,
            String namespace) {
        boolean deleted = false;
        try (InputStream is = IOUtils.toInputStream(manifest,
                CharEncoding.UTF_8)) {
            List<HasMetadata> resourcesMetadata = client.load(is).get();
            resourcesMetadata.forEach(metadata -> {
                logger.debug("Resource '{}' of kind '{}' to remove",
                        metadata.getMetadata().getName(), metadata.getKind());
                switch (metadata.getKind()) {
                case "Deployment":
                    deleteDeployment((Deployment) metadata, namespace);
//                    deleteReplicaSet((Deployment) metadata, namespace);
                    break;
                case "PersistentVolumeClain":
                    deletePVC((PersistentVolumeClaim) metadata, namespace);
                    break;
                case "Service":
                    deleteService(((Service) metadata).getMetadata().getName(),
                            namespace);
                    break;
                }
            });

            deleted = true;
        } catch (Exception e) {
            logger.error("Error deleting resources from a yaml string");
            e.printStackTrace();
        }
        return deleted;
    }

    public boolean deleteDeployment(Deployment deployment, String namespace) {
        logger.debug("Delete deployment \"{}\"",
                deployment.getMetadata().getName());
        namespace = namespace != null && !namespace.isEmpty() ? namespace
                : DEFAULT_NAMESPACE;
        if (client.apps().deployments().inNamespace(namespace)
                .withName(deployment.getMetadata().getName()).get() != null) {
            return client.apps().deployments().inNamespace(namespace)
                    .withName(deployment.getMetadata().getName())
                    .cascading(true).delete();
        } else {
            return true;
        }
    }

    public boolean deleteReplicaSet(Deployment deployment, String namespace) {
        logger.debug("Delete replica set \"{}\"",
                deployment.getMetadata().getName());

        client.apps().replicaSets().inNamespace(namespace)
                .withLabels(deployment.getMetadata().getLabels()).list()
                .getItems().forEach(rs -> {
                    client.apps().replicaSets()
                            .inNamespace(
                                    namespace != null && !namespace.isEmpty()
                                            ? namespace
                                            : DEFAULT_NAMESPACE)
                            .withName(rs.getMetadata().getName()).delete();
                });
        return true;
    }

    public boolean deletePVC(PersistentVolumeClaim pvc, String namespace) {
        logger.debug("Delete PVC \"{}\"", pvc.getMetadata().getName());
        namespace = namespace != null && !namespace.isEmpty() ? namespace
                : DEFAULT_NAMESPACE;
        if (client.persistentVolumeClaims().inNamespace(namespace)
                .withName(pvc.getMetadata().getName()).get() != null) {
            return client.persistentVolumeClaims().inNamespace(namespace)
                    .withName(pvc.getMetadata().getName()).delete();
        } else {
            return true;
        }
    }

    public void waitForResourcesToBeDeleted(HasMetadata deployment,
            String namespace) {
        while (client.pods().inNamespace(namespace).list().getItems()
                .size() > 0) {
            logger.debug("Waiting");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public DockerProject createk8sProject(String projectName,
            String serviceDescriptor, Map<String, String> envs,
            List<String> extraHosts) {
        logger.debug(
                "Store new project \"{}\" with this deployment manifest -> {}",
                projectName, serviceDescriptor);
        DockerProject project = new DockerProject(projectName,
                serviceDescriptor);
        if (envs != null) {
            project.getEnv().putAll(envs);
        }

        if (extraHosts != null) {
            project.getExtraHosts().addAll(extraHosts);
        }

        projects.put(projectName, project);
        return project;
    }

    public DockerProject createk8sProject(String projectName,
            String serviceDescriptor, Map<String, String> envs) {
        return createk8sProject(projectName, serviceDescriptor, envs, null);
    }

    public String createServiceSUT(String serviceName, Integer port,
            String protocol) {
        Service service = new ServiceBuilder().withNewMetadata()
                .withName(serviceName).endMetadata().withNewSpec()
                .withSelector(
                        Collections.singletonMap(LABEL_APP_NAME, serviceName))
                .addNewPort().withName(SUT_PORT_NAME).withProtocol(protocol)
                .withPort(port).endPort().withType("NodePort").endSpec()
                .build();

        service = client.services().inNamespace(client.getNamespace())
                .create(service);
        String serviceURL = client.services().inNamespace(client.getNamespace())
                .withName(service.getMetadata().getName())
                .getURL(SUT_PORT_NAME);

        if (client.nodes().list().getItems().size() == 1) {
            try {
                for (NodeAddress address : client.nodes().list().getItems()
                        .get(0).getStatus().getAddresses()) {
                    if (address.getType().equals("ExternalIP")) {
                        URL nodeURL;
                        nodeURL = new URL(serviceURL);
                        URL newServiceURL = new URL(nodeURL.getProtocol(),
                                address.getAddress(), nodeURL.getPort(), "");
                        serviceURL = newServiceURL.toString();
                    }
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return serviceURL;
    }

    public ServiceInfo createService(String serviceName, String podName,
            Integer port, Integer targetPort, String protocol, String namespace,
            String selector) throws MalformedURLException {
        protocol = protocol.contains("http")
                ? ServiceProtocolEnum.TCP.toString()
                : protocol;
        serviceName = serviceName.toLowerCase();
        String k8sServiceName = serviceName;
        String hostPortName = (podName + "-" + targetPort + BINDING_PORT_SUFIX)
                .replace("_", "-");
        Service service = null;
        if (!checkIfServiceExistsInNamespace(k8sServiceName, namespace)) {
            logger.debug(
                    "Creating a new service for the service {} with port {}",
                    serviceName, targetPort);

            ServicePortBuilder servicePortBuilder = new ServicePortBuilder()
                    .withName(hostPortName).withProtocol(protocol)
                    .withPort(targetPort);
            if (port != null) {
                servicePortBuilder.withNodePort(port);
            }
            ServicePort servicePort = servicePortBuilder.build();

            service = new ServiceBuilder().withNewMetadata()
                    .withName(serviceName).endMetadata().withNewSpec()
                    .withSelector(Collections.singletonMap(selector, podName))
                    .addNewPortLike(servicePort).endPort()
                    .withType(ServicesType.NODE_PORT.toString()).endSpec()
                    .build();

            service = client.services()
                    .inNamespace(
                            namespace == null ? DEFAULT_NAMESPACE : namespace)
                    .create(service);
        } else {
            service = client.services()
                    .inNamespace(
                            namespace == null ? DEFAULT_NAMESPACE : namespace)
                    .withName(k8sServiceName).get();
        }

        if (servicesAssociatedWithAPod.get(podName) != null) {
            servicesAssociatedWithAPod.get(podName).add(k8sServiceName);
        } else {
            List<String> associatedServices = new ArrayList<>();
            associatedServices.add(k8sServiceName);
            servicesAssociatedWithAPod.put(podName, associatedServices);
        }

        String serviceURL = client.services()
                .inNamespace(namespace == null ? DEFAULT_NAMESPACE : namespace)
                .withName(service.getMetadata().getName()).getURL(hostPortName);

        logger.debug("Nodes in the cluster: {}",
                client.nodes().list().getItems().size());
        for (NodeAddress address : client.nodes().list().getItems().get(0)
                .getStatus().getAddresses()) {
            logger.debug("Check ip for the cluster 1. Ip type: {}",
                    address.getType());
            if (address.getType().equals("ExternalIP")) {
                logger.debug("Replace old ip with the node external ip: {}",
                        address.getAddress());
                URL nodeURL = new URL(serviceURL.replaceAll("tcp", "http"));
                try {
                    URL newServiceURL = new URL(nodeURL.getProtocol(),
                            address.getAddress(), nodeURL.getPort(), "");
                    serviceURL = newServiceURL.toString();
                } catch (Exception e) {
                    logger.error("Error message: {}", e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        logger.debug("Service url: {}", serviceURL);
        String[] urlParts = serviceURL.split(":");
        return new ServiceInfo(urlParts[2], serviceName, // service.getMetadata().getName(),
                new URL(serviceURL.replaceAll("tcp", "http")));
    }

    private boolean checkIfServiceExistsInNamespace(String name,
            String namespace) {
        logger.debug("Checking if exist the service \"{}\"", name);
        boolean result = false;
        result = client.services()
                .inNamespace(
                        namespace != null && !namespace.isEmpty() ? namespace
                                : DEFAULT_NAMESPACE)
                .withName(name).get() != null ? true : result;
        return result;
    }

    public String getServiceIp(String serviceName, String port,
            String namespace) {

        String serviceIP = "";

        logger.debug("Nodes in the cluster: {}",
                client.nodes().list().getItems().size());
        for (NodeAddress address : client.nodes().list().getItems().get(0)
                .getStatus().getAddresses()) {
            logger.debug("Check ip for the cluster 1. Ip type: {}",
                    address.getType());
            if (address.getType().equals("ExternalIP")) {
                serviceIP = address.getAddress();
            }
        }

        return serviceIP;
    }

    public String getServiceIp(String serviceName, String namespace) {

        logger.debug("Getting the service ip for the service {}", serviceName);

        String serviceIP = "";

        logger.debug("Nodes in the cluster: {}",
                client.nodes().list().getItems().size());
        for (NodeAddress address : client.nodes().list().getItems().get(0)
                .getStatus().getAddresses()) {
            logger.debug("Check ip for the cluster 1. Ip type: {}",
                    address.getType());
            if (address.getType().equals("ExternalIP")) {
                serviceIP = address.getAddress();
            }
        }

        return serviceIP;
    }

    public void createNamespace(String name) {
        logger.debug("Checking if the namespace {} exists", name);
        if (client.namespaces().withName(name).get() == null) {
            logger.debug("Creating namespace -> {}", name);
            Namespace ns = new NamespaceBuilder().withNewMetadata()
                    .withName(name).addToLabels("io.elastest.name", name)
                    .endMetadata().build();
            client.namespaces().create(ns);
        } else {
            logger.info("Namespace -> {} already exists", name);
        }
    }

    public boolean deleteNamespace(String name) {
        if (!name.equals(DEFAULT_NAMESPACE)
                && client.namespaces().withName(name).get() != null) {
            logger.debug("Deleting the namespace \"{}\" and all its resources.",
                    name);
            return client.namespaces().withName(name).delete();
        } else {
            logger.info("Namespace {} can not be deleted", name);
            return false;
        }
    }

    public boolean checkIfExistNamespace(String name) {
        if (name != null && !name.equals(DEFAULT_NAMESPACE)
                && client.namespaces().withName(name).get() != null) {
            logger.debug("There is a namespace with the name \"{}\"", name);
            return true;
        } else {
            logger.debug("There isn't a namespace with the name \"{}\"", name);
            return false;
        }
    }

    public void deleteServiceAssociatedWithAPOD(String serviceName,
            String namespace) {
        serviceName = serviceName.toLowerCase();
        logger.debug("Removing kubernetes services associated with {}",
                serviceName);

        servicesAssociatedWithAPod.get(serviceName).forEach(service -> {
            deleteService(service, namespace);
        });
    }

    public void deleteService(String name, String namespace) {
        logger.debug("Remove service {}", name);
        client.services()
                .inNamespace(
                        (namespace != null && !namespace.isEmpty()) ? namespace
                                : DEFAULT_NAMESPACE)
                .withName(name).cascading(true).delete();

    }

    public void deleteJob(String jobName) {
        logger.info("Deleting job {}.", jobName);
        client.batch().jobs().inNamespace(DEFAULT_NAMESPACE).withName(jobName)
                .delete();
        deletePods(client.pods().inNamespace(DEFAULT_NAMESPACE)
                .withLabel(LABEL_JOB_NAME, jobName).list());
    }

    public void deletePods(PodList podList) {
        podList.getItems().forEach(pod -> {
            deletePod(pod.getMetadata().getName());
        });
    }

    public void deletePod(String podName) {
        logger.info("Deleting pod {}.", podName);
        client.pods().inNamespace(DEFAULT_NAMESPACE)
                .withName(podName.replace("_", "-")).delete();
    }

    public boolean isReady(String podName, String namespace) {
        Pod pod = client.pods()
                .inNamespace(
                        namespace != null && !namespace.isEmpty() ? namespace
                                : DEFAULT_NAMESPACE)
                .withName(podName).get();
        if (pod == null) {
            return false;
        } else {
            return pod.getStatus().getConditions().stream()
                    .filter(condition -> condition.getType().equals("Ready"))
                    .map(condition -> condition.getStatus().equals("True"))
                    .findFirst().orElse(false);
        }
    }

    private List<EnvVar> getEnvVarListFromStringList(
            List<String> envVarsAsStringList) {
        List<EnvVar> envVars = new ArrayList<EnvVar>();
        envVarsAsStringList.forEach(envVarAsString -> {
            String[] keyValuePar = envVarAsString.split("=");
            EnvVar envVar = new EnvVar(keyValuePar[0], keyValuePar[1], null);
            envVars.add(envVar);
        });
        return envVars;
    }

    private List<EnvVar> getEnvVarListFromMap(Map<String, String> envs) {
        List<EnvVar> envVarsList = new ArrayList<>();
        if (envs != null) {
            for (Map.Entry<String, String> envEntryMap : envs.entrySet()) {
                EnvVar envVar = new EnvVar(envEntryMap.getKey(),
                        envEntryMap.getValue(), null);
                envVarsList.add(envVar);
            }
        }

        return envVarsList;
    }

    public List<String> readFilesFromContainer(String podName, String filePath,
            List<String> filterExtensions) {
        List<String> filesList = new ArrayList<>();
        logger.info("Reading files from k8s pod {} in this path {}", podName,
                filePath);

        if (filePath != null && !filePath.isEmpty()) {
            try (InputStream is = client.pods().inNamespace(DEFAULT_NAMESPACE)
                    .withName(podName).dir(filePath).read()) {
                TarArchiveInputStream tarInput = new TarArchiveInputStream(is);
                try {
                    List<InputStream> filesIS = filesService
                            .getFilesFromTarInputStreamAsInputStreamList(
                                    tarInput, filePath, filterExtensions);
                    if (filesIS != null) {
                        for (InputStream fileIS : filesIS) {
                            try {
                                filesList.add(IOUtils.toString(fileIS,
                                        StandardCharsets.UTF_8));
                                fileIS.close();
                            } catch (IOException e) {
                                logger.error(
                                        "Error on transform InputStream file to String.");
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error retrieving files from container.");
                }

            } catch (Exception e) {
                logger.error("Error reading files");
                e.printStackTrace();
            }
        }
        return filesList;
    }

    public Integer copyFileFromContainer(String podName, String originPath,
            String targetPath, String namespace) {
        logger.info(
                "Copying files in the folder {} in the pod {}, in this local path {}",
                originPath, podName, targetPath);
        Integer result = 0;
        result = client.pods()
                .inNamespace(
                        namespace != null && !namespace.isEmpty() ? namespace
                                : DEFAULT_NAMESPACE)
                .withName(podName).dir(originPath).copy(Paths.get(targetPath))
                        ? result
                        : 1;
        if (result != 1) {
            logger.debug("*** File copied ***");
        } else {
            logger.debug("*** File not copied ***");
        }

        return result;
    }

    public void execCommand(Pod pod, String container, Boolean awaitCompletion,
            String... command) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final CountDownLatch latch = new CountDownLatch(1);
        try (ExecWatch execWatch = client.pods()
                .inNamespace(pod.getMetadata().getNamespace())
                .withName(pod.getMetadata().getName()).readingInput(null)
                .writingOutput(baos).usingListener(new ExecListener() {

                    @Override
                    public void onClose(int i, String s) {
                        logger.debug("Command finalized without errors.");
                        latch.countDown();
                    }

                    @Override
                    public void onOpen(Response response) {
                        logger.info("Running command on pod {}: {}",
                                pod.getMetadata().getName(), command);
                    }

                    @Override
                    public void onFailure(Throwable t, Response response) {
                        logger.debug("Command finalized with errors.");
                        latch.countDown();
                    }
                }).exec(command)) {
            if (awaitCompletion) {
                latch.await();
            } else {
                latch.await(3, TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            logger.error("Exception running command {} on pod: {}", command,
                    e.getMessage());
        }
    }

    public Service getServiceByName(String serviceName, String namespace) {
        logger.debug("Get service by name-namespace: {}-{}", serviceName,
                namespace);
        return client.services()
                .inNamespace(namespace == null ? DEFAULT_NAMESPACE : namespace)
                .withName(serviceName).get();
    }

    public String getServiceIpByName(String serviceName, String namespace)
            throws NullPointerException {
        return getServiceByName(serviceName, namespace).getMetadata().getName();
    }

    public List<Pod> getPodsFromNamespace(String namespace) {
        return client.pods().inNamespace(namespace).list().getItems();
    }

    public boolean existPodByName(String name) {
        return client.pods().inNamespace(DEFAULT_NAMESPACE).withName(name)
                .get() != null ? Boolean.TRUE : Boolean.FALSE;
    }

    public Pod getPodByName(String name, String namespace) {
        return client.pods()
                .inNamespace(
                        (namespace != null && !namespace.isEmpty()) ? namespace
                                : DEFAULT_NAMESPACE)
                .withName(name).get();
    }

    public String getPodIpByPodName(String name) {
        logger.debug("Get pod IP by pod name -> {}", name);
        return getPodIpByPodName(name, null);
    }

    public String getPodIpByPodName(String name, String namespace) {
        logger.debug("Get pod IP by pod name -> {}", name);
        String ip = getPodByName(name, namespace).getStatus().getPodIP();
        logger.debug("Pod {} has the ip {}", name, ip);
        return ip;
    }

    public String getPodIpByLabel(String label, String value,
            String namespace) {
        logger.debug("Get Ip by label {}-{}", label, value);
        Map<String, String> labels = new HashMap<>();
        labels.put(label, value.replace("_", "-"));
        UtilTools.sleep(4);
        List<Pod> pods = getPodsByLabels(labels, namespace);
        while (!isReady(pods.get(0).getMetadata().getName(), namespace)) {
            UtilTools.sleep(1);
        }
        pods = getPodsByLabels(labels, namespace);
        logger.debug("Pods recovered -> {}",
                pods.get(0).getMetadata().getName());
        logger.debug("Pod ip: {}", pods.get(0).getStatus().getPodIP());

        return pods.get(0).getStatus().getPodIP();
    }

    public List<Pod> getPodsByLabels(Map<String, String> labels,
            String namespace) {
        return client.pods()
                .inNamespace(
                        (namespace != null && !namespace.isEmpty()) ? namespace
                                : DEFAULT_NAMESPACE)
                .withLabels(labels).list().getItems();
    }

    public List<Pod> getPodsByLabelKey(String key, String namespace) {
        List<Pod> pods = client.pods()
                .inNamespace(
                        (namespace != null && !namespace.isEmpty()) ? namespace
                                : DEFAULT_NAMESPACE)
                .withLabel(key).list().getItems();

        logger.debug("Retrieved {} pods", pods.size());
        return pods;
    }

    public Boolean existJobByName(String name) {
        Boolean result = false;
        if (name != null && !name.isEmpty()) {
            result = client.batch().jobs().inNamespace(DEFAULT_NAMESPACE)
                    .withName(name).get() != null ? Boolean.TRUE
                            : Boolean.FALSE;
        }
        return result;
    }

    public String getServiceAccountToken() {
        return client.getConfiguration().getOauthToken();
    }

    public HostPathVolumeSource createEtToolsVolume() throws IOException {
        // TODO
        String etToolsVolumePath = etDataInHost + "/" + etToolsInternalPath;
        String etToolsVolumePathIntoEtm = etSharedFolder + "/"
                + etToolsInternalPath;

        HostPathVolumeSource etToolsVolume = new HostPathVolumeSourceBuilder()
                .withPath(etToolsVolumePath).build();

        // Copy to volume
        try {

            // Method 1
            // InputStream a = filesService
            // .getResourceAsInputStream("/" + etToolsResourceFolderPath);
            // logger.debug("aaaaaaaaaaaaaa {}", a);
            //
            // final InputStreamReader isr = new InputStreamReader(a,
            // StandardCharsets.UTF_8);
            // logger.debug("bbbbb");
            // try (BufferedReader br = new BufferedReader(isr, 1024)) {
            // String line;
            // while ((line = br.readLine()) != null) {
            // logger.info("File name (dev mode):" + line);
            // // if (line.equals(fileName)) {
            // // file = new ClassPathResource(path + line).getFile();
            // // return file;
            // // }
            // }
            // logger.debug("dddd");
            // } catch (Exception e) {
            // logger.error("cccc", e);
            // }

            // Method 2

            List<File> tools = filesService
                    .getFilesFromResources("/" + etToolsResourceFolderPath);
            if (tools == null || tools.size() == 0) {
                logger.debug("No tools found in {}", etToolsResourceFolderPath);
            } else {
                filesService.saveFilesInPath(tools, etToolsVolumePathIntoEtm);
                logger.debug("EtTools copied to {} successfully",
                        etToolsVolumePathIntoEtm);
            }
            // File sourceDirectoryFile = ResourceUtils
            // .getFile(etToolsResourceFolderPath);
            //
            // if (!sourceDirectoryFile.exists()) { // Dev mode
            // Resource resource = new ClassPathResource(
            // etToolsResourceFolderPath);
            // sourceDirectoryFile = resource.getFile();
            // }
            //
            // File targetDirectoryFile = new File(etToolsVolumePathIntoEtm);
            //
            // FileUtils.copyDirectory(sourceDirectoryFile,
            // targetDirectoryFile);
        } catch (Exception e) {
            logger.error("Error on create EtTools Volume", e);
        }

        return etToolsVolume;
    }

    public String getFullDNS(String serviceName, String namespace) {
        String fullDNS = "";
        fullDNS = getServiceNameByLabelAppName(serviceName, namespace)
                + ((namespace != null && !namespace.isEmpty())
                        ? "." + namespace + "."
                        : ".")
                + CLUSTER_DOMAIN;
        logger.debug("Full DNS: {}", fullDNS);
        return fullDNS;
    }

    private String getServiceNameByLabelAppName(String value,
            String namespace) {
        logger.debug("Value label {}", value);
        String serviceName = "";
        LabelSelectorBuilder selectorBuilder = new LabelSelectorBuilder();
        LabelSelector labelSelector = selectorBuilder
                .withMatchLabels(
                        Collections.singletonMap(LABEL_APP_NAME, value))
                .build();
        List<Service> services = client.services()
                .inNamespace(
                        (namespace != null && !namespace.isEmpty()) ? namespace
                                : DEFAULT_NAMESPACE)
                .list().getItems();
        if (!services.isEmpty()) {
            logger.debug("There are services with the label {}:{}",
                    LABEL_APP_NAME, value);
            serviceName = services.get(0).getMetadata().getName();
        }

        return serviceName;
    }

    public class PodInfo {
        private String podName;
        private String podIp;
        private Map<String, String> labels;

        public String getPodName() {
            return podName;
        }

        public void setPodName(String podName) {
            this.podName = podName;
        }

        public String getPodIp() {
            return podIp;
        }

        public void setPodIp(String podIp) {
            this.podIp = podIp;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, String> labels) {
            this.labels = labels;
        }
    }

    public class JobResult {
        private Integer result;
        private String jobName;
        private String podName;

        public String getPodName() {
            return podName;
        }

        public void setPodName(String podName) {
            this.podName = podName;
        }

        private String testResults;

        public String getTestResults() {
            return testResults;
        }

        public void setTestResults(String testResults) {
            this.testResults = testResults;
        }

        public String getJobName() {
            return jobName;
        }

        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        public Integer getResult() {
            return result;
        }

        public void setResult(Integer result) {
            this.result = result;
        }
    }

    public class ServiceInfo {
        private String servicePort;
        private String serviceName;
        private URL serviceURL;

        public ServiceInfo(String targetPort, String serviceName, URL url) {
            super();
            this.servicePort = targetPort;
            this.serviceName = serviceName;
            this.serviceURL = url;
        }

        public String getServicePort() {
            return servicePort;
        }

        public void setServicePort(String targetPort) {
            this.servicePort = targetPort;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public URL getServiceURL() {
            return serviceURL;
        }

        public void setServiceURL(URL serviceURL) {
            this.serviceURL = serviceURL;
        }
    }
}
