package io.elastest.etm.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.elastest.epm.client.model.Cluster;
import io.elastest.epm.client.model.Worker;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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

    /* ******************************************* */
    /* ***************** Cluster ***************** */
    /* ******************************************* */

    @ApiOperation(value = "Creates a new Cluster", notes = "Creates a new cluster.", response = Cluster.class, tags = {
            "EPM", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = Cluster.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/epm/cluster", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<Cluster> createCluster();

    @ApiOperation(value = "Deletes a Cluster", notes = "Deletes a cluster.", response = String.class, tags = {
            "EPM", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = String.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/epm/cluster/{clusterId}", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.DELETE)
    ResponseEntity<String> deleteCluster(
            @ApiParam(value = "Id of a Cluster.", required = true) @PathVariable("clusterId") String clusterId);

    @ApiOperation(value = "Returns all clusters", notes = "Returns all clusters.", response = Cluster.class, responseContainer = "List", tags = {
            "EPM", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Cluster.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resource not found") })
    @RequestMapping(value = "/epm/cluster", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<List<Cluster>> getAllClusters();

    @ApiOperation(value = "Returns a cluster", notes = "Returns a cluster.", response = Cluster.class, tags = {
            "EPM", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Cluster.class),
            @ApiResponse(code = 404, message = "Resource not found") })
    @RequestMapping(value = "/epm/cluster/{clusterId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Cluster> getCluster(
            @ApiParam(value = "Id of a Cluster.", required = true) @PathVariable("clusterId") String clusterId);

    /* ***************************************** */
    /* ***************** Nodes ***************** */
    /* ***************************************** */

    @ApiOperation(value = "Creates a new node in the cluster specified", notes = "Creates a new node in the cluster specified.", response = String.class, tags = {
            "EPM", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = String.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/epm/cluster/{clusterId}/node", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.POST)
    ResponseEntity<String> createNode(
            @ApiParam(value = "Id of a Cluster.", required = true) @PathVariable("clusterId") String clusterId);

    @ApiOperation(value = "Creates a new node in the cluster specified", notes = "Creates a new node in the cluster specified.", response = Cluster.class, tags = {
            "EPM", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Creation successful", response = Cluster.class),
            @ApiResponse(code = 405, message = "Invalid input") })
    @RequestMapping(value = "/epm/cluster/{clusterId}/node/{nodeId}", produces = {
            "application/json" }, consumes = {
                    "application/json" }, method = RequestMethod.DELETE)
    ResponseEntity<Cluster> deleteNode(
            @ApiParam(value = "Id of a cluster.", required = true) @PathVariable("clusterId") String clusterId,
            @ApiParam(value = "Id of a node.", required = true) @PathVariable("nodeId") String nodeId);

    @ApiOperation(value = "Returns all cluster nodes", notes = "Returns all cluster nodes.", response = Worker.class, responseContainer = "List", tags = {
            "EPM", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Worker.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Resource not found") })
    @RequestMapping(value = "/epm/cluster/{clusterId}/node", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<List<Worker>> getAllClusterNodes(
            @ApiParam(value = "Id of a cluster.", required = true) @PathVariable("clusterId") String clusterId);

    @ApiOperation(value = "Returns a cluster node", notes = "Returns a cluster node.", response = Worker.class, tags = {
            "EPM", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation", response = Worker.class),
            @ApiResponse(code = 404, message = "Resource not found") })
    @RequestMapping(value = "/epm/cluster/{clusterId}/node/{nodeId}", produces = {
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Worker> getClusterNode(
            @ApiParam(value = "Id of a cluster.", required = true) @PathVariable("clusterId") String clusterId,
            @ApiParam(value = "Id of a node.", required = true) @PathVariable("nodeId") String nodeId);

}
