package io.elastest.etm.test.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectImageResponse;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;

import io.elastest.etm.service.DockerService2;
import io.elastest.etm.service.TJobStoppedException;
import io.elastest.etm.test.extensions.MockitoExtension;
import io.elastest.etm.utils.UtilTools;

@RunWith(JUnitPlatform.class)
@ExtendWith({ MockitoExtension.class })
public class DockerServiceUnitTest {
    private static final Logger logger = LoggerFactory
            .getLogger(DockerServiceUnitTest.class);

    @Autowired
    @InjectMocks
    public DockerService2 dockerService;
    private DockerClient dockerClient;

    String image = "elastest/test-etm-test1";

    @BeforeEach
    public void before() {
        DockerClientConfig config = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock").build();
        dockerClient = DockerClientBuilder.getInstance(config).build();
    }

    @Test
    public void inspectImageTest() throws TJobStoppedException {
        this.dockerService.doPull(dockerClient, image);
        InspectImageResponse imageInfo = this.dockerService
                .getImageInfoByName(image);
        assertNotNull(imageInfo);
    }

    @Test
    public void runStopAndRemoveContainerTest() throws TJobStoppedException {
        String image = "elastest/test-etm-test1";
        String containerName = "testContainer" + UtilTools.generateUniqueId();
        logger.info("Starting container {}", containerName);
        String containerId = this.dockerService.runDockerContainer(dockerClient,
                image, null, containerName, null, null, null);
        logger.info("Container {} started with id {}", containerName,
                containerId);

        assertTrue(this.dockerService.existsContainer(containerName));

        logger.info("Stopping container {}", containerName);
        this.dockerService.stopDockerContainer(containerId);
        logger.info("Removing container {}", containerName);
        this.dockerService.removeDockerContainer(containerId);
        assertFalse(this.dockerService.existsContainer(containerName));
    }

}
