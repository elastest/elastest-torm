package io.elastest.etm.api;

import java.util.HashMap;
import java.util.List;
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
import io.elastest.etm.service.CoreServiceInfo;
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
            return new ResponseEntity<Map<String, String>>(
                    tSSInstanceContextMap, HttpStatus.OK);
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

        ContextInfo context = etmContextService.getContextInfo();
        logstashInfo.put("logstashIp", context.getLogstashIp());

        logstashInfo.put("logstashTcpHost", context.getLogstashTcpHost());
        logstashInfo.put("logstashTcpPort", context.getLogstashTcpPort());
        logstashInfo.put("logstashBeatsHost", context.getLogstashBeatsHost());
        logstashInfo.put("logstashBeatsPort", context.getLogstashBeatsPort());

        logstashInfo.put("logstashBindedTcpHost",
                context.getLogstashBindedTcpHost());
        logstashInfo.put("logstashBindedTcpPort",
                context.getLogstashBindedTcpPort());
        logstashInfo.put("logstashBindedBeatsHost",
                context.getLogstashBindedBeatsHost());
        logstashInfo.put("logstashBindedBeatsPort",
                context.getLogstashBindedBeatsPort());

        logstashInfo.put("logstashHttpApiUrl", context.getLogstashHttpUrl());
        logstashInfo.put("logstashHttpPort", context.getLogstashHttpPort());

        return new ResponseEntity<Map<String, String>>(logstashInfo,
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<HelpInfo> getHelpInfo() {
        return new ResponseEntity<HelpInfo>(etmContextService.getHelpInfo(),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<CoreServiceInfo>> getCoreServicesInfo() {
        return new ResponseEntity<List<CoreServiceInfo>>(
                etmContextService.getCoreServicesInfo(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getAllCoreServiceLogs(
            @ApiParam(value = "Name of Core Service.", required = true) @PathVariable("coreServiceName") String coreServiceName) {
        String logs = null;
        try {
            logs = etmContextService.getAllCoreServiceLogs(coreServiceName,
                    false);
            return new ResponseEntity<String>(logs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>(logs, HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<String> getAllCoreServiceLogsAndFollow(
            @ApiParam(value = "Name of Core Service.", required = true) @PathVariable("coreServiceName") String coreServiceName) {
        String logs = null;
        try {
            logs = etmContextService.getAllCoreServiceLogs(coreServiceName,
                    true);
            return new ResponseEntity<String>(logs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>(logs, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> getSomeCoreServiceLogs(
            @ApiParam(value = "Name of Core Service.", required = true) @PathVariable("coreServiceName") String coreServiceName,
            @ApiParam(value = "Number of logs to get.", required = true) @PathVariable("amount") int amount) {
        String logs = null;
        try {
            logs = etmContextService.getSomeCoreServiceLogs(coreServiceName,
                    amount, false);
            return new ResponseEntity<String>(logs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>(logs, HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<String> getSomeCoreServiceLogsAndFollow(
            @ApiParam(value = "Name of Core Service.", required = true) @PathVariable("coreServiceName") String coreServiceName,
            @ApiParam(value = "Number of logs to get.", required = true) @PathVariable("amount") int amount) {
        String logs = null;
        try {
            logs = etmContextService.getSomeCoreServiceLogs(coreServiceName,
                    amount, true);
            return new ResponseEntity<String>(logs, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>(logs, HttpStatus.NOT_FOUND);
        }
    }

    /* ******************** */
    /* *** Log Analyzer *** */
    /* ******************** */

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
