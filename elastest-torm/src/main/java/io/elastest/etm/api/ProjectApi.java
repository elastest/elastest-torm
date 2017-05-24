package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.api.model.Project;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "project", description = "the sut API")
public interface ProjectApi {
	
	@ApiOperation(value = "Create new Project", notes = "", response = Project.class, tags={ "Project", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Project.class),
        @ApiResponse(code = 405, message = "Invalid input", response = Project.class) })
    
    @RequestMapping(value = "/project",
    	produces = { "application/json" },
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<Project> createProject(@ApiParam(value = "Project configuration" ,required=true )  @Valid @RequestBody Project body);
	
	

}
