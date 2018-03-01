package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Object that stores the relevant information of the services to be used by third parties.")
public class ContextInfo {

    @JsonProperty("elasticSearchUrl")
    private String elasticSearchUrl;

    @JsonProperty("logstashHttpUrl")
    private String logstashHttpUrl;

    @JsonProperty("logstashHttpPort")
    private String logstashHttpPort;

    @JsonProperty("logstashTcpHost")
    private String logstashTcpHost;

    @JsonProperty("logstashTcpPort")
    private String logstashTcpPort;

    @JsonProperty("logstashBeatsHost")
    private String logstashBeatsHost;

    @JsonProperty("logstashBeatsPort")
    private String logstashBeatsPort;

    @JsonProperty("logstashIp")
    private String logstashIp;

    @JsonProperty("rabbitPath")
    private String rabbitPath;

    @JsonProperty("elasTestExecMode")
    private String elasTestExecMode;

    @JsonProperty("eusSSInstance")
    private SupportServiceInstance eusSSInstance;

    @JsonProperty("testLinkStarted")
    private boolean testLinkStarted;

    public String getElasticSearchUrl() {
        return elasticSearchUrl;
    }

    public void setElasticSearchUrl(String elasticSearchUrl) {
        this.elasticSearchUrl = elasticSearchUrl;
    }

    public String getLogstashHttpUrl() {
        return logstashHttpUrl;
    }

    public void setLogstashHttpUrl(String logstashHttpUrl) {
        this.logstashHttpUrl = logstashHttpUrl;
    }

    public String getLogstashHttpPort() {
        return logstashHttpPort;
    }

    public void setLogstashHttpPort(String logstashHttpPort) {
        this.logstashHttpPort = logstashHttpPort;
    }

    public String getLogstashTcpHost() {
        return logstashTcpHost;
    }

    public void setLogstashTcpHost(String logstashTcpHost) {
        this.logstashTcpHost = logstashTcpHost;
    }

    public String getLogstashTcpPort() {
        return logstashTcpPort;
    }

    public void setLogstashTcpPort(String logstashTcpPort) {
        this.logstashTcpPort = logstashTcpPort;
    }

    public String getLogstashBeatsHost() {
        return logstashBeatsHost;
    }

    public void setLogstashBeatsHost(String logstashBeatsHost) {
        this.logstashBeatsHost = logstashBeatsHost;
    }

    public String getLogstashBeatsPort() {
        return logstashBeatsPort;
    }

    public void setLogstashBeatsPort(String logstashBeatsPort) {
        this.logstashBeatsPort = logstashBeatsPort;
    }

    public String getLogstashIp() {
        return logstashIp;
    }

    public void setLogstashIp(String logstashIp) {
        this.logstashIp = logstashIp;
    }

    public String getRabbitPath() {
        return rabbitPath;
    }

    public void setRabbitPath(String rabbitPath) {
        this.rabbitPath = rabbitPath;
    }

    public String getElasTestExecMode() {
        return elasTestExecMode;
    }

    public void setElasTestExecMode(String elasTestExecMode) {
        this.elasTestExecMode = elasTestExecMode;
    }

    public SupportServiceInstance getEusSSInstance() {
        return eusSSInstance;
    }

    public void setEusSSInstance(SupportServiceInstance eusSSInstance) {
        this.eusSSInstance = eusSSInstance;
    }

    public boolean isTestLinkStarted() {
        return testLinkStarted;
    }

    public void setTestLinkStarted(boolean testLinkStarted) {
        this.testLinkStarted = testLinkStarted;
    }

}
