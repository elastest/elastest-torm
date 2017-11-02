package io.elastest.etm.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "/context")
public interface EtmServiceDiscoveryApi extends EtmApiRoot {

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

    @ApiOperation(value="Returns the Logstash ip",notes="Returns the Logstash ip.",response=String.class,tags={"CONTEXT",})@ApiResponses(value={

    @ApiResponse(code = 200, message = "Successful operation", response = String.class),
			@ApiResponse(code = 404, message = "Resource not found") })
	@RequestMapping(value = "/context/logstash/ip", produces = { "text/plain" }, method = RequestMethod.GET)
	public ResponseEntity<String> getLogstashIp();

    @ApiOperation(value = "Returns the logstash info", notes = "Returns the logstash info.", response = Map.class, tags = {
            "CONTEXT", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Map.class),
            @ApiResponse(code = 404, message = "Resource not found") })
    @RequestMapping(value = "/context/logstash/info", produces = {
            "application/json" }, method = RequestMethod.GET)
    public ResponseEntity<Map<String, String>> getLogstashInfo();

}
