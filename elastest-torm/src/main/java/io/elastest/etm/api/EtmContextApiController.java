package io.elastest.etm.api;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.ContextInfo;
import io.elastest.etm.model.HelpInfo;
import io.elastest.etm.model.LogAnalyzerConfig;
import io.elastest.etm.model.LogAnalyzerConfig.BasicAttLogAnalyzerConfig;
import io.elastest.etm.service.EsmService;
import io.elastest.etm.service.EtmContextService;
import io.swagger.annotations.ApiParam;

@Controller
public class EtmContextApiController implements EtmContextApi {

    @Autowired
    EsmService esmService;
    @Autowired
    EtmContextService etmContextService;
    @Value("${et.public.host}")
    public String publicHost;

    @Override
    public ResponseEntity<Map<String, String>> getTSSInstanceContext(
            @PathVariable("tSSInstanceId") String tSSInstanceId) {
        Map<String, String> tSSInstanceContextMap = esmService
                .getTSSInstanceContext(tSSInstanceId, true, true);
        if (tSSInstanceContextMap != null) {
            return new ResponseEntity<Map<String, String>>(tSSInstanceContextMap,
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<ContextInfo> getContextInfo() {
        return new ResponseEntity<ContextInfo>(
                etmContextService.getContextInfo(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getElasticsearchApiUrl() {
        return new ResponseEntity<String>(
                "http://" + publicHost + ":37000/elasticsearch", HttpStatus.OK);
        // return new ResponseEntity<String>(elasticsearchApi, HttpStatus.OK);
        // TODO
    }

    @Override
    public ResponseEntity<String> getRabbitHost() {
        return new ResponseEntity<String>(publicHost + ":37006", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getLogstashIp() {
        return new ResponseEntity<String>(publicHost, HttpStatus.OK);
        // TODO real logstash ip with proxy
    }

    @Override
    public ResponseEntity<Map<String, String>> getLogstashInfo() {
        Map<String, String> logstashInfo = new HashMap<>();
        // TODO real logstash ip with proxy
        logstashInfo.put("logstashIp", publicHost);
        logstashInfo.put("logstashBeatsPort", "5044");
        logstashInfo.put("logstashHttpPort", "5003");

        return new ResponseEntity<Map<String, String>>(logstashInfo,
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<HelpInfo> getHelpInfo() {
        return new ResponseEntity<HelpInfo>(etmContextService.getHelpInfo(),
                HttpStatus.OK);
    }

    @JsonView(BasicAttLogAnalyzerConfig.class)
    public ResponseEntity<LogAnalyzerConfig> saveLogAnalyzerConfig(
            @ApiParam(value = "Data to create a LogAnalizerConfig", required = true) @Valid @RequestBody LogAnalyzerConfig body) {

        LogAnalyzerConfig logAnalyzerConfig = this.etmContextService
                .saveLogAnalyzerConfig(body);
        return new ResponseEntity<LogAnalyzerConfig>(logAnalyzerConfig,
                HttpStatus.OK);
    }

    @JsonView(BasicAttLogAnalyzerConfig.class)
    public ResponseEntity<LogAnalyzerConfig> getLogAnalyzerConfig() {
        LogAnalyzerConfig logAnalyzerConfig = this.etmContextService
                .getLogAnalyzerConfig();
        return new ResponseEntity<LogAnalyzerConfig>(logAnalyzerConfig,
                HttpStatus.OK);
    }

}
