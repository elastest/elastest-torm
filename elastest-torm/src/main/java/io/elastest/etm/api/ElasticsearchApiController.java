package io.elastest.etm.api;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import io.elastest.etm.service.AbstractMonitoringService;
import io.elastest.etm.service.ElasticsearchService;
import io.swagger.annotations.ApiParam;

@Controller
public class ElasticsearchApiController implements ElasticsearchApi {
    public final Logger logger = getLogger(lookup().lookupClass());

    private AbstractMonitoringService monitoringService;

    public ElasticsearchApiController(
            AbstractMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    public ResponseEntity<List<String>> getAllIndices() throws Exception {
        if (monitoringService instanceof ElasticsearchService) {
            return new ResponseEntity<List<String>>(
                    ((ElasticsearchService) monitoringService).getAllIndices(),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<List<String>>(
                    HttpStatus.METHOD_NOT_ALLOWED);
        }

    }

    public ResponseEntity<List<String>> getIndicesByHealth(
            @ApiParam(value = "Id of the SUT executed.", required = true) @PathVariable("health") String health)
            throws Exception {
        if (monitoringService instanceof ElasticsearchService) {
            return new ResponseEntity<List<String>>(
                    ((ElasticsearchService) monitoringService)
                            .getIndicesByHealth(health),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<List<String>>(
                    HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

    public ResponseEntity<Boolean> deleteIndicesByHealth(
            @ApiParam(value = "Id of the SUT executed.", required = true) @PathVariable("health") String health)
            throws Exception {
        if (monitoringService instanceof ElasticsearchService) {
            return new ResponseEntity<Boolean>(
                    ((ElasticsearchService) monitoringService)
                            .deleteIndicesByHealth(health),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<Boolean>(HttpStatus.METHOD_NOT_ALLOWED);
        }
    }

}
