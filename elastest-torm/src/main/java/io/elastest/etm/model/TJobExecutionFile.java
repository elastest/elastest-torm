package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Entity that represents a file generated during a TJob execution.")
public class TJobExecutionFile {
	
	@JsonProperty("name")
	private String name;
	@JsonProperty("url")
	private String url;
	@JsonProperty("serviceName")
	private String serviceName;
	
	public TJobExecutionFile(String name, String url, String serviceName) {
		super();
		this.name = name;
		this.url = url;
		this.serviceName = serviceName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
}
