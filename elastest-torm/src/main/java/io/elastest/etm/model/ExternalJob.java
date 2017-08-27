package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class ExternalJob {
	
	@JsonProperty("jobName")
	private String jobName;

	@JsonProperty("executionUrl")
	private String executionUrl;
	
	@JsonProperty("analyzerUrl")
	private String logAnalyzerUrl;
	
	@JsonProperty("tJobExecId")
	private Long tJobExecId;	
	
	@JsonProperty("logstashPort")	
	private String logstashPort;
	
	@JsonProperty("servicesIp")	
	private String servicesIp;
	
	public ExternalJob(){}
	
	public ExternalJob(String jobName, String executionUrl, String logAnalyzerUrl,  Long tJobExecId,
			String logstashPort, String servicesIp){
		this.jobName = jobName;
		this.executionUrl = executionUrl;
		this.logAnalyzerUrl = logAnalyzerUrl;
		this.tJobExecId = tJobExecId;		
		this.logstashPort = logstashPort;
		this.servicesIp = servicesIp;
	}
		
	@ApiModelProperty(example = "job1", required = true, value = "Job name on any external system.")
	@NotNull
	public String getJobName() {
		return jobName;
	}
	
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@ApiModelProperty(example = "http://192.168.99.100:8091/#/projects/2/tjob/8", value = "URL to acces to TJob execution in ElasTest.")	
	public String getExecutionUrl() {
		return executionUrl;
	}
	public void setExecutionUrl(String executionUrl) {
		this.executionUrl = executionUrl;
	}
	
	@ApiModelProperty(example = "http://localhost:4200#/logmanager?indexName=8", value = "URL to acces to Log Analyzer in ElasTest.")	
	public String getLogAnalyzerUrl() {
		return logAnalyzerUrl;
	}

	public void setLogAnalyzerUrl(String logAnalyzerUrl) {
		this.logAnalyzerUrl = logAnalyzerUrl;
	}

	@ApiModelProperty(example = "0", value = "Id of the TJobExecution created.")
	public Long gettJobExecId() {
		return tJobExecId;
	}

	public void settJobExecId(Long tJobExecId) {
		this.tJobExecId = tJobExecId;
	}	

	@ApiModelProperty(example = "9200", value = "Port where the Logstash service is listening.")
	public String getLogstashPort() {
		return logstashPort;
	}

	public void setLogstashPort(String logstashPort) {
		this.logstashPort = logstashPort;
	}

	@ApiModelProperty(example = "192.168.99.100", value = "IP where ElasTest services are located.")
	public String getServicesIp() {
		return servicesIp;
	}

	public void setServicesIp(String servicesIp) {
		this.servicesIp = servicesIp;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ExternalJob externalJob = (ExternalJob) o;
		return Objects.equals(this.jobName, externalJob.jobName) && Objects.equals(this.executionUrl, externalJob.executionUrl)
				&& Objects.equals(this.logAnalyzerUrl, externalJob.logAnalyzerUrl)
				&& Objects.equals(this.tJobExecId, externalJob.tJobExecId) 
				&& Objects.equals(this.logstashPort, externalJob.logstashPort)
				&& Objects.equals(this.servicesIp, externalJob.servicesIp);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jobName, executionUrl, tJobExecId);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class DeployConfig {\n");
		sb.append("    jobName: ").append(toIndentedString(jobName)).append("\n");
		sb.append("    executionUrl: ").append(toIndentedString(executionUrl)).append("\n");
		sb.append("    logAnalyzerUrl: ").append(toIndentedString(logAnalyzerUrl)).append("\n");
		sb.append("    tJobExecId: ").append(toIndentedString(tJobExecId)).append("\n");
		sb.append("    logstashPort: ").append(toIndentedString(logstashPort)).append("\n");
		sb.append("    servicesIp: ").append(toIndentedString(servicesIp)).append("\n");
		sb.append("}");
		
		return sb.toString();
	}
	
}
