package io.elastest.etm.api;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.elastest.etm.model.Project;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(value = "/project")
public interface ProjectApi extends EtmApiRoot {

    @ApiOperation(value = "Creates a new ElasTest Project", notes = "Creates a new Project entity for a given name.This method,"
            + " at least must receive as input a JSON with the following fields: name.", response = Project.class, tags = {
                    "Project", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Project.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/project", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Project> createProject(
            @ApiParam(value = "Object with the project data to create.", required = true) @Valid @RequestBody Project body);

    @ApiOperation(value = "Returns all projects", notes = "Returns the projects and the detailed information of each of them.", response = Project.class, responseContainer = "List", tags = {
            "Project", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Project.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resources not found") })
    @RequestMapping(value = "/project", method = RequestMethod.GET)
    MappingJacksonValue getAllProjects(
            @RequestParam(value = "minimal", required = false) Boolean minimal);

    @ApiOperation(value = "Returns the project found for a given ID", notes = "Returns the project found for a given id and its detail.", response = Project.class, tags = {
            "Project", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Project.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/project/{id}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Project> getProject(
            @ApiParam(value = "Project id to find.", required = true) @PathVariable("id") Long id);

    @ApiOperation(value = "Deletes a Project", notes = "Deletes the project whose id matches for a given id.", response = Long.class, tags = {
            "Project", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Deleteted successful", response = Long.class),
            @ApiResponse(code = 404, message = "Project not found", response = Long.class) })
    @RequestMapping(value = "/project/{id}", method = RequestMethod.DELETE)
    ResponseEntity<Long> deleteProject(
            @ApiParam(value = "ID of the project to delete.", required = true) @PathVariable("id") Long id);

    @ApiOperation(value = "Restores demo projects", notes = "Restores demo projects.", response = Boolean.class, tags = {
            "Project", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/project/restore", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Boolean> restoreDemoProjects();
}
