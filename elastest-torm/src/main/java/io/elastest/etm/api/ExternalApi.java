package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.model.TJobExecution;
import io.elastest.etm.model.TJobExecutionFile;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTJobExecution;
import io.elastest.etm.model.external.ExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "external")
public interface ExternalApi extends EtmApiExternalRoot {

    @ApiOperation(value = "Create new TJob associated with an external Job", notes = "The association is based on the "
            + "name of the external Job received. The Project and the TJob that will be created will have the same name "
            + "as the one received as a parameter. If a Project or Job already exists with the received name, a new one will not be created.", response = ExternalJob.class, tags = {
                    "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ExternalJob.class),
            @ApiResponse(code = 405, message = "Invalid input", response = ExternalJob.class) })
    @RequestMapping(value = "/tjob", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ExternalJob execTJobFromExternalTJob(
            @ApiParam(value = "Object with the external Job Data (the name).", required = true) @Valid @RequestBody ExternalJob body);

    @ApiOperation(value = "Return an ExternalJob object with the necessary information to continue the external execution of the Job.", notes = "", response = ExternalJob.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ExternalJob.class),
            @ApiResponse(code = 405, message = "Invalid input", response = ExternalJob.class) })
    @RequestMapping(value = "/tjob/{tJobExecId}", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.GET)
    ExternalJob isReadyTJobForExternalExecution(
            @ApiParam(value = "TJob Execution id.", required = true) @Valid @PathVariable Long tJobExecId);

    @ApiOperation(value = "Receives the completion signal of an External Job", notes = "Sets the execution of TJob in the Completed state.", tags = {
            "External", })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/tjob", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.PUT)
    void finishExternalJob(
            @ApiParam(value = "Object with the id of the TJob to update the state.", required = true) @Valid @RequestBody ExternalJob body);

    @ApiOperation(value = "Returns the ElasTest version.", notes = "Returns the ElasTest version.", tags = {
            "External", })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 404, message = "Invalid input") })
    @RequestMapping(value = "/elastest/version", produces = {
            "text/plain" }, method = RequestMethod.GET)
    ResponseEntity<String> getElasTestVersion();

    /* *************************************************/
    /* *************** ExternalProject *************** */
    /* *************************************************/

    @ApiOperation(value = "Returns all External Projects", notes = "Returns all External Projects.", response = ExternalProject.class, responseContainer = "List", tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalProject.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "project", method = RequestMethod.GET)
    ResponseEntity<List<ExternalProject>> getAllExternalProjects();

    @ApiOperation(value = "Returns all External Projects By Service Type", notes = "Returns all External Projects By Service Type.", response = ExternalProject.class, responseContainer = "List", tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalProject.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "project/type/{type}", method = RequestMethod.GET)
    ResponseEntity<List<ExternalProject>> getAllExternalProjectsByType(
            @ApiParam(value = "Type of the project.", required = true) @PathVariable("type") TypeEnum type);

    @ApiOperation(value = "Return an External Projects By Id", notes = "Returns an External Projects By Id.", response = ExternalProject.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalProject.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "project/{projectId}", method = RequestMethod.GET)
    ResponseEntity<ExternalProject> getExternalProjectById(
            @ApiParam(value = "Id of an External Project.", required = true) @PathVariable("projectId") Long projectId);

    /* **************************************************/
    /* ***************** ExternalTJob ***************** */
    /* **************************************************/
    @ApiOperation(value = "Returns all External TJobs", notes = "Returns all External TJobs.", response = ExternalTJob.class, responseContainer = "List", tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTJob.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "extjob", method = RequestMethod.GET)
    ResponseEntity<List<ExternalTJob>> getAllExternalTJobs();

    @ApiOperation(value = "Return an External TJob By Id", notes = "Returns an External TJob By Id.", response = ExternalTJob.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTJob.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "extjob/{tjobId}", method = RequestMethod.GET)
    ResponseEntity<ExternalTJob> getExternalTJobById(
            @ApiParam(value = "Id of an External TJob.", required = true) @PathVariable("tjobId") Long tjobId);

    @ApiOperation(value = "Modifies a existing External TJob", notes = "Modifies the External TJob that matches the received External TJob.", response = ExternalTJob.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "TJob Mofification Successful", response = ExternalTJob.class),
            @ApiResponse(code = 405, message = "Invalid input", response = ExternalTJob.class) })
    @RequestMapping(value = "/extjob", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.PUT)
    ResponseEntity<ExternalTJob> modifyExternalTJob(
            @ApiParam(value = "TJob object that needs to modify.", required = true) @Valid @RequestBody ExternalTJob body);

    /* **************************************************/
    /* *************** ExternalTJobExec *************** */
    /* **************************************************/
    @ApiOperation(value = "Returns all External TJob Executions", notes = "Returns all External TJob Executions.", response = ExternalTJobExecution.class, responseContainer = "List", tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTJobExecution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "tjobexec", method = RequestMethod.GET)
    ResponseEntity<List<ExternalTJobExecution>> getAllExternalTJobExecs();

    @ApiOperation(value = "Return an External TJob Exec By Id", notes = "Returns an External TJob Exec By Id.", response = ExternalTJobExecution.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTJobExecution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "tjobexec/{tJobExecId}", method = RequestMethod.GET)
    ResponseEntity<ExternalTJobExecution> getExternalTJobExecById(
            @ApiParam(value = "Id of an External TJob Exec.", required = true) @PathVariable("tJobExecId") Long tJobExecId);

    @ApiOperation(value = "Creates a new External TJob Execution", notes = "Creates a new External TJob Execution", response = ExternalTJobExecution.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTJobExecution.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "tjobexec", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<ExternalTJobExecution> createExternalTJobExecution(
            @ApiParam(value = "Object with the External TJob Execution data to create.", required = true) @Valid @RequestBody ExternalTJobExecution body);

    @ApiOperation(value = "Modifies a existing External TJobExecution ", notes = "Modifies the External TJob that matches the received External TJob Execution.", response = ExternalTJobExecution.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "TJob Mofification Successful", response = ExternalTJobExecution.class),
            @ApiResponse(code = 405, message = "Invalid input", response = ExternalTJobExecution.class) })
    @RequestMapping(value = "/tjobexec", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.PUT)
    ResponseEntity<ExternalTJobExecution> modifyExternalTJobExecution(
            @ApiParam(value = "TJob Execution object that needs to modify.", required = true) @Valid @RequestBody ExternalTJobExecution body);

    @ApiOperation(value = "Returns all files associated to an External TJob Execution.", notes = "Returns all files associated to an External TJob Execution, for a given TJob execution id.", response = TJobExecutionFile.class, responseContainer = "List", tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = TJobExecution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "TJob Executions files not found") })
    @RequestMapping(value = "tjobexec/{tJobExecId}/files", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<List<TJobExecutionFile>> getExternalTJobExecutionFiles(
            @ApiParam(value = "TJobExec Id.", required = true) @PathVariable("tJobExecId") Long tJobExecId);

    /* **************************************************/
    /* *************** ExternalTestCase *************** */
    /* **************************************************/

    @ApiOperation(value = "Returns all External Test Cases", notes = "Returns all External Test Cases.", response = ExternalTestCase.class, responseContainer = "List", tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "testcase", method = RequestMethod.GET)
    ResponseEntity<List<ExternalTestCase>> getAllExternalTestCases();

    @ApiOperation(value = "Return an External Test Case By Id", notes = "Returns an External Test Case By Id.", response = ExternalTestCase.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "testcase/{caseId}", method = RequestMethod.GET)
    ResponseEntity<ExternalTestCase> getExternalTestCaseById(
            @ApiParam(value = "Id of an External Test Case.", required = true) @PathVariable("caseId") Long caseId);

    /* *************************************************/
    /* ************ ExternalTestExecution ************ */
    /* *************************************************/

    @ApiOperation(value = "Returns all External Test Executions", notes = "Returns all External Test Executions.", response = ExternalTestExecution.class, responseContainer = "List", tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTestExecution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "testexec", method = RequestMethod.GET)
    ResponseEntity<List<ExternalTestExecution>> getAllExternalTestExecutions();

    @ApiOperation(value = "Return an External Test Executions By Id", notes = "Returns an External Test Executions By Id.", response = ExternalTestExecution.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTestExecution.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "testexec/{execId}", method = RequestMethod.GET)
    ResponseEntity<ExternalTestExecution> getExternalTestExecutionById(
            @ApiParam(value = "Id of an External Test Execution.", required = true) @PathVariable("execId") Long execId);

    @ApiOperation(value = "Creates a new External Test Execution", notes = "Creates a new External Test Execution", response = ExternalTestExecution.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTestExecution.class),
            @ApiResponse(code = 405, message = "Invalid input"),
            @ApiResponse(code = 409, message = "Already exist") })
    @RequestMapping(value = "testexec", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<ExternalTestExecution> createExternalTestExecution(
            @ApiParam(value = "Object with the External Test Execution data to create.", required = true) @Valid @RequestBody ExternalTestExecution body);

}
