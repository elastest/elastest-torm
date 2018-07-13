package io.elastest.etm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Entity that represents an ElasTest Test Support Service")
public class SupportService {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("manifest")
    private TssManifest manifest;

    public SupportService() {

    }

    public SupportService(String id, String name, String shortName,
            TssManifest manifest) {
        super();
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.manifest = manifest;
    }

    /**
     * Get the TSS id
     * 
     * @return Id of the service
     **/
    @ApiModelProperty(example = "1", value = "The service id.")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the name of the TSS
     * 
     * @return The name of the TSS
     **/
    @ApiModelProperty(example = "EUS", value = "The name of the TSS.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the short name of the TSS
     * 
     * @return The short name of the TSS
     **/
    @ApiModelProperty(example = "EUS", value = "The short name of the TSS.")
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @ApiModelProperty(example = "EUS", value = "The manifest of the TSS.")
    public TssManifest getManifest() {
        return manifest;
    }

    public void setManifest(TssManifest manifest) {
        this.manifest = manifest;
    }

    @Override
    public String toString() {
        return "SupportService [id=" + id + ", name=" + name + ", shortName="
                + shortName + ", manifest=" + manifest + "]";
    }


    
    

}
