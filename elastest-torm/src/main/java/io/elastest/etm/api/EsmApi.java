package io.elastest.etm.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Api(value = "/esm")
public interface EsmApi extends EtmApiRoot{
    
    @ApiOperation(value = "Returns all ElasTest Services registered in the ESM", notes = "Returns all ElasTest Services registered in the ESM.", response = String.class, responseContainer = "List", tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Resource not found") })    
    @RequestMapping(value = "/esm/service",
    	produces = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<String>> getSupportServicesNames();
    
    @ApiOperation(value = "Returns all ElasTest Services registered in the ESM", notes = "Returns all ElasTest Services registered in the ESM.", response = SupportService.class, responseContainer = "List", tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = SupportService.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Resource not found") })    
    @RequestMapping(value = "/esm/services",
    	produces = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<SupportService>> getSupportServices();
    
    @ApiOperation(value = "Request to create a service instance in the ESM.", notes = "Start the provisioning proces of a service Instance.", response = SupportServiceInstance.class, tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Instance created", response = SupportServiceInstance.class),
        @ApiResponse(code = 500, message = "Internal error") })    
    @RequestMapping(value = "/esm/services/{serviceId}/prov",
    	produces = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<SupportServiceInstance> provisionServiceInstance(@ApiParam(value = "serviceId", required=true) @PathVariable(value="serviceId", required=false) String serviceId);
    
    @ApiOperation(value = "Request to delete a service instance in the ESM.", notes = "Start the delete proces of a service Instance.", response = String.class, tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Instance deleted", response = String.class),
        @ApiResponse(code = 500, message = "Internal error") })    
    @RequestMapping(value = "/esm/services/instances/{instanceId}",
    	produces = { "application/json" },
        method = RequestMethod.DELETE)
    ResponseEntity<String> deprovisionServiceInstance(@ApiParam(value = "Service Instance id", required=true) @PathVariable("instanceId") String instanceId);
    
    @ApiOperation(value = "Returns all Support Services Insances provided by the ESM", notes = "Returns all Support Services Insances provided by the ESM.", response = SupportServiceInstance.class, responseContainer = "List", tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = SupportServiceInstance.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Resource not found") })    
    @RequestMapping(value = "/esm/services/instances",
    	produces = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<SupportServiceInstance>> getSupportServicesInstances();
    
    @ApiOperation(value = "Returns the Support Services Insances info of a given id", notes = "Returns the Support Services Insances info of a given id.", response = SupportServiceInstance.class, tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = SupportServiceInstance.class),
        @ApiResponse(code = 404, message = "Resource not found") })    
    @RequestMapping(value = "/esm/services/instances/{id}",
    	produces = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<SupportServiceInstance> getSupportServiceInstanceById(@ApiParam(value = "id", required=true) @PathVariable(value="id", required=true) String id );
    
    @ApiOperation(value = "Returns all Support Services Instances associated with a tJobExec", notes = "Returns all Support Services Instances associated with a tJobExec.", response = SupportServiceInstance.class, responseContainer = "List", tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = SupportServiceInstance.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Resource not found") })    
    @RequestMapping(value = "/esm/services/instances/tJobExec/{id}",
    	produces = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<SupportServiceInstance>> getTSSInstByTJobExecId(@ApiParam(value = "id", required=true) @PathVariable(value="id", required=true) Long id );

}
