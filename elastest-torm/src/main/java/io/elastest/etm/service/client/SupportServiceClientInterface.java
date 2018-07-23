package io.elastest.etm.service.client;

import java.io.IOException;

import org.springframework.scheduling.annotation.Async;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.model.SupportService;
import io.elastest.etm.model.SupportServiceInstance;
import io.elastest.etm.model.TssManifest;

public interface SupportServiceClientInterface {

    public void registerService(String serviceRegistry);

    public void registerManifest(String serviceManifest, String id);

    public String provisionServiceInstance(
            SupportServiceInstance serviceInstance, String instanceId,
            String accept_incomplete);

    @Async
    public void deprovisionServiceInstance(String instanceId,
            SupportServiceInstance serviceInstance);

    public SupportService[] getRegisteredServices();

    public JsonNode getRawRegisteredServices() throws IOException;

    public JsonNode getRawServiceById(String serviceId) throws IOException;

    public SupportServiceInstance getServiceInstanceInfo(
            SupportServiceInstance instance);

    public SupportServiceInstance initSupportServiceInstanceData(
            SupportServiceInstance serviceInstance);

    public ObjectNode getServiceInstanceInfo(String instanceId);

    public TssManifest getManifestById(String manifestId);
}