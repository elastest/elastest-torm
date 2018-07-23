package io.elastest.etm.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Entity that represents an ElasTest Test Support Service")
@SuppressWarnings("rawtypes")
public class SupportService {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("short_name")
    @JsonAlias("shortName")
    private String shortName;

    @JsonProperty("manifest")
    private TssManifest manifest;

    @JsonProperty("bindable")
    private boolean bindable;

    @JsonProperty("description")
    private String description;

    @JsonProperty("plans")
    private List plans;

    @JsonProperty("plan_updateable")
    @JsonAlias("planUpdateable")
    private boolean planUpdateable;

    @JsonProperty("requires")
    private List requires;

    @JsonProperty("tags")
    private List tags;

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

    public SupportService(String id, String name, String shortName,
            TssManifest manifest, boolean bindable, String description,
            List plans, boolean planUpdateable, List requires, List tags) {
        super();
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.manifest = manifest;
        this.bindable = bindable;
        this.description = description;
        this.plans = plans;
        this.planUpdateable = planUpdateable;
        this.requires = requires;
        this.tags = tags;
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

    public boolean isBindable() {
        return bindable;
    }

    public void setBindable(boolean bindable) {
        this.bindable = bindable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List getPlans() {
        return plans;
    }

    public void setPlans(List plans) {
        this.plans = plans;
    }

    public boolean isPlanUpdateable() {
        return planUpdateable;
    }

    public void setPlanUpdateable(boolean planUpdateable) {
        this.planUpdateable = planUpdateable;
    }

    public List getRequires() {
        return requires;
    }

    public void setRequires(List requires) {
        this.requires = requires;
    }

    public List getTags() {
        return tags;
    }

    public void setTags(List tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "SupportService [id=" + id + ", name=" + name + ", shortName="
                + shortName + ", manifest=" + manifest + ", bindable="
                + bindable + ", description=" + description + ", plans=" + plans
                + ", planUpdateable=" + planUpdateable + ", requires="
                + requires + ", tags=" + tags + "]";
    }

}
