package io.elastest.etm.api;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.model.ContextInfo;
import io.elastest.etm.model.HelpInfo;
import io.elastest.etm.model.LogAnalyzerConfig;
import io.elastest.etm.service.CoreServiceInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "/context")
public interface EtmContextApi extends EtmApiRoot {

    @ApiOperation(value = "Returns the env variables for a provided service instance", notes = "Returns the env variables for a provided service instance.", response = Map.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Map.class),
            @ApiResponse(code = 404, message = "Resource not found") })
    @RequestMapping(value = "/context/tss/{tSSInstanceId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> getTSSInstanceContext(
            @ApiParam(value = "tSSInstanceId", required = true) @PathVariable(value = "tSSInstanceId", required = true) String tSSInstanceId);

    @ApiOperation(value = "Returns the Elasticsearch API url", notes = "Returns the Elasticsearch API url.", response = String.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Resource not found") })
    @RequestMapping(value = "/context/elasticsearch/api", produces = {
            "text/plain" }, method = RequestMethod.GET)
    public ResponseEntity<String> getElasticsearchApiUrl();

    @ApiOperation(value = "Returns the Rabbit Host", notes = "Returns the Rabbit Host.", response = String.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Resource not found") })
    @RequestMapping(value = "/context/ws-host", produces = {
            "text/plain" }, method = RequestMethod.GET)
    public ResponseEntity<String> getRabbitHost();

    @ApiOperation(value = "Returns the Logstash ip", notes = "Returns the Logstash ip.", response = String.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {

            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 404, message = "Resource not found") })
    @RequestMapping(value = "/context/logstash/ip", produces = {
            "text/plain" }, method = RequestMethod.GET)
    public ResponseEntity<String> getLogstashIp();

    @ApiOperation(value = "Returns the logstash info", notes = "Returns the logstash info.", response = Map.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Map.class),
            @ApiResponse(code = 404, message = "Resource not found") })
    @RequestMapping(value = "/context/logstash/info", produces = {
            "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> getLogstashInfo();

    @ApiOperation(value = "Return relevant information about the ElasTest services", notes = "Return relevant information about the ElasTest services.", response = ContextInfo.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ContextInfo.class),
            @ApiResponse(code = 400, message = "Information not found.") })
    @RequestMapping(value = "/context/services/info", produces = {
            "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<ContextInfo> getContextInfo();

    @ApiOperation(value = "Return help information about ElasTest", notes = "Return information such as the version of ElasTest components.", response = HelpInfo.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = HelpInfo.class),
            @ApiResponse(code = 400, message = "Information not found.") })
    @RequestMapping(value = "/context/help/info", produces = {
            "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<HelpInfo> getHelpInfo();

    @ApiOperation(value = "Return Core Services information about ElasTest", notes = "Return information such as the version of ElasTest components.", response = CoreServiceInfo.class, responseContainer = "List", tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = CoreServiceInfo.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Information not found.") })
    @RequestMapping(value = "/context/coreservices/info", produces = {
            "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<List<CoreServiceInfo>> getCoreServicesInfo();

    @ApiOperation(value = "Return All Logs of a Core Service", notes = "Return All Logs of a Core Service.", response = String.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 400, message = "Information not found.") })
    @RequestMapping(value = "/context/coreservices/{coreServiceName}/logs", produces = {
            "text/plain" }, method = RequestMethod.GET)
    public ResponseEntity<String> getAllCoreServiceLogs(
            @ApiParam(value = "Name of Core Service.", required = true) @PathVariable("coreServiceName") String coreServiceName);

    @ApiOperation(value = "Return All Logs of a Core Service and follow", notes = "Return All Logs of a Core Service and follow.", response = String.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 400, message = "Information not found.") })
    @RequestMapping(value = "/context/coreservices/{coreServiceName}/logs/follow", produces = {
            "text/plain" }, method = RequestMethod.GET)
    public ResponseEntity<String> getAllCoreServiceLogsAndFollow(
            @ApiParam(value = "Name of Core Service.", required = true) @PathVariable("coreServiceName") String coreServiceName);

    @ApiOperation(value = "Return Some Logs of a Core Service", notes = "Return Some Logs of a Core Service.", response = String.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 400, message = "Information not found.") })
    @RequestMapping(value = "/context/coreservices/{coreServiceName}/logs/{amount}", produces = {
            "text/plain" }, method = RequestMethod.GET)
    public ResponseEntity<String> getSomeCoreServiceLogs(
            @ApiParam(value = "Name of Core Service.", required = true) @PathVariable("coreServiceName") String coreServiceName,
            @ApiParam(value = "Number of logs to get.", required = true) @PathVariable("amount") int amount);

    @ApiOperation(value = "Return Some Logs of a Core Service and follow", notes = "Return Some Logs of a Core Service and follow.", response = String.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 400, message = "Information not found.") })
    @RequestMapping(value = "/context/coreservices/{coreServiceName}/logs/{amount}/follow", produces = {
            "text/plain" }, method = RequestMethod.GET)
    public ResponseEntity<String> getSomeCoreServiceLogsAndFollow(
            @ApiParam(value = "Name of Core Service.", required = true) @PathVariable("coreServiceName") String coreServiceName,
            @ApiParam(value = "Number of logs to get.", required = true) @PathVariable("amount") int amount);

    @ApiOperation(value = "Return Logs of a Core Service since timestamp", notes = "Return Some Logs of a Core Service since timestamp.", response = String.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 400, message = "Information not found.") })
    @RequestMapping(value = "/context/coreservices/{coreServiceName}/logs/since/{since}", produces = {
            "text/plain" }, method = RequestMethod.GET)
    public ResponseEntity<String> getCoreServiceLogsSince(
            @ApiParam(value = "Name of Core Service.", required = true) @PathVariable("coreServiceName") String coreServiceName,
            @ApiParam(value = "Since timestamp.", required = true) @PathVariable("since") Long since);

    /* ***************** */
    /* LogAnalyzerConfig */
    /* ***************** */

    @ApiOperation(value = "Creates or modify a LogAnalyzerConfig", notes = "Create or modify a LogAnalyzerConfig", response = LogAnalyzerConfig.class, tags = {
            "LogAnalyzerConfig", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = LogAnalyzerConfig.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/loganalyzerconfig", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<LogAnalyzerConfig> saveLogAnalyzerConfig(
            @ApiParam(value = "Data to create a LogAnalizerConfig", required = true) @Valid @RequestBody LogAnalyzerConfig body);

    @ApiOperation(value = "Returns a LogAnalyzerConfig", notes = "Returns a LogAnalyzerConfig", response = LogAnalyzerConfig.class, tags = {
            "LogAnalyzerConfig", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = LogAnalyzerConfig.class),
            @ApiResponse(code = 400, message = "Information not found.") })
    @RequestMapping(value = "/loganalyzerconfig", produces = {
            "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<LogAnalyzerConfig> getLogAnalyzerConfig();

}
