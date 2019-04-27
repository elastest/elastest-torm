package io.elastest.epm.client.service;

import org.springframework.stereotype.Service;

import io.elastest.epm.client.DockerContainer;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionFluent.SpecNested;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.api.model.batch.JobSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecFluentImpl.SpecNestedImpl;
import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.CopyOrReadable;
import io.fabric8.kubernetes.client.dsl.ExecWatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class K8Service {
    private static final Logger logger = LoggerFactory
            .getLogger(K8Service.class);

    private static final String DEFAULT_NAMESPACE = "default";
    private static final String SYSTEM_NAMESPACE = "kube-system";

    public final KubernetesClient client;

    public K8Service() {
        client = new DefaultKubernetesClient();
    }

    public void deployJob() {
        try {
            final String namespace = "default";
            final Job job = new JobBuilder().withApiVersion("batch/v1")
                    .withNewMetadata().withName("pi")
                    .withLabels(Collections.singletonMap("label1",
                            "maximum-length-of-63-characters"))
                    .withAnnotations(Collections.singletonMap("annotation1",
                            "some-very-long-annotation"))
                    .endMetadata().withNewSpec().withNewTemplate().withNewSpec()
                    .addNewContainer().withName("pi").withImage("perl")
                    .withArgs("perl", "-Mbignum=bpi", "-wle", "print bpi(2000)")
                    .endContainer().withRestartPolicy("Never").endSpec()
                    .endTemplate().endSpec().build();

            logger.info("Creating job pi.");
            client.batch().jobs().inNamespace(namespace).create(job);
            logger.info("Job pi is created, waiting for result...");

            final CountDownLatch watchLatch = new CountDownLatch(1);
            try (final Watch ignored = client.pods().inNamespace(namespace)
                    .withLabel("job-name").watch(new Watcher<Pod>() {
                        @Override
                        public void eventReceived(final Action action,
                                Pod pod) {
                            logger.info("Job phase: {}.",
                                    pod.getStatus().getPhase());
                            if (pod.getStatus().getPhase()
                                    .equals("Succeeded")) {
                                logger.info("Job pi is completed!");
                                logger.info(client.pods().inNamespace(namespace)
                                        .withName(pod.getMetadata().getName())
                                        .getLog());
                                watchLatch.countDown();
                            }
                        }

                        @Override
                        public void onClose(final KubernetesClientException e) {
                            logger.info("Cleaning up job pi.");
                            client.batch().jobs().inNamespace(namespace)
                                    .delete(job);
                        }
                    })) {
                watchLatch.await(2, TimeUnit.MINUTES);
            } catch (final KubernetesClientException | InterruptedException e) {
                logger.error("Could not watch pod", e);
            }
        } catch (final KubernetesClientException e) {
            logger.error("Unable to create job", e);
        }

        logger.error("Finish Job deployment");
    }

    public JobResult deployJob(DockerContainer container) {
        final JobResult result = new JobResult();
        container.getCmd().get().forEach((command) -> {
            logger.debug("Commands to execute: {}", command);
        });
        final String namespace = SYSTEM_NAMESPACE;// "default";
        try {
            logger.info("Container name: {}",
                    container.getContainerName().get());
            logger.info(String.join(",", container.getCmd().get()));

            final Job job = new JobBuilder(Boolean.FALSE)
                    .withApiVersion("batch/v1").withNewMetadata()
                    .withName(container.getContainerName().get().replace("_",
                            "-"))
                    .withLabels(container.getLabels().get()).endMetadata()
                    .withNewSpec().withNewTemplate().withNewSpec()
                    .addNewContainer()
                    .withName(container.getContainerName().get().replace("_",
                            "-"))
                    .withImage(container.getImageId())
                    .withArgs(container.getCmd().get()).endContainer()
                    .withRestartPolicy("Never").endSpec().endTemplate()
                    .endSpec().build();

            logger.info("Creating job: {}.",
                    container.getContainerName().get().replace("_", "-"));
            client.batch().jobs().inNamespace(namespace).create(job);
            logger.info("Job {} is created, waiting for result...",
                    client.batch().jobs().inNamespace(namespace)
                            .withName(container.getContainerName().get()
                                    .replace("_", "-"))
                            .get().getMetadata().getName());

            final CountDownLatch watchLatch = new CountDownLatch(1);
            try (final Watch ignored = client.pods().inNamespace(namespace)
                    .withLabel("job-name",
                            job.getMetadata().getLabels().get("job-name"))
                    .watch(new Watcher<Pod>() {
                        @Override
                        public void eventReceived(final Action action,
                                Pod pod) {
                            logger.info("Event received: {}",
                                    pod.getStatus().getPhase());
                            logger.info("Action: {}", action.toString());
//                            client.pods().inNamespace(namespace).list()
//                                    .getItems().forEach((podItem) -> {
//                                        logger.info("Pod name: {}", podItem
//                                                .getMetadata().getName());
//                                    });
                            if (pod.getStatus().getPhase()
                                    .equals("Succeeded")) {
                                result.setResult(pod.getStatus().getPhase());
                                result.setJobName(job.getMetadata().getName());
//                                client.pods().inNamespace(namespace).list()
//                                        .getItems().forEach((podItem) -> {
//                                            logger.info("Pod name: {}", podItem
//                                                    .getMetadata().getName());
//                                        });
                                result.setPodName(pod.getMetadata().getName());
                                logger.info("Job {} is completed!",
                                        pod.getMetadata().getName());
//                                logger.info(client.pods().inNamespace(namespace)
//                                        .withName(pod.getMetadata().getName())
//                                        .getLog());
//                                
//                                readFileFromContainer(result.getPodName(),
//                                        "/demo-projects/unit/junit5-unit-test/target/surefire-reports/");
//                                readFileFromContainer(
//                                        pod.getMetadata().getName(), "/msg");

                                watchLatch.countDown();
                            }

                            if (pod.getStatus().getPhase().equals("Running")) {
                                logger.info("Reading file from Running phase");
                                ExecWatch watch = client.pods()
                                        .inNamespace(namespace)
                                        .withName(client.pods()
                                                .inNamespace(namespace)
                                                .withLabel("job-name").list()
                                                .getItems().get(0).getMetadata()
                                                .getName())
                                        .writingOutput(System.out).exec("sh",
                                                "-c", "echo 'hello' > /msg");
//                                client.pods()
//                                .inNamespace(namespace)
//                                .withName(client.pods()
//                                        .inNamespace(namespace)
//                                        .withLabel("job-name").list()
//                                        .getItems().get(0).getMetadata()
//                                        .getName())
//                                .writingOutput(System.out).exec("sh",
//                                        "-c", "ls");
//                                readFileFromContainer(
//                                        pod.getMetadata().getName(), "/msg");
                            }
                        }

                        @Override
                        public void onClose(final KubernetesClientException e) {
                            logger.info("Cleaning up job {}.",
                                    job.getSpec().getTemplate().getSpec()
                                            .getContainers().get(0).getName());

                        }
                    })) {
                watchLatch.await(2, TimeUnit.MINUTES);
            } catch (final KubernetesClientException | InterruptedException e) {
                logger.error("Could not watch pod", e);

            }
        } catch (final KubernetesClientException e) {
            logger.error("Unable to create job", e);
        } finally {
//            String jobName = container.getContainerName().get().replace("_",
//                    "");
//            client.pods().inNamespace(namespace).withLabel("job-name", jobName)
//                    .delete();

//            client.batch().jobs().inNamespace(namespace)
//                    .delete(client.batch().jobs().inNamespace(namespace)
//                            .withLabel("job-name", jobName).list().getItems()
//                            .get(0));

        }

        logger.info("Job deployed");

        return result;
    }

    public void deleteJob(String jobName) {
        logger.info("Cleaning up job {}.", jobName);
        client.pods().inNamespace(SYSTEM_NAMESPACE)
                .withLabel("job-name", jobName).delete();
    }

    public JobResult deployPod(DockerContainer container) {
        final JobResult result = new JobResult();
        container.getCmd().get().forEach((command) -> {
            logger.debug("Commands to execute: {}", command);
        });
        final String namespace = SYSTEM_NAMESPACE;// "default";
        try {
            logger.info("Container name: {}",
                    container.getContainerName().get());
            logger.info(String.join(",", container.getCmd().get()));

            final Job job = new JobBuilder(Boolean.FALSE)
                    .withApiVersion("batch/v1").withNewMetadata()
                    .withName(container.getContainerName().get().replace("_",
                            "-"))
                    .withLabels(container.getLabels().get()).endMetadata()
                    .withNewSpec().withNewTemplate().withNewSpec()
                    .addNewContainer()
                    .withName(container.getContainerName().get().replace("_",
                            "-"))
                    .withImage(container.getImageId())
                    .withArgs(container.getCmd().get()).endContainer()
                    .withRestartPolicy("Never").endSpec().endTemplate()
                    .endSpec().build();

            logger.info("Creating job: {}.",
                    container.getContainerName().get().replace("_", ""));
            client.batch().jobs().inNamespace(namespace).create(job);
            logger.info("Job {} is created, waiting for result...",
                    client.batch().jobs().inNamespace(namespace)
                            .withName(container.getContainerName().get()
                                    .replace("_", "-"))
                            .get().getMetadata().getName());

            final CountDownLatch watchLatch = new CountDownLatch(1);
            try (final Watch ignored = client.pods().inNamespace(namespace)
                    .withLabel("job-name",
                            job.getMetadata().getLabels().get("job-name"))
                    .watch(new Watcher<Pod>() {
                        @Override
                        public void eventReceived(final Action action,
                                Pod pod) {
                            logger.info("Event received: {}",
                                    pod.getStatus().getPhase());
                            logger.info("Action: {}", action.toString());
//                            client.pods().inNamespace(namespace).list()
//                                    .getItems().forEach((podItem) -> {
//                                        logger.info("Pod name: {}", podItem
//                                                .getMetadata().getName());
//                                    });
                            if (pod.getStatus().getPhase()
                                    .equals("Succeeded")) {
                                result.setResult(pod.getStatus().getPhase());
                                result.setJobName(job.getMetadata().getName());
//                                client.pods().inNamespace(namespace).list()
//                                        .getItems().forEach((podItem) -> {
//                                            logger.info("Pod name: {}", podItem
//                                                    .getMetadata().getName());
//                                        });
                                result.setPodName(pod.getMetadata().getName());
                                logger.info("Job {} is completed!",
                                        pod.getMetadata().getName());
//                                logger.info(client.pods().inNamespace(namespace)
//                                        .withName(pod.getMetadata().getName())
//                                        .getLog());
//                                
//                                readFileFromContainer(result.getPodName(),
//                                        "/demo-projects/unit/junit5-unit-test/target/surefire-reports/");
                                readFileFromContainer(
                                        pod.getMetadata().getName(), "/msg");

                                watchLatch.countDown();
                            }

                            if (pod.getStatus().getPhase().equals("Running")) {
                                logger.info("Reading file from Running phase");
                                ExecWatch watch = client.pods()
                                        .inNamespace(namespace)
                                        .withName(client.pods()
                                                .inNamespace(namespace)
                                                .withLabel("job-name").list()
                                                .getItems().get(0).getMetadata()
                                                .getName())
                                        .writingOutput(System.out).exec("sh",
                                                "-c", "echo 'hello' > /msg");
//                                client.pods()
//                                .inNamespace(namespace)
//                                .withName(client.pods()
//                                        .inNamespace(namespace)
//                                        .withLabel("job-name").list()
//                                        .getItems().get(0).getMetadata()
//                                        .getName())
//                                .writingOutput(System.out).exec("sh",
//                                        "-c", "ls");
                                readFileFromContainer(
                                        pod.getMetadata().getName(), "/msg");
                            }
                        }

                        @Override
                        public void onClose(final KubernetesClientException e) {
                            logger.info("Cleaning up job {}.",
                                    job.getSpec().getTemplate().getSpec()
                                            .getContainers().get(0).getName());

                        }
                    })) {
                watchLatch.await(2, TimeUnit.MINUTES);
            } catch (final KubernetesClientException | InterruptedException e) {
                logger.error("Could not watch pod", e);

            }
        } catch (final KubernetesClientException e) {
            logger.error("Unable to create job", e);
        } finally {
//            String jobName = container.getContainerName().get().replace("_",
//                    "");
//            client.pods().inNamespace(namespace).withLabel("job-name", jobName)
//                    .delete();

//            client.batch().jobs().inNamespace(namespace)
//                    .delete(client.batch().jobs().inNamespace(namespace)
//                            .withLabel("job-name", jobName).list().getItems()
//                            .get(0));

        }

        logger.info("Job deployed");

        return result;
    }

    public String readFileFromContainer(String podName, String filePath) {
        logger.info("Reading file from k8s pod {} in this path {}", podName,
                filePath);
        String result = null;

        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath);
            try (InputStream is = client.pods().inNamespace(SYSTEM_NAMESPACE)
                    .withName(podName).dir(filePath).read()) {
                result = new BufferedReader(new InputStreamReader(is)).lines()
                        .collect(Collectors.joining("\n"));
                logger.info("File content: {}", result);
            } catch (Exception e) {
                // TODO Auto-generated catch block
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
//        result = client.pods().inNamespace(SYSTEM_NAMESPACE).withName(podName)
//                .dir(originPath).copy(Paths.get(targetPath + "/file.tar")) ? result : 1;

        readFileFromContainer(podName, originPath);
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
            jobName = jobName;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

}
