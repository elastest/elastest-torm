package io.elastest.epm.client.service;

import static org.apache.commons.lang.SystemUtils.IS_OS_WINDOWS;

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
import io.elastest.epm.client.model.Adapter;
import io.elastest.epm.client.model.Cluster;
import io.elastest.epm.client.model.ClusterFromResourceGroup;
import io.elastest.epm.client.model.KeyValuePair;
import io.elastest.epm.client.model.PoP;
import io.elastest.epm.client.model.ResourceGroup;
import io.elastest.epm.client.model.Worker;

@Service
public class EpmClusterService {
    private static final Logger logger = LoggerFactory
            .getLogger(K8sService.class);

    private final PackageApi packageApi = new PackageApi();
    private final WorkerApi workerApi = new WorkerApi();
    private final RuntimeApi runtimeApi = new RuntimeApi();
    private final AdapterApi adapterApi = new AdapterApi();
    private final PoPApi poPApi = new PoPApi();
    private final ClusterApi clusterApi = new ClusterApi();

    private ResourceGroup ansibleRG;
    private ResourceGroup ansible2RG;

    @Value("${et.enable.cloud.mode}")
    public boolean enableCloudMode;

    @Value("${et.epm.api}")
    String etEpmApi;

    @Value("${et.shared.folder}")
    String etSharedFolder;

    @Value("${et.epm.packages.path}")
    String etEpmPackagesPath;

    @Value("${et.k8.interface.url:null}")
    String interfaceUrl;

    @Value("${et.k8.interface.pass:null}")
    String interfacePass;

    @Value("${et.k8.interface.project.name:null}")
    String interfaceProjectName;

    @Value("${et.k8.interface.user:null}")
    String interfaceUser;

    @Value("${et.k8.interface.name:null}")
    String interfaceName;

    String epmPackagescompletePath;

    private static final String CLUSTER_TAR_FILE_NAME = "ansible-cluster.tar";
    private static final String NODE_TAR_FILE_NAME = "ansible-node.tar";

    @PostConstruct
    private void init() throws ApiException {
        if (enableCloudMode) {
            String fileSeparator = IS_OS_WINDOWS ? "\\\\" : "/";
            epmPackagescompletePath = etSharedFolder;
            if (!epmPackagescompletePath.endsWith(fileSeparator)) {
                epmPackagescompletePath += fileSeparator;
            }

            epmPackagescompletePath += etEpmPackagesPath;

            if (!epmPackagescompletePath.endsWith(fileSeparator)) {
                epmPackagescompletePath += fileSeparator;
            }

            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(etEpmApi);

            clusterApi.setApiClient(apiClient);
            packageApi.setApiClient(apiClient);
            workerApi.setApiClient(apiClient);
            adapterApi.setApiClient(apiClient);
            poPApi.setApiClient(apiClient);
            runtimeApi.setApiClient(apiClient);

            try {
                PoP pop = new PoP();
                pop.setName(interfaceName);
                pop.setInterfaceEndpoint(interfaceUrl);
                pop.addInterfaceInfoItem(
                        new KeyValuePair().key("auth_url").value(interfaceUrl));
                pop.addInterfaceInfoItem(new KeyValuePair().key("password")
                        .value(interfacePass));
                pop.addInterfaceInfoItem(new KeyValuePair().key("project_name")
                        .value(interfaceProjectName));
                pop.addInterfaceInfoItem(new KeyValuePair().key("username")
                        .value(interfaceUser));
                String interfaceType = "openstack";
                pop.addInterfaceInfoItem(
                        new KeyValuePair().key("type").value(interfaceType));
                PoP poPR = poPApi.registerPoP(pop);

                List<Adapter> adapters = adapterApi.getAllAdapters();
                for (Adapter adapter : adapters) {
                    if ("ansible".equals(adapter.getType())) {
                        logger.debug("Ansible adapter available!");
                    }
                }

            } catch (ApiException e) {
                logger.error("Exception when calling PoPApi#registerPoP", e);
            }

            try {
                updateClusterTar();
            } catch (Exception e) {
                logger.error("Error on updateClusterTar:", e);
            }

            try {
                updateNodesTar();
            } catch (Exception e) {
                logger.error("Error on updateNodesTar:", e);
            }
        }
    }

    /* ******************************************* */
    /* ***************** Cluster ***************** */
    /* ******************************************* */

    public void updateClusterTar() throws ApiException {
        File ansible = new File(
                epmPackagescompletePath + CLUSTER_TAR_FILE_NAME);
        ansibleRG = packageApi.receivePackage(ansible);
    }

    public void updateNodesTar() throws ApiException {
        File ansible2 = new File(epmPackagescompletePath + NODE_TAR_FILE_NAME);
        ansible2RG = packageApi.receivePackage(ansible2);
    }

    public Cluster createCluster() throws Exception {
        return createClusterFromResourceGroup(ansible2RG.getId(),
                ansible2RG.getVdus().get(0).getId(), "kubernetes");
    }

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

    public String createNodeWorker(String clusterId) throws ApiException {
        return clusterApi.addWorker(clusterId,
                ansibleRG.getVdus().get(0).getId());
    }

    public Cluster removeNode(String clusterId, String workerId)
            throws ApiException {
        return clusterApi.removeNode(clusterId, workerId);
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
