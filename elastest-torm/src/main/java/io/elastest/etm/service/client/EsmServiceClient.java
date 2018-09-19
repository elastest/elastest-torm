package io.elastest.etm.service.client;

import static java.lang.invoke.MethodHandles.lookup;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.SupportServiceInstance.ProvisionView;
import io.elastest.etm.model.TssManifest;
import io.elastest.etm.utils.UtilTools;
import io.elastest.etm.utils.UtilsService;

public class EsmServiceClient implements SupportServiceClientInterface {
    final Logger logger = getLogger(lookup().lookupClass());

    @Value("${elastest.esm.port}")
    private String esmPort;

    @Value("${elastest.esm.url.register.service}")
    private String URL_ESM_REGISTER_SERVICE;

    @Value("${elastest.esm.url.catalog}")
    private String URL_GET_CATALOG_ESM;

    @Value("${elastest.esm.url.register.manifest}")
    private String URL_ESM_REGISTER_MANIFEST;

    @Value("${elastest.esm.url.request.instance}")
    private String URL_ESM_PROVISION_INSTANCE;

    @Value("${elastest.esm.url.deprovision.instance}")
    private String URL_ESM_DEPROVISION_INSTANCE;

    @Value("${elastest.esm.url.service-instance.info}")
    private String URL_ESM_SERVICE_INSTANCE_INFO;

    @Value("${elastest.esm.url.get.manifest}")
    private String URL_ESM_GET_MANIFEST;

    RestTemplate httpClient;
    HttpHeaders headers;

    UtilsService utilsService;

    public EsmServiceClient(UtilsService utilsService) {
        this.utilsService = utilsService;
        httpClient = new RestTemplate();
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-broker-api-version", "2.12");
    }

    @Override
    public void registerService(String serviceRegistry) {
        logger.info("Registering the service in the ESM.");
        HttpEntity<String> entity = new HttpEntity<String>(serviceRegistry,
                headers);

        try {
            httpClient.put(URL_ESM_REGISTER_SERVICE, entity);
            logger.info("Registered service.");
        } catch (Exception e) {
            // throw new RuntimeException("Exception registering service
            // \""+serviceRegistry+"\"",e);
        }
    }

    @Override
    public void registerManifest(String serviceManifest, String id) {
        logger.info("Registering the service manifest in the ESM.");
        HttpEntity<String> entity = new HttpEntity<String>(serviceManifest,
                headers);

        Map<String, String> params = new HashMap<>();
        params.put("manifest_id", id);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(URL_ESM_REGISTER_MANIFEST);
        ResponseEntity<ObjectNode> result = null;
        try {
            result = httpClient.exchange(builder.buildAndExpand(params).toUri(),
                    HttpMethod.PUT, entity, ObjectNode.class);
            logger.info(
                    "Manifest registered: {}" + result.getBody().toString());
        } catch (Exception e) {
            // if (result != null &&
            // result.getBody().toString().equals("\"duplicate\"")){
            // throw new RuntimeException("The manifest already exists",e);
            // }else{
            // throw new RuntimeException("Exception registering manifest of
            // service \""+id+"\"", e);
            // }
        }
    }

    @Override
    public String provisionServiceInstance(
            SupportServiceInstance serviceInstance, String instanceId,
            String accept_incomplete) {
        String serviceInstanceData = "";
        logger.info("Request a service instance.");
        HttpEntity<String> entity = new HttpEntity<String>(
                UtilTools.convertJsonString(serviceInstance,
                        ProvisionView.class, Include.NON_EMPTY),
                headers);

        Map<String, String> params = new HashMap<>();
        params.put("instance_id", instanceId);

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(URL_ESM_PROVISION_INSTANCE)
                .queryParam("accept_incomplete", accept_incomplete);

        try {
            httpClient.exchange(builder.buildAndExpand(params).toUri(),
                    HttpMethod.PUT, entity, String.class);
            logger.info("Registered service.");
        } catch (Exception e) {
            throw new RuntimeException("Exception provisioning service \""
                    + serviceInstance.getService_id() + "\" with instanceId \""
                    + instanceId + "\"", e);
        }

        return serviceInstanceData;
    }

    @Override
    @Async
    public void deprovisionServiceInstance(String instanceId,
            SupportServiceInstance serviceInstance) {
        logger.info("Request removal of a service instance.");
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        Map<String, String> params = new HashMap<>();
        params.put("instance_id", instanceId);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(URL_ESM_DEPROVISION_INSTANCE)
                .queryParam("service_id", serviceInstance.getService_id())
                .queryParam("plan_id", serviceInstance.getPlan_id());

        try {
            httpClient.exchange(builder.buildAndExpand(params).toUri(),
                    HttpMethod.DELETE, entity, ObjectNode.class);
            logger.info("Service {} deprovisioned.",
                    serviceInstance.getServiceName());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Exception deprovisioning instance \"" + instanceId + "\"",
                    e);
        }
    }

    @Override
    public SupportService[] getRegisteredServices() {
        logger.info("Retrieving the services.");
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        try {
            ResponseEntity<ObjectNode> objNode = httpClient.exchange(
                    URL_GET_CATALOG_ESM, HttpMethod.GET, entity,
                    ObjectNode.class);
            logger.info("Retrieved services.");
            return UtilTools.convertJsonStringToObj(
                    objNode.getBody().get("services").toString(),
                    SupportService[].class, Include.NON_EMPTY, false);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Exception retrieving registered services", e);
        }
    }

    @Override
    public JsonNode getRawRegisteredServices() throws IOException {
        logger.info("Get registered all data of a service.");
        logger.info("Retrieving the services.");
        HttpEntity<String> entity = new HttpEntity<String>(headers);

        try {
            ResponseEntity<ObjectNode> objNode = httpClient.exchange(
                    URL_GET_CATALOG_ESM, HttpMethod.GET, entity,
                    ObjectNode.class);
            logger.info("Retrieved services.");
            return objNode.getBody().get("services");
        } catch (Exception e) {
            throw new RuntimeException(
                    "Exception retrieving registered services", e);
        }
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
        return initSupportServiceInstanceData(instance);

    }

    @Override
    public ObjectNode getServiceInstanceInfo(String instanceId) {
        logger.info("Retrieving service instance info.");

        HttpEntity<String> entity = new HttpEntity<String>(headers);
        Map<String, String> params = new HashMap<>();
        params.put("instance_id", instanceId);

        try {
            ResponseEntity<ObjectNode> objNode = httpClient.exchange(
                    URL_ESM_SERVICE_INSTANCE_INFO, HttpMethod.GET, entity,
                    ObjectNode.class, params);
            logger.info("Retrieved services instance info.");
            logger.info("Instance info: " + objNode.getBody().toString());
            return objNode.getBody();
        } catch (Exception e) {
            throw new RuntimeException(
                    "Exception retrieving info of instance \"" + instanceId
                            + "\"",
                    e);
        }
    }

    public ObjectNode getManifestJsonById(String manifestId) {
        logger.info("Manifest to retrieve " + manifestId);
        Map<String, String> params = new HashMap<>();
        params.put("manifest_id", manifestId);

        try {
            ResponseEntity<ObjectNode[]> objNode = httpClient.exchange(
                    URL_ESM_GET_MANIFEST, HttpMethod.GET, null,
                    ObjectNode[].class, params);
            logger.info("Manifest info: " + objNode.getBody()[0].toString());
            return objNode.getBody()[0];
        } catch (Exception e) {
            throw new RuntimeException(
                    "Exception retrieving info of manifest \"" + manifestId
                            + "\"",
                    e);
        }
    }

    @Override
    public TssManifest getManifestById(String manifestId) {
        ObjectNode manifestJson = getManifestJsonById(manifestId);
        return UtilTools.convertJsonStringToObj(manifestJson.toString(),
                TssManifest.class, Include.NON_EMPTY);
    }

    @Override
    public TssManifest getManifestBySupportServiceInstance(
            SupportServiceInstance serviceInstance) {
        return null; // TODO
    }

    @Override
    public SupportServiceInstance initSupportServiceInstanceData(
            SupportServiceInstance serviceInstance) {
        ObjectNode serviceInstanceDetail = getServiceInstanceInfo(
                serviceInstance.getInstanceId());

        serviceInstance.setManifestId(serviceInstanceDetail.get("context")
                .get("manifest_id").toString().replaceAll("\"", ""));

        ObjectNode manifest = getManifestJsonById(
                serviceInstance.getManifestId());
        JsonNode manifestEndpoints = manifest.get("endpoints");

        Iterator<String> subServicesNames = manifestEndpoints.fieldNames();
        Iterator<String> itEsmRespContextFields = serviceInstanceDetail
                .get("context").fieldNames();

        while (subServicesNames.hasNext()) {
            String serviceName = subServicesNames.next();
            logger.info("Manifest services: {}", serviceName);

            String serviceIpFieldSufix = serviceName + "_Ip";
            String serviceIp = null;
            boolean ipFound = false;

            while (itEsmRespContextFields.hasNext() && !ipFound) {
                String fieldName = itEsmRespContextFields.next();
                logger.info("Instance data fields {}:" + fieldName);

                if (fieldName.contains(serviceIpFieldSufix)) {

                    String tssContainerName = fieldName.substring(0,
                            fieldName.indexOf("_Ip"));
                    serviceInstance.setContainerName(tssContainerName);

                    String containerIp = serviceInstanceDetail.get("context")
                            .get(fieldName).toString().replaceAll("\"", "");

                    logger.info("ET_PUBLIC_HOST value: "
                            + utilsService.getEtPublicHostValue());
                    serviceIp = !utilsService.isDefaultEtPublicHost()
                            ? utilsService.getEtPublicHostValue()
                            : containerIp;
                    serviceInstance.setContainerIp(containerIp);
                    serviceInstance.setServiceIp(serviceIp);
                    logger.info(
                            "Service Ip {}:" + serviceInstance.getServiceIp());

                    ipFound = true;
                }
            }
        }

        return serviceInstance;
    }
}
