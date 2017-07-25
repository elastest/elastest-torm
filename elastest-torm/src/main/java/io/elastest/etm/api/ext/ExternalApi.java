package io.elastest.etm.api.ext;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.api.EtmApiRoot;
import io.elastest.etm.api.model.Project;
import io.elastest.etm.api.model.TJob;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "external", description = "the external API to use for non ElasTest applications")
@RequestMapping("/external")
public interface ExternalApi extends EtmApiRoot {
	
//	@ApiOperation(value = "Create new external TJob", notes = "", response = TJob.class, tags={ "external", })
//    @ApiResponses(value = { 
//        @ApiResponse(code = 200, message = "OK", response = TJob.class),
//        @ApiResponse(code = 405, message = "Invalid input", response = TJob.class) })
//    
//    @RequestMapping(value = "/tjob",
//    	produces = { "application/json" },
//        consumes = { "application/json" },
//        method = RequestMethod.POST)
//    ResponseEntity<TJob> createExternalTJob(@ApiParam(value = "TJob configuration" ,required=true )  @Valid @RequestBody TJob body);

}
