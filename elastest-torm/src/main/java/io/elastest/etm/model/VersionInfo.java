package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Object that stores the relevant information about the version of an ElasTest module.")
public class VersionInfo {
    
    @JsonProperty("commitId")
    private String commitId;
    @JsonProperty("date")
    private String date;
    @JsonProperty("name")
    private String name;
    
    public VersionInfo(String commitId, String date, String name) {
        super();
        this.commitId = commitId;
        this.date = date;
        this.name = name;
    }
    public String getCommitId() {
        return commitId;
    }
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    

}
