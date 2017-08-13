package io.elastest.etm.api;

import io.elastest.etm.model.DeployConfig;
import io.elastest.etm.model.Log;
import io.elastest.etm.model.SuTMonitoring;
import io.elastest.etm.model.SutExecution;
import io.elastest.etm.model.SutSpecification;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import javax.validation.Valid;
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

    //Create SutExecution
    @ApiOperation(value = "Deploys a SuT", notes = "", response = SutExecution.class, tags={ "sut execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "SuT has been deployed successfully", response = SutExecution.class),
        @ApiResponse(code = 400, message = "Invalid SuT ID supplied", response = SutExecution.class),
        @ApiResponse(code = 404, message = "SuT not found", response = SutExecution.class) })
    
    @RequestMapping(value = "/sut/{sutId}/exec",
        produces = { "application/json" }, 
        method = RequestMethod.POST)
    ResponseEntity<SutExecution> deploySuT(@ApiParam(value = "SuT id to deploy",required=true ) @PathVariable("sutId") Long sutId,@ApiParam(value = "Configuration for deploy" ,required=true )  @Valid @RequestBody DeployConfig deployConfig);

    
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


    @ApiOperation(value = "Returns a SuT logs", notes = "", response = Log.class, responseContainer = "List", tags={ "sut execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Log.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid SuT ID supplied", response = Log.class),
        @ApiResponse(code = 404, message = "SuT not found", response = Log.class) })
    
    @RequestMapping(value = "/sut/{sutId}/exec/{sutExecId}/logs",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<List<Log>> suTLogs(@ApiParam(value = "SuT id to return logs",required=true ) @PathVariable("sutId") Long sutId,@ApiParam(value = "SuT Execution id to deploy",required=true ) @PathVariable("sutExecId") Long sutExecId);


    @ApiOperation(value = "Returns a SuT monitoring info", notes = "", response = SuTMonitoring.class, tags={ "sut execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = SuTMonitoring.class),
        @ApiResponse(code = 400, message = "Invalid SuT ID supplied", response = SuTMonitoring.class),
        @ApiResponse(code = 404, message = "SuT not found", response = SuTMonitoring.class) })
    
    @RequestMapping(value = "/sut/{sutId}/exec/{sutExecId}/monitoring",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<SuTMonitoring> suTMonitoring(@ApiParam(value = "SuT id to return monitoring information",required=true ) @PathVariable("sutId") Long sutId,@ApiParam(value = "SuT Execution id to deploy",required=true ) @PathVariable("sutExecId") Long sutExecId);


    @ApiOperation(value = "Returns a SuT status", notes = "", response = String.class, tags={ "sut execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = String.class),
        @ApiResponse(code = 400, message = "Invalid SuT ID supplied", response = String.class),
        @ApiResponse(code = 404, message = "SuT not found", response = String.class) })
    
    @RequestMapping(value = "/sut/{sutId}/exec/{sutExecId}/status",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<String> suTStatus(@ApiParam(value = "SuT id to return status",required=true ) @PathVariable("sutId") Long sutId,@ApiParam(value = "SuT Execution id to deploy",required=true ) @PathVariable("sutExecId") Long sutExecId);


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


    @ApiOperation(value = "Undeploys a SuT", notes = "", response = Void.class, tags={ "sut execution", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "SuT undeployed successfully", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid SuT ID supplied", response = Void.class),
        @ApiResponse(code = 404, message = "SuT not found", response = Void.class) })
    
    @RequestMapping(value = "/sut/{sutId}/exec/{sutExecId}/undeploy",
        produces = { "application/json" }, 
        method = RequestMethod.POST)
    ResponseEntity<Void> undeploySuT(@ApiParam(value = "SuT id to undeploy",required=true ) @PathVariable("sutId") Long sutId,@ApiParam(value = "SuT Execution id to deploy",required=true ) @PathVariable("sutExecId") Long sutExecId);

}
