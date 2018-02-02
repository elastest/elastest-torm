package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.model.external.ExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution;
import io.elastest.etm.service.ExternalService;
import io.swagger.annotations.ApiParam;

@RestController
public class ExternalApiController implements ExternalApi {

    private ExternalService externalService;

    public ExternalApiController(ExternalService externalService) {
        this.externalService = externalService;
    }

    @Override
    public ExternalJob execTJobFromExternalTJob(
            @ApiParam(value = "ExternalJob object that needs to create", required = true) @Valid @RequestBody ExternalJob body) {
        try {
            return externalService.executeExternalTJob(body);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void finishExternalJob(
            @ApiParam(value = "ExternalJob configuration", required = true) @Valid @RequestBody ExternalJob body) {
        try {
            externalService.endExtTJobExecution(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ExternalJob isReadyTJobForExternalExecution(
            @ApiParam(value = "TJob Execution id.", required = true) @Valid @PathVariable Long tJobExecId) {
        return externalService.isReadyTJobForExternalExecution(tJobExecId);
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

    public ResponseEntity<ExternalProject> getExternalProjectById(
            @ApiParam(value = "Id of an External Project.", required = true) @PathVariable("projectId") Long projectId) {
        return new ResponseEntity<ExternalProject>(
                externalService.getExternalProjectById(projectId),
                HttpStatus.OK);
    }

    /* **************************************************/
    /* ***************** ExternalTJob ***************** */
    /* **************************************************/

    public ResponseEntity<List<ExternalTJob>> getAllExternalTJobs() {
        return new ResponseEntity<List<ExternalTJob>>(
                externalService.getAllExternalTJobs(), HttpStatus.OK);
    }

    public ResponseEntity<ExternalTJob> getExternalTJobById(
            @ApiParam(value = "Id of an External TJob.", required = true) @PathVariable("tjobId") Long tjobId) {
        return new ResponseEntity<ExternalTJob>(
                externalService.getExternalTJobById(tjobId), HttpStatus.OK);
    }

    public ResponseEntity<ExternalTJob> modifyExternalTJob(
            @ApiParam(value = "TJob object that needs to modify.", required = true) @Valid @RequestBody ExternalTJob body) {
        return new ResponseEntity<ExternalTJob>(
                externalService.modifyExternalTJob(body), HttpStatus.OK);
    }

    /* **************************************************/
    /* *************** ExternalTJobExec *************** */
    /* **************************************************/

    public ResponseEntity<List<ExternalTJobExecution>> getAllExternalTJobExecs() {
        return new ResponseEntity<List<ExternalTJobExecution>>(
                externalService.getAllExternalTJobExecs(), HttpStatus.OK);
    }

    public ResponseEntity<ExternalTJobExecution> getExternalTJobExecById(
            @ApiParam(value = "Id of an External TJob Exec.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {
        return new ResponseEntity<ExternalTJobExecution>(
                externalService.getExternalTJobExecById(tJobExecId),
                HttpStatus.OK);
    }

    public ResponseEntity<ExternalTJobExecution> createExternalTJobExecution(
            @ApiParam(value = "Object with the External TJob Execution data to create.", required = true) @Valid @RequestBody ExternalTJobExecution body) {
        return new ResponseEntity<ExternalTJobExecution>(
                externalService.createExternalTJobExecution(body),
                HttpStatus.OK);
    }

    /* **************************************************/
    /* *************** ExternalTestCase *************** */
    /* **************************************************/

    public ResponseEntity<List<ExternalTestCase>> getAllExternalTestCases() {
        return new ResponseEntity<List<ExternalTestCase>>(
                externalService.getAllExternalTestCases(), HttpStatus.OK);
    }

    public ResponseEntity<ExternalTestCase> getExternalTestCaseById(
            @ApiParam(value = "Id of an External Test Case.", required = true) @PathVariable("caseId") Long caseId) {
        return new ResponseEntity<ExternalTestCase>(
                externalService.getExternalTestCaseById(caseId), HttpStatus.OK);
    }

    /* *************************************************/
    /* ************ ExternalTestExecution ************ */
    /* *************************************************/

    public ResponseEntity<List<ExternalTestExecution>> getAllExternalTestExecutions() {
        return new ResponseEntity<List<ExternalTestExecution>>(
                externalService.getAllExternalTestExecutions(), HttpStatus.OK);
    }

    public ResponseEntity<ExternalTestExecution> getExternalTestExecutionById(
            @ApiParam(value = "Id of an External Test Execution.", required = true) @PathVariable("execId") Long execId) {
        return new ResponseEntity<ExternalTestExecution>(
                externalService.getExternalTestExecutionById(execId),
                HttpStatus.OK);
    }

    public ResponseEntity<ExternalTestExecution> createExternalTestExecution(
            @ApiParam(value = "Object with the External Test Execution data to create.", required = true) @Valid @RequestBody ExternalTestExecution body) {
        return new ResponseEntity<ExternalTestExecution>(
                externalService.createExternalTestExecution(body),
                HttpStatus.OK);
    }
}
