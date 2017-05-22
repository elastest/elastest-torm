package io.elastest.etm.api;

import io.elastest.etm.api.model.TJob;
import io.elastest.etm.api.model.TJobExecution;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Api(value = "tjob", description = "the tjob API")
public interface TjobApi {

    @ApiOperation(value = "Create a new tjob.", notes = "Creates a new tjob with the received information.", response = TJob.class, tags={ "tjob", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "TJob Creation OK", response = TJob.class),
        @ApiResponse(code = 405, message = "Invalid input", response = TJob.class) })
    
    @RequestMapping(value = "/tjob",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<TJob> createTJob(@ApiParam(value = "Tjob object that needs to create" ,required=true )  @Valid @RequestBody TJob body);


    @ApiOperation(value = "Deletes a TJob.", notes = "Delete the TJob that matches with a given a tJobId.", response = Long.class, tags={ "tjob", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Deleteted successful", response = Long.class),
        @ApiResponse(code = 404, message = "TJob not found", response = Long.class) })
    
    @RequestMapping(value = "/tjob/{tJobId}",
        method = RequestMethod.DELETE)
    ResponseEntity<Long> deleteTJob(@ApiParam(value = "ID of tJob to delete.",required=true ) @PathVariable("tJobId") Long tJobId);


    @ApiOperation(value = "Deletes a TJob Execution.", notes = "Deletes the TJob Execution for a given id. Returns the id.", response = Long.class, tags={ "tjob execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Long.class),
        @ApiResponse(code = 404, message = "TJob Execution not found", response = Long.class) })
    
    @RequestMapping(value = "/tjob/{tJobId}/exec/{tJobExecId}",
        method = RequestMethod.DELETE)
    ResponseEntity<Long> deleteTJobExecution(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId,@ApiParam(value = "TJob Execution Id.",required=true ) @PathVariable("tJobExecId") Long tJobExecId);


    @ApiOperation(value = "Executes a TJob.", notes = "Runs the TJob for a give tJobId and returns a Tjob Execution Id.", response = Long.class, tags={ "tjob execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "successful operation", response = Long.class),
        @ApiResponse(code = 400, message = "Invalid ID supplied", response = Long.class),
        @ApiResponse(code = 404, message = "TJob not found", response = Long.class) })
    
    @RequestMapping(value = "/tjob/{tJobId}/exec",
        produces = { "application/json" }, 
        method = RequestMethod.POST)
    ResponseEntity<Long> execTJob(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId);


    @ApiOperation(value = "Returns all tjobs.", notes = "Returns all tjobs for a user loged.", response = TJob.class, responseContainer = "List", tags={ "tjob", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = TJob.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Resource not found", response = TJob.class) })
    
    @RequestMapping(value = "/tjob",
        consumes = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<TJob>> getAllTJobs();


    @ApiOperation(value = "Returns a TJob.", notes = "Returns the TJob for a fiven TJobId. Returns its detail information.", response = TJob.class, tags={ "tjob", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = TJob.class),
        @ApiResponse(code = 400, message = "TJob not found.", response = TJob.class) })
    
    @RequestMapping(value = "/tjob/{tJobId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<TJob> getTJobById(@ApiParam(value = "ID of tJob to retrieve.",required=true ) @PathVariable("tJobId") Long tJobId);


    @ApiOperation(value = "Returns a TJob Execution.", notes = "Returns the TJob Execution for a fiven id.", response = TJobExecution.class, tags={ "tjob execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = TJobExecution.class),
        @ApiResponse(code = 404, message = "TJob Execution not found", response = TJobExecution.class) })
    
    @RequestMapping(value = "/tjob/{tJobId}/exec/{tJobExecId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<TJobExecution> getTJobsExecution(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId,@ApiParam(value = "TJob Execution Id.",required=true ) @PathVariable("tJobExecId") Long tJobExecId);


    @ApiOperation(value = "Returns all TJob Executions of a Tjob.", notes = "Returns all TJob Executions of a Tjob.", response = TJobExecution.class, responseContainer = "List", tags={ "tjob execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = TJobExecution.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "TJobs Executions not found", response = TJobExecution.class) })
    
    @RequestMapping(value = "/tjob/{tJobId}/exec",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<TJobExecution>> getTJobsExecutionsByTJob(@ApiParam(value = "TJob Id.",required=true ) @PathVariable("tJobId") Long tJobId);


    @ApiOperation(value = "Modifies a existing tjob.", notes = "Modifies a existing tjob received as a parameter.", response = TJob.class, tags={ "tjob", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "TJob Mofification OK", response = TJob.class),
        @ApiResponse(code = 405, message = "Invalid input", response = TJob.class) })
    
    @RequestMapping(value = "/tjob",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    ResponseEntity<TJob> modifyTJob(@ApiParam(value = "Tjob object that needs to modify." ,required=true )  @Valid @RequestBody TJob body);

}
