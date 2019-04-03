package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Object that stores the relevant information of the services to be used by third parties.")
public class ContextInfo {

    @JsonProperty("elasticSearchUrl")
    private String elasticSearchUrl;

    @JsonProperty("elasticSearchSSlUrl")
    private String elasticSearchSSlUrl;

    @JsonProperty("elasticsearchPath")
    private String elasticsearchPath;

    @JsonProperty("logstashHttpUrl")
    private String logstashHttpUrl;

    @JsonProperty("logstashSSLHttpUrl")
    private String logstashSSLHttpUrl;

    @JsonProperty("logstashHttpPort")
    private String logstashHttpPort;

    @JsonProperty("logstashTcpHost")
    private String logstashTcpHost;

    @JsonProperty("logstashTcpPort")
    private String logstashTcpPort;

    @JsonProperty("logstashInternalTcpPort")
    private String logstashInternalTcpPort;

    @JsonProperty("logstashBeatsHost")
    private String logstashBeatsHost;

    @JsonProperty("logstashBeatsPort")
    private String logstashBeatsPort;

    @JsonProperty("internalLogstashBeatsPort")
    private String internalLogstashBeatsPort;

    @JsonProperty("logstashBindedTcpHost")
    private String logstashBindedTcpHost;

    @JsonProperty("logstashBindedTcpPort")
    private String logstashBindedTcpPort;

    @JsonProperty("logstashBindedInternalTcpPort")
    private String logstashBindedInternalTcpPort;

    @JsonProperty("logstashBindedBeatsHost")
    private String logstashBindedBeatsHost;

    @JsonProperty("logstashBindedBeatsPort")
    private String logstashBindedBeatsPort;

    @JsonProperty("logstashBindedInternalBeatsPort")
    private String logstashBindedInternalBeatsPort;

    @JsonProperty("logstashIp")
    private String logstashIp;

    @JsonProperty("logstashPath")
    private String logstashPath;

    @JsonProperty("rabbitPath")
    private String rabbitPath;

    @JsonProperty("elasTestExecMode")
    private String elasTestExecMode;

    @JsonProperty("eusSSInstance")
    private SupportServiceInstance eusSSInstance;

    @JsonProperty("testLinkStarted")
    private boolean testLinkStarted;

    @JsonProperty("empGrafanaUrl")
    private String empGrafanaUrl;

    @JsonProperty("edmCommandUrl")
    private String edmCommandUrl;

    @JsonProperty("etmApiUrl")
    private String etmApiUrl;

    public ContextInfo() {
    }

    // Getters & Setters

    public String getElasticSearchUrl() {
        return elasticSearchUrl;
    }

    public void setElasticSearchUrl(String elasticSearchUrl) {
        this.elasticSearchUrl = elasticSearchUrl;
    }

    public String getElasticSearchSSlUrl() {
        return elasticSearchSSlUrl;
    }

    public void setElasticSearchSSlUrl(String elasticSearchSSlUrl) {
        this.elasticSearchSSlUrl = elasticSearchSSlUrl;
    }

    public String getElasticsearchPath() {
        return elasticsearchPath;
    }

    public void setElasticsearchPath(String elasticsearchPath) {
        this.elasticsearchPath = elasticsearchPath;
    }

    public String getLogstashHttpUrl() {
        return logstashHttpUrl;
    }

    public void setLogstashHttpUrl(String logstashHttpUrl) {
        this.logstashHttpUrl = logstashHttpUrl;
    }

    public String getLogstashSSLHttpUrl() {
        return logstashSSLHttpUrl;
    }

    public void setLogstashSSLHttpUrl(String logstashSSLHttpUrl) {
        this.logstashSSLHttpUrl = logstashSSLHttpUrl;
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

    public String getLogstashInternalTcpPort() {
        return logstashInternalTcpPort;
    }

    public void setLogstashInternalTcpPort(String logstashInternalTcpPort) {
        this.logstashInternalTcpPort = logstashInternalTcpPort;
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

    public String getInternalLogstashBeatsPort() {
        return internalLogstashBeatsPort;
    }

    public void setInternalLogstashBeatsPort(String internalLogstashBeatsPort) {
        this.internalLogstashBeatsPort = internalLogstashBeatsPort;
    }

    public String getLogstashBindedTcpHost() {
        return logstashBindedTcpHost;
    }

    public void setLogstashBindedTcpHost(String logstashBindedTcpHost) {
        this.logstashBindedTcpHost = logstashBindedTcpHost;
    }

    public String getLogstashBindedTcpPort() {
        return logstashBindedTcpPort;
    }

    public void setLogstashBindedTcpPort(String logstashBindedTcpPort) {
        this.logstashBindedTcpPort = logstashBindedTcpPort;
    }

    public String getLogstashBindedInternalTcpPort() {
        return logstashBindedInternalTcpPort;
    }

    public void setLogstashBindedInternalTcpPort(
            String logstashBindedInternalTcpPort) {
        this.logstashBindedInternalTcpPort = logstashBindedInternalTcpPort;
    }

    public String getLogstashBindedBeatsHost() {
        return logstashBindedBeatsHost;
    }

    public void setLogstashBindedBeatsHost(String logstashBindedBeatsHost) {
        this.logstashBindedBeatsHost = logstashBindedBeatsHost;
    }

    public String getLogstashBindedBeatsPort() {
        return logstashBindedBeatsPort;
    }

    public void setLogstashBindedBeatsPort(String logstashBindedBeatsPort) {
        this.logstashBindedBeatsPort = logstashBindedBeatsPort;
    }

    public String getLogstashBindedInternalBeatsPort() {
        return logstashBindedInternalBeatsPort;
    }

    public void setLogstashBindedInternalBeatsPort(
            String logstashBindedInternalBeatsPort) {
        this.logstashBindedInternalBeatsPort = logstashBindedInternalBeatsPort;
    }

    public String getLogstashIp() {
        return logstashIp;
    }

    public void setLogstashIp(String logstashIp) {
        this.logstashIp = logstashIp;
    }

    public String getLogstashPath() {
        return logstashPath;
    }

    public void setLogstashPath(String logstashPath) {
        this.logstashPath = logstashPath;
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

    public String getEmpGrafanaUrl() {
        return empGrafanaUrl;
    }

    public void setEmpGrafanaUrl(String empGrafanaUrl) {
        this.empGrafanaUrl = empGrafanaUrl;
    }

    public String getEdmCommandUrl() {
        return edmCommandUrl;
    }

    public void setEdmCommandUrl(String edmCommandUrl) {
        this.edmCommandUrl = edmCommandUrl;
    }

    public String getEtmApiUrl() {
        return etmApiUrl;
    }

    public void setEtmApiUrl(String etmApiUrl) {
        this.etmApiUrl = etmApiUrl;
    }

    @Override
    public String toString() {
        return "ContextInfo [elasticSearchUrl=" + elasticSearchUrl
                + ", elasticSearchSSlUrl=" + elasticSearchSSlUrl
                + ", elasticsearchPath=" + elasticsearchPath
                + ", logstashHttpUrl=" + logstashHttpUrl
                + ", logstashSSLHttpUrl=" + logstashSSLHttpUrl
                + ", logstashHttpPort=" + logstashHttpPort
                + ", logstashTcpHost=" + logstashTcpHost + ", logstashTcpPort="
                + logstashTcpPort + ", logstashInternalTcpPort="
                + logstashInternalTcpPort + ", logstashBeatsHost="
                + logstashBeatsHost + ", logstashBeatsPort=" + logstashBeatsPort
                + ", internalLogstashBeatsPort=" + internalLogstashBeatsPort
                + ", logstashBindedTcpHost=" + logstashBindedTcpHost
                + ", logstashBindedTcpPort=" + logstashBindedTcpPort
                + ", logstashBindedInternalTcpPort="
                + logstashBindedInternalTcpPort + ", logstashBindedBeatsHost="
                + logstashBindedBeatsHost + ", logstashBindedBeatsPort="
                + logstashBindedBeatsPort + ", logstashBindedInternalBeatsPort="
                + logstashBindedInternalBeatsPort + ", logstashIp=" + logstashIp
                + ", logstashPath=" + logstashPath + ", rabbitPath="
                + rabbitPath + ", elasTestExecMode=" + elasTestExecMode
                + ", eusSSInstance=" + eusSSInstance + ", testLinkStarted="
                + testLinkStarted + ", empGrafanaUrl=" + empGrafanaUrl
                + ", edmCommandUrl=" + edmCommandUrl + ", etmApiUrl="
                + etmApiUrl + "]";
    }

}
