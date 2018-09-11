package io.elastest.etm.api.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Object that contains the exchange information between Jenkins and ElasTest.")
public class ExternalJob {

    @JsonProperty("jobName")
    private String jobName;

    @JsonProperty("project")
    private String project;

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

    @JsonProperty("tSServices")
    private List<TestSupportServices> tSServices;

    @JsonProperty("envVars")
    private Map<String, String> envVars;

    @JsonProperty("result")
    private int result;

    @JsonProperty("isReady")
    private boolean isReady;

    @JsonProperty("status")
    private ExternalJobStatusEnum status;

    @JsonProperty("error")
    private String error;

    @JsonProperty("testResultFilePattern")
    private String testResultFilePattern;

    @JsonProperty("testResults")
    private List<String> testResults;

    @JsonProperty("sut")
    private Sut sut;

    @JsonProperty("fromIntegratedJenkins")
    private boolean fromIntegratedJenkins;

    @JsonProperty("buildUrl")
    private String buildUrl;

    @JsonProperty("jobUrl")
    private String jobUrl;

    public ExternalJob() {
    }

    public ExternalJob(String jobName, String executionUrl,
            String logAnalyzerUrl, Long tJobExecId, String logstashPort,
            String servicesIp, List<TestSupportServices> tSServices,
            Map<String, String> envVars, int result, boolean isReady,
            ExternalJobStatusEnum status, String error,
            String testResultFilePattern, List<String> testResults, Sut sut,
            boolean fromIntegratedJenkins, String buildUrl, String jobUrl,
            String project) {
        super();
        this.jobName = jobName;
        this.executionUrl = executionUrl;
        this.logAnalyzerUrl = logAnalyzerUrl;
        this.tJobExecId = tJobExecId;
        this.logstashPort = logstashPort;
        this.servicesIp = servicesIp;
        this.tSServices = tSServices;
        this.envVars = envVars;
        this.result = result;
        this.isReady = isReady;
        this.status = status;
        this.error = error;
        this.testResultFilePattern = testResultFilePattern;
        this.testResults = testResults;
        this.sut = sut;
        this.fromIntegratedJenkins = fromIntegratedJenkins;
        this.buildUrl = buildUrl;
        this.jobUrl = jobUrl;
        this.project = project;
    }

    public enum ExternalJobStatusEnum {
        STARTING("Starting"), READY("Ready"), ERROR("Error");

        private String value;

        ExternalJobStatusEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ExternalJobStatusEnum fromValue(String text) {
            for (ExternalJobStatusEnum b : ExternalJobStatusEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }

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

    @ApiModelProperty(example = "EUS", value = "List where an external Job stores it's TSS.")
    public List<TestSupportServices> getTSServices() {
        return tSServices;
    }

    public void setTSServices(List<TestSupportServices> tSServices) {
        this.tSServices = tSServices;
    }

    @ApiModelProperty(example = "{\"ET_EUS_API\": \"http://dev.elastest.io:37000/eus/v1/execution/tJob_17_220/\"}", value = "Map where the env vars are stored.")
    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public String getTestResultFilePattern() {
        return testResultFilePattern;
    }

    public void setTestResultFilePattern(String testResultFilePattern) {
        this.testResultFilePattern = testResultFilePattern;
    }

    public List<String> getTestResults() {
        return testResults;
    }

    public void setTestResults(List<String> testResults) {
        this.testResults = testResults;
    }

    @ApiModelProperty(example = "Sut object", value = "Object where information related to a Sut is stored.")
    public Sut getSut() {
        return sut;
    }

    public void setSut(Sut sut) {
        this.sut = sut;
    }

    public ExternalJobStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ExternalJobStatusEnum status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isFromIntegratedJenkins() {
        return fromIntegratedJenkins;
    }

    public String getBuildUrl() {
        return buildUrl;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setFromIntegratedJenkins(boolean fromIntegratedJenkins) {
        this.fromIntegratedJenkins = fromIntegratedJenkins;
    }

    public void setBuildUrl(String buildUrl) {
        this.buildUrl = buildUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
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
        return Objects.equals(this.jobName, externalJob.jobName)
                && Objects.equals(this.executionUrl, externalJob.executionUrl)
                && Objects.equals(this.logAnalyzerUrl,
                        externalJob.logAnalyzerUrl)
                && Objects.equals(this.tJobExecId, externalJob.tJobExecId)
                && Objects.equals(this.logstashPort, externalJob.logstashPort)
                && Objects.equals(this.servicesIp, externalJob.servicesIp)
                && Objects.equals(this.tSServices, externalJob.tSServices)
                && Objects.equals(this.envVars, externalJob.envVars)
                && this.result == externalJob.result
                && this.isReady == externalJob.isReady
                && Objects.equals(this.testResultFilePattern,
                        externalJob.testResultFilePattern)
                && Objects.equals(this.testResults, externalJob.testResults)
                && Objects.equals(this.sut, externalJob.sut)
                && Objects.equals(this.status, externalJob.status)
                && Objects.equals(this.error, externalJob.error)
                && this.isFromIntegratedJenkins() == externalJob
                        .isFromIntegratedJenkins()
                && Objects.equals(this.buildUrl, externalJob.buildUrl)
                && Objects.equals(this.jobUrl, externalJob.jobUrl)
                && Objects.equals(this.project, externalJob.project);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobName, executionUrl, logAnalyzerUrl, tJobExecId,
                logstashPort, servicesIp, tSServices, envVars, result, isReady,
                testResultFilePattern, testResults, sut, status, error,
                fromIntegratedJenkins, buildUrl, jobUrl, project);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeployConfig {\n");
        sb.append("    jobName: ").append(toIndentedString(jobName))
                .append("\n");
        sb.append("    executionUrl: ").append(toIndentedString(executionUrl))
                .append("\n");
        sb.append("    logAnalyzerUrl: ")
                .append(toIndentedString(logAnalyzerUrl)).append("\n");
        sb.append("    tJobExecId: ").append(toIndentedString(tJobExecId))
                .append("\n");
        sb.append("    logstashPort: ").append(toIndentedString(logstashPort))
                .append("\n");
        sb.append("    servicesIp: ").append(toIndentedString(servicesIp))
                .append("\n");
        sb.append("    tSServices: ").append(toIndentedString(tSServices))
                .append("\n");
        sb.append("    envVars: ").append(toIndentedString(envVars))
                .append("\n");
        sb.append("    result: ").append(toIndentedString(result)).append("\n");
        sb.append("    isReady: ").append(toIndentedString(isReady))
                .append("\n");
        sb.append("    testResultFilePattern: ")
                .append(toIndentedString(testResultFilePattern)).append("\n");
        sb.append("    testResults: ").append(toIndentedString(testResults))
                .append("\n");
        sb.append("    sut: ").append(toIndentedString(sut)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    error: ").append(toIndentedString(error)).append("\n");
        sb.append("    fromIntegratedJenkins: ")
                .append(toIndentedString(fromIntegratedJenkins)).append("\n");
        sb.append("    buildUrl: ").append(toIndentedString(buildUrl))
                .append("\n");
        sb.append("    jobBuild: ").append(toIndentedString(jobUrl))
                .append("\n");
        sb.append("    project: ").append(toIndentedString(project))
                .append("\n");
        sb.append("}");

        return sb.toString();
    }

}
