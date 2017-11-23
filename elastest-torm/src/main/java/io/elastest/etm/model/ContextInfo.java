package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Object that stores the relevant information of the services to be used by third parties.")
public class ContextInfo {
    
    @JsonProperty("elasticSearchUrl")
    private String elasticSearchUrl;
    @JsonProperty("rabbitPath")
    private String rabbitPath;
    @JsonProperty("elasTestExecMode")
    private String elasTestExecMode;
    @JsonProperty("eusSSInstance")
    private SupportServiceInstance eusSSInstance;
    
    public String getElasticSearchUrl() {
        return elasticSearchUrl;
    }

    public void setElasticSearchUrl(String elasticSearchUrl) {
        this.elasticSearchUrl = elasticSearchUrl;
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
}
