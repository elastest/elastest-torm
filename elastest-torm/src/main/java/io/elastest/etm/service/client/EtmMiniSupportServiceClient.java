package io.elastest.etm.service.client;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.epm.client.service.DockerComposeService;
import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TssManifest;
import io.elastest.etm.platform.service.PlatformService;
import io.elastest.etm.service.EtPluginsService;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

public class EtmMiniSupportServiceClient
        implements SupportServiceClientInterface {
    final Logger logger = getLogger(lookup().lookupClass());

    @Value("${et.shared.folder}")
    private String sharedFolder;

    @Value("${elastest.docker.network}")
    private String etDockerNetwork;

    Map<String, SupportService> supportServicesMap;
    Map<String, TssManifest> tssManifestMap;

    PlatformService platformService;
    EtPluginsService etPluginsService;

    UtilsService utilsService;

    public EtmMiniSupportServiceClient(EtPluginsService etPluginsService,
            UtilsService utilsService, PlatformService platformService) {
        this.etPluginsService = etPluginsService;
        this.utilsService = utilsService;
        this.platformService = platformService;
        supportServicesMap = new HashMap<>();
        tssManifestMap = new HashMap<>();
    }

    // @PreDestroy
    // void destroy() {
    // if (!supportServiceInstanceMap.isEmpty()) {
    // for (Entry<String, SupportServiceInstance> instance :
    // supportServiceInstanceMap
    // .entrySet()) {
    // this.deprovisionServiceInstance(instance.getKey(),
    // instance.getValue());
    // }
    // }
    // }

    @Override
    public void registerService(String serviceRegistry) {
        SupportService tss = UtilTools.convertJsonStringToObj(serviceRegistry,
                SupportService.class, Include.NON_EMPTY);
        supportServicesMap.put(tss.getId(), tss);
    }

    @Override
    public void registerManifest(String serviceManifest, String id) {
        TssManifest manifest = UtilTools.convertJsonStringToObj(serviceManifest,
                TssManifest.class, Include.NON_EMPTY);
        supportServicesMap.get(manifest.getServiceId()).setManifest(manifest);
        tssManifestMap.put(id, manifest);
    }

    @Override
    public String provisionServiceInstance(
            SupportServiceInstance serviceInstance, String instanceId,
            String accept_incomplete) {
        TssManifest manifest = supportServicesMap
                .get(serviceInstance.getService_id()).getManifest();
        String composeYml = manifest.getManifestContent();

        composeYml.replaceAll("\\\\n", "\\n");

        try {
            etPluginsService.createTssInstanceProject(instanceId, composeYml,
                    serviceInstance);
            etPluginsService.startEtPlugin(instanceId);
        } catch (Exception e) {
            throw new RuntimeException("Exception provisioning service \""
                    + serviceInstance.getService_id() + "\" with instanceId \""
                    + instanceId + "\"", e);
        }

        return "";
    }

    @Override
    @Async
    public void deprovisionServiceInstance(String instanceId,
            SupportServiceInstance serviceInstance) {
        if (etPluginsService.stopAndRemoveProject(instanceId)) {
            logger.info("Service {} deprovisioned.",
                    serviceInstance.getServiceName());
        } else {
            throw new RuntimeException(
                    "Exception deprovisioning instance \"" + instanceId + "\"");
        }
    }

    @Override
    public SupportService[] getRegisteredServices() {
        return supportServicesMap.values().toArray(new SupportService[0]);
    }

    @Override
    public JsonNode getRawRegisteredServices() throws IOException {
        return UtilTools.convertObjToJsonNode(supportServicesMap.values());
    }

    @Override
    public JsonNode getRawServiceById(String serviceId) throws IOException {
        JsonNode service = null;
        JsonNode services = getRawRegisteredServices();
        for (JsonNode currentService : services) {
            if (currentService.get("id").toString().replaceAll("\"", "")
                    .equals(serviceId)) {
                service = currentService;
                break;
            }
        }
        return service;
    }

    @Override
    public SupportServiceInstance getServiceInstanceInfo(
            SupportServiceInstance instance) {
        return initSupportServiceInstanceData(
                (SupportServiceInstance) etPluginsService
                        .getEtPlugin(instance.getInstanceId()));
    }

    @Override
    public TssManifest getManifestById(String manifestId) {
        return new TssManifest(tssManifestMap.get(manifestId));
    }

    @Override
    public TssManifest getManifestBySupportServiceInstance(
            SupportServiceInstance serviceInstance) {
        return supportServicesMap.containsKey(serviceInstance.getService_id())
                ? new TssManifest(supportServicesMap
                        .get(serviceInstance.getService_id()).getManifest())
                : null;
    }

    @Override
    public SupportServiceInstance initSupportServiceInstanceData(
            SupportServiceInstance serviceInstance) {
        TssManifest manifest = supportServicesMap
                .get(serviceInstance.getService_id()).getManifest();
        serviceInstance.setManifestId(manifest.getId());

        if (serviceInstance.getContainerName() == null) {
            JsonNode manifestEndpoints = manifest.getEndpoints();
            Iterator<String> subServicesNames = manifestEndpoints.fieldNames();

            while (subServicesNames.hasNext()) {
                String serviceName = subServicesNames.next();
                logger.debug("Building service instance data for a {} TSS.",
                        serviceName);
                JsonNode manifestEndpointService = manifestEndpoints
                        .get(serviceName);
                String containerName = serviceInstance.getInstanceId() + "_"
                        + serviceName + "_1";
                try {
                    String containerIp = platformService
                            .getContainerIpByNetwork(containerName,
                                    etDockerNetwork);
                    logger.debug("Container ip {} for the service {}",
                            containerIp, containerName);
                    logger.info("ET_PUBLIC_HOST value: "
                            + utilsService.getEtPublicHostValue());

                    String internalServiceIp = containerIp;
                    String bindedServiceIp = utilsService
                            .getEtPublicHostValue();

                    String serviceIp = !utilsService.isDefaultEtPublicHost()
                            ? bindedServiceIp
                            : internalServiceIp;

                    if (manifestEndpointService.get("main") != null
                            && manifestEndpointService.get("main")
                                    .booleanValue()) {
                        logger.debug(
                                "Building data for the main sub-service {}",
                                serviceName);
                        serviceInstance.setContainerName(containerName);
                        serviceInstance.setInternalServiceIp(internalServiceIp);
                        serviceInstance.setBindedServiceIp(bindedServiceIp);
                        serviceInstance.setServiceIp(serviceIp);
                        serviceInstance.setEndpointName(serviceName);
                        serviceInstance.setContainerIp(containerIp);
                    } else {
                        logger.debug("Building data for a sub-service {}",
                                serviceName);
                        SupportServiceInstance auxServiceInstance = null;
                        auxServiceInstance = new SupportServiceInstance();
                        auxServiceInstance
                                .setInternalServiceIp(internalServiceIp);
                        auxServiceInstance.setBindedServiceIp(bindedServiceIp);
                        auxServiceInstance.setContainerName(containerName);
                        auxServiceInstance.setEndpointName(serviceName);
                        auxServiceInstance.setContainerIp(containerIp);
                        auxServiceInstance.setServiceIp(serviceIp);
                        auxServiceInstance
                                .setParameters(serviceInstance.getParameters());
                        serviceInstance.getSubServices()
                                .add(auxServiceInstance);
                    }

                } catch (Exception e) {
                    logger.error("Error on getting TSS instance container ip",
                            e);
                }
            }
        }
        logger.debug("Service instance to return {}",
                serviceInstance.getEndpointName());
        return serviceInstance;
    }

    public void initTssInstanceContainerName(
            SupportServiceInstance serviceInstance) {
        if (serviceInstance.getContainerName() == null) {
            TssManifest manifest = supportServicesMap
                    .get(serviceInstance.getService_id()).getManifest();
            JsonNode manifestEndpoints = manifest.getEndpoints();
            Iterator<String> subServicesNames = manifestEndpoints.fieldNames();

            while (subServicesNames.hasNext()) {
                String serviceName = subServicesNames.next();
                JsonNode manifestEndpointService = manifestEndpoints
                        .get(serviceName);

                SupportServiceInstance auxServiceInstance = null;
                String containerName = serviceInstance.getInstanceId() + "_"
                        + manifest.getEndpoints().fieldNames().next() + "_1";

                if (manifestEndpointService.get("main") != null
                        && manifestEndpointService.get("main").booleanValue()) {
                    serviceInstance.setContainerName(containerName);
                    auxServiceInstance = serviceInstance;
                } else {
                    auxServiceInstance = new SupportServiceInstance();
                    auxServiceInstance.setContainerName(containerName);
                    auxServiceInstance
                            .setParameters(serviceInstance.getParameters());
                    serviceInstance.getSubServices().add(auxServiceInstance);
                }
                auxServiceInstance.setEndpointName(serviceName);
            }
        }
    }

    @Override
    public ObjectNode getServiceInstanceInfo(String instanceId) {
        return UtilTools
                .convertObjToObjectNode(getServiceInstanceInfo(instanceId));
    }

}