package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;

public class SupportServiceInstance {

	public interface ProvisionView {
	}

	public interface FrontView {
	}
	
	/**
	 * Gets or Sets status
	 */
	public enum SSIStatusEnum {
		INITIALIZATION("INITIALIZATION"),

		FAILURE("FAILURE"),

		READY("READY"),
		
		STOPPING("STOPPING");

		private String value;

		SSIStatusEnum(String value) {
			this.value = value;
		}
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
	@JsonProperty("serviceReady")
	private boolean serviceReady;
	
	@JsonView(FrontView.class)
	@JsonProperty("serviceStatus")
	private SSIStatusEnum serviceStatus;
	
	@JsonView(ProvisionView.class)
	@JsonProperty("plan_id")	
	private String plan_id;
	
	@JsonView(ProvisionView.class)
	@JsonProperty("organization_guid")
	private String organization_guid;
	
	@JsonView({ProvisionView.class, FrontView.class})
	@JsonProperty("parameters")
	private Map<String, String> parameters;
	
	@JsonView(ProvisionView.class)
	@JsonProperty("context")
	private Map<String, String> context;
	
	@JsonView(ProvisionView.class)
	@JsonProperty("space_guid")
	private String space_guid;

	@JsonView(FrontView.class)
	@JsonProperty("tJobExecId")
	private Long tJobExecId;
	
	@JsonView(FrontView.class)
	@JsonProperty("serviceIp")
	private String serviceIp;
	
	@JsonView(FrontView.class)
	@JsonProperty("servicePort")
	private int servicePort;
	
	@JsonProperty("containerIp")
	private String containerIp;

	@JsonProperty("manifestId")
	private String manifestId;

	@JsonView(FrontView.class)
	@JsonProperty("urls")
	private Map<String, String> urls;
	
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

	public SupportServiceInstance() {
		this.urls = new HashMap<>();
		this.subServices = new ArrayList<>();
		this.endpointsData = new HashMap<>();
		this.portBindingContainers = new ArrayList<>();
		this.serviceReady = false;
		this.serviceStatus = SSIStatusEnum.INITIALIZATION;
		this.endpointsBindingsPorts = new HashMap<>();
		
	}

	public SupportServiceInstance(String instanceId, String service_id, String serviceName, String serviceShortName, String plan_id, Long bindToTJob) {
		this();
		this.instanceId = instanceId;
		this.service_id = service_id;
		this.serviceName = serviceName;
		this.serviceShortName = serviceShortName;
		this.plan_id = plan_id;
		this.tJobExecId = bindToTJob;
		this.parameters = new HashMap<>();
		this.context = new HashMap<>();
		this.organization_guid = "org";
		this.space_guid = "space";
		this.manifestId = "";
	}

	public String getService_id() {
		return service_id;
	}

	public void setService_id(String service_id) {
		this.service_id = service_id;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceShortName() {
		return serviceShortName;
	}

	public void setServiceShortName(String serviceShortName) {
		this.serviceShortName = serviceShortName;
	}
	
	public boolean isServiceReady() {
		return serviceReady;
	}

	public void setServiceReady(boolean serviceInstanceUp) {
		this.serviceReady = serviceInstanceUp;
	}
	
	public SSIStatusEnum getServiceStatus() {
		return serviceStatus;
	}

	public void setServiceStatus(SSIStatusEnum serviceStatus) {
		this.serviceStatus = serviceStatus;
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

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getSpace_guid() {
		return space_guid;
	}

	public Long gettJobExecId() {
		return tJobExecId;
	}

	public void settJobExecId(Long tJobExecId) {
		this.tJobExecId = tJobExecId;
	}

	public void setSpace_guid(String space_guid) {
		this.space_guid = space_guid;
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

	public void setEndpointsBindingsPorts(Map<String, String> endpointsBindingsPorts) {
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

	public Map<String, String> getUrls() {
		return urls;
	}

	public void setUrls(Map<String, String> urls) {
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

	public Map<String, String> getContext() {
		return context;
	}

	public void setContext(Map<String, String> context) {
		this.context = context;
	}

}
