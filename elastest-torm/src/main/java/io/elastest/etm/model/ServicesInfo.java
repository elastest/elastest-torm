package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Object that stores the relevant information of the services to be used by third parties.")
public class ServicesInfo {
    
    @JsonProperty("elasticSearchUrl")
    private String elasticSearchUrl;
    @JsonProperty("rabbitPath")
    private String rabbitPath;
    
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
}
