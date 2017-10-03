package io.elastest.etm.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.model.SupportServiceInstance;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-10-03T12:35:11.074+02:00")

@Api(value = "/engines")
public interface TestEnginesApi extends EtmApiRoot {

	@ApiOperation(value = "Create and return a new instance of a passed Test Engine", notes = "Create and return a new instance of a passed Test Engine"
			+ " at least must receive as input a JSON with the following fields: String engineName", response = String.class, tags = {
					"Engines", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Creation successful", response = String.class),
			@ApiResponse(code = 405, message = "Invalid input") })
	@RequestMapping(value = "/engines", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<String> createTestEngine(
			@ApiParam(value = "Data to create the new Test Engine", required = true) @Valid @RequestBody String engineName);

	@ApiOperation(value = "Stops an instance of a passed Test Engine", notes = "Stops an instance of a passed Test Engine"
			+ " at least must receive as input a JSON with the following fields: String engineName", response = String.class, tags = {
					"Engines", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Creation successful", response = String.class),
			@ApiResponse(code = 405, message = "Invalid input") })
	@RequestMapping(value = "/engines/{name}", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<String> stopTestEngine(
			@ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name);

	@ApiOperation(value = "Returns if service is running", notes = "Returns if service is running", response = Boolean.class, tags = {
			"Engines", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
			@ApiResponse(code = 400, message = "Not found.", response = Boolean.class) })
	@RequestMapping(value = "/engines/{name}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<Boolean> isRunning(
			@ApiParam(value = "Engine Name.", required = true) @PathVariable("name") String name);

	@ApiOperation(value = "Returns test engines list", notes = "Returns test engines list", response = String.class, responseContainer = "List", tags = {
			"Engines", })
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
			@ApiResponse(code = 400, message = "Not found.", response = String.class) })
	@RequestMapping(value = "/engines", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<String>> getTestEngines();

}
