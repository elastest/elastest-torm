package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.TJob.BasicAttTJob;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * SutSpecification
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Entity
@ApiModel(description = "SUT definition.")
public class SutSpecification {

	public interface SutView {
	}

	@Id
	@JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class, BasicAttTJobExec.class })
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonProperty("id")
	private Long id = null;

	@JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
	@JsonProperty("name")
	private String name = null;

	@JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
	@Column(name = "specification")
	@JsonProperty("specification")
	private String specification = null;

	@JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
	@JsonProperty("description")
	private String description = null;

	@JsonProperty("sutExecution")
	@OneToMany(mappedBy = "sutSpecification", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<SutExecution> sutExecution = null;

	@JsonView(SutView.class)
	@JsonProperty("project")
	@ManyToOne(fetch = FetchType.LAZY)
	private Project project = null;

	@JsonProperty("tjobs")
	@OneToMany(mappedBy = "sut")
	private List<TJob> tJobs;
	
	@JsonView({ SutView.class, BasicAttProject.class, BasicAttTJob.class })
	@Column(name = "sutType", nullable=false)
	@JsonProperty("sutType")
	private SutTypeEnum sutType;


	public SutSpecification() {
	}

	public SutSpecification(Long id, String name, String specification, String description, Project project,
			List<TJob> tJobs, SutTypeEnum sutType) {
		this.id = id == null ? 0 : id;
		this.name = name;		
		this.specification = specification;
		this.description = description;
		this.project = project;
		this.tJobs = tJobs;
		this.sutType = sutType;
	}

	
	
	public enum SutTypeEnum {
		IMAGENAME("IMAGENAME"),

		REPOSITORY("REPOSITORY"),

		EXTERNAL("EXTERNAL");

		private String value;

		SutTypeEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static SutTypeEnum fromValue(String text) {
			for (SutTypeEnum b : SutTypeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}
	
	/**
	 * Get id
	 * 
	 * @return id
	 **/
	@ApiModelProperty(value = "Value that identifies the SUT Specification.")

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id == null ? 0 : id;
	}

	/**
	 * Get name
	 * 
	 * @return name
	 **/
	@ApiModelProperty(example = "sut definition 1", required = true, value = "Name of the SUT.")
	@NotNull

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Specification
	 * 
	 * @return specification
	 **/
	@ApiModelProperty(example = "https://github.com/elastest/elastest-torm.git", required = true, value = "URL of the GitHub repository where the SUT code is stored (not necessary if the ImageName field is filled)")
	@NotNull
	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}

	/**
	 * Get desc
	 * 
	 * @return desc
	 **/
	@ApiModelProperty(example = "My Web Application", value = "Brief description of a SUT")
	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}


	/**
	 * Get sutExecution
	 * 
	 * @return sutExecution
	 **/
	@ApiModelProperty(value = "List of the SUT Executions of a SUT")
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
	 * Get project
	 * 
	 * @return project
	 **/
	@ApiModelProperty(required = true, value = "Project to which the SUT is associated", example = "{ id:\"1\" }")
	@NotNull
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Get tJobs
	 * 
	 * @return tJobs
	 **/

	@Valid
	@ApiModelProperty( value = "List of TJobs associated to a SUT Specification")
	public List<TJob> getTJobs() {
		return tJobs;
	}

	public void setTJobs(List<TJob> tJobs) {
		this.tJobs = tJobs;
	}

	public TJob addTJob(TJob tJob) {
		getTJobs().add(tJob);
		tJob.setSut(this);

		return tJob;
	}

	public TJob removeTJob(TJob tJob) {
		getTJobs().remove(tJob);
		tJob.setSut(null);

		return tJob;
	}
	
	
	/**
	 * Get sutType
	 * 
	 * @return sutType
	 **/
	
	public SutTypeEnum getSutType() {
		return sutType;
	}

	public void setSutType(SutTypeEnum sutType) {
		this.sutType = sutType;
	}
	
	
	// Other methods
	
	public String sutBy() {
		if(this.sutType == SutTypeEnum.REPOSITORY){
			return "repository";
		}

		if(this.sutType == SutTypeEnum.IMAGENAME){
			return "imageName";
		}
		
		if(this.sutType == SutTypeEnum.EXTERNAL){
			return "external";
		}
		
		return "";
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
		return Objects.equals(this.id, sutSpecification.id) 
				&& Objects.equals(this.name, sutSpecification.name)
				&& Objects.equals(this.specification, sutSpecification.specification)
				&& Objects.equals(this.description, sutSpecification.description)
				&& Objects.equals(this.project, sutSpecification.project)
				&& Objects.equals(this.sutType, sutSpecification.sutType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, specification, description, project, sutType);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class SutSpecification {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    specification: ").append(toIndentedString(specification)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("    project: ").append(toIndentedString(project)).append("\n");
		sb.append("    sutType: ").append(toIndentedString(sutType)).append("\n");
		sb.append("}");
		return sb.toString();
	}

}
