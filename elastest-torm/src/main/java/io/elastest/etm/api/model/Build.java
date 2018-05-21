package io.elastest.etm.api.model;

public class Build {
    
    String workspace;
    
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
