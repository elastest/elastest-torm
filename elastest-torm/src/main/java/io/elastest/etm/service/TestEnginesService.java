package io.elastest.etm.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import io.elastest.epm.client.json.DockerContainerInfo.DockerContainer;
import io.elastest.epm.client.json.DockerContainerInfo.PortInfo;
import io.elastest.epm.client.service.DockerComposeService;

@Service
public class TestEnginesService {

    private final static Logger log = LoggerFactory
            .getLogger(TestEnginesService.class);

    public DockerComposeService dockerComposeService;
    public DockerEtmService dockerEtmService;

    public List<String> enginesList = new ArrayList<>();

    @Value("${elastest.docker.network}")
    public String network;

    @Value("${et.test.engines.path}")
    public String ET_TEST_ENGINES_PATH;

    @Value("${exec.mode")
    public String execmode;

    @Value("${et.public.host}")
    public String etPublicHost;

    @Value("${et.edm.mysql.host}")
    public String etEdmMysqlHost;

    @Value("${et.edm.mysql.port}")
    public String etEdmMysqlPort;

    public TestEnginesService(DockerComposeService dockerComposeService,
            DockerEtmService dockerEtmService) {
        this.dockerComposeService = dockerComposeService;
        this.dockerEtmService = dockerEtmService;
    }

    public void registerEngines() {
        this.enginesList.add("ece");
        this.enginesList.add("ere"); // It's necessary to auth:
                                     // https://docs.google.com/document/d/1RMMnJO3rA3KRg-q_LRgpmmvSTpaCPsmfAQjs9obVNeU
    }

    @PostConstruct
    public void init() {
        if (!execmode.equals("normal")) {
            registerEngines();
            for (String engine : this.enginesList) {
                createProject(engine);
            }
        }
    }

    public void createProject(String name) {
        String dockerComposeYml = getDockerCompose(name);
        try {
            dockerComposeService.createProject(name, dockerComposeYml);
        } catch (Exception e) {
            log.error("Exception creating project {}", name, e);
        }
    }

    public String getDockerCompose(String engineName) {
        String content = "";
        try {
            InputStream inputStream = getClass().getResourceAsStream(
                    "/" + ET_TEST_ENGINES_PATH + engineName + ".yml");
            content = IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    public String createInstance(String engineName) {
        String url = "";
        log.error("Checking if {} is not already running", engineName);
        if (!isRunning(engineName)) {
            try {
                log.error("Creating {} instance", engineName);
                dockerComposeService.startProject(engineName);
                insertIntoETNetwork(engineName);
                url = getServiceUrl(engineName);
            } catch (IOException e) {
                log.error("Cannot create {} instance", engineName, e);
            } catch (Exception e) {
                log.error("{}", e.getMessage());
                log.error("Stopping service {}", engineName);
                this.stopInstance(engineName);
            }
        } else {
            url = getServiceUrl(engineName);
        }
        return url;
    }

    public void insertIntoETNetwork(String engineName) throws Exception {
        try {
            for (DockerContainer container : dockerComposeService
                    .getContainers(engineName).getContainers()) {
                try {
                    dockerEtmService.dockerService.insertIntoNetwork(network,
                            container.getName());
                } catch (DockerException | InterruptedException
                        | DockerCertificateException e) {
                    throw new Exception(
                            "Error on insert container " + container.getName()
                                    + " into " + network + " network");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getServiceUrl(String serviceName) {
        String url = "";
        try {
            for (DockerContainer container : dockerComposeService
                    .getContainers(serviceName).getContainers()) {

                log.debug("Container info: {}", container);

                String containerName = serviceName + "_" + serviceName + "_1"; // example:
                                                                               // ece_ece_1
                if (container.getName() != null
                        && container.getName().equals(containerName)) {
                    String ip = etPublicHost;

                    String port = "";
                    for (Entry<String, List<PortInfo>> portList : container
                            .getPorts().entrySet()) {
                        if (portList.getValue() != null) {
                            if (ip.equals("localhost")) {
                                port = portList.getKey().split("/")[0];
                                ip = dockerEtmService.dockerService
                                        .getContainerIpByNetwork(containerName,
                                                network);
                            } else {
                                port = portList.getValue().get(0).getHostPort();
                            }
                            break;

                        }
                    }

                    if ("".equals(port)) {
                        throw new Exception("Port not found");
                    }

                    url = "http://" + ip + ":" + port;

                    if (serviceName.equals("ere")) {
                        url += "/ere-app";
                    }
                    log.debug("Url: " + url);
                }
            }

        } catch (Exception e) {
            log.error("Service url not exist {}", serviceName, e);
        }
        return url;
    }

    public boolean checkIfEngineUrlIsUp(String engineName) {
        String url = getServiceUrl(engineName);
        return checkIfUrlIsUp(url);
    }

    public boolean checkIfUrlIsUp(String engineUrl) {
        boolean up = false;
        URL url;
        try {
            url = new URL(engineUrl);
            log.info("Service url to check: " + engineUrl);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            int responseCode = huc.getResponseCode();
            up = (responseCode >= 200 && responseCode <= 299);
            if (!up) {
                log.info("Service no ready at url: " + engineUrl);
                return up;
            }
        } catch (IOException e) {
            return false;
        }

        log.info("Service ready at url: " + engineUrl);

        return up;
    }

    public void stopInstance(String engineName) {
        try {
            dockerComposeService.stopProject(engineName);
        } catch (IOException e) {
            log.error("Error while stopping engine {}", engineName);
        }
    }

    public String getUrlIfIsRunning(String engineName) {
        return getServiceUrl(engineName);
    }

    public Boolean isRunning(String engineName) {
        try {
            for (DockerContainer container : dockerComposeService
                    .getContainers(engineName).getContainers()) {
                String containerName = engineName + "_" + engineName + "_1";
                if (container.getName().equals(containerName)) {
                    return container.isRunning();
                }
            }
            return false;

        } catch (Exception e) {
            log.error("Engine not started or not exist {}", engineName, e);
            return false;
        }
    }

    public List<String> getTestEngines() {
        return enginesList;
    }

}
