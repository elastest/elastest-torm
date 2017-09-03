package io.elastest.etm.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Api(value = "/esm")
public interface EsmApi extends EtmApiRoot{
    
    @ApiOperation(value = "Returns all ElasTest Services registered in the ESM", notes = "Returns all ElasTest Services registered in the ESM.", response = String.class, responseContainer = "List", tags={ "ESM", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Resource not found") })    
    @RequestMapping(value = "/esm/services",
    	produces = { "application/json" },
        method = RequestMethod.GET)
    ResponseEntity<List<String>> getElastestServices();  

}
