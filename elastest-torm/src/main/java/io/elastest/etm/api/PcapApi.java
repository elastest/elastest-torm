package io.elastest.etm.api;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-10-04T14:53:11.074+02:00")

@Api(value = "/pcap")
public interface PcapApi extends EtmApiRoot {
    @ApiOperation(value = "Starts pcap monitoring of a SuT with a given Execution Id", notes = "Starts pcap monitoring of a SuT with a given Execution Id"
            + " at least must receive as input a JSON with the following fields: String execID", response = Boolean.class, tags = {
                    "Pcap", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = Boolean.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/pcap", produces = {
            "application/json" }, consumes = {
                    "text/plain" }, method = RequestMethod.POST)
    ResponseEntity<Boolean> startPcap(
            @ApiParam(value = "Execution Id", required = true) @Valid @RequestBody String execId);

    @ApiOperation(value = "Stops pcap monitoring of a SuT with a given Execution Id", notes = "Stops pcap monitoring of a SuT with a given Execution Id"
            + " at least must receive as input a JSON with the following fields: String execID", response = void.class, tags = {
                    "Pcap", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = void.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/pcap/{execId}", produces = {
            "application/octet-stream" }, method = RequestMethod.DELETE)
    void stopPcap(
            @ApiParam(value = "Execution Id.", required = true) @PathVariable("execId") String execId,
            HttpServletResponse response);

    @ApiOperation(value = "Gets pcap container Name with a given Execution Id", notes = "Gets pcap container Name with a given Execution Id"
            + " at least must receive as input a JSON with the following fields: String execID", response = String.class, tags = {
                    "Pcap", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = String.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/pcap/{execId}", produces = {
            "application/octet-stream" }, method = RequestMethod.GET)
    ResponseEntity<String> getPcapContainerName(
            @ApiParam(value = "Execution Id.", required = true) @PathVariable("execId") String execId);
}
