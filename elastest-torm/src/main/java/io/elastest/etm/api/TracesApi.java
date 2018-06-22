package io.elastest.etm.api;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = "traces")
public interface TracesApi extends EtmApiRoot {

    @RequestMapping(value = "/", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Map<String, Object>> processTrace(@Valid @RequestBody Map<String, Object> data);
}
