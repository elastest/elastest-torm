package io.elastest.etm.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.elasticsearch.action.search.SearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.model.LogTrace;
import io.elastest.etm.model.MonitoringQuery;
import io.elastest.etm.model.SutSpecification;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public interface TracesApi extends EtmApiRoot {

    @RequestMapping(value = "/", produces = { "application/json" }, consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Map<String, Object>> processTrace(
            @Valid @RequestBody Map<String, Object> data);

    @ApiOperation(value = "Returns the result of ElasticSearch query.", notes = "Returns the result of ElasticSearch query.", response = SearchResponse.class, tags = {
            "Elasticsearch", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SutSpecification.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/monitoring/log", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<List<LogTrace>> searchLog(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws IOException;
}
