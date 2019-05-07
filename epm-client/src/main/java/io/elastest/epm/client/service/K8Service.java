package io.elastest.epm.client.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.elastest.epm.client.DockerContainer;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
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
    private static final String FHASE_SUCCEEDED = "Succeeded";
    public final KubernetesClient client;

    public K8Service() {
        client = new DefaultKubernetesClient();
    }

    public JobResult deployJob(DockerContainer container) {
        return deployJob(container, DEFAULT_NAMESPACE);
    }

    public JobResult deployJob(DockerContainer container, String nameSpace) {
        final JobResult result = new JobResult();
        container.getCmd().get().forEach((command) -> {
            logger.debug("Commands to execute: {}", command);
        });
        final String namespace = nameSpace;
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
                    .withArgs(container.getCmd().get()).endContainer()
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

    public void deleteJob(String jobName) {
        logger.info("Cleaning up job {}.", jobName);
        client.pods().inNamespace(DEFAULT_NAMESPACE)
                .withLabel(LABEL_JOB_NAME, jobName).delete();
    }

    public String readFileFromContainer(String podName, String filePath) {
        logger.info("Reading file from k8s pod {} in this path {}", podName,
                filePath);
        String result = null;

        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath);
            try (InputStream is = client.pods().inNamespace(DEFAULT_NAMESPACE)
                    .withName(podName).dir(filePath).read()) {
                result = new BufferedReader(new InputStreamReader(is)).lines()
                        .collect(Collectors.joining("\n"));
                logger.info("File content: {}", result);
            } catch (Exception e) {
                logger.error("Error reading test results' file");
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
