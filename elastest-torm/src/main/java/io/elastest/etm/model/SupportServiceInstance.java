package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Entity that represents an ElasTest Test Support Service Instance")
public class SupportServiceInstance extends EtPlugin {

    private static final String API_STATUS_KEY = "api-status";

    public interface ProvisionView {
    }

    public interface FrontView {
    }

    @JsonView(FrontView.class)
    @JsonProperty("instanceId")
    private String instanceId;

    @JsonView({ ProvisionView.class, FrontView.class })
    @JsonProperty("service_id")
    private String service_id;

    @JsonView(FrontView.class)
    @JsonProperty("serviceName")
    private String serviceName;

    @JsonView(FrontView.class)
    @JsonProperty("serviceShortName")
    private String serviceShortName;

    @JsonView(FrontView.class)
    @JsonProperty("fullyInitialized")
    private boolean fullyInitialized;

    @JsonView(ProvisionView.class)
    @JsonProperty("plan_id")
    private String plan_id;

    @JsonView(ProvisionView.class)
    @JsonProperty("organization_guid")
    private String organization_guid;

    @JsonView(ProvisionView.class)
    @JsonProperty("context")
    private Map<String, String> context;

    @JsonView(ProvisionView.class)
    @JsonProperty("space_guid")
    private String space_guid;

    @JsonView(FrontView.class)
    @JsonProperty("tJobExecIdList")
    private List<Long> tJobExecIdList;

    @JsonView(FrontView.class)
    @JsonProperty("internalServiceIp")
    private String internalServiceIp;

    @JsonView(FrontView.class)
    @JsonProperty("internalServicePort")
    private int internalServicePort;

    @JsonView(FrontView.class)
    @JsonProperty("bindedServiceIp")
    private String bindedServiceIp;

    @JsonView(FrontView.class)
    @JsonProperty("bindedServicePort")
    private int bindedServicePort;

    @JsonView(FrontView.class)
    @JsonProperty("serviceIp")
    private String serviceIp;

    @JsonView(FrontView.class)
    @JsonProperty("servicePort")
    private int servicePort;

    @JsonProperty("containerIp")
    private String containerIp;

    @JsonProperty("containerName")
    private String containerName;

    @JsonProperty("manifestId")
    private String manifestId;

    @JsonView(FrontView.class)
    @JsonProperty("urls")
    private Map<String, SSIUrl> urls;

    @JsonView(FrontView.class)
    @JsonProperty("subServices")
    private List<SupportServiceInstance> subServices;

    @JsonView(FrontView.class)
    @JsonProperty("endpointName")
    private String endpointName;

    @JsonView(FrontView.class)
    @JsonProperty("endpointsData")
    private Map<String, JsonNode> endpointsData;

    @JsonProperty("portBindingContainers")
    private List<String> portBindingContainers;

    Map<String, String> endpointsBindingsPorts;

    // Parent attrs
    @Override
    @JsonView({ ProvisionView.class, FrontView.class })
    public DockerServiceStatusEnum getStatus() {
        return super.getStatus();
    }

    @Override
    @JsonView({ ProvisionView.class, FrontView.class })
    public String getStatusMsg() {
        return super.getStatusMsg();
    }

    @JsonView({ ProvisionView.class, FrontView.class })
    @JsonProperty("parameters")
    @Override
    public Map<String, String> getParameters() {
        return super.getParameters();
    }

    public SupportServiceInstance() {
        super();
        this.urls = new HashMap<>();
        this.subServices = new ArrayList<>();
        this.endpointsData = new HashMap<>();
        this.portBindingContainers = new ArrayList<>();
        this.fullyInitialized = false;
        this.endpointsBindingsPorts = new HashMap<>();
        this.tJobExecIdList = new ArrayList<>();

    }

    public SupportServiceInstance(String instanceId, String service_id,
            String serviceName, String serviceShortName, String plan_id,
            List<Long> bindToTJob) {
        this();
        this.instanceId = instanceId;
        this.service_id = service_id;
        this.serviceName = serviceName;
        this.serviceShortName = serviceShortName;
        this.plan_id = plan_id;
        this.tJobExecIdList = bindToTJob;
        this.context = new HashMap<>();
        this.organization_guid = "org";
        this.space_guid = "space";
        this.manifestId = "";
        this.fullyInitialized = false;

        // EtPlugin
        this.name = instanceId;
    }

    /**
     * Get id
     * 
     * @return id
     **/
    @ApiModelProperty(example = "1", value = "Identifies the TSS.")
    public String getService_id() {
        return service_id;
    }

    public void setService_id(String service_id) {
        this.service_id = service_id;
    }

    /**
     * Get Service Name
     * 
     * @return The Service Name
     **/
    @ApiModelProperty(example = "EUS", value = "Name of the TSS.")
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Get The short name of the TSS
     * 
     * @return The short name of the service
     **/
    @ApiModelProperty(example = "EUS", value = "The short name of the TSS.")
    public String getServiceShortName() {
        return serviceShortName;
    }

    public void setServiceShortName(String serviceShortName) {
        this.serviceShortName = serviceShortName;
    }

    /**
     * Indicates whether the TSS instance is Fully initialized
     * 
     * @return True or false
     **/
    @ApiModelProperty(example = "EUS", value = "The short name of the TSS.")
    public boolean isFullyInitialized() {
        return fullyInitialized;
    }

    public void setFullyInitialized(boolean fullyInitialized) {
        this.fullyInitialized = fullyInitialized;
    }

    public String getPlan_id() {
        return plan_id;
    }

    public void setPlan_id(String plan_id) {
        this.plan_id = plan_id;
    }

    public String getOrganization_guid() {
        return organization_guid;
    }

    public void setOrganization_guid(String organization_guid) {
        this.organization_guid = organization_guid;
    }

    public String getSpace_guid() {
        return space_guid;
    }

    public List<Long> gettJobExecIdList() {
        return tJobExecIdList;
    }

    public void settJobExecIdList(List<Long> tJobExecIdList) {
        this.tJobExecIdList = tJobExecIdList;
    }

    public void setSpace_guid(String space_guid) {
        this.space_guid = space_guid;
    }

    public String getInternalServiceIp() {
        return internalServiceIp;
    }

    public void setInternalServiceIp(String internalServiceIp) {
        this.internalServiceIp = internalServiceIp;
    }

    public int getInternalServicePort() {
        return internalServicePort;
    }

    public void setInternalServicePort(int internalServicePort) {
        this.internalServicePort = internalServicePort;
    }

    public String getBindedServiceIp() {
        return bindedServiceIp;
    }

    public void setBindedServiceIp(String bindedServiceIp) {
        this.bindedServiceIp = bindedServiceIp;
    }

    public int getBindedServicePort() {
        return bindedServicePort;
    }

    public void setBindedServicePort(int bindedServicePort) {
        this.bindedServicePort = bindedServicePort;
    }

    public String getServiceIp() {
        return serviceIp;
    }

    public void setServiceIp(String serviceIp) {
        this.serviceIp = serviceIp;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public Map<String, String> getEndpointsBindingsPorts() {
        return endpointsBindingsPorts;
    }

    public void setEndpointsBindingsPorts(
            Map<String, String> endpointsBindingsPorts) {
        this.endpointsBindingsPorts = endpointsBindingsPorts;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getManifestId() {
        return manifestId;
    }

    public void setManifestId(String manifestId) {
        this.manifestId = manifestId;
    }

    public List<SupportServiceInstance> getSubServices() {
        return subServices;
    }

    public void setSubServices(List<SupportServiceInstance> subServices) {
        this.subServices = subServices;
    }

    public Map<String, SSIUrl> getUrls() {
        return urls;
    }

    public void setUrls(Map<String, SSIUrl> urls) {
        this.urls = urls;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public Map<String, JsonNode> getEndpointsData() {
        return endpointsData;
    }

    public void setEndpointsData(Map<String, JsonNode> endpointsData) {
        this.endpointsData = endpointsData;
    }

    public List<String> getPortBindingContainers() {
        return portBindingContainers;
    }

    public void setPortBindingContainers(List<String> portBindingContainers) {
        this.portBindingContainers = portBindingContainers;
    }

    public String getContainerIp() {
        return containerIp;
    }

    public void setContainerIp(String containerIp) {
        this.containerIp = containerIp;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public String getUrlIfExistsByKey(String urlKey, boolean external) {
        String url = null;
        if (getUrls().containsKey(urlKey)) {
            SSIUrl urlObj = getUrls().get(urlKey);
            // If external, return external, else return internal
            if (urlObj != null) {
                url = external ? urlObj.getExternal() : urlObj.getInternal();
            }
        }
        return url;
    }

    public void setUrlValue(String urlKey, String value, boolean external) {
        if (!getUrls().containsKey(urlKey)) {
            SSIUrl newUrl = new SSIUrl();
            getUrls().put(urlKey, newUrl);
        }

        SSIUrl urlObj = getUrls().get(urlKey);
        // If external, return external, else return internal
        if (urlObj != null) {
            if (external) {
                urlObj.setExternal(value);

            } else {
                urlObj.setInternal(value);
            }
            getUrls().put(urlKey, urlObj);
        }
    }

    public String getApiUrlIfExist(boolean external) {
        for (Map.Entry<String, SSIUrl> urlHash : getUrls().entrySet()) {
            SSIUrl url = urlHash.getValue();
            if (!urlHash.getKey().equals(API_STATUS_KEY)) {
                String apiUrl = external ? url.getExternal()
                        : url.getInternal();
                if (apiUrl != null && (apiUrl.contains("http")
                        || apiUrl.contains("https"))) {
                    return apiUrl;
                }
            }
        }
        return null;
    }

    public String getApiStatusUrlIfExist() {
        if (getUrls().containsKey(API_STATUS_KEY)) {
            return getUrls().get(API_STATUS_KEY).getInternal();
        }
        return null;
    }

    public void setApiStatusUrl(String url, boolean external) {
        SSIUrl apiStatusObj;
        if (!getUrls().containsKey(API_STATUS_KEY)) {
            apiStatusObj = new SSIUrl();
            getUrls().put(API_STATUS_KEY, apiStatusObj);
        }

        apiStatusObj = getUrls().get(API_STATUS_KEY);

        if (external) {
            apiStatusObj.setExternal(url);
        } else {
            apiStatusObj.setInternal(url);
        }
        getUrls().put(API_STATUS_KEY, apiStatusObj);
    }

    @Override
    public String toString() {
        return "SupportServiceInstance [instanceId=" + instanceId
                + ", service_id=" + service_id + ", serviceName=" + serviceName
                + ", serviceShortName=" + serviceShortName
                + ", fullyInitialized=" + fullyInitialized + ", plan_id="
                + plan_id + ", organization_guid=" + organization_guid
                + ", context=" + context + ", space_guid=" + space_guid
                + ", tJobExecIdList=" + tJobExecIdList + ", internalServiceIp="
                + internalServiceIp + ", internalServicePort="
                + internalServicePort + ", bindedServiceIp=" + bindedServiceIp
                + ", bindedServicePort=" + bindedServicePort + ", serviceIp="
                + serviceIp + ", servicePort=" + servicePort + ", containerIp="
                + containerIp + ", containerName=" + containerName
                + ", manifestId=" + manifestId + ", urls=" + urls
                + ", subServices=" + subServices + ", endpointName="
                + endpointName + ", endpointsData=" + endpointsData
                + ", portBindingContainers=" + portBindingContainers
                + ", endpointsBindingsPorts=" + endpointsBindingsPorts
                + ", toString()=" + super.toString() + "]";
    }

    public class SSIUrl {
        @JsonView({ ProvisionView.class, FrontView.class })
        private String internal;

        @JsonView({ ProvisionView.class, FrontView.class })
        private String external;

        public void SupportServiceInstance() {
        }

        public void SupportServiceInstance(String internal, String external) {
            this.internal = internal;
            this.external = external;
        }

        public String getInternal() {
            return internal;
        }

        public void setInternal(String internal) {
            this.internal = internal;
        }

        public String getExternal() {
            return external;
        }

        public void setExternal(String external) {
            this.external = external;
        }

        @Override
        public String toString() {
            return "SSIUrl [internal=" + internal + ", external=" + external
                    + "]";
        }
    }

}