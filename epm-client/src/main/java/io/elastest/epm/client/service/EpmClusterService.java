package io.elastest.epm.client.service;

import java.io.File;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.elastest.epm.client.ApiClient;
import io.elastest.epm.client.ApiException;
import io.elastest.epm.client.api.AdapterApi;
import io.elastest.epm.client.api.ClusterApi;
import io.elastest.epm.client.api.PackageApi;
import io.elastest.epm.client.api.PoPApi;
import io.elastest.epm.client.api.RuntimeApi;
import io.elastest.epm.client.api.WorkerApi;
import io.elastest.epm.client.model.Cluster;
import io.elastest.epm.client.model.ClusterFromResourceGroup;
import io.elastest.epm.client.model.ResourceGroup;
import io.elastest.epm.client.model.Worker;

@Service
public class EpmClusterService {
    private static final Logger logger = LoggerFactory
            .getLogger(K8Service.class);

    private final PackageApi packageApi = new PackageApi();
    private final WorkerApi workerApi = new WorkerApi();
    private final RuntimeApi runtimeApi = new RuntimeApi();
    private final AdapterApi adapterApi = new AdapterApi();
    private final PoPApi poPApi = new PoPApi();
    private final ClusterApi clusterApi = new ClusterApi();

    private Cluster cluster;
    private ResourceGroup ansibleRG;

    @Value("${et.epm.api}")
    String etEpmApi;

    // TODO change name and variable
    @Value("${ansible.tar.path:null}")
    String ansibleTarPath;

    @PostConstruct
    public void init() throws ApiException {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(etEpmApi);

        clusterApi.setApiClient(apiClient);
        packageApi.setApiClient(apiClient);
        workerApi.setApiClient(apiClient);
        adapterApi.setApiClient(apiClient);
        poPApi.setApiClient(apiClient);
        runtimeApi.setApiClient(apiClient);

        // Create cluster
        try {
            File ansible = new File(ansibleTarPath);
            ansibleRG = packageApi.receivePackage(ansible);

            cluster = createClusterFromResourceGroup(ansibleRG.getId(),
                    ansibleRG.getVdus().get(0).getId(), "kubernetes");
        } catch (Exception e) {
            logger.error("Error on create cluster with tar path: {}",
                    ansibleTarPath, e);
        }
    }

    /* ******************************************* */
    /* ***************** Cluster ***************** */
    /* ******************************************* */

    public Cluster createClusterFromResourceGroup(String resorceGroupId,
            String masterId, String typeItem) throws ApiException {
        ClusterFromResourceGroup clusterFromResourceGroup = new ClusterFromResourceGroup();
        clusterFromResourceGroup.setResourceGroupId(resorceGroupId);
        clusterFromResourceGroup.setMasterId(masterId);
        clusterFromResourceGroup.addTypeItem(typeItem);

        return clusterApi.createCluster(clusterFromResourceGroup);
    }

    public String deleteCluster(String clusterId) throws ApiException {
        return clusterApi.deleteCluster(clusterId);
    }

    private String deleteCluster() throws ApiException {
        return deleteCluster(cluster.getId());
    }

    public List<Cluster> getAllClusters() throws ApiException {
        return clusterApi.getAllClusters();
    }

    public Cluster getCluster(String clusterId) throws ApiException {
        if (getAllClusters() != null) {
            for (Cluster cluster : getAllClusters()) {
                if (clusterId.equals(cluster.getId())) {
                    return cluster;
                }
            }
        }
        return null;
    }

    /* ***************************************** */
    /* ***************** Nodes ***************** */
    /* ***************************************** */

    public String createNodeWorker() throws ApiException {
        return clusterApi.addWorker(cluster.getId(),
                ansibleRG.getVdus().get(0).getId());
    }

    public Cluster removeNode(String workerId) throws ApiException {
        return clusterApi.removeNode(cluster.getId(), workerId);
    }

    public List<Worker> getAllClusterNodes(String clusterId)
            throws ApiException {
        Cluster cluster = getCluster(clusterId);
        if (cluster != null) {
            return cluster.getNodes();
        }
        return null;
    }

    public Worker getClusterNode(String clusterId, String nodeId)
            throws ApiException {
        List<Worker> nodes = getAllClusterNodes(clusterId);
        if (nodes != null) {
            for (Worker node : nodes) {
                if (nodeId.equals(node.getId())) {
                    return node;
                }
            }
        }
        return null;
    }
}
