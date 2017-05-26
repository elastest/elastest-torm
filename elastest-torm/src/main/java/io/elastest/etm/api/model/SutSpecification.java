package io.elastest.etm.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.ApiModelProperty;

/**
 * SutSpecification
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Entity
public class SutSpecification {
	
	public interface SutView {
	}
		
	@Id
	@JsonView(SutView.class)
	@JsonProperty("id")
	private Long id = null;

	@JsonView(SutView.class)
	@JsonProperty("name")
	private String name = null;

	@JsonView(SutView.class)
	@JsonProperty("specification")
	private String specification = null;

	@JsonView(SutView.class)
	@JsonProperty("description")
	private String description = null;

	@JsonView(SutView.class)
	@JsonProperty("deployedSut")
	@OneToMany(mappedBy="sutSpecification", fetch = FetchType.LAZY)
	private List<DeployedSut> deployedSut = null;
		
	@JsonView(SutView.class)
	@JsonProperty("sutExecution")
	@OneToMany(mappedBy="sutSpecification", fetch = FetchType.LAZY)
	private List<SutExecution> sutExecution = null;

	@JsonProperty("project")
	@ManyToOne(fetch=FetchType.LAZY)
	private Project project = null;
	

	public SutSpecification id(Long id) {
		this.id = id==null? 0: id;
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
		this.id = id==null? 0: id;
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

	public SutSpecification description(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Get desc
	 * 
	 * @return desc
	 **/
	@ApiModelProperty(example = "This is a SuT description example", value = "")

	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
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
	
	
	public List<SutExecution> getSutExecution() {
		return sutExecution;
	}

	public void setSutExecution(List<SutExecution> sutExecution) {
		this.sutExecution = sutExecution;
	}
	
	public SutExecution addSuTExecution(SutExecution sutExecution) {
		getSutExecution().add(sutExecution);
		sutExecution.setSutSpecification(this);

		return sutExecution;
	}

	public SutExecution removeSuTExecution(SutExecution sutExecution) {
		getSutExecution().remove(sutExecution);
		sutExecution.setSutSpecification(null);

		return sutExecution;
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
	
	public SutSpecification project(Project project){
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
				&& Objects.equals(this.description, sutSpecification.description)
				&& Objects.equals(this.deployedSut, sutSpecification.deployedSut)
				&& Objects.equals(this.project, sutSpecification.project);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, specification, description, deployedSut, project);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class SutSpecification {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    specification: ").append(toIndentedString(specification)).append("\n");
		sb.append("    desc: ").append(toIndentedString(description)).append("\n");
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
