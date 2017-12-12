package io.elastest.etm.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(description = "Object that stores the relevant information about the ElasTest Version and it's modules.")
public class HelpInfo {
    
    @JsonProperty("versionsInfo")
    private Map<String, VersionInfo> versionsInfo;

    public HelpInfo() {
        super();
        this.versionsInfo = new HashMap<>();
    }

    public Map<String, VersionInfo> getVersionsInfo() {
        return versionsInfo;
    }

    public void setVersionsInfo(Map<String, VersionInfo> versionsInfo) {
        this.versionsInfo = versionsInfo;
    }

}
