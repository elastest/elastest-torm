package io.elastest.etm.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.elastest.etm.model.EtPlugin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-10-03T12:35:11.074+02:00")

@Api(value = "/etplugins")
public interface EtPluginsApi extends EtmApiRoot {

    @ApiOperation(value = "Starts a new instance of a passed ET Plugin", notes = "Starts a new instance of a passed Et Plugin"
            + " at least must receive as input a JSON with the following fields: String etpluginName", response = EtPlugin.class, tags = {
                    "ETPlugins", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = EtPlugin.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/etplugins/{name}/start", produces = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<EtPlugin> startEtPlugin(
            @ApiParam(value = "EtPlugin Name.", required = true) @PathVariable("name") String name);

    @ApiOperation(value = "Starts a new instance of a passed ETPlugin asynchronously", notes = "Starts a new instance of a passed ETPlugin"
            + " at least must receive as input a JSON with the following fields: String etpluginName", response = EtPlugin.class, tags = {
                    "ETPlugins", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = EtPlugin.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/etplugins/{name}/start/async", produces = {
            "application/json" }, method = RequestMethod.POST)
    ResponseEntity<EtPlugin> startEtPluginAsync(
            @ApiParam(value = "EtPlugin Name.", required = true) @PathVariable("name") String name);

    @ApiOperation(value = "Returns ETPlugins list", notes = "Returns ETPlugins list", response = EtPlugin.class, responseContainer = "List", tags = {
            "ETPlugins", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = EtPlugin.class),
            @ApiResponse(code = 400, message = "Not found.", response = EtPlugin.class) })
    @RequestMapping(value = "/etplugins", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<List<EtPlugin>> getEtPlugins();

    @ApiOperation(value = "Returns ETPlugin by givven name", notes = "Returns ETPlugin by givven name", response = String.class, responseContainer = "List", tags = {
            "ETPlugins", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
            @ApiResponse(code = 400, message = "Not found.", response = String.class) })
    @RequestMapping(value = "/etplugins/{name}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<EtPlugin> getEtPlugin(
            @ApiParam(value = "EtPlugin Name.", required = true) @PathVariable("name") String name);

    @ApiOperation(value = "Stops an instance of a passed ETPlugin", notes = "Stops an instance of a passed ETPlugin"
            + " at least must receive as input a JSON with the following fields: String etpluginName", response = EtPlugin.class, tags = {
                    "ETPlugins", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = EtPlugin.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/etplugins/{name}", produces = {
            "application/json" }, method = RequestMethod.DELETE)
    ResponseEntity<EtPlugin> stopEtPlugin(
            @ApiParam(value = "EtPlugin Name.", required = true) @PathVariable("name") String name);

    @ApiOperation(value = "Returns ETPlugin url if service is running", notes = "Returns ETPlugin url if service is running", response = String.class, tags = {
            "ETPlugins", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = String.class),
            @ApiResponse(code = 400, message = "Not found.", response = String.class) })
    @RequestMapping(value = "/etplugins/{name}/url", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<String> getUrlIfIsRunning(
            @ApiParam(value = "EtPlugin Name.", required = true) @PathVariable("name") String name);

    @ApiOperation(value = "Returns if service is running", notes = "Returns if service is running", response = Boolean.class, tags = {
            "ETPlugins", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
            @ApiResponse(code = 400, message = "Not found.", response = Boolean.class) })
    @RequestMapping(value = "/etplugins/{name}/started", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Boolean> isRunning(
            @ApiParam(value = "EtPlugin Name.", required = true) @PathVariable("name") String name);

    @ApiOperation(value = "Returns if service is working", notes = "Returns if service is working", response = Boolean.class, tags = {
            "ETPlugins", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
            @ApiResponse(code = 400, message = "Not found.", response = Boolean.class) })
    @RequestMapping(value = "/etplugins/{name}/working", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Boolean> isWorking(
            @ApiParam(value = "EtPlugin Name.", required = true) @PathVariable("name") String name);

    /* **************************************************************** */
    /* *********************** SPECIFIC METHODS *********************** */
    /* **************************************************************** */

    @ApiOperation(value = "Returns Unique ETPlugins list", notes = "Returns Unique ETPlugins list", response = EtPlugin.class, responseContainer = "List", tags = {
            "ETPlugins", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = EtPlugin.class),
            @ApiResponse(code = 400, message = "Not found.", response = EtPlugin.class) })
    @RequestMapping(value = "/etplugins/unique", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<List<EtPlugin>> getUniqueEtPlugins();

    @ApiOperation(value = "Returns an Unique ETPlugin by given name", notes = "Returns an Unique ETPlugin by given name", response = String.class, responseContainer = "List", tags = {
            "ETPlugins", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
            @ApiResponse(code = 400, message = "Not found.", response = String.class) })
    @RequestMapping(value = "/etplugins/unique/{name}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<EtPlugin> getUniqueEtPlugin(
            @ApiParam(value = "Unique EtPlugin Name.", required = true) @PathVariable("name") String name);
}
