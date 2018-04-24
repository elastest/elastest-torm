package io.elastest.etm.service;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;

import org.apache.commons.io.IOUtils;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;

import io.elastest.epm.client.DockerContainer.DockerBuilder;
import io.elastest.epm.client.DockerContainer;
import io.elastest.epm.client.service.DockerService;

@Service
public class PcapService {
    DockerService dockerService;
    private Logger log = Logger.getLogger(PcapService.class);

    String dockpcapImage = "elastest/etm-dockpcap";
    String containerPrefix = "dockpcap_";
    String dockpcapNetworkPrefix = "container:sut_";

    Map<String, DockerContainer> containersList;

    public PcapService(DockerService dockerService) {
        this.dockerService = dockerService;
    }

    @PostConstruct
    public void init() {
        this.containersList = new HashMap<>();
    }

    public Boolean startPcap(String execId) {
        String containerName = this.getPcapContainerName(execId);
        if (!containersList.containsKey(containerName)) {
            DockerBuilder dockerBuilder = DockerContainer
                    .dockerBuilder(dockpcapImage, containerName)
                    .network(dockpcapNetworkPrefix + execId);
            DockerContainer dockerContainer = dockerBuilder.build();
            containersList.put(containerName, dockerContainer);

            try {
                dockerService.startAndWaitContainer(dockerContainer);
                return true;
            } catch (InterruptedException e) {
                log.error("Pcap not started {}", execId, e);
            }
        }

        return false;
    }

    public String getPcapContainerName(String execId) {
        return containerPrefix + execId;
    }

    public Boolean stopPcap(String execId) {
        String containerName = this.getPcapContainerName(execId);
        try {
            dockerService.stopAndRemoveContainer(containerName);
            containersList.remove(containerName);
            return true;
        } catch (Exception e) {
            log.error("Pcap not stopped {}", execId, e);
        }
        return false;
    }

    public void stopContainerAndSendFileTo(String execId,
            ServletOutputStream servletOutputStream) {
        String containerName = containerPrefix + execId;
        try {
            InputStream inputStream = dockerService
                    .getFileFromContainer(containerName, "/data/capture.pcap");

            // remove bad characters
            byte[] removedChars = new byte[350];
            new DataInputStream(inputStream).readFully(removedChars);
            log.info("Removed chars: " + new String(removedChars));

            // Send file
            IOUtils.copy(inputStream, servletOutputStream);

            // And stop dockpcap container
            stopPcap(execId);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
