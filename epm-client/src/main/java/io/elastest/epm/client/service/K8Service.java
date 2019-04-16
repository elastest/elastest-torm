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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class K8Service {
    private static final Logger logger = LoggerFactory
            .getLogger(K8Service.class);

    final KubernetesClient client;

    public K8Service() {
        client = new DefaultKubernetesClient();
    }

    public void deployJob() {
//        String master = "https://localhost:8443/";
//        if (args.length == 1) {
//            master = args[0];
//        }

//        final Config config = new ConfigBuilder().withMasterUrl(master).build();
//        ConfigBuilder builder = new ConfigBuilder();

//        Config config = builder.build();
//        try (final KubernetesClient client = new DefaultKubernetesClient(config)) {
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

    public String deployJob(DockerContainer container) {
        final JobResult result = new JobResult();
        try {
            final String namespace = "default";
            client.pods().inNamespace(namespace).delete();
            logger.info("Container name: {}", container.getContainerName().get());
            
            final Job job = new JobBuilder(Boolean.FALSE).withApiVersion("batch/v1")
                    .withNewMetadata()
                    .withName(container.getContainerName().get().replace("_", "-"))
                    .withLabels(container.getLabels().get()).endMetadata()
                    .withNewSpec()
                    .withNewTemplate()
                    .withNewSpec()
                    .addNewContainer()
                    .withName(container.getContainerName().get().replace("_", "-"))
                    .withImage(container.getImageId())
                    .withArgs(container.getCmd().get())
                    .endContainer()
                    .withRestartPolicy("Never")
                    .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build();

            logger.info("Creating job: {}.", container.getContainerName());
            client.batch().jobs().inNamespace(namespace).create(job);
            logger.info("Job {} is created, waiting for result...", container.getContainerName().get());

            final CountDownLatch watchLatch = new CountDownLatch(1);
            try (final Watch ignored = client.pods().inNamespace(namespace)
                    .withLabel("job-name").watch(new Watcher<Pod>() {
                        @Override
                        public void eventReceived(final Action action,
                                Pod pod) {
                            if (pod.getStatus().getPhase()
                                    .equals("Succeeded")) {
                                result.setResult(pod.getStatus().getPhase());
                                logger.info("Job {} is completed!",
                                        pod.getMetadata().getName());
                                logger.info(client.pods().inNamespace(namespace)
                                        .withName(pod.getMetadata().getName())
                                        .getLog());
                                watchLatch.countDown();
                            }
                        }

                        @Override
                        public void onClose(final KubernetesClientException e) {
                            logger.info("Cleaning up job {}.",
                                    job.getSpec().getTemplate().getSpec()
                                            .getContainers().get(0).getName());
                            client.pods().inNamespace(namespace).withLabel("job-name", job.getMetadata().getName()).delete();
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
        logger.info("Job deployed");
        return result.getResult();
    }
    
//    public DockerContainer deployPod(DockerContainer container) {
//        
//    }
    
    private class JobResult {
        private String result;

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

}
