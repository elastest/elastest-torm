package io.elastest.etm.model;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ServiceInstance {
	
	private String service_id;
	private String plan_id;
	//private ObjectNode context;
	private String organization_guid;
	private ObjectNode parameters;
	private String space_guid;
	
	@JsonIgnore
	private boolean bindedToTJob;
		
	public ServiceInstance(String service_id, String plan_id, boolean bindToTJob) {
		super();
		this.service_id = service_id;
		this.plan_id = plan_id;
		this.bindedToTJob = bindToTJob;
		//this.context = new ObjectNode(JsonNodeFactory.instance);
		this.parameters =  new ObjectNode(JsonNodeFactory.instance);
		this.organization_guid = "org";
		this.space_guid = "space";		
	}
	public ServiceInstance(String service_id, String plan_id,/* ObjectNode context,*/ String organization_guid,
			ObjectNode parameters, String space_guid, boolean bindToTJob) {
		super();
		this.service_id = service_id;
		this.plan_id = plan_id;
		//this.context = context;
		this.organization_guid = organization_guid;
		this.parameters = parameters;
		this.space_guid = space_guid;
		this.bindedToTJob = bindToTJob;
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
//	public ObjectNode getContext() {
//		return context;
//	}
//	public void setContext(ObjectNode context) {
//		this.context = context;
//	}
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
	
}
