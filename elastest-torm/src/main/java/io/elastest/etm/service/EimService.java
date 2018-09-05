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
import io.elastest.etm.model.EimMonitoringConfig.ApiEimMonitoringConfig;
import io.elastest.etm.model.EimMonitoringConfig.BeatsStatusEnum;
import io.elastest.etm.utils.UtilsService;

@Service
public class EimService {
    private static final Logger logger = LoggerFactory
            .getLogger(EimService.class);

    private final EimConfigRepository eimConfigRepository;
    private final EimMonitoringConfigRepository eimMonitoringConfigRepository;
    private final EimBeatConfigRepository eimBeatConfigRepository;
    private final EtPluginsService etPluginsService;
    private final UtilsService utilsService;
    private DatabaseSessionManager dbmanager;

    @Value("${exec.mode}")
    public String execMode;

    @Value("${et.eim.api}")
    public String eimUrl;

    public String eimApiPath = "eim/api";

    public String eimApiUrl;

    public EimService(EimConfigRepository eimConfigRepository,
            EimMonitoringConfigRepository eimMonitoringConfigRepository,
            EimBeatConfigRepository eimBeatConfigRepository,
            EtPluginsService testEnginesService, UtilsService utilsService,
            DatabaseSessionManager dbmanager) {
        this.eimConfigRepository = eimConfigRepository;
        this.eimMonitoringConfigRepository = eimMonitoringConfigRepository;
        this.eimBeatConfigRepository = eimBeatConfigRepository;
        this.etPluginsService = testEnginesService;
        this.utilsService = utilsService;
        this.dbmanager = dbmanager;
    }

    @PostConstruct
    public void initEimApiUrl() {
        this.eimApiUrl = this.eimUrl + eimApiPath;
    }

    private void startEimIfNotStarted() {
        // Only in normal mode
        String eimProjectName = "eim";
        if (utilsService.isElastestMini()
                && !etPluginsService.isRunning(eimProjectName)) {
            etPluginsService.startEngineOrUniquePlugin(eimProjectName);

            // Init URL
            this.eimUrl = etPluginsService.getEtPluginUrl(eimProjectName);
            this.eimUrl = this.eimUrl.endsWith("/") ? this.eimUrl
                    : this.eimUrl + "/";
            this.eimApiPath = this.eimApiPath.startsWith("/")
                    ? this.eimApiPath.substring(1)
                    : this.eimApiPath;
            this.initEimApiUrl();
            logger.debug("EIM is now ready at {}", this.eimApiUrl);
        }
    }

    /* ***************** */
    /* **** EIM API **** */
    /* ***************** */

    @SuppressWarnings("unchecked")
    public String getPublickey() {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> publicKeyObj = restTemplate
                .getForObject(eimUrl + eimApiPath + "/publickey", Map.class);
        return publicKeyObj.get("publickey");
    }

    @SuppressWarnings("unchecked")
    public EimConfig instrumentalize(EimConfig eimConfig) throws Exception {
        this.startEimIfNotStarted();
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> body = new HashMap<>();
        body.put("address", eimConfig.getIp());
        body.put("user", eimConfig.getUser());
        if (eimConfig.getPassword() != null
                && !eimConfig.getPassword().isEmpty()) {
            body.put("password", eimConfig.getPassword());
        }
        body.put("private_key", eimConfig.getPrivateKey());

        // Dev
        if (!utilsService.isEtmInContainer()
                || utilsService.isEtmInDevelopment()) {
            body.put("logstash_ip", eimConfig.getLogstashIp());
            body.put("logstash_port", eimConfig.getLogstashBeatsPort());
        } else { // Prod
            body.put("logstash_ip", eimConfig.getLogstashBindedBeatsHost());
            body.put("logstash_port", eimConfig.getLogstashBindedBeatsPort());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(
                Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X_Broker_Api_Version", "2.12");

        HttpEntity<Map<String, String>> request = new HttpEntity<Map<String, String>>(
                body, headers);

        String url = this.eimApiUrl + "/agent";
        logger.debug("Instrumentalizing SuT: {}", url);
        Map<String, String> response = restTemplate.postForObject(url, request,
                Map.class);
        logger.debug("Instrumentalized! Saving agentId into SuT EimConfig");
        eimConfig.setAgentId(response.get("agentId"));
        return this.eimConfigRepository.save(eimConfig);
    }

    public EimConfig deinstrumentalize(EimConfig eimConfig) {
        this.startEimIfNotStarted();

        RestTemplate restTemplate = new RestTemplate();
        restTemplate
                .delete(this.eimApiUrl + "/agent/" + eimConfig.getAgentId());
        eimConfig.setAgentId(null);
        return this.eimConfigRepository.save(eimConfig);

    }

    @SuppressWarnings("unchecked")
    public void deployBeats(EimConfig eimConfig,
            EimMonitoringConfig eimMonitoringConfig) throws Exception {
        eimMonitoringConfig = this.updateEimMonitoringConfigBeatsStatus(
                eimMonitoringConfig, BeatsStatusEnum.ACTIVATING);
        this.startEimIfNotStarted();

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(
                Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X_Broker_Api_Version", "2.12");

        HttpEntity<ApiEimMonitoringConfig> request = new HttpEntity<ApiEimMonitoringConfig>(
                eimMonitoringConfig.getEimMonitoringConfigInApiFormat(),
                headers);

        String url = this.eimApiUrl + "/agent/" + eimConfig.getAgentId()
                + "/monitor";
        logger.debug("Activating beats: {} {}", url, request);

        Map<String, Object> response = restTemplate.postForObject(url, request,
                Map.class);
        if (response.get("monitored").toString().equals("true")) {
            eimMonitoringConfig = this.updateEimMonitoringConfigBeatsStatus(
                    eimMonitoringConfig, BeatsStatusEnum.ACTIVATED);
            logger.debug("Beats activated successfully!");
        } else {
            eimMonitoringConfig = this.updateEimMonitoringConfigBeatsStatus(
                    eimMonitoringConfig, BeatsStatusEnum.DEACTIVATED);
            throw new Exception("Beats not activated");
        }

    }

    public void unDeployBeats(EimConfig eimConfig,
            EimMonitoringConfig eimMonitoringConfig) {
        eimMonitoringConfig = this.updateEimMonitoringConfigBeatsStatus(
                eimMonitoringConfig, BeatsStatusEnum.DEACTIVATING);
        this.startEimIfNotStarted();

        String url = this.eimApiUrl + "/agent/" + eimConfig.getAgentId()
                + "/unmonitor";
        logger.debug("Deactivating beats: {}", url);

        RestTemplate restTemplate = new RestTemplate();

        try {
            restTemplate.delete(url);
            eimMonitoringConfig = this.updateEimMonitoringConfigBeatsStatus(
                    eimMonitoringConfig, BeatsStatusEnum.DEACTIVATED);
        } catch (Exception e) {
            logger.error("Error on Deactivate Beats: not Deactivated", e);
            eimMonitoringConfig = this.updateEimMonitoringConfigBeatsStatus(
                    eimMonitoringConfig, BeatsStatusEnum.ACTIVATED);
        }
    }

    /* ****************** */
    /* ****** BBDD ****** */
    /* ****************** */

    public EimMonitoringConfig createEimMonitoringConfig(
            EimMonitoringConfig eimMonitoringConfig) {
        return this.eimMonitoringConfigRepository.save(eimMonitoringConfig);
    }

    public EimMonitoringConfig createEimMonitoringConfigAndChilds(
            EimMonitoringConfig eimMonitoringConfig) {
        if (eimMonitoringConfig != null) {
            Map<String, EimBeatConfig> beats = eimMonitoringConfig.getBeats();
            eimMonitoringConfig.setBeats(null);
            eimMonitoringConfig = this.eimMonitoringConfigRepository
                    .save(eimMonitoringConfig);
            if (beats != null) {
                for (Map.Entry<String, EimBeatConfig> currentBeat : beats
                        .entrySet()) {
                    currentBeat.getValue()
                            .setEimMonitoringConfig(eimMonitoringConfig);
                    EimBeatConfig savedBeat = this.eimBeatConfigRepository
                            .save(currentBeat.getValue());
                    currentBeat.setValue(savedBeat);
                }
                eimMonitoringConfig.setBeats(beats);
            }
        }
        return eimMonitoringConfig;
    }

    public EimMonitoringConfig updateEimMonitoringConfigBeatsStatus(
            EimMonitoringConfig eimMonitoringConfig, BeatsStatusEnum status) {
        eimMonitoringConfig.setBeatsStatus(status);
        return this.eimMonitoringConfigRepository.save(eimMonitoringConfig);
    }

    /* ****************** */
    /* *** Additional *** */
    /* ****************** */
    @Async
    public void instrumentalizeAndDeployBeats(EimConfig eimConfig,
            EimMonitoringConfig eimMonitoringConfig) throws Exception {
        try {
            dbmanager.bindSession();
            eimMonitoringConfig = this.updateEimMonitoringConfigBeatsStatus(
                    eimMonitoringConfig, BeatsStatusEnum.ACTIVATING);

            eimConfig = this.instrumentalize(eimConfig);
            try {
                this.deployBeats(eimConfig, eimMonitoringConfig);
            } catch (Exception e) {
                dbmanager.unbindSession();
                throw new Exception("Error on activate Beats: not activated",
                        e);
            }
        } catch (Exception e) {
            dbmanager.unbindSession();
            throw new Exception(
                    "EIM is not started or response is a 500 Internal Server Error",
                    e);
        }
        dbmanager.unbindSession();
    }

    @Async
    public void deInstrumentalizeAndUnDeployBeats(EimConfig eimConfig,
            EimMonitoringConfig eimMonitoringConfig) {
        dbmanager.bindSession();
        eimMonitoringConfig = this.updateEimMonitoringConfigBeatsStatus(
                eimMonitoringConfig, BeatsStatusEnum.DEACTIVATING);
        this.unDeployBeats(eimConfig, eimMonitoringConfig);
        this.deinstrumentalize(eimConfig);
        dbmanager.unbindSession();
    }

}
