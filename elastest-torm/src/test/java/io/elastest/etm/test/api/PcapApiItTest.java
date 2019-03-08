package io.elastest.etm.test.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.etm.platform.service.DockerEtmService;
import io.elastest.etm.test.IntegrationBaseTest;
import io.elastest.etm.utils.UtilTools;

@RunWith(JUnitPlatform.class)
public class PcapApiItTest extends IntegrationBaseTest {
    private Logger log = Logger.getLogger(PcapApiItTest.class);

    @Autowired
    TestRestTemplate httpClient;

    @Autowired
    DockerEtmService dockerEtmService;

    @Test
    public void startStopPcapTest() throws Exception {
        String execId = UtilTools.generateUniqueId();

        log.info("Starting sut");

        String imageName = "elastest/test-etm-sut3";
        DockerBuilder dockerBuilder = new DockerBuilder(imageName);
        dockerBuilder.containerName("sut_" + execId);

        this.dockerEtmService.dockerService.pullImage(imageName);
        String sutContainerId = this.dockerEtmService.dockerService
                .createAndStartContainerWithPull(dockerBuilder.build(), false,
                        true);

        log.info("Starting pcap");
        boolean started = this.startPcap(execId);
        Thread.sleep(5000);
        assertTrue(started);

        log.info("Stopping pcap");
        this.stopPcap(execId);

        log.info("Stopping sut");
        this.dockerEtmService.dockerService.stopDockerContainer(sutContainerId);
        this.dockerEtmService.removeDockerContainer(sutContainerId);

        String containerName = this.getPcapContainerName(execId);
        assertFalse(this.dockerEtmService.dockerService
                .existsContainer(containerName));
    }

    @Test
    public void stopFailsTest() {
        this.stopPcap("dummy");
    }

    public boolean startPcap(String execId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        String body = execId;
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Boolean> response = httpClient.postForEntity("/api/pcap",
                entity, boolean.class);
        return response.getBody();
    }

    public void stopPcap(String execId) {
        httpClient.delete("/api/pcap/" + execId);
    }

    public String getPcapContainerName(String execId) {
        return httpClient.getForEntity("/api/pcap/" + execId, String.class)
                .getBody();
    }
}
