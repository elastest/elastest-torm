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

@Api(value = "sut", description = "the sut API")
public interface SutApi extends EtmApiRoot {

    @ApiOperation(value = "Create SuT Description", notes = "", response = SutSpecification.class, tags={ "sut", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SutSpecification.class),
        @ApiResponse(code = 405, message = "Invalid input") })
    
    @RequestMapping(value = "/sut",
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<SutSpecification> createSuT(@ApiParam(value = "SuT configuration" ,required=true )  @Valid @RequestBody SutSpecification body);


    @ApiOperation(value = "Deletes a SuT", notes = "", response = Long.class, tags={ "sut", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "SuT deleteted successfully", response = Long.class),
        @ApiResponse(code = 400, message = "Invalid SuT ID supplied", response = Long.class),
        @ApiResponse(code = 404, message = "SuT not found", response = Long.class) })
    
    @RequestMapping(value = "/sut/{sutId}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Long> deleteSuT(@ApiParam(value = "SuT id to delete",required=true ) @PathVariable("sutId") Long sutId);
   
    @ApiOperation(value = "Deletes a SuT execution", notes = "", response = Long.class, tags={ "sut execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "SuT Execution deleteted successfully", response = Long.class),
        @ApiResponse(code = 400, message = "Invalid SuT Execution ID supplied", response = Long.class),
        @ApiResponse(code = 404, message = "SuT Execution not found", response = Long.class) })
    
    @RequestMapping(value = "/sut/{sutId}/exec/{sutExecId}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    ResponseEntity<Long> deleteSuTExec(@ApiParam(value = "SuT execution id to delete",required=true ) @PathVariable("sutExecId") Long sutExecId);


    @ApiOperation(value = "Returns a SuT Execution information", notes = "", response = SutExecution.class, tags={ "sut execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SutExecution.class),
        @ApiResponse(code = 400, message = "Invalid SuT ID supplied", response = SutExecution.class),
        @ApiResponse(code = 404, message = "SuT not found", response = SutExecution.class) })
    
    @RequestMapping(value = "/sut/{sutId}/exec/{sutExecId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<SutExecution> getSutExec(@ApiParam(value = "SuT id to return logs",required=true ) @PathVariable("sutId") Long sutId,@ApiParam(value = "SuT Execution id to deploy",required=true ) @PathVariable("sutExecId") Long sutExecId);

    @ApiOperation(value = "List all SuT definitions", notes = "", response = SutSpecification.class, responseContainer = "List", tags={ "sut", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SutSpecification.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "SuTs not found") })
    
    @RequestMapping(value = "/sut",
        produces = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<SutSpecification>> getSutList();


    @ApiOperation(value = "Updates an existing SuT Description", notes = "", response = SutSpecification.class, tags={ "sut", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SutSpecification.class),
        @ApiResponse(code = 405, message = "Invalid input", response = SutSpecification.class) })
    
    @RequestMapping(value = "/sut",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<SutSpecification> modifySut(@ApiParam(value = "SuT configuration" ,required=true )  @Valid @RequestBody SutSpecification body);


    @ApiOperation(value = "List all SuT executions", notes = "", response = SutExecution.class, responseContainer = "List", tags={ "sut execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SutExecution.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "SuTs not found", response = SutExecution.class) })
    
    @RequestMapping(value = "/sut/{sutId}/exec",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<SutExecution>> getAllSutExecBySut(@ApiParam(value = "Sut id",required=true ) @PathVariable("sutId") Long sutId);
    

    @ApiOperation(value = "Returns a Sut.", notes = "Returns the Sut that matches the given id.", response = SutSpecification.class, tags={ "sut", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Sut returned successfully", response = SutSpecification.class) })
    
    @RequestMapping(value = "/sut/{sutId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<SutSpecification> getSutById(@ApiParam(value = "SuT id to return.",required=true ) @PathVariable("sutId") Long sutId);

}
