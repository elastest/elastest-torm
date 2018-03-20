package io.elastest.etm.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.elastest.etm.dao.EimBeatConfigRepository;
import io.elastest.etm.dao.EimConfigRepository;
import io.elastest.etm.dao.EimMonitoringConfigRepository;
import io.elastest.etm.model.EimBeatConfig;
import io.elastest.etm.model.EimConfig;
import io.elastest.etm.model.EimMonitoringConfig;

@Service
public class EimService {
	private static final Logger logger = LoggerFactory.getLogger(EimService.class);

	private final EimConfigRepository eimConfigRepository;
	private final EimMonitoringConfigRepository eimMonitoringConfigRepository;
	private final EimBeatConfigRepository eimBeatConfigRepository;

	@Value("${et.eim.api}")
	public String eimUrl;

	public String eimApiPath = "eim/api";

	public String eimApiUrl;

	public EimService(EimConfigRepository eimConfigRepository,
			EimMonitoringConfigRepository eimMonitoringConfigRepository,
			EimBeatConfigRepository eimBeatConfigRepository) {
		this.eimConfigRepository = eimConfigRepository;
		this.eimMonitoringConfigRepository = eimMonitoringConfigRepository;
		this.eimBeatConfigRepository = eimBeatConfigRepository;
	}

	@PostConstruct
	public void initEimApiUrl() {
		this.eimApiUrl = this.eimUrl + eimApiPath;
	}

	/* ***************** */
	/* **** EIM API **** */
	/* ***************** */

	@SuppressWarnings("unchecked")
	public String getPublickey() {
		RestTemplate restTemplate = new RestTemplate();
		Map<String, String> publicKeyObj = restTemplate.getForObject(eimUrl + eimApiPath + "/publickey", Map.class);
		return publicKeyObj.get("publickey");
	}

	@SuppressWarnings("unchecked")
	public void instrumentalize(EimConfig eimConfig) throws Exception {
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

		String url = this.eimApiUrl + "/agent";
		logger.debug("Instrumentalizing SuT: " + url);
		Map<String, String> response = restTemplate.postForObject(url, request, Map.class);
		logger.debug("Instrumentalized! Saving agentId into SuT EimConfig");
		eimConfig.setAgentId(response.get("agentId"));
		this.eimConfigRepository.save(eimConfig);
	}

	public void deinstrumentalize(EimConfig eimConfig) {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.delete(this.eimApiUrl + "/agent/" + eimConfig.getAgentId());
	}

	@SuppressWarnings("unchecked")
	public void deployBeats(EimConfig eimConfig, EimMonitoringConfig eimMonitoringConfig) throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

		HttpEntity<Map<String, String>> request = new HttpEntity<Map<String, String>>(
				eimMonitoringConfig.getEimMonitoringConfigInApiFormat(), headers);

		String url = this.eimApiUrl + "/agent/" + eimConfig.getAgentId() + "/monitor";
		logger.debug("Activating beats: " + url);
		Map<String, String> response = restTemplate.postForObject(url, request, Map.class);
		if (response.get("monitored").equals("true")) {
			logger.debug("Beats activated!");
		} else {
			throw new Exception("Beats not activated");
		}

	}

	public void unDeployBeats(EimConfig eimConfig) {
		RestTemplate restTemplate = new RestTemplate();

		try {
			String url = this.eimApiUrl + "/agent/" + eimConfig.getAgentId() + "/unmonitor";
			logger.debug("Deactivating beats: " + url);
			restTemplate.delete(this.eimApiUrl + "/agent/" + eimConfig.getAgentId());
		} catch (Exception e) {
			logger.error("Error on Deactivate Beats: not Deactivated", e);
		}
	}

	/* ****************** */
	/* ****** BBDD ****** */
	/* ****************** */

	public EimMonitoringConfig createEimMonitoringConfig(EimMonitoringConfig eimMonitoringConfig) {
		return this.eimMonitoringConfigRepository.save(eimMonitoringConfig);
	}

	public EimMonitoringConfig createEimMonitoringConfigAndChilds(EimMonitoringConfig eimMonitoringConfig) {
		if (eimMonitoringConfig != null) {
			Map<String, EimBeatConfig> beats = eimMonitoringConfig.getBeats();
			eimMonitoringConfig.setBeats(null);
			eimMonitoringConfig = this.eimMonitoringConfigRepository.save(eimMonitoringConfig);
			if (beats != null) {
				for (Map.Entry<String, EimBeatConfig> currentBeat : beats.entrySet()) {
					currentBeat.getValue().setEimMonitoringConfig(eimMonitoringConfig);
					EimBeatConfig savedBeat = this.eimBeatConfigRepository.save(currentBeat.getValue());
					currentBeat.setValue(savedBeat);
				}
				eimMonitoringConfig.setBeats(beats);
			}
		}
		return eimMonitoringConfig;
	}

	/* ****************** */
	/* *** Additional *** */
	/* ****************** */
	@Async
	public void instrumentalizeAndDeployBeats(EimConfig eimConfig, EimMonitoringConfig eimMonitoringConfig) {
		try {
			this.instrumentalize(eimConfig);
			try {
				this.deployBeats(eimConfig, eimMonitoringConfig);
			} catch (Exception e) {
				logger.error("Error on activate Beats: not activated", e);
			}
		} catch (Exception e) {
			logger.error("EIM is not started or response is an 500 Internal Server Error", e);
		}
	}

	@Async
	public void deInstrumentalizeAndUnDeployBeats(EimConfig eimConfig) {
		this.unDeployBeats(eimConfig);
		this.deinstrumentalize(eimConfig);
	}

}
