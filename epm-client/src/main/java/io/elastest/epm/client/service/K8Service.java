package io.elastest.epm.client.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.elastest.epm.client.DockerContainer;
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
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.SecurityContextBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import okhttp3.Response;

@org.springframework.stereotype.Service
public class K8Service {
    private static final Logger logger = LoggerFactory
            .getLogger(K8Service.class);

    @Value("${et.enable.cloud.mode}")
    public boolean enableCloudMode;

    private static final String DEFAULT_NAMESPACE = "default";
    private static final String LABEL_JOB_NAME = "job-name";
    private static final String LABEL_POD_NAME = "pod-name";
    private static final String LABEL_APP_NAME = "app";
    private static final String SUT_PORT_NAME = "sut-port";
    private static final String PHASE_SUCCEEDED = "Succeeded";
    private static final String LOCAL_K8S_MASTER = "localhost";
    private static final String SERVICE_ACCOUNT_DEFAULT = "default";

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

    public K8Service(FilesService filesService) {
        this.filesService = filesService;
    }

    @PostConstruct
    public void init() throws IOException {
        if (enableCloudMode) {
            logger.debug("Default K8s");
            client = new DefaultKubernetesClient();

            // TODO use volume into tjob to get testresults
            // etToolsVolume = createEtToolsVolume();
        }
    }

    public enum PodsStatusEnum {
        PENDING("Not initialized"), RUNNING("Running"), SUCCEEDED("Succeeded"),
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

    // TODO
    public void startFromYml(String ymlPath) {
        try {
            File file = new File(ymlPath);

            InputStream initialStream = new FileInputStream(file);
            List<HasMetadata> resourcesList = client.load(initialStream).get();

            // KubernetesList itemList = new KubernetesList();
            for (HasMetadata resource : resourcesList) {
                // itemList.getItems().add(resource);

                // TODO resource instance of (Pod, ConfigMap, Service...)
                // and create each like:
                // client.pods().create(resource);
            }

            // client.lists().create(itemList); Not working...

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
                            logger.debug("Job {} receive an event",
                                    job.getMetadata().getLabels()
                                            .get(LABEL_JOB_NAME));
                            logger.debug("Event received: {}",
                                    pod.getStatus().getPhase());
                            logger.debug("Action: {}", action.toString());

                            if (result.getPodName().isEmpty()) {
                                result.setPodName(pod.getMetadata().getName());
                            }

                            if (!pod.getStatus().getPhase().equals("Pending")
                                    && !pod.getStatus().getPhase()
                                            .equals("Running")) {
                                logger.info("Pod executed with result: {}",
                                        pod.getStatus().getPhase());
                                result.setResult(pod.getStatus().getPhase()
                                        .equals("Succeeded") ? 0 : 1);
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
            String containerNameWithoutUnderscore = container.getContainerName()
                    .get().replace("_", "-");

            k8sPobLabels.put(LABEL_POD_NAME, containerNameWithoutUnderscore);

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

            podBuilder.buildSpec().getContainers().get(0);
            pod = client.pods().inNamespace(namespace)
                    .createOrReplace(podBuilder.build());

            while (!isReady(containerNameWithoutUnderscore)) {
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

    public String createService(String serviceName, Integer port,
            String protocol) {
        io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
                .withNewMetadata().withName(serviceName + "-service")
                .endMetadata().withNewSpec()
                .withSelector(
                        Collections.singletonMap(LABEL_APP_NAME, serviceName))
                .addNewPort().withName(SUT_PORT_NAME).withProtocol(protocol)
                .withPort(port).withTargetPort(new IntOrString(9376)).endPort()
                .withType("NodePort").endSpec().build();

        service = client.services().inNamespace(client.getNamespace())
                .create(service);
        String serviceURL = client.services().inNamespace(client.getNamespace())
                .withName(service.getMetadata().getName())
                .getURL(SUT_PORT_NAME);
        return serviceURL;
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

    public boolean isReady(String podName) {
        Pod pod = client.pods().inNamespace(DEFAULT_NAMESPACE).withName(podName)
                .get();
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
            String targetPath) {
        logger.info("Copying file from k8s pod {} in this path {}", podName,
                targetPath);
        Integer result = 0;
        result = client.pods().inNamespace(DEFAULT_NAMESPACE).withName(podName)
                .dir(originPath).copy(Paths.get(targetPath)) ? result : 1;
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

    public class PodInfo {
        private String podName;
        private String podIp;

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

    public MixedOperation<Service, ServiceList, DoneableService, ServiceResource<Service, DoneableService>> getAllServicesToOperate() {
        return client.services();
    }

    public Service getServiceByName(String serviceName)
            throws NullPointerException {
        return getAllServicesToOperate().withName(serviceName).get();
    }

    public String getServiceIpByName(String serviceName)
            throws NullPointerException {
        return getServiceByName(serviceName).getSpec().getClusterIP();
    }

    public List<Pod> getPodsFromNamespace(String namespace) {
        return client.pods().inNamespace(namespace).list().getItems();
    }

    public boolean existPodByName(String name) {
        return client.pods().inNamespace(DEFAULT_NAMESPACE).withName(name)
                .get() != null ? Boolean.TRUE : Boolean.FALSE;
    }

    public Pod getPodByName(String name) {
        return client.pods().inNamespace(DEFAULT_NAMESPACE).withName(name)
                .get();
    }

    public String getPodIpByPodName(String name) {
        return getPodByName(name).getStatus().getPodIP();
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
}
