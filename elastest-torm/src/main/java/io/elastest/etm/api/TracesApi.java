package io.elastest.etm.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.model.LogTrace;
import io.elastest.etm.model.MonitoringQuery;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public interface TracesApi extends EtmApiRoot {

    @RequestMapping(value = "/", produces = { "application/json" }, consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Map<String, Object>> processTrace(
            @Valid @RequestBody Map<String, Object> data);

    @ApiOperation(value = "Returns all Logs.", notes = "Returns all Logs.", response = LogTrace.class, responseContainer = "List", tags = {
            "Elasticsearch", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LogTrace.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/log", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<LogTrace>> searchLog(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws IOException;

    @ApiOperation(value = "Returns all logs until given timestamp.", notes = "Returns all logs until given timestamp.", response = LogTrace.class, responseContainer = "List", tags = {
            "Elasticsearch", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LogTrace.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/log/previous", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<LogTrace>> searchPreviousLog(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws IOException;

    @ApiOperation(value = "Returns last N logs.", notes = "Returns last N logs.", response = LogTrace.class, responseContainer = "List", tags = {
            "Elasticsearch", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LogTrace.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/log/last/{size}", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<LogTrace>> searchLastLogs(
            @ApiParam(value = "Number of logs to get.", required = true) @PathVariable("size") int size,
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws IOException;
}
