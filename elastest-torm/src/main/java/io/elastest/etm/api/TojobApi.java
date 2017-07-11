package io.elastest.etm.api;

import io.elastest.etm.api.model.TOJob;
import io.elastest.etm.api.model.TOJobExecution;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import javax.validation.Valid;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Api(value = "tojob", description = "the tojob API")
public interface TojobApi extends EtmApiRoot{

    @ApiOperation(value = "Creates a new TOJob.", notes = "Creates new TOJob with the received description.", response = TOJob.class, tags={ "tojob", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = TOJob.class) })
    
    @RequestMapping(value = "/tojob",
        method = RequestMethod.POST)
    ResponseEntity<TOJob> createTOJob(@ApiParam(value = "TOJjob's desctiption." ,required=true )  @Valid @RequestBody TOJob body);


    @ApiOperation(value = "Deletes a TOJob.", notes = "Deletes the TOJob that maches with a given TOJobId.", response = Long.class, tags={ "tojob", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Long.class),
        @ApiResponse(code = 404, message = "TOJob not exists", response = Long.class) })
    
    @RequestMapping(value = "/tojob/{tOJobId}",
        method = RequestMethod.DELETE)
    ResponseEntity<Long> deleteTOJob(@ApiParam(value = "ID of TOJob to delete.",required=true ) @PathVariable("tOJobId") Long tOJobId);


    @ApiOperation(value = "Deletes a TOJobs Execution.", notes = "Deletes a TOJobs Execution for a given TOJobExecId.", response = Long.class, tags={ "tojob execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Long.class),
        @ApiResponse(code = 404, message = "TOJob Execution not found", response = Long.class) })
    
    @RequestMapping(value = "/tojob/{tOJobId}/exec/{tOJobExecId}",
        method = RequestMethod.DELETE)
    ResponseEntity<Long> deleteTOJobExec(@ApiParam(value = "ID of TOJob to return.",required=true ) @PathVariable("tOJobId") Long tOJobId,@ApiParam(value = "ID of TOJob Execution to return.",required=true ) @PathVariable("tOJobExecId") Long tOJobExecId);


    @ApiOperation(value = "Executes a TOJob.", notes = "Executes the TOJob for a given TOJobId.", response = Long.class, tags={ "tojob execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Long.class),
        @ApiResponse(code = 400, message = "TOJob not found", response = Long.class) })
    
    @RequestMapping(value = "/tojob/{tOJobId}/exec",
        method = RequestMethod.POST)
    ResponseEntity<Long> execTOJob(@ApiParam(value = "ID of TOJob to execute.",required=true ) @PathVariable("tOJobId") Long tOJobId);


    @ApiOperation(value = "Returns all executions.", notes = "Returns all executions of a TOJobId passed as parameter.", response = TOJobExecution.class, responseContainer = "List", tags={ "tojob execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = TOJobExecution.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "TOJob Executions not found", response = TOJobExecution.class) })
    
    @RequestMapping(value = "/tojob/{tOJobId}/exec",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<TOJobExecution>> getAllTOJobExecution(@ApiParam(value = "ID of TOJob execution to return.",required=true ) @PathVariable("tOJobId") Long tOJobId);


    @ApiOperation(value = "Returns a TOJob.", notes = "Returns the TOJob that maches with the given TOJobId.", response = TOJob.class, tags={ "tojob", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = TOJob.class),
        @ApiResponse(code = 404, message = "TOJob not found", response = TOJob.class) })
    
    @RequestMapping(value = "/tojob/{tOJobId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<TOJob> getTOJobById(@ApiParam(value = "ID of TOJob to return.",required=true ) @PathVariable("tOJobId") Long tOJobId);


    @ApiOperation(value = "Returns a TOJobExecution.", notes = "Returns the TOJobExecution for a given TOJobExecId. ", response = TOJobExecution.class, tags={ "tojob execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = TOJobExecution.class),
        @ApiResponse(code = 404, message = "TOJob Execution not found", response = TOJobExecution.class) })
    
    @RequestMapping(value = "/tojob/{tOJobId}/exec/{tOJobExecId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<TOJobExecution> getTOJobExecById(@ApiParam(value = "ID of TOJob to return.",required=true ) @PathVariable("tOJobId") Long tOJobId,@ApiParam(value = "ID of TOJob Execution to return.",required=true ) @PathVariable("tOJobExecId") Long tOJobExecId);


    @ApiOperation(value = "Finds and returns all TOJobs.", notes = "Returns all TOJobs for a logged user.", response = TOJob.class, responseContainer = "List", tags={ "tojob", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = TOJob.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "TOJobs not found", response = TOJob.class) })
    
    @RequestMapping(value = "/tojob",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<TOJob>> getTOJobs();


    @ApiOperation(value = "Updates the description of a TOJob.", notes = "Updates the description of a TOJob.", response = TOJob.class, tags={ "tojob", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = TOJob.class),
        @ApiResponse(code = 404, message = "TOJob not exists", response = TOJob.class) })
    
    @RequestMapping(value = "/tojob/{tOJobId}",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<TOJob> modifyTOJob(@ApiParam(value = "Id of TJob that needs to be update.",required=true ) @PathVariable("tOJobId") Long tOJobId,@ApiParam(value = "TOJob object that needs to be updated." ,required=true )  @Valid @RequestBody TOJob body);

}
