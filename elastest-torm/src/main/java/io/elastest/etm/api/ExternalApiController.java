package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.model.external.ExternalId;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;
import io.elastest.etm.model.external.ExternalTestCase;
import io.elastest.etm.service.ExternalService;
import io.elastest.etm.service.TJobService;
import io.swagger.annotations.ApiParam;

@RestController
public class ExternalApiController implements ExternalApi {

    private TJobService tJobService;
    private ExternalService externalService;

    public ExternalApiController(TJobService tJobService,
            ExternalService externalService) {
        this.tJobService = tJobService;
        this.externalService = externalService;
    }

    public ExternalJob executeExternalTJob(
            @ApiParam(value = "ExternalJob object that needs to create", required = true) @Valid @RequestBody ExternalJob body) {

        try {
            return externalService.executeExternalTJob(body);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void finishExternalJob(
            @ApiParam(value = "ExternalJob configuration", required = true) @Valid @RequestBody ExternalJob body) {

        tJobService.finishExternalTJobExecution(body);
    }

    /* *************************************************/
    /* *************** ExternalProject *************** */
    /* *************************************************/

    public ResponseEntity<List<ExternalProject>> getAllExternalProjects() {
        return new ResponseEntity<List<ExternalProject>>(
                externalService.getAllExternalProjects(), HttpStatus.OK);
    }

    public ResponseEntity<List<ExternalProject>> getAllExternalProjectsByType(
            @ApiParam(value = "Type of the project.", required = true) @PathVariable("type") TypeEnum type) {
        return new ResponseEntity<List<ExternalProject>>(
                externalService.getAllExternalProjectsByType(type),
                HttpStatus.OK);
    }

    public ResponseEntity<ExternalProject> getExternalProjectById(ExternalId id,
            Model model) {
        return new ResponseEntity<ExternalProject>(
                externalService.getExternalProjectById(id), HttpStatus.OK);
    }

    /* **************************************************/
    /* *************** ExternalTestCase *************** */
    /* **************************************************/

    public ResponseEntity<List<ExternalTestCase>> getAllExternalTestCases() {
        return new ResponseEntity<List<ExternalTestCase>>(
                externalService.getAllExternalTestCases(), HttpStatus.OK);
    }

    public ResponseEntity<ExternalTestCase> getExternalTestCaseById(
            ExternalId id, Model model) {
        return new ResponseEntity<ExternalTestCase>(
                externalService.getExternalTestCaseById(id), HttpStatus.OK);
    }
}
