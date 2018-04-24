package io.elastest.etm.test.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.elastest.etm.ElasTestTormApp;
import io.elastest.etm.service.DockerService2;
import io.elastest.etm.service.TJobStoppedException;
import io.elastest.etm.utils.UtilTools;

@RunWith(JUnitPlatform.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ElasTestTormApp.class, webEnvironment = WebEnvironment.RANDOM_PORT)
public class PcapApiItTest {
    private Logger log = Logger.getLogger(PcapApiItTest.class);

    @Autowired
    TestRestTemplate httpClient;

    @Autowired
    DockerService2 dockerService;

    @Test
    public void startStopPcapTest()
            throws InterruptedException, TJobStoppedException {
        String execId = UtilTools.generateUniqueId();

        log.info("Starting sut");
        String sutContainerId = this.dockerService.runDockerContainer(
                "elastest/test-etm-sut3", null, "sut_" + execId, null, null,
                null);

        log.info("Starting pcap");
        boolean started = this.startPcap(execId);
        Thread.sleep(5000);
        assertTrue(started);

        log.info("Stopping pcap");
        this.stopPcap(execId);

        log.info("Stopping sut");
        this.dockerService.stopDockerContainer(sutContainerId);
        this.dockerService.removeDockerContainer(sutContainerId);

        String containerName = this.getPcapContainerName(execId);
        assertFalse(this.dockerService.existsContainer(containerName));
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
