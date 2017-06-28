package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.api.model.Project;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "project", description = "the Project API")
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
	
    @ApiOperation(value = "Returns all projects.", notes = "Returns all projects for a user loged.", response = Project.class, responseContainer = "List", tags={ "Project", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful operation", response = Project.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Resource not found") })
    
    @RequestMapping(value = "/project",
        method = RequestMethod.GET)
    ResponseEntity<List<Project>> getAllProjects();
	
	@ApiOperation(value = "Return a project for a given id.", notes = "", response = Project.class, tags={ "Project", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Project.class),
        @ApiResponse(code = 405, message = "Invalid input", response = Project.class) })
    
    @RequestMapping(value = "/project/{id}",
    	produces = { "application/json" },        
        method = RequestMethod.GET)
    ResponseEntity<Project> getProject(@ApiParam(value = "Project id." ,required=true )  @PathVariable("id") Long id);
	
	
	@ApiOperation(value = "Deletes a Project.", notes = "Delete the Project that matches with a given a ProjectId.", response = Long.class, tags={ "Project", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Deleteted successful", response = Long.class),
        @ApiResponse(code = 404, message = "Project not found", response = Long.class) })
    
    @RequestMapping(value = "/project/{projectId}",
        method = RequestMethod.DELETE)
    ResponseEntity<Long> deleteProject(@ApiParam(value = "ID of project to delete.",required=true ) @PathVariable("projectId") Long projectId);
	

}
