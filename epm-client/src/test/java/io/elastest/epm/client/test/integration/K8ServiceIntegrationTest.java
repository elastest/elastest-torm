package io.elastest.epm.client.test.integration;

import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.epm.client.service.K8sService;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.api.model.batch.JobBuilder;
import io.fabric8.kubernetes.client.dsl.ExecWatch;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { K8sService.class }, webEnvironment = RANDOM_PORT)
@Tag("integration")
@DisplayName("Integration tests for K8Docker Compose Service")
@EnableAutoConfiguration
@PropertySources({ @PropertySource(value = "classpath:epm-client.properties") })
@TestInstance(Lifecycle.PER_CLASS)
@Disabled
public class K8ServiceIntegrationTest {
    private static final Logger logger = getLogger((lookup().lookupClass()));
    @Autowired
    K8sService k8Service;

    private static final String SYSTEM_NAMESPACE = "default";
    private Pod pod1;
    private Job job;
    Map<String, String> jobsLabels = new HashMap<String, String>();
    private String sufix = RandomStringUtils.randomAlphanumeric(6)
            .toLowerCase();
    String jobName;
    String podName;

    @BeforeAll
    public void init() {
        String currentNamespace = SYSTEM_NAMESPACE;
        k8Service.client.pods().inNamespace(currentNamespace).withName("pod1-")
                .delete();
        k8Service.client.batch().jobs().inNamespace(currentNamespace).delete();
        k8Service.client.pods().inNamespace(currentNamespace)
                .withLabel("job-name").delete();
        
        jobName = "job1-" + sufix;
        podName = "pod1-" + sufix;
        
        pod1 = new PodBuilder()
                .withNewMetadata()
                .withName(podName)
                .endMetadata()
                .withNewSpec()
                .addNewContainer()
                .withName(podName)
                .withImage("busybox")
                .withCommand("sleep", "1")
                .endContainer()
                .endSpec()
                .build();

        k8Service.client.pods().inNamespace(currentNamespace)
                .createOrReplace(pod1);

        
        job = new JobBuilder(Boolean.FALSE)
                .withApiVersion("batch/v1")
                .withNewMetadata()
                .withName(jobName)
                .endMetadata()
                .withNewSpec()
                .withNewTemplate()
                .withNewSpec()
                .addNewContainer()
                .withName(jobName)
                .withImage("busybox")
                .withArgs("/bin/sh", "-c", "ls")
//                .withCommand("sleep", "5000")
//                .withCommand(Arrays.asList("/bin/bash", "-c", "sleep", "10000"))
                .endContainer()
                .withRestartPolicy("Never").endSpec().endTemplate().endSpec()
                .build();

        k8Service.client.batch().jobs().inNamespace(currentNamespace)
                .create(job);
    }

    @AfterAll
    public void finish() {
        String currentNamespace = SYSTEM_NAMESPACE;
        k8Service.client.pods().inNamespace(currentNamespace)
                .withName("pod1-" + sufix).delete();
        k8Service.client.batch().jobs().inNamespace(currentNamespace)
                .delete(job);
        k8Service.client.pods().inNamespace(currentNamespace)
        .withLabel("job-name").delete();
    }

    @Test
    public void readFileFromPod() throws IOException, InterruptedException {
        String currentNamespace = SYSTEM_NAMESPACE;
        Thread.sleep(10000);
        ExecWatch watch = k8Service.client.pods().inNamespace(currentNamespace)
                .withName(pod1.getMetadata().getName())
                .writingOutput(System.out)
                .exec("sh", "-c", "echo 'hello' > /msg");
        try (InputStream is = k8Service.client.pods()
                .inNamespace(currentNamespace)
                .withName(pod1.getMetadata().getName()).file("/msg").read()) {
            String result = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
            assertEquals("hello", result);
        }
    }

    @Test
    public void readFileFromJob() throws IOException, InterruptedException {
        String currentNamespace = SYSTEM_NAMESPACE;
        Thread.sleep(10000);

        ExecWatch watch = k8Service.client.pods().inNamespace(currentNamespace)
                .withName(k8Service.client.pods().inNamespace(currentNamespace)
                        .withLabel("job-name").list().getItems().get(0)
                        .getMetadata().getName())
                .writingOutput(System.out)
                .exec("sh", "-c", "echo 'hello' > /msg");
        
        logger.info("Pod's name: {}", k8Service.client.pods().inNamespace(currentNamespace)
                        .withLabel("job-name").list().getItems().get(0)
                        .getMetadata().getName());
        Thread.sleep(120000);
        
        try (InputStream is = k8Service.client.pods()
                .inNamespace(currentNamespace)
                .withName(k8Service.client.pods().inNamespace(currentNamespace)
                        .withLabel("job-name", jobName).list().getItems().get(0)
                        .getMetadata().getName())
                .file("/msg").read()) {
            String result = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
            assertEquals("hello", result);
        }
    }
}
