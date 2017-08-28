package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.TJob.BasicAttTJob;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Embeddable
@ApiModel(description = "Environment Variable to pass to a docker container.")
public class Parameter {

	public interface BasicAttParameter {
	}

	@JsonView({ BasicAttParameter.class, BasicAttTJob.class, BasicAttProject.class, BasicAttTJobExec.class })
	@Column(name = "name")
	@JsonProperty("name")
	private String name = null;

	@JsonView({ BasicAttParameter.class, BasicAttTJob.class, BasicAttProject.class, BasicAttTJobExec.class })
	@Column(name = "value")
	@JsonProperty("value")
	private String value = null;

	// Constructors
	public Parameter() {
	}

	// Getters and setters

	public Parameter(Long id, String name, String value, TJob tJob, TJobExecution tJobExec) {
		this.name = name;
		this.value = value;

	}

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

	// Others

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Parameter {\n");

		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    value: ").append(toIndentedString(value)).append("\n");
		sb.append("}");
		return sb.toString();
	}

}
