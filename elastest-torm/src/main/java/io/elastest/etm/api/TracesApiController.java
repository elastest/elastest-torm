package io.elastest.etm.api;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Trace.TraceView;
import io.elastest.etm.service.TracesService;

@Controller
public class TracesApiController implements TracesApi {
    public final Logger logger = getLogger(lookup().lookupClass());

    @Autowired
    private TracesService tracesService;

    @JsonView({ TraceView.class })
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
}
