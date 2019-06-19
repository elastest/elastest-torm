/*
 * (C) Copyright 2017-2019 ElasTest (http://elastest.io/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.elastest.epm.client.test.integration;

import static io.elastest.epm.client.DockerContainer.dockerBuilder;
import static java.lang.invoke.MethodHandles.lookup;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.PortBinding;

import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.epm.client.service.DockerService;
import io.elastest.epm.client.service.EpmServiceClient;
import io.elastest.epm.client.service.FilesService;
import io.elastest.epm.client.service.ShellService;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for Docker service.
 *
 * @author Boni Garcia (boni.garcia@urjc.es)
 * @since 0.0.1
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { DockerService.class, ShellService.class,
        EpmServiceClient.class, FilesService.class }, webEnvironment = RANDOM_PORT)
@Tag("integration")
@DisplayName("Integration test for Docker Service")
@EnableAutoConfiguration
@PropertySources({ @PropertySource(value = "classpath:epm-client.properties") })
public class DockerIntegrationTest {

    final Logger log = getLogger(lookup().lookupClass());
    
    String image = "elastest/test-etm-test1";

    @Autowired
    private DockerService dockerService;

    @Test
    @DisplayName("Ask for Chrome to Docker")
    void testDocker() throws Exception {
        // Test data (input)
        String imageId = "selenium/standalone-chrome-debug:3.5.3";
        log.debug("Starting Hub with image {}", imageId);

        String containerName = dockerService
                .generateContainerName("hub-for-test-");

        DockerBuilder dockerBuilder = dockerBuilder(imageId)
                .containerName(containerName);

        String exposedPort = String.valueOf(4444);
        dockerBuilder.exposedPorts(Arrays.asList(exposedPort));

        String exposedHubVncPort = String.valueOf(5900);
        dockerBuilder.exposedPorts(Arrays.asList(exposedHubVncPort));
        // portBindings
        Map<String, List<PortBinding>> portBindings = new HashMap<>();
        portBindings.put(String.valueOf(exposedPort),
                Arrays.asList(PortBinding.of("0.0.0.0",
                        Integer.toString(dockerService.findRandomOpenPort()))));

        portBindings.put(String.valueOf(exposedHubVncPort),
                Arrays.asList(PortBinding.of("0.0.0.0",
                        Integer.toString(dockerService.findRandomOpenPort()))));

        dockerBuilder.portBindings(portBindings);
        dockerService.pullImage(imageId);
        dockerService.createAndStartContainer(dockerBuilder.build());

        // Assertions
        assertTrue(dockerService.existsContainer(containerName));

        // Tear down
        log.debug("Stoping Hub");
        dockerService.stopAndRemoveContainer(containerName);
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
        String containerName = "testContainer" + System.currentTimeMillis() % 1000;
        log.info("Starting container {}", containerName);

        DockerBuilder dockerBuilder = new DockerBuilder(image);
        dockerBuilder.containerName(containerName);

        String containerId = this.dockerService
                .createAndStartContainer(dockerBuilder.build());
        log.info("Container {} started with id {}", containerName,
                containerId);

        assertTrue(this.dockerService.existsContainer(containerName));

        log.info("Stopping container {}", containerName);
        this.dockerService.stopDockerContainer(containerId);
        log.info("Removing container {}", containerName);
        this.dockerService.removeDockerContainer(containerId);
        assertFalse(this.dockerService.existsContainer(containerName));
    }

}
