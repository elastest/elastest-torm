package io.elastest.etm.api;

import javax.validation.Valid;

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
	
	@ApiOperation(value = "Create new TJob associated with an external Job", notes = "The association is based on the "
			+ "name of the external Job received. The Project and the TJob that will be created will have the same name "
			+ "as the one received as a parameter. If a Project or Job already exists with the received name, a new one will not be created.",
			response = ExternalJob.class, tags={ "External", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ExternalJob.class),
        @ApiResponse(code = 405, message = "Invalid input", response = ExternalJob.class) })    
    @RequestMapping(value = "/tjob",
    	produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ExternalJob createExternalTJob(@ApiParam(value = "Object with the external Job Data (the name)." ,required=true )  @Valid @RequestBody ExternalJob body);
	
	
	@ApiOperation(value = "Receives the completion signal of an External Job", notes = "Sets the execution of TJob in the Completed state.", tags={ "External", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 405, message = "Invalid input") })    
    @RequestMapping(value = "/tjob",
    	produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.PUT)
    void finishExternalJob(@ApiParam(value = "Object with the id of the TJob to update the state." ,required=true )  @Valid @RequestBody ExternalJob body);

}
