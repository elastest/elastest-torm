package io.elastest.etm.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

	@Value("${et.eim.api}")
	public String eimUrl;

	public String eimApiPath = "eim/api";

	public EimService(EimConfigRepository eimConfigRepository) {
		this.eimConfigRepository = eimConfigRepository;
	}

	@Async
	@SuppressWarnings("unchecked")
	public void instrumentSut(EimConfig eimConfig) {
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
			Map<String, String> response = restTemplate.postForObject(eimUrl + eimApiPath + "/agent", request,
					Map.class);
			eimConfig.setAgentId(response.get("agentId"));
			this.eimConfigRepository.save(eimConfig);
		} catch (Exception e) {
			System.err.println("EIM is not started");
//			e.printStackTrace();
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
