package io.elastest.etm.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.elastest.etm.dao.EimConfigRepository;
import io.elastest.etm.model.EimConfig;

@Service
public class EimService {
	private final EimConfigRepository eimConfigRepository;
	private static final Logger logger = LoggerFactory.getLogger(EimService.class);

	@Value("${et.eim.api}")
	public String eimUrl;

	public String eimApiPath = "eim/api";

	public EimService(EimConfigRepository eimConfigRepository) {
		this.eimConfigRepository = eimConfigRepository;
	}

	@Async
	@SuppressWarnings("unchecked")
	public void instrumentalizeSut(EimConfig eimConfig) {
		RestTemplate restTemplate = new RestTemplate();

		Map<String, String> body = new HashMap<>();
		body.put("address", eimConfig.getIp());
		body.put("user", eimConfig.getUser());
		body.put("private_key", eimConfig.getPrivateKey());
		body.put("logstash_ip", eimConfig.getLogstashIp());
		body.put("logstash_port", eimConfig.getLogstashBeatsPort());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		headers.set("X_Broker_Api_Version", "2.12");

		HttpEntity<Map<String, String>> request = new HttpEntity<Map<String, String>>(body, headers);

		try {
			String url = eimUrl + eimApiPath + "/agent";
			logger.debug("Instrumentalizing SuT: " + url);
			Map<String, String> response = restTemplate.postForObject(url, request, Map.class);
			logger.debug("Instrumentalized! Saving agentId into SuT EimConfig");
			eimConfig.setAgentId(response.get("agentId"));
			this.eimConfigRepository.save(eimConfig);
		} catch (Exception e) {
			logger.error("EIM is not started or response is an 500 Internal Server Error");
		}
	}

	@Async
	public void deinstrumentSut(EimConfig eimConfig) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.delete(eimUrl + eimApiPath + "/agent/" + eimConfig.getAgentId());
	}

	@SuppressWarnings("unchecked")
	public String getPublickey() {
		RestTemplate restTemplate = new RestTemplate();
		Map<String, String> publicKeyObj = restTemplate.getForObject(eimUrl + eimApiPath + "/publickey", Map.class);
		return publicKeyObj.get("publickey");
	}
}
