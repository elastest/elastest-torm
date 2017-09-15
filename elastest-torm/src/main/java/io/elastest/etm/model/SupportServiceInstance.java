package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SupportServiceInstance {
	
	public interface ProvisionView {		
	}
	
	public interface FrontView {		
	}
	
	@JsonView(FrontView.class)
	private String instanceId;
	@JsonView({ProvisionView.class, FrontView.class})
	private String service_id;
	@JsonView(ProvisionView.class)
	private String plan_id;
	@JsonView(ProvisionView.class)
	private String organization_guid;
	@JsonView(ProvisionView.class)
	private ObjectNode parameters;
	@JsonView(ProvisionView.class)
	private String space_guid;
	
	private boolean bindedToTJob;
	@JsonView(FrontView.class)
	private String serviceIp;	
	@JsonView(FrontView.class)
	private String servicePort;
	@JsonView(FrontView.class)
	private String uiURL;
	@JsonView(FrontView.class)
	private String apiURL;
		
	public SupportServiceInstance(String instanceId, String service_id, String plan_id, boolean bindToTJob) {
		super();
		this.instanceId = instanceId;
		this.service_id = service_id;
		this.plan_id = plan_id;
		this.bindedToTJob = bindToTJob;
		this.parameters =  new ObjectNode(JsonNodeFactory.instance);
		this.organization_guid = "org";
		this.space_guid = "space";		
	}

	public SupportServiceInstance(String instanceId, String service_id, String plan_id, String organization_guid,
			ObjectNode parameters, String space_guid, boolean bindedToTJob, String serviceIp, String servicePort,
			String uiURL, String apiURL) {
		super();
		this.instanceId = instanceId;
		this.service_id = service_id;
		this.plan_id = plan_id;
		this.organization_guid = organization_guid;
		this.parameters = parameters;
		this.space_guid = space_guid;
		this.bindedToTJob = bindedToTJob;
		this.serviceIp = serviceIp;
		this.servicePort = servicePort;
		this.uiURL = uiURL;
		this.apiURL = apiURL;
	}

	public String getService_id() {
		return service_id;
	}
	public void setService_id(String service_id) {
		this.service_id = service_id;
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
	public ObjectNode getParameters() {
		return parameters;
	}
	public void setParameters(ObjectNode parameters) {
		this.parameters = parameters;
	}
	public String getSpace_guid() {
		return space_guid;
	}
	public void setSpace_guid(String space_guid) {
		this.space_guid = space_guid;
	}
	public boolean isBindToTJob() {
		return bindedToTJob;
	}
	public void setBindToTJob(boolean bindToTJob) {
		this.bindedToTJob = bindToTJob;
	}

	public String getServiceIp() {
		return serviceIp;
	}

	public void setServiceIp(String serviceIp) {
		this.serviceIp = serviceIp;
	}

	public String getServicePort() {
		return servicePort;
	}

	public void setServicePort(String servicePort) {
		this.servicePort = servicePort;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public boolean isBindedToTJob() {
		return bindedToTJob;
	}

	public void setBindedToTJob(boolean bindedToTJob) {
		this.bindedToTJob = bindedToTJob;
	}

	public String getUiURL() {
		return uiURL;
	}

	public void setUiURL(String uiURL) {
		this.uiURL = uiURL;
	}

	public String getApiURL() {
		return apiURL;
	}

	public void setApiURL(String apiURL) {
		this.apiURL = apiURL;
	}
	
}
