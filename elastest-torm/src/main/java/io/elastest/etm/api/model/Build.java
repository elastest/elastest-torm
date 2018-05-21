package io.elastest.etm.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Build {
    
    @JsonProperty("workspace")
    String workspace;
    
    public Build() {
        
    }
    
    public Build(String workspace) {
        this.workspace = workspace;        
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

}
