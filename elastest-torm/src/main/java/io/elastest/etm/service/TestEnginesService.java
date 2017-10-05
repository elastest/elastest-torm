package io.elastest.etm.service;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import io.elastest.epm.client.json.DockerContainerInfo.DockerContainer;
import io.elastest.epm.client.json.DockerContainerInfo.PortInfo;
import io.elastest.epm.client.service.DockerComposeService;
import io.elastest.etm.model.SupportServiceInstance;

@Service
public class TestEnginesService {

	private Logger log = Logger.getLogger(TestEnginesService.class);

	public DockerComposeService dockerComposeService;

	public List<String> enginesList = new ArrayList<>();

	@Value("${et.test.engines.path}")
	public String ET_TEST_ENGINES_PATH;

	public TestEnginesService(DockerComposeService dockerComposeService) {
		this.dockerComposeService = dockerComposeService;
	}

	public void registerEngines() {
		this.enginesList.add("ece");
		this.enginesList.add("ere"); // It's necessary to auth:
										// https://docs.google.com/document/d/1RMMnJO3rA3KRg-q_LRgpmmvSTpaCPsmfAQjs9obVNeU
	}

	@PostConstruct
	public void init() {
		registerEngines();
		for (String engine : this.enginesList) {
			createProject(engine);
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
		File engineFile;
		String content = "";
		try {
			engineFile = new ClassPathResource(ET_TEST_ENGINES_PATH + engineName + ".yml").getFile();
			content = new String(Files.readAllBytes(engineFile.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public String createInstance(String engineName) {
		String url = "";
		if (!isRunning(engineName)) {
			try {
				dockerComposeService.startProject(engineName);
				url = getServiceUrl(engineName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return url;
	}

	public String getServiceUrl(String serviceName) {
		String url = "";
		try {
			for (DockerContainer container : dockerComposeService.getContainers(serviceName).getContainers()) {

				log.info(container);

				String containerName = serviceName + "_" + serviceName + "_1"; // example:
																				// ece_ece_1
				if (container.getName().equals(containerName)) {
					PortInfo portInfo = container.getPorts().entrySet().iterator().next().getValue().get(0);
					url = "http://" + portInfo.getHostIp() + ":" + portInfo.getHostPort();
					if (serviceName.equals("ere")) {
						url += "/elastest-recommendation-engine";
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
			e.printStackTrace();
		}
	}

	public String getUrlIfIsRunning(String engineName) {
		return getServiceUrl(engineName);
	}

	public Boolean isRunning(String engineName) {
		try {
			for (DockerContainer container : dockerComposeService.getContainers(engineName).getContainers()) {
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
