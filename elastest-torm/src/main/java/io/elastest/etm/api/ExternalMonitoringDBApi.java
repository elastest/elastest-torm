package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.model.external.ExternalElasticsearch;
import io.elastest.etm.model.external.ExternalPrometheus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Api(value = "/externalmonitoringdb")
public interface ExternalMonitoringDBApi extends EtmApiRoot {

    @ApiOperation(value = "Gets connection status of an External ElasticSearch", notes = "Gets connection status of an External ElasticSearch", response = Boolean.class, tags = {
            "External Monitoring DB", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/externalmonitoringdb/elasticsearch/connection", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Boolean> checkExternalElasticsearchConnection(
            @ApiParam(value = "External Elasticsearch", required = true) @Valid @RequestBody ExternalElasticsearch body);

    @ApiOperation(value = "Gets connection status of an External Prometheus", notes = "Gets connection status of an External Prometheus", response = Boolean.class, tags = {
            "External Monitoring DB", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/externalmonitoringdb/prometheus/connection", consumes = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Boolean> checkExternalPrometheusConnection(
            @ApiParam(value = "External Prometheus", required = true) @Valid @RequestBody ExternalPrometheus body);

    @ApiOperation(value = "Returns an ExternalElasticsearch DB Monitoring by Id", notes = "Returns an ExternalElasticsearch DB Monitoring by Id.", response = ExternalElasticsearch.class, tags = {
            "External Monitoring DB", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "External Elasticsearch returned successfully", response = ExternalElasticsearch.class) })
    @RequestMapping(value = "/externalmonitoringdb/elasticsearch/{externalESId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<ExternalElasticsearch> getExternalElasticsearchById(
            @ApiParam(value = "ES id.", required = true) @PathVariable("externalESId") Long externalESId);

    @ApiOperation(value = "Returns an ExternalPrometheus DB Monitoring by Id", notes = "Returns an ExternalPrometheus DB Monitoring by Id.", response = ExternalPrometheus.class, tags = {
            "External Monitoring DB", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "External Prometheus returned successfully", response = ExternalPrometheus.class) })
    @RequestMapping(value = "/externalmonitoringdb/prometheus/{externalPrometheusId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<ExternalPrometheus> getExternalPrometheusById(
            @ApiParam(value = "Prometheus id.", required = true) @PathVariable("externalPrometheusId") Long externalPrometheusId);

}
