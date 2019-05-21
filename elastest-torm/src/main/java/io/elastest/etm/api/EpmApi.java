package io.elastest.etm.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Api(value = "epm")
public interface EpmApi extends EtmApiRoot {

    @ApiOperation(value = "Uploads a tar file to create clusters", notes = "Uploads a tar file to create clusters.", response = Boolean.class, tags = {
            "EPM", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
            @ApiResponse(code = 202, message = "The request has been accepted, but the processing has not been completed"),
            @ApiResponse(code = 400, message = "Invalid File supplied"),
            @ApiResponse(code = 500, message = "Server Error") })
    @RequestMapping(value = "/epm/cluster/file", consumes = {
            "multipart/form-data" }, produces = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Boolean> uploadClusterTarFile(
            @RequestParam(value = "file") MultipartFile file);

    @ApiOperation(value = "Uploads a tar file to create nodes", notes = "Uploads a tar file to create nodes.", response = Boolean.class, tags = {
            "EPM", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Boolean.class),
            @ApiResponse(code = 202, message = "The request has been accepted, but the processing has not been completed"),
            @ApiResponse(code = 400, message = "Invalid File supplied"),
            @ApiResponse(code = 500, message = "Server Error") })
    @RequestMapping(value = "/epm/node/file", consumes = {
            "multipart/form-data" }, produces = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Boolean> uploadNodeTarFile(
            @RequestParam(value = "file") MultipartFile file);

}
