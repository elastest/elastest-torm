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

import com.spotify.docker.client.messages.ImageInfo;

import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.epm.client.service.DockerService;
import io.elastest.etm.test.extensions.MockitoExtension;
import io.elastest.etm.utils.UtilTools;

@RunWith(JUnitPlatform.class)
@ExtendWith({ MockitoExtension.class })
public class DockerServiceUnitTest {
    private static final Logger logger = LoggerFactory
            .getLogger(DockerServiceUnitTest.class);

    @Autowired
    @InjectMocks
    public DockerService dockerService;

    String image = "elastest/test-etm-test1";

    @BeforeEach
    public void before() {

    }

    @Test
    public void inspectImageTest() throws Exception {
        this.dockerService.pullImage(image);
        ImageInfo imageInfo = this.dockerService.getImageInfoByName(image);
        assertNotNull(imageInfo);
    }

    @Test
    public void runStopAndRemoveContainerTest() throws Exception {
        String image = "elastest/test-etm-test1";
        String containerName = "testContainer" + UtilTools.generateUniqueId();
        logger.info("Starting container {}", containerName);

        DockerBuilder dockerBuilder = new DockerBuilder(image);
        dockerBuilder.containerName(containerName);

        String containerId = this.dockerService
                .createAndStartContainer(dockerBuilder.build(), false);
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
