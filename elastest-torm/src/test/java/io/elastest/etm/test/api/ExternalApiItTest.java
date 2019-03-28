package io.elastest.etm.test.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import io.elastest.etm.api.model.ExternalJob;

@RunWith(JUnitPlatform.class)
public class ExternalApiItTest extends EtmApiItTest {

    static final Logger log = LoggerFactory.getLogger(ExternalApiItTest.class);

    @Test
    public void testCreateExternalJob() {
        log.info("Start the test testCreateTJob");

        String requestJson = "{ \"jobName\" : \"ExternalJobName\" }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(requestJson,
                headers);

        log.info("POST /api/external/tjob");
        ResponseEntity<ExternalJob> response = httpClient
                .postForEntity("/api/external/tjob", entity, ExternalJob.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.warn("Error creating external Job: " + response);
        }

        log.info("TJob created:" + response.getBody());

        assertAll("Validating tJob Properties",
                () -> assertTrue(response.getBody().getJobName()
                        .equals("ExternalJobName")),
                () -> assertNotNull(response.getBody().gettJobExecId()));

    }

}
