package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.model.ExternalJob;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "external", description = "the external API to use for non ElasTest applications")
public interface ExternalApi extends EtmApiExternalRoot {
	
	@ApiOperation(value = "Create new external TJob", notes = "", response = ExternalJob.class, tags={ "external", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ExternalJob.class),
        @ApiResponse(code = 405, message = "Invalid input", response = ExternalJob.class) })
    
    @RequestMapping(value = "/tjob",
    	produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<ExternalJob> createExternalTJob(@ApiParam(value = "ExternalJob configuration" ,required=true )  @Valid @RequestBody ExternalJob body);
	
	
	@ApiOperation(value = "Finish external Job", notes = "", tags={ "external", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 405, message = "Invalid input") })
    
    @RequestMapping(value = "/tjob",
    	produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    void finishExternalJob(@ApiParam(value = "ExternalJob configuration" ,required=true )  @Valid @RequestBody ExternalJob body);

}
