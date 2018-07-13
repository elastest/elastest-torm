package io.elastest.etm.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModelProperty;

public class TssManifest {
    @JsonProperty("id")
    String id;

    @JsonProperty("manifest_content")
    @JsonAlias("manifestContent")
    String manifestContent;

    @JsonProperty("manifest_type")
    @JsonAlias("manifestType")
    String manifestType;

    @JsonProperty("plan_id")
    @JsonAlias("plan_id")
    String planId;

    @JsonProperty("service_id")
    @JsonAlias("serviceId")
    String serviceId;

    @JsonProperty("endpoints")
    String endpoints;

    @JsonProperty("config")
    @JsonInclude(Include.NON_NULL)
    private Map<String, Object> config = new HashMap<String, Object>();

    public TssManifest() {
    }

    public TssManifest(Map<String, Object> config) {
        this.config = config;
    }

    public TssManifest(String id, String manifestContent, String manifestType,
            String planId, String serviceId, String endpoints,
            Map<String, Object> config) {
        super();
        this.id = id;
        this.manifestContent = manifestContent;
        this.manifestType = manifestType;
        this.planId = planId;
        this.serviceId = serviceId;
        this.endpoints = endpoints;
        this.config = config;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManifestContent() {
        return manifestContent;
    }

    public void setManifestContent(String manifestContent) {
        this.manifestContent = manifestContent;
    }

    public String getManifestType() {
        return manifestType;
    }

    public void setManifestType(String manifestType) {
        this.manifestType = manifestType;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }

    @ApiModelProperty(example = "EUS", value = "The config of the TSS.")
    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

}
