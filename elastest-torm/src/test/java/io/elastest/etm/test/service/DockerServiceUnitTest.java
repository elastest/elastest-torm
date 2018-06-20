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

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ImageInfo;

import io.elastest.etm.model.DockerContainer.DockerBuilder;
import io.elastest.etm.service.DockerEtmService;
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
    public DockerEtmService dockerEtmService;

    String image = "elastest/test-etm-test1";

    @BeforeEach
    public void before() {

    }

    @Test
    public void inspectImageTest() throws Exception {
        this.dockerEtmService.dockerService.pullImage(image);
        ImageInfo imageInfo = this.dockerEtmService.dockerService
                .getImageInfoByName(image);
        assertNotNull(imageInfo);
    }

    @Test
    public void runStopAndRemoveContainerTest() throws TJobStoppedException,
            DockerException, InterruptedException, DockerCertificateException {
        String image = "elastest/test-etm-test1";
        String containerName = "testContainer" + UtilTools.generateUniqueId();
        logger.info("Starting container {}", containerName);

        DockerBuilder dockerBuilder = new DockerBuilder(image);
        dockerBuilder.containerName(containerName);

        String containerId = this.dockerEtmService.dockerService
                .createAndStartContainer(dockerBuilder.build());
        logger.info("Container {} started with id {}", containerName,
                containerId);

        assertTrue(this.dockerEtmService.dockerService
                .existsContainer(containerName));

        logger.info("Stopping container {}", containerName);
        this.dockerEtmService.dockerService.stopDockerContainer(containerId);
        logger.info("Removing container {}", containerName);
        this.dockerEtmService.removeDockerContainer(containerId);
        assertFalse(this.dockerEtmService.dockerService
                .existsContainer(containerName));
    }

}
