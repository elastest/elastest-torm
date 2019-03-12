package io.elastest.etm.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

public interface ElasticsearchApi extends EtmApiRoot {

    @ApiOperation(value = "Returns All indices.", notes = "Returns All indices.", response = String.class, responseContainer = "List", tags = {
            "Elasticsearch", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Not allowed without Elasticsearch") })
    @RequestMapping(value = "/elasticsearch/index", method = RequestMethod.GET)
    ResponseEntity<List<String>> getAllIndices() throws Exception;

    @ApiOperation(value = "Returns All indices.", notes = "Returns All indices.", response = String.class, responseContainer = "List", tags = {
            "Elasticsearch", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = String.class, responseContainer = "List"),
            @ApiResponse(code = 405, message = "Not allowed without Elasticsearch") })
    @RequestMapping(value = "/elasticsearch/index/byhealth/{health}", method = RequestMethod.GET)
    ResponseEntity<List<String>> getIndicesByHealth(
            @ApiParam(value = "Id of the SUT executed.", required = true) @PathVariable("health") String health) throws Exception;

    @ApiOperation(value = "Deletes indices with RED health status.", notes = "Deletes indices with RED health status.", response = Boolean.class, tags = {
            "Elasticsearch", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Boolean.class),
            @ApiResponse(code = 405, message = "Not allowed without Elasticsearch") })
    @RequestMapping(value = "/elasticsearch/index/byhealth/{health}", method = RequestMethod.DELETE)
    ResponseEntity<Boolean> deleteIndicesByHealth(
            @ApiParam(value = "Id of the SUT executed.", required = true) @PathVariable("health") String health) throws Exception;
}