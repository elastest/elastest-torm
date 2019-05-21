package io.elastest.etm.api;

import static org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.elastest.epm.client.model.Cluster;
import io.elastest.epm.client.model.Worker;
import io.elastest.epm.client.service.EpmClusterService;
import io.elastest.etm.utils.EtmFilesService;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Controller
public class EpmApiController implements EpmApi {

    private static final Logger logger = LoggerFactory
            .getLogger(EpmApiController.class);
    @Autowired
    EtmFilesService etmFilesService;

    @Autowired
    EpmClusterService epmClusterService;

    private static final String CLUSTER_TAR_FILE_NAME = "ansible-cluster.tar";
    private static final String NODE_TAR_FILE_NAME = "ansible-node.tar";

    @Value("${et.shared.folder}")
    String etSharedFolder;

    @Value("${et.epm.packages.path}")
    String etEpmPackagesPath;

    String epmPackagescompletePath;

    @PostConstruct
    private void init() {
        String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
        epmPackagescompletePath = etSharedFolder;
        if (!epmPackagescompletePath.endsWith(fileSeparator)) {
            epmPackagescompletePath += fileSeparator;
        }

        epmPackagescompletePath += etEpmPackagesPath;
    }

    @Override
    public ResponseEntity<Boolean> uploadClusterTarFile(
            @RequestParam("file") MultipartFile file) {
        try {
            Boolean saved = etmFilesService.saveMultipartFile(
                    CLUSTER_TAR_FILE_NAME, file, epmPackagescompletePath);
            return new ResponseEntity<Boolean>(saved, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on upload cluster tar file");
            return new ResponseEntity<Boolean>(false,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Boolean> uploadNodeTarFile(
            @RequestParam("file") MultipartFile file) {
        try {
            Boolean saved = etmFilesService.saveMultipartFile(
                    NODE_TAR_FILE_NAME, file, epmPackagescompletePath);
            return new ResponseEntity<Boolean>(saved, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on upload node tar file");
            return new ResponseEntity<Boolean>(false,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* ******************************************* */
    /* ***************** Cluster ***************** */
    /* ******************************************* */

    @Override
    public ResponseEntity<Cluster> createCluster() {
        try {
            Cluster cluster = epmClusterService.createCluster();
            return new ResponseEntity<Cluster>(cluster, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on create Cluster", e);
            return new ResponseEntity<Cluster>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> deleteCluster(
            @ApiParam(value = "Id of a Cluster.", required = true) @PathVariable("clusterId") String clusterId) {
        try {
            String responseString = epmClusterService.deleteCluster(clusterId);
            return new ResponseEntity<String>(responseString, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on delete Cluster {}", clusterId, e);
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<Cluster>> getAllClusters() {
        try {
            List<Cluster> clusters = epmClusterService.getAllClusters();
            return new ResponseEntity<List<Cluster>>(clusters, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on get all clusters {}", e);
            return new ResponseEntity<List<Cluster>>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Cluster> getCluster(
            @ApiParam(value = "Id of a Cluster.", required = true) @PathVariable("clusterId") String clusterId) {
        try {
            Cluster cluster = epmClusterService.getCluster(clusterId);
            return new ResponseEntity<Cluster>(cluster, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on get Cluster {}", clusterId, e);
            return new ResponseEntity<Cluster>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* ***************************************** */
    /* ***************** Nodes ***************** */
    /* ***************************************** */

    @Override
    public ResponseEntity<String> createNode(
            @ApiParam(value = "Id of a Cluster.", required = true) @PathVariable("clusterId") String clusterId) {
        try {
            String responseString = epmClusterService
                    .createNodeWorker(clusterId);
            return new ResponseEntity<String>(responseString, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on create node in cluster {}", clusterId, e);
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Cluster> deleteNode(
            @ApiParam(value = "Id of a cluster.", required = true) @PathVariable("clusterId") String clusterId,
            @ApiParam(value = "Id of a node.", required = true) @PathVariable("nodeId") String nodeId) {
        try {
            Cluster cluster = epmClusterService.removeNode(clusterId, nodeId);
            return new ResponseEntity<Cluster>(cluster, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on delete node {} in Cluster {}", nodeId,
                    clusterId, e);
            return new ResponseEntity<Cluster>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<Worker>> getAllClusterNodes(
            @ApiParam(value = "Id of a cluster.", required = true) @PathVariable("clusterId") String clusterId) {
        try {
            List<Worker> clusterNodes = epmClusterService
                    .getAllClusterNodes(clusterId);
            return new ResponseEntity<List<Worker>>(clusterNodes,
                    HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on get all nodes of cluster {}", clusterId, e);
            return new ResponseEntity<List<Worker>>(
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Worker> getClusterNode(
            @ApiParam(value = "Id of a cluster.", required = true) @PathVariable("clusterId") String clusterId,
            @ApiParam(value = "Id of a node.", required = true) @PathVariable("nodeId") String nodeId) {
        try {
            Worker node = epmClusterService.getClusterNode(clusterId, nodeId);
            return new ResponseEntity<Worker>(node, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error on get node {} in Cluster {}", nodeId,
                    clusterId, e);
            return new ResponseEntity<Worker>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
