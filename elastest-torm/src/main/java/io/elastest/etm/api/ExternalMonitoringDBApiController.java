package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.external.ExternalElasticsearch;
import io.elastest.etm.model.external.ExternalMonitoringDB.ExternalMonitoringDBView;
import io.elastest.etm.model.external.ExternalPrometheus;
import io.elastest.etm.service.ExternalMonitoringDBService;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class ExternalMonitoringDBApiController
        implements ExternalMonitoringDBApi {

    @Autowired
    ExternalMonitoringDBService externalMonitoringDBService;

    @JsonView(ExternalMonitoringDBView.class)
    public ResponseEntity<Boolean> checkExternalElasticsearchConnection(
            @ApiParam(value = "External Elasticsearch", required = true) @Valid @RequestBody ExternalElasticsearch body) {
        return new ResponseEntity<Boolean>(externalMonitoringDBService
                .checkExtElasticsearchConnection(body), HttpStatus.OK);
    }

    @JsonView(ExternalMonitoringDBView.class)
    public ResponseEntity<Boolean> checkExternalPrometheusConnection(
            @ApiParam(value = "External Prometheus", required = true) @Valid @RequestBody ExternalPrometheus body) {
        return new ResponseEntity<Boolean>(externalMonitoringDBService
                .checkExternalPrometheusConnection(body), HttpStatus.OK);
    }

    @JsonView(ExternalMonitoringDBView.class)
    public ResponseEntity<ExternalElasticsearch> getExternalElasticsearchById(
            @ApiParam(value = "ES id.", required = true) @PathVariable("externalESId") Long externalESId) {
        return new ResponseEntity<ExternalElasticsearch>(
                externalMonitoringDBService.getExternalElasticsearchById(
                        externalESId),
                HttpStatus.OK);
    }

    @JsonView(ExternalMonitoringDBView.class)
    public ResponseEntity<ExternalPrometheus> getExternalPrometheusById(
            @ApiParam(value = "Prometheus id.", required = true) @PathVariable("externalPrometheusId") Long externalPrometheusId) {
        return new ResponseEntity<ExternalPrometheus>(
                externalMonitoringDBService.getExternalPrometheusById(
                        externalPrometheusId),
                HttpStatus.OK);
    }
}
