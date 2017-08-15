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
	
	@JsonProperty("elasticsearchUrl")
	private String elasticsearchUrl;
	
	@JsonProperty("rabbitMqConfig")
	private ExternalRabbitConfig rabbitMqconfig;
	
	public ExternalJob(){}
	
	public ExternalJob(String jobName, String executionUrl, String logAnalyzerUrl,  Long tJobExecId, String elasticsearchUrl, ExternalRabbitConfig rabbitMqconfig){
		this.jobName = jobName;
		this.executionUrl = executionUrl;
		this.logAnalyzerUrl = logAnalyzerUrl;
		this.tJobExecId = tJobExecId;
		this.elasticsearchUrl = elasticsearchUrl;		
		this.rabbitMqconfig = rabbitMqconfig;		
	}
		
	@ApiModelProperty(example = "job1", required = true, value = "")
	@NotNull
	public String getJobName() {
		return jobName;
	}
	
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	@ApiModelProperty(example = "http://192.168.99.100:8091/#/projects/2/tjob/8", value = "")	
	public String getExecutionUrl() {
		return executionUrl;
	}
	public void setExecutionUrl(String executionUrl) {
		this.executionUrl = executionUrl;
	}
	
	@ApiModelProperty(example = "http://localhost:4200#/logmanager?indexName=8", value = "")	
	public String getLogAnalyzerUrl() {
		return logAnalyzerUrl;
	}

	public void setLogAnalyzerUrl(String logAnalyzerUrl) {
		this.logAnalyzerUrl = logAnalyzerUrl;
	}

	@ApiModelProperty(example = "0", value = "")
	public Long gettJobExecId() {
		return tJobExecId;
	}

	public void settJobExecId(Long tJobExecId) {
		this.tJobExecId = tJobExecId;
	}
	
	@ApiModelProperty(example = "http://192.168.99.100:9200", value = "")
	public String getElasticsearchUrl() {
		return elasticsearchUrl;
	}

	public void setElasticsearchUrl(String elasticsearchUrl) {
		this.elasticsearchUrl = elasticsearchUrl;
	}
	
	public ExternalRabbitConfig getRabbitMqconfig() {
		return rabbitMqconfig;
	}

	public void setRabbitMqconfig(ExternalRabbitConfig rabbitMqconfig) {
		this.rabbitMqconfig = rabbitMqconfig;
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
				&& Objects.equals(this.tJobExecId, externalJob.tJobExecId);
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
		sb.append("    tJobExecId: ").append(toIndentedString(tJobExecId)).append("\n");
		sb.append("}");
		
		return sb.toString();
	}
	
}
