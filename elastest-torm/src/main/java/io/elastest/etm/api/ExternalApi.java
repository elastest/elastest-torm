package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.api.model.ExternalJob;
import io.elastest.etm.model.external.ExternalId;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.TypeEnum;
import io.elastest.etm.model.external.ExternalTestCase;
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
    ExternalJob executeExternalTJob(
            @ApiParam(value = "Object with the external Job Data (the name).", required = true) @Valid @RequestBody ExternalJob body);

    @ApiOperation(value = "Receives the completion signal of an External Job", notes = "Sets the execution of TJob in the Completed state.", tags = {
            "External", })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/tjob", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.PUT)
    void finishExternalJob(
            @ApiParam(value = "Object with the id of the TJob to update the state.", required = true) @Valid @RequestBody ExternalJob body);

    /* *************************************************/
    /* *************** ExternalProject *************** */
    /* *************************************************/

    @ApiOperation(value = "Returns all External Projects", notes = "Returns all External Projects.", response = ExternalProject.class, responseContainer = "List", tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalProject.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/extproject", method = RequestMethod.GET)
    ResponseEntity<List<ExternalProject>> getAllExternalProjects();

    @ApiOperation(value = "Returns all External Projects By Service Type", notes = "Returns all External Projects By Service Type.", response = ExternalProject.class, responseContainer = "List", tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalProject.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/extproject/type/{type}", method = RequestMethod.GET)
    ResponseEntity<List<ExternalProject>> getAllExternalProjectsByType(
            @ApiParam(value = "Type of the project.", required = true) @PathVariable("type") TypeEnum type);

    @ApiOperation(value = "Return an External Projects By Id", notes = "Returns an External Projects By Id.", response = ExternalProject.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalProject.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/extproject/id", method = RequestMethod.GET)
    ResponseEntity<ExternalProject> getExternalProjectById(ExternalId id,
            Model model);

    /* **************************************************/
    /* *************** ExternalTestCase *************** */
    /* **************************************************/

    @ApiOperation(value = "Returns all External Test Cases", notes = "Returns all External Test Cases.", response = ExternalTestCase.class, responseContainer = "List", tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/exttestcase", method = RequestMethod.GET)
    ResponseEntity<List<ExternalTestCase>> getAllExternalTestCases();

    @ApiOperation(value = "Return an External Test Case By Id", notes = "Returns an External Test Case By Id.", response = ExternalTestCase.class, tags = {
            "External", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = ExternalTestCase.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/exttestcase/id", method = RequestMethod.GET)
    ResponseEntity<ExternalTestCase> getExternalTestCaseById(ExternalId id,
            Model model);

}
