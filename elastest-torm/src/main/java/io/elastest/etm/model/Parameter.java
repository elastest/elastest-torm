package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.SutExecution.SutExecView;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.TJob.BasicAttTJob;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Embeddable
@ApiModel(description = "Environment Variable to pass to a docker container.")
public class Parameter {

    public interface BasicAttParameter {
    }

    @JsonView({ BasicAttParameter.class, BasicAttTJob.class,
            BasicAttProject.class, BasicAttTJobExec.class, SutView.class,
            SutExecView.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name = null;

    @JsonView({ BasicAttParameter.class, BasicAttTJob.class,
            BasicAttProject.class, BasicAttTJobExec.class, SutView.class,
            SutExecView.class })
    @Column(name = "value")
    @JsonProperty("value")
    private String value = null;

    @JsonView({ BasicAttParameter.class, BasicAttTJob.class,
            BasicAttProject.class, BasicAttTJobExec.class, SutView.class,
            SutExecView.class })
    @Column(name = "multiConfig")
    @JsonProperty("multiConfig")
    private Boolean multiConfig = false;

    // Constructors
    public Parameter() {
    }

    public Parameter(String name, String value, Boolean multiConfig) {
        this.name = name;
        this.value = value;
        this.multiConfig = multiConfig != null ? multiConfig : false;
    }

    public Parameter(String name, String value) {
        this.name = name;
        this.value = value;
        this.multiConfig = false;
    }

    public Parameter(Parameter param) {
        this.name = param.name;
        this.multiConfig = param.multiConfig;
        this.value = param.value;
    }

    // Getters and setters
    @ApiModelProperty(example = "ELASTEST_HOME", value = "Name of the Environment Variable.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(example = "/bin/elastest", value = "Value of the Environment Variable.")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @ApiModelProperty(example = "/bin/elastest", value = "Value of the Environment Variable.")

    public Boolean getMultiConfig() {
        return multiConfig;
    }

    public void setMultiConfig(Boolean multiConfig) {
        this.multiConfig = multiConfig != null ? multiConfig : false;
    }

    // Others
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result
                + ((multiConfig == null) ? 0 : multiConfig.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Parameter other = (Parameter) obj;

        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        if (multiConfig == null) {
            if (other.multiConfig != null)
                return false;
        } else if (!multiConfig.equals(other.multiConfig))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Parameter {\n");

        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("    multiConfig: ").append(toIndentedString(multiConfig))
                .append("\n");
        sb.append("}");
        return sb.toString();
    }

}
