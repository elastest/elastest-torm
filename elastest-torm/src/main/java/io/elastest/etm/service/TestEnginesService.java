package io.elastest.etm.service;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import io.elastest.epm.client.json.DockerContainerInfo.DockerContainer;
import io.elastest.epm.client.json.DockerContainerInfo.PortInfo;
import io.elastest.epm.client.service.DockerComposeService;

@Service
public class TestEnginesService {
    final Logger logger = getLogger(lookup().lookupClass());

    public DockerComposeService dockerComposeService;
    public DockerEtmService dockerEtmService;

    public List<String> enginesList = new ArrayList<>();
    public List<String> noEnginesList = new ArrayList<>();

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

    @Value("${et.shared.folder}")
    private String sharedFolder;

    public TestEnginesService(DockerComposeService dockerComposeService,
            DockerEtmService dockerEtmService) {
        this.dockerComposeService = dockerComposeService;
        this.dockerEtmService = dockerEtmService;
    }

    public void registerEngines() {
        this.enginesList.add("ece");
        this.enginesList.add("ere"); // It's necessary to auth:
                                     // https://docs.google.com/document/d/1RMMnJO3rA3KRg-q_LRgpmmvSTpaCPsmfAQjs9obVNeU
        this.noEnginesList.add("eim");
    }

    @PostConstruct
    public void init() throws Exception {
        registerEngines();
        for (String engine : this.enginesList) {
            createProject(engine);
        }

        for (String engine : this.noEnginesList) {
            createNoEngineProject(engine);
        }
    }

    @PreDestroy
    public void destroy() {
        for (String engine : this.enginesList) {
            removeProject(engine);
        }
        for (String engine : this.noEnginesList) {
            removeProject(engine);
        }
    }

    public void createProject(String name) {
        String dockerComposeYml = getDockerCompose(name);
        this.createProject(name, dockerComposeYml);
    }

    public void createNoEngineProject(String name) throws Exception {
        String dockerComposeYml = getDockerCompose(name);
        if ("eim".equals(name)) {
            try {
                String mysqlHost = dockerEtmService.getEdmMySqlHost();
                dockerComposeYml = dockerComposeYml.replace("edm-mysql",
                        mysqlHost);
            } catch (Exception e) {
                throw new Exception("Error on get MySQL host", e);
            }
        }
        this.createProject(name, dockerComposeYml);
    }

    public void createProject(String name, String dockerComposeYml) {
        try {
            String path = sharedFolder.endsWith("/") ? sharedFolder
                    : sharedFolder + "/";
            path += "tmp-engines-yml";
            dockerComposeService.createProject(name, dockerComposeYml, path,
                    true);
        } catch (Exception e) {
            logger.error("Exception creating project {}", name, e);
        }
    }

    private void removeProject(String engineName) {
        dockerComposeService.stopAndRemoveProject(engineName);
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
        logger.error("Checking if {} is not already running", engineName);
        if (!isRunning(engineName)) {
            try {
                logger.error("Creating {} instance", engineName);
                dockerComposeService.startProject(engineName, true);
                insertIntoETNetwork(engineName);
                url = getServiceUrl(engineName);
            } catch (IOException e) {
                logger.error("Cannot create {} instance", engineName, e);
            } catch (Exception e) {
                logger.error("{}", e.getMessage());
                logger.error("Stopping service {}", engineName);
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

                logger.debug("Container info: {}", container);

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
                    logger.debug("Url: " + url);
                }
            }

        } catch (Exception e) {
            logger.error("Service url not exist {}", serviceName, e);
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
            logger.info("Service url to check: " + engineUrl);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            int responseCode = huc.getResponseCode();
            up = (responseCode >= 200 && responseCode <= 299);
            if (!up) {
                logger.info("Service no ready at url: " + engineUrl);
                return up;
            }
        } catch (IOException e) {
            return false;
        }

        logger.info("Service ready at url: " + engineUrl);

        return up;
    }

    public void stopInstance(String engineName) {
        try {
            dockerComposeService.stopProject(engineName);
        } catch (IOException e) {
            logger.error("Error while stopping engine {}", engineName);
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
            logger.error("Engine not started or not exist {}", engineName, e);
            return false;
        }
    }

    public List<String> getTestEngines() {
        return enginesList;
    }

}
