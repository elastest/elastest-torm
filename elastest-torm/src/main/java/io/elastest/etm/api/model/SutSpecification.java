package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;

import io.elastest.etm.api.model.DeployedSut;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * SutSpecification
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

public class SutSpecification {
	@JsonProperty("id")
	private Long id = null;

	@JsonProperty("name")
	private String name = null;

	@JsonProperty("specification")
	private String specification = null;

	@JsonProperty("desc")
	private String desc = null;

	@JsonProperty("deployedSut")
	private List<DeployedSut> deployedSut = null;

	@JsonProperty("project")
	@ManyToOne(fetch=FetchType.LAZY)
	private Project project = null;

	public SutSpecification id(Long id) {
		this.id = id;
		return this;
	}

	/**
	 * Get id
	 * 
	 * @return id
	 **/
	@ApiModelProperty(value = "")

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SutSpecification name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Get name
	 * 
	 * @return name
	 **/
	@ApiModelProperty(example = "sut definition 1", required = true, value = "")
	@NotNull

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SutSpecification specification(String specification) {
		this.specification = specification;
		return this;
	}

	/**
	 * Get specification
	 * 
	 * @return specification
	 **/
	@ApiModelProperty(example = "", required = true, value = "")
	@NotNull

	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}

	public SutSpecification desc(String desc) {
		this.desc = desc;
		return this;
	}

	/**
	 * Get desc
	 * 
	 * @return desc
	 **/
	@ApiModelProperty(example = "This is a SuT description example", value = "")

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public SutSpecification deployedSut(List<DeployedSut> deployedSut) {
		this.deployedSut = deployedSut;
		return this;
	}

	public SutSpecification addDeployedSutItem(DeployedSut deployedSutItem) {
		if (this.deployedSut == null) {
			this.deployedSut = new ArrayList<DeployedSut>();
		}
		this.deployedSut.add(deployedSutItem);
		return this;
	}

	/**
	 * Get deployedSut
	 * 
	 * @return deployedSut
	 **/
	@ApiModelProperty(value = "")

	@Valid

	public List<DeployedSut> getDeployedSut() {
		return deployedSut;
	}

	public void setDeployedSut(List<DeployedSut> deployedSut) {
		this.deployedSut = deployedSut;
	}
	
	/**
	 * Get name
	 * 
	 * @return name
	 **/
	@ApiModelProperty(example = "12345678", required = true, value = "")
	@NotNull
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
	public SutSpecification desc(Project project) {
		this.project = project;
		return this;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SutSpecification sutSpecification = (SutSpecification) o;
		return Objects.equals(this.id, sutSpecification.id) && Objects.equals(this.name, sutSpecification.name)
				&& Objects.equals(this.specification, sutSpecification.specification)
				&& Objects.equals(this.desc, sutSpecification.desc)
				&& Objects.equals(this.deployedSut, sutSpecification.deployedSut)
				&& Objects.equals(this.project, sutSpecification.project);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, specification, desc, deployedSut, project);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class SutSpecification {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    specification: ").append(toIndentedString(specification)).append("\n");
		sb.append("    desc: ").append(toIndentedString(desc)).append("\n");
		sb.append("    deployedSut: ").append(toIndentedString(deployedSut)).append("\n");
		sb.append("    project: ").append(toIndentedString(project)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
