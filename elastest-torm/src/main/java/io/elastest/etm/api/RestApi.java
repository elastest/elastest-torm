package io.elastest.etm.api;

import javax.servlet.http.HttpServletRequest;
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

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Api(value = "/rest")
public interface RestApi extends EtmApiRoot {

    @ApiOperation(value = "Rest Api Request.", notes = "Rest Api Request.", response = String.class, tags = {
            "Rest", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Instance created", response = String.class),
            @ApiResponse(code = 500, message = "Internal error") })
    @RequestMapping(value = "/rest/{method}/**", produces = {
            "text/plain" }, method = RequestMethod.POST)
    ResponseEntity<String> provisionServiceInstance(
            @ApiParam(value = "method", required = true) @PathVariable(value = "method", required = false) String method,
            @ApiParam(value = "Body in Json String format", required = false) @Valid @RequestBody String jsonBody,
            HttpServletRequest request);

}
