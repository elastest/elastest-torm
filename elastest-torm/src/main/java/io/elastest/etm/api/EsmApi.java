package io.elastest.etm.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.elastest.etm.model.EsmServiceModel;
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
    ResponseEntity<List<String>> getElastestServicesNames();
    
    @ApiOperation(value = "Returns all ElasTest Services registered in the ESM", notes = "Returns all ElasTest Services registered in the ESM.", response = EsmServiceModel.class, responseContainer = "List", tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = EsmServiceModel.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Resource not found") })    
    @RequestMapping(value = "/esm/services",
    	produces = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<EsmServiceModel>> getElastestServices();
    
    @ApiOperation(value = "Request to create a service instance in the ESM.", notes = "Start the provisioning proces of a service Instance.", response = String.class, tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Instance created", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Internal error") })    
    @RequestMapping(value = "/esm/service_instances/",
    	produces = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<String> provisionServiceInstance(@ApiParam(value = "Service id", required=true) @RequestParam(value="service_id", required=false) String service_id);
    
    @ApiOperation(value = "Request to delete a service instance in the ESM.", notes = "Start the delete proces of a service Instance.", response = String.class, tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Instance deleted", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Internal error") })    
    @RequestMapping(value = "/esm/service_instances/{instance_id}",
    	produces = { "application/json" },
        method = RequestMethod.DELETE)
    ResponseEntity<String> deprovisionServiceInstance(@ApiParam(value = "Service Instance id", required=true) @PathVariable("instance_id") String instance_id);

}
