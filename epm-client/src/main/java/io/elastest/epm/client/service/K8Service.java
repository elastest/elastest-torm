package io.elastest.epm.client.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.epm.client.DockerContainer;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;

@Service
public class K8Service {
    private static final Logger logger = LoggerFactory
            .getLogger(K8Service.class);

    private static final String DEFAULT_NAMESPACE = "default";
    private static final String LABEL_JOB_NAME = "job-name";
    private static final String LABEL_POD_NAME = "pod-name";
    private static final String LABEL_APP_NAME = "app";
    private static final String SUT_PORT_NAME = "sut-port";
    private static final String PHASE_SUCCEEDED = "Succeeded";
    private static final String LOCAL_K8S_MASTER ="localhost"; 
    public KubernetesClient client;
    
    @Value("${et.epm.k8s.master}")
    public String etEpmK8sMaster;

    public K8Service() {
 
    }
    
    @PostConstruct
    public void init() {
        if (etEpmK8sMaster.equals(LOCAL_K8S_MASTER)) {
            client = new DefaultKubernetesClient();
        } else {
            Config config = new ConfigBuilder().withMasterUrl(etEpmK8sMaster).build();
            client = new DefaultKubernetesClient(config);
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

    public JobResult deployJob(DockerContainer container) {
        return deployJob(container, DEFAULT_NAMESPACE);
    }

    public JobResult deployJob(DockerContainer container, String namespace) {
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
            final Job job = new JobBuilder(Boolean.FALSE)
                    .withApiVersion("batch/v1").withNewMetadata()
                    .withName(containerNameWithoutUnderscore)
                    .withLabels(k8sJobLabels).endMetadata().withNewSpec()
                    .withNewTemplate().withNewSpec().addNewContainer()
                    .withName(containerNameWithoutUnderscore)
                    .withImage(container.getImageId())
                    .withArgs(container.getCmd().get())
                    .withEnv(getEnvVarListFromStringList(container.getEnvs().get()))
                    .endContainer()
                    .withRestartPolicy("Never").endSpec().endTemplate()
                    .endSpec().build();

            logger.info("Creating job: {}.",
                    job.getMetadata().getLabels().get(LABEL_JOB_NAME));
            client.batch().jobs().inNamespace(namespace).create(job);
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

                            if (pod.getStatus().getPhase()
                                    .equals("Succeeded")) {
                                result.setResult(pod.getStatus().getPhase());
                                result.setJobName(job.getMetadata().getName());
                                result.setPodName(pod.getMetadata().getName());
                                logger.info("Job {} is completed!",
                                        pod.getMetadata().getName());
                                watchLatch.countDown();
                            }
                        }

                        @Override
                        public void onClose(final KubernetesClientException e) {
                            logger.debug("Cleaning up job {}.",
                                    job.getMetadata().getName());
                            client.batch().jobs().inNamespace(namespace)
                                    .delete(job);
                            deleteJob(job.getMetadata().getName());
                        }
                    })) {
                watchLatch.await(2, TimeUnit.MINUTES);
            } catch (final KubernetesClientException | InterruptedException e) {
                logger.error("Could not watch pod", e);

            }
        } catch (final KubernetesClientException e) {
            logger.error("Unable to create job", e);
        }

        return result;
    }
    
    public PodInfo deployPod(DockerContainer container) throws Exception {
        return deployPod(container, DEFAULT_NAMESPACE);
    }
    
    public PodInfo deployPod(DockerContainer container, String namespace) throws Exception{
        PodInfo podInfo = new PodInfo();
        Pod pod = null;
      
        try {
            logger.info("Container name: {}",
                    container.getContainerName().get());
            logger.info(String.join(",", container.getCmd().get()));

            Map<String, String> k8sPobLabels = container.getLabels().get();
            String containerNameWithoutUnderscore = container.getContainerName()
                    .get().replace("_", "-");
            
            k8sPobLabels.put(LABEL_POD_NAME, containerNameWithoutUnderscore);

            pod = new PodBuilder()
                    .withNewMetadata()
                    .withName(containerNameWithoutUnderscore)
                    .endMetadata()
                    .withNewSpec()
                    .addNewContainer()
                    .withName(containerNameWithoutUnderscore)
                    .withImage(container.getImageId())
                    .withEnv(getEnvVarListFromStringList(container.getEnvs().get()))
                    .endContainer()
                    .endSpec()
                    .build();
            
            pod = client.pods().inNamespace(namespace).createOrReplace(pod);
             
            while(!isReady(containerNameWithoutUnderscore)) {}
            pod = client.pods().inNamespace(DEFAULT_NAMESPACE).withName(containerNameWithoutUnderscore).get();
//            Pod podUpdated = client.pods().withName(containerNameWithoutUnderscore).get();
//            logger.debug("Sut Pod ip: {}", pod.getMetadata().getName());
            logger.debug("Sut Pod ip: {}", pod.getStatus().getPodIP());
            
           
        } catch (final KubernetesClientException e) {
            logger.error("Unable to create job", e);
            throw e;
        }
        podInfo.setPodIp(pod.getStatus().getPodIP());
        podInfo.setPodName(pod.getMetadata().getName());

        return podInfo;
    } 
    
    public String createService(String serviceName, Integer port, String protocol) {
        io.fabric8.kubernetes.api.model.Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName(serviceName + "-service")
                .endMetadata()
                .withNewSpec()
                .withSelector(Collections.singletonMap(LABEL_APP_NAME, serviceName ))
                .addNewPort()
                .withName(SUT_PORT_NAME)
                .withProtocol(protocol)
                .withPort(port)
                .withTargetPort(new IntOrString(9376))
                .endPort()
                .withType("NodePort")
                .endSpec()
                .build();
        
        service = client.services().inNamespace(client.getNamespace()).create(service);
        String serviceURL = client.services().inNamespace(client.getNamespace()).withName(service.getMetadata().getName()).getURL(SUT_PORT_NAME);
        return serviceURL;
    }

    public void deleteJob(String jobName) {
        logger.info("Cleaning up job {}.", jobName);
        client.pods().inNamespace(DEFAULT_NAMESPACE)
                .withLabel(LABEL_JOB_NAME, jobName).delete();
    }
    
    public void deletePod(String podName) {
        logger.info("Deleting pod {}.", podName);
        client.pods().inNamespace(DEFAULT_NAMESPACE)
                .withName(podName.replace("_", "-")).delete();
    }
    
    public boolean isReady(String podName) {
        Pod pod = client.pods().inNamespace(DEFAULT_NAMESPACE).withName(podName).get();
        if (pod == null) {
          return false;
        }
        else {
          return pod.getStatus().getConditions().stream()
              .filter(condition -> condition.getType().equals("Ready"))
              .map(condition -> condition.getStatus().equals("True"))
              .findFirst()
              .orElse(false);
        }
      }
    
    private List<EnvVar> getEnvVarListFromStringList(List<String> envVarsAsStringList) {
        List<EnvVar> envVars = new ArrayList<EnvVar>();
        envVarsAsStringList.forEach(envVarAsString -> {
            String[] keyValuePar = envVarAsString.split("="); 
            EnvVar envVar = new EnvVar(keyValuePar[0], keyValuePar[1], null);
            envVars.add(envVar);
        });
        return envVars;
    }

    public String readFileFromContainer(String podName, String filePath) {
        logger.info("Reading files from k8s pod {} in this path {}", podName,
                filePath);
        String result = null;

        if (filePath != null && !filePath.isEmpty()) {
            try (InputStream is = client.pods().inNamespace(DEFAULT_NAMESPACE)
                    .withName(podName).dir(filePath).read()) {
                result = new BufferedReader(new InputStreamReader(is)).lines()
                        .collect(Collectors.joining("\n"));
                logger.debug("File content: {}", result);
            } catch (Exception e) {
                logger.error("Error reading files");
                e.printStackTrace();
            }
        }
        return result;
    }

    public Integer copyFileFromContainer(String podName, String originPath,
            String targetPath) {
        logger.info("Copying file from k8s pod {} in this path {}", podName,
                targetPath);
        Integer result = 0;
        result = client.pods().inNamespace(DEFAULT_NAMESPACE).withName(podName)
                .dir(originPath).copy(Paths.get(targetPath)) ? result : 1;
        return result;
    }
    
    public class PodInfo {
        private String podName;
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
        private String podIp;
    }

    public class JobResult {
        private String result;
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

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

}
