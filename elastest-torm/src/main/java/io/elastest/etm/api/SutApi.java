package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Api(value = "/sut")
public interface SutApi extends EtmApiRoot {

    @ApiOperation(value = "Creates a new SuT Description", notes = "A SUT will be associated with a Project. "
    		+ "This method, at least must receive as input a JSON with the following fields: "
    		+ "name, imageName, project, specification", response = SutSpecification.class, tags={ "SUT", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SutSpecification.class),
        @ApiResponse(code = 405, message = "Invalid input") })    
    @RequestMapping(value = "/sut",
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<SutSpecification> createSuT(@ApiParam(value = "SUT configuration" ,required=true )  @Valid @RequestBody SutSpecification body);

    @ApiOperation(value = "Deletes a SUT", notes = "Deletes the SUT whose id matches with a given id.", response = Long.class, tags={ "SUT", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "SUT deleteted successfully", response = Long.class),
        @ApiResponse(code = 400, message = "Invalid SUT ID supplied", response = Long.class),
        @ApiResponse(code = 404, message = "SUT not found", response = Long.class) })    
    @RequestMapping(value = "/sut/{sutId}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Long> deleteSuT(@ApiParam(value = "Id of the SUT to delete.",required=true ) @PathVariable("sutId") Long sutId);
   
    @ApiOperation(value = "Deletes a SUT execution", notes = "Deteltes the SUT Execution whose id matches with a given id.", response = Long.class, tags={ "SUT Execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "SUT Execution deleteted successfully", response = Long.class),
        @ApiResponse(code = 400, message = "Invalid SUT Execution ID supplied", response = Long.class),
        @ApiResponse(code = 404, message = "SUT Execution not found", response = Long.class) })    
    @RequestMapping(value = "/sut/{sutId}/exec/{sutExecId}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Long> deleteSuTExec(@ApiParam(value = "Id of the SUT Execution to delete.",required=true ) @PathVariable("sutExecId") Long sutExecId);

    @ApiOperation(value = "Returns a SUT Execution.", notes = "Returns the SUT Execution whose id matches with a given id.", response = SutExecution.class, tags={ "SUT Execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SutExecution.class),
        @ApiResponse(code = 400, message = "Invalid SUT ID supplied", response = SutExecution.class),
        @ApiResponse(code = 404, message = "SUT not found", response = SutExecution.class) })    
    @RequestMapping(value = "/sut/{sutId}/exec/{sutExecId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<SutExecution> getSutExec(@ApiParam(value = "Id of the SUT executed.",required=true ) @PathVariable("sutId") Long sutId,@ApiParam(value = "Id of the SUT Execution.",required=true ) @PathVariable("sutExecId") Long sutExecId);

    @ApiOperation(value = "List all SUT definitions", notes = "Returns a list of all SUT definitions.", response = SutSpecification.class, responseContainer = "List", tags={ "SUT", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SutSpecification.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "SUTs not found") })    
    @RequestMapping(value = "/sut",
        produces = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<SutSpecification>> getSutList();

    @ApiOperation(value = "Updates an existing SUT Description", notes = "Updates a SUT with the request body data.", response = SutSpecification.class, tags={ "SUT", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SutSpecification.class),
        @ApiResponse(code = 405, message = "Invalid input", response = SutSpecification.class) })    
    @RequestMapping(value = "/sut",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<SutSpecification> modifySut(@ApiParam(value = "SUT configuration" ,required=true )  @Valid @RequestBody SutSpecification body);

    @ApiOperation(value = "List all SUT executions", notes = "Returns a list of all SUT Executions for a given SUT.", response = SutExecution.class, responseContainer = "List", tags={ "SUT Execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SutExecution.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "SUTs not found", response = SutExecution.class) })    
    @RequestMapping(value = "/sut/{sutId}/exec",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<SutExecution>> getAllSutExecBySut(@ApiParam(value = "SUT id",required=true ) @PathVariable("sutId") Long sutId);
 
    @ApiOperation(value = "Returns a SUT", notes = "Returns the SUT that matches for a given id.", response = SutSpecification.class, tags={ "SUT", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "SUT returned successfully", response = SutSpecification.class) })    
    @RequestMapping(value = "/sut/{sutId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<SutSpecification> getSutById(@ApiParam(value = "SUT id to return.",required=true ) @PathVariable("sutId") Long sutId);

}
