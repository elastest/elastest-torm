package io.elastest.etm.api;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTJob.ExternalTJobView;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.model.external.ExternalTJobExecution.ExternalTJobExecutionView;
import io.elastest.etm.model.external.ExternalTestCase;
import io.elastest.etm.model.external.ExternalTestCase.ExternalTestCaseView;
import io.elastest.etm.model.external.ExternalTestExecution;
import io.elastest.etm.model.external.ExternalTestExecution.ExternalTestExecutionView;
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
            return externalService.executeExternalTJob(body);
    }

    @Override
    public void finishExternalJob(
            @ApiParam(value = "ExternalJob configuration", required = true) @Valid @RequestBody ExternalJob body) {
            externalService.endExtTJobExecution(body);
    }

    @Override
    public ExternalJob isReadyTJobForExternalExecution(
            @ApiParam(value = "TJob Execution id.", required = true) @Valid @PathVariable Long tJobExecId) {
        return externalService.isReadyTJobForExternalExecution(tJobExecId);
    }

    @Override
    public ResponseEntity<String> getElasTestVersion() {
        return new ResponseEntity<String>(externalService.getElasTestVersion(),
                HttpStatus.OK);
    }

    /* *************************************************/
    /* *************** ExternalProject *************** */
    /* *************************************************/

    @JsonView({ ExternalProjectView.class })
    public ResponseEntity<List<ExternalProject>> getAllExternalProjects() {
        return new ResponseEntity<List<ExternalProject>>(
                externalService.getAllExternalProjects(), HttpStatus.OK);
    }

    @JsonView({ ExternalProjectView.class })
    public ResponseEntity<List<ExternalProject>> getAllExternalProjectsByType(
            @ApiParam(value = "Type of the project.", required = true) @PathVariable("type") TypeEnum type) {
        return new ResponseEntity<List<ExternalProject>>(
                externalService.getAllExternalProjectsByType(type),
                HttpStatus.OK);
    }

    @JsonView({ ExternalProjectView.class })
    public ResponseEntity<ExternalProject> getExternalProjectById(
            @ApiParam(value = "Id of an External Project.", required = true) @PathVariable("projectId") Long projectId) {
        return new ResponseEntity<ExternalProject>(
                externalService.getExternalProjectById(projectId),
                HttpStatus.OK);
    }

    /* **************************************************/
    /* ***************** ExternalTJob ***************** */
    /* **************************************************/

    @JsonView({ ExternalTJobView.class })
    public ResponseEntity<List<ExternalTJob>> getAllExternalTJobs() {
        return new ResponseEntity<List<ExternalTJob>>(
                externalService.getAllExternalTJobs(), HttpStatus.OK);
    }

    @JsonView({ ExternalTJobView.class })
    public ResponseEntity<ExternalTJob> getExternalTJobById(
            @ApiParam(value = "Id of an External TJob.", required = true) @PathVariable("tjobId") Long tjobId) {
        return new ResponseEntity<ExternalTJob>(
                externalService.getExternalTJobById(tjobId), HttpStatus.OK);
    }

    @JsonView({ ExternalTJobView.class })
    public ResponseEntity<ExternalTJob> createExternalTJob(
            @ApiParam(value = "TJob object to create.", required = true) @Valid @RequestBody ExternalTJob body) {
        return new ResponseEntity<ExternalTJob>(
                externalService.createExternalTJob(body), HttpStatus.OK);
    }

    @JsonView({ ExternalTJobView.class })
    public ResponseEntity<ExternalTJob> modifyExternalTJob(
            @ApiParam(value = "TJob object that needs to modify.", required = true) @Valid @RequestBody ExternalTJob body) {
        return new ResponseEntity<ExternalTJob>(
                externalService.modifyExternalTJob(body), HttpStatus.OK);
    }

    /* **************************************************/
    /* *************** ExternalTJobExec *************** */
    /* **************************************************/

    @JsonView({ ExternalTJobExecutionView.class })
    public ResponseEntity<List<ExternalTJobExecution>> getAllExternalTJobExecs() {
        return new ResponseEntity<List<ExternalTJobExecution>>(
                externalService.getAllExternalTJobExecs(), HttpStatus.OK);
    }

    @JsonView({ ExternalTJobExecutionView.class })
    public ResponseEntity<List<ExternalTJobExecution>> getExternalTJobExecsByExternalTJobId(
            @ApiParam(value = "Id of an External TJob.", required = true) @PathVariable("tJobId") Long tJobId) {
        return new ResponseEntity<List<ExternalTJobExecution>>(
                externalService.getExternalTJobExecsByExternalTJobId(tJobId),
                HttpStatus.OK);
    }

    @JsonView({ ExternalTJobExecutionView.class })
    public ResponseEntity<ExternalTJobExecution> getExternalTJobExecById(
            @ApiParam(value = "Id of an External TJob Exec.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {
        return new ResponseEntity<ExternalTJobExecution>(
                externalService.getExternalTJobExecById(tJobExecId),
                HttpStatus.OK);
    }

    @JsonView({ ExternalTJobExecutionView.class })
    public ResponseEntity<ExternalTJobExecution> createExternalTJobExecution(
            @ApiParam(value = "Object with the External TJob Execution data to create.", required = true) @Valid @RequestBody ExternalTJobExecution body) {
        return new ResponseEntity<ExternalTJobExecution>(
                externalService.createExternalTJobExecution(body),
                HttpStatus.OK);
    }

    @JsonView({ ExternalTJobExecutionView.class })
    public ResponseEntity<ExternalTJobExecution> createExternalTJobExecutionByExternalTJobId(
            @ApiParam(value = "Id of an External TJob.", required = true) @PathVariable("tJobId") Long tJobId,
            @ApiParam(value = "", required = true) @Valid @RequestBody String body) {
        return new ResponseEntity<ExternalTJobExecution>(externalService
                .createExternalTJobExecutionByExternalTJobId(tJobId),
                HttpStatus.OK);
    }

    @JsonView({ ExternalTJobExecutionView.class })
    public ResponseEntity<ExternalTJobExecution> modifyExternalTJobExecution(
            @ApiParam(value = "TJob Execution object that needs to modify.", required = true) @Valid @RequestBody ExternalTJobExecution body) {
        return new ResponseEntity<ExternalTJobExecution>(
                externalService.modifyExternalTJobExec(body), HttpStatus.OK);
    }

    public ResponseEntity<List<TJobExecutionFile>> getExternalTJobExecutionFiles(
            @ApiParam(value = "TJobExec Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {

        ResponseEntity<List<TJobExecutionFile>> response;
        try {
            response = new ResponseEntity<List<TJobExecutionFile>>(
                    externalService.getExternalTJobExecutionFilesUrls(
                            tJobExecId),
                    HttpStatus.OK);
        } catch (Exception e) {
            response = new ResponseEntity<List<TJobExecutionFile>>(
                    new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }

    @JsonView({ ExternalTJobExecutionView.class })
    public ResponseEntity<List<ExternalTestExecution>> getAllExternalTJobExecExternalTestExecutions(
            @ApiParam(value = "TJobExec Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {
        return new ResponseEntity<List<ExternalTestExecution>>(
                externalService.getTJobExecTestExecutions(tJobExecId),
                HttpStatus.OK);
    }

    /* **************************************************/
    /* *************** ExternalTestCase *************** */
    /* **************************************************/

    @JsonView({ ExternalTestCaseView.class })
    public ResponseEntity<List<ExternalTestCase>> getAllExternalTestCases() {
        return new ResponseEntity<List<ExternalTestCase>>(
                externalService.getAllExternalTestCases(), HttpStatus.OK);
    }

    @JsonView({ ExternalTestCaseView.class })
    public ResponseEntity<ExternalTestCase> getExternalTestCaseById(
            @ApiParam(value = "Id of an External Test Case.", required = true) @PathVariable("caseId") Long caseId) {
        return new ResponseEntity<ExternalTestCase>(
                externalService.getExternalTestCaseById(caseId), HttpStatus.OK);
    }

    /* *************************************************/
    /* ************ ExternalTestExecution ************ */
    /* *************************************************/

    @JsonView({ ExternalTestExecutionView.class })
    public ResponseEntity<List<ExternalTestExecution>> getAllExternalTestExecutions() {
        return new ResponseEntity<List<ExternalTestExecution>>(
                externalService.getAllExternalTestExecutions(), HttpStatus.OK);
    }

    @JsonView({ ExternalTestExecutionView.class })
    public ResponseEntity<ExternalTestExecution> getExternalTestExecutionById(
            @ApiParam(value = "Id of an External Test Execution.", required = true) @PathVariable("execId") Long execId) {
        return new ResponseEntity<ExternalTestExecution>(
                externalService.getExternalTestExecutionById(execId),
                HttpStatus.OK);
    }

    @JsonView({ ExternalTestExecutionView.class })
    public ResponseEntity<ExternalTestExecution> createExternalTestExecution(
            @ApiParam(value = "Object with the External Test Execution data to create.", required = true) @Valid @RequestBody ExternalTestExecution body) {
        return new ResponseEntity<ExternalTestExecution>(
                externalService.createExternalTestExecution(body),
                HttpStatus.OK);
    }

    @JsonView({ ExternalTestExecutionView.class })
    public ResponseEntity<ExternalTestExecution> modifyExternalTJob(
            @ApiParam(value = "ExternalTestExecution object that needs to modify.", required = true) @Valid @RequestBody ExternalTestExecution body) {
        return new ResponseEntity<ExternalTestExecution>(
                externalService.modifyExternalTestExecution(body),
                HttpStatus.OK);
    }

    public ResponseEntity<List<ExternalTestExecution>> getExternalTestExecutionsByExternalTJobExec(
            @ApiParam(value = "TJobExec Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId) {
        return new ResponseEntity<List<ExternalTestExecution>>(externalService
                .getExternalTestExecutionsByExternalTJobExec(tJobExecId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ExternalTestExecution> getExternalTestExecutionByExecutionId(
            @ApiParam(value = "ID of the Execution.", required = true) @PathVariable("externalId") Integer externalId,
            @ApiParam(value = "External TJob Exec to bind.", required = true) @PathVariable("exTJobExecId") Long exTJobExecId) {
        return new ResponseEntity<ExternalTestExecution>(
                externalService.setExternalTJobExecToTestExecutionByExecutionId(
                        externalId, exTJobExecId),
                HttpStatus.OK);
    }
}
