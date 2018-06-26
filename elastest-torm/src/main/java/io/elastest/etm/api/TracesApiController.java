package io.elastest.etm.api;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import io.elastest.etm.model.MonitoringQuery;
import io.elastest.etm.service.ElasticsearchService;
import io.elastest.etm.service.TracesService;
import io.swagger.annotations.ApiParam;

@Controller
public class TracesApiController implements TracesApi {
    public final Logger logger = getLogger(lookup().lookupClass());

    @Autowired
    private TracesService tracesService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    public ResponseEntity<Map<String, Object>> processTrace(
            @Valid @RequestBody Map<String, Object> data) {
        try {
            logger.debug("Processing HTTP trace {}", data.toString());
            this.tracesService.processHttpTrace(data);
            return new ResponseEntity<Map<String, Object>>(data, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<Map<String, Object>>(data,
                    HttpStatus.NOT_ACCEPTABLE);
        }

    }

    public ResponseEntity<List<Map<String, Object>>> searchAllByTerms(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws IOException {
        return new ResponseEntity<List<Map<String, Object>>>(
                elasticsearchService.searchAllByTerms(body), HttpStatus.OK);
    }

    /* ****************************************** */
    /* ****************** Logs ****************** */
    /* ****************************************** */

    public ResponseEntity<List<Map<String, Object>>> searchAllLogs(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws IOException {
        return new ResponseEntity<List<Map<String, Object>>>(
                elasticsearchService.searchAllLogs(body), HttpStatus.OK);
    }

    public ResponseEntity<List<Map<String, Object>>> searchPreviousLogs(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body) {
        return new ResponseEntity<List<Map<String, Object>>>(
                elasticsearchService.getPreviousLogsFromTimestamp(body),
                HttpStatus.OK);
    }

    public ResponseEntity<List<Map<String, Object>>> searchLastLogs(
            @ApiParam(value = "Number of logs to get.", required = true) @PathVariable("size") int size,
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws IOException {
        return new ResponseEntity<List<Map<String, Object>>>(
                elasticsearchService.getLastLogs(body, size), HttpStatus.OK);
    }

    /* ***************************************** */
    /* **************** Metrics **************** */
    /* ***************************************** */

    public ResponseEntity<List<Map<String, Object>>> searchAllMetrics(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws IOException {
        return new ResponseEntity<List<Map<String, Object>>>(
                elasticsearchService.searchAllMetrics(body), HttpStatus.OK);
    }

    public ResponseEntity<List<Map<String, Object>>> searchPreviousMetrics(
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body) {
        return new ResponseEntity<List<Map<String, Object>>>(
                elasticsearchService.getPreviousMetricsFromTimestamp(body),
                HttpStatus.OK);
    }

    public ResponseEntity<List<Map<String, Object>>> searchLastMetrics(
            @ApiParam(value = "Number of Metrics to get.", required = true) @PathVariable("size") int size,
            @ApiParam(value = "Search Request configuration", required = true) @Valid @RequestBody MonitoringQuery body)
            throws IOException {
        return new ResponseEntity<List<Map<String, Object>>>(
                elasticsearchService.getLastMetrics(body, size), HttpStatus.OK);
    }
}
