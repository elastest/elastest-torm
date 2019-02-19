package io.elastest.etm.api;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.elastest.etm.model.AggregationTree;
import io.elastest.etm.model.LogAnalyzerQuery;
import io.elastest.etm.model.MonitoringQuery;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public interface TracesApi extends EtmApiRoot {

    @RequestMapping(value = "/monitoring/", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Map<String, Object>> processTrace(
            @Valid @RequestBody Map<String, Object> data);

    @ApiOperation(value = "Returns all matching traces.", notes = "Returns all matching traces.", response = Map.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Map.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/byterms", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<Map<String, Object>>> searchAllByTerms(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception;

    /* ****************************************** */
    /* ****************** Logs ****************** */
    /* ****************************************** */

    @ApiOperation(value = "Returns all Logs.", notes = "Returns all Logs.", response = Map.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Map.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/log", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<Map<String, Object>>> searchAllLogs(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception;

    @ApiOperation(value = "Returns all logs until given timestamp.", notes = "Returns all logs until given timestamp.", response = Map.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Map.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/log/previous", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<Map<String, Object>>> searchPreviousLogs(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception;

    @ApiOperation(value = "Returns last N logs.", notes = "Returns last N logs.", response = Map.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Map.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/log/last/{size}", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<Map<String, Object>>> searchLastLogs(
            @ApiParam(value = "Number of logs to get.", required = true) @PathVariable("size") int size,
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception;

    @ApiOperation(value = "Returns Logs Aggregation Tree.", notes = "Returns Logs Aggregation Tree.", response = AggregationTree.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AggregationTree.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/log/tree", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<AggregationTree>> searchLogsAggregationTree(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception;

    @ApiOperation(value = "Returns Logs Levels Tree.", notes = "Returns Logs Levels Tree.", response = AggregationTree.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AggregationTree.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/log/tree/levels", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<AggregationTree>> searchLogsLevelsTree(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception;

    @ApiOperation(value = "Returns Logs Pair comparation.", notes = "Returns Logs Pair comparation.", response = String.class, tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/log/compare", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<String> compareLogsPair(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body,
            @RequestParam(value = "view", required = true) String view,
            @RequestParam(value = "comparison", required = true) String comparison)
            throws Exception;

    /* ***************************************** */
    /* **************** Metrics **************** */
    /* ***************************************** */

    @ApiOperation(value = "Returns all Metrics.", notes = "Returns all Metrics.", response = Map.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Map.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/metric", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<Map<String, Object>>> searchAllMetrics(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception;

    @ApiOperation(value = "Returns all Metrics until given timestamp.", notes = "Returns all Metrics until given timestamp.", response = Map.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Map.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/metric/previous", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<Map<String, Object>>> searchPreviousMetrics(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception;

    @ApiOperation(value = "Returns last N Metrics.", notes = "Returns last N Metrics.", response = Map.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Map.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/metric/last/{size}", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<Map<String, Object>>> searchLastMetrics(
            @ApiParam(value = "Number of Metrics to get.", required = true) @PathVariable("size") int size,
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception;

    @ApiOperation(value = "Returns Metrics Aggregation Tree.", notes = "Returns Metrics Aggregation Tree.", response = AggregationTree.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AggregationTree.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/metric/tree", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<AggregationTree>> searchMetricsAggregationTree(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws Exception;

    /* ********************************************* */
    /* **************** LogAnalyzer **************** */
    /* ********************************************* */

    @ApiOperation(value = "Returns the result of search LogAnalyzer query.", notes = "Returns the result of search LogAnalyzer query.", response = Map.class, responseContainer = "List", tags = {
            "Monitoring", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Map.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/loganalyzer", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<Map<String, Object>>> searchLogAnalyzerQuery(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody LogAnalyzerQuery body)
            throws Exception;
}
