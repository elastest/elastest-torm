package io.elastest.etm.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.swagger.annotations.ApiModelProperty;

/**
 * TOJob
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Entity
public class TOJob {
	
	public interface TOJobView {
	}
	
	@Id
	@JsonView({ TOJobView.class, BasicAttProject.class })
	@JsonProperty("id")
	private Long id = null;

	@JsonView({ TOJobView.class, BasicAttProject.class })
	@JsonProperty("name")
	private String name = null;

	@JsonView({ TOJobView.class, BasicAttProject.class })
	@JsonProperty("tOScript")
	private String tOScript = null;

	@ManyToOne(fetch = FetchType.LAZY)
	private Project project = null;
	
	@OneToMany(mappedBy="tOJob")
	private List<TOJobExecution> tOJobExecutions = null;
	

	public TOJob id(Long id) {
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

	public TOJob name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Get name
	 * 
	 * @return name
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TOJob tOScript(String tOScript) {
		this.tOScript = tOScript;
		return this;
	}

	/**
	 * Get tOScript
	 * 
	 * @return tOScript
	 **/
	@ApiModelProperty(example = "tjob1 {sut1, browser}, tjob2 {sut1, browser}, tjob3 {sut2, browser2}", required = true, value = "")
	@NotNull

	public String getTOScript() {
		return tOScript;
	}

	public void setTOScript(String tOScript) {
		this.tOScript = tOScript;
	}
	
	/**
	 * Get name
	 * 
	 * @return name
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
	public TOJob project(Project project) {
		this.project = project;
		return this;
	}
	
	
	/**
	 * Get tOJobExecutions
	 * 
	 * @return tOJobExecutions
	 **/
	@ApiModelProperty(required = false, value = "")
	
	public List<TOJobExecution> getTOJobExecutions() {
		return tOJobExecutions;
	}

	public void setTOJobExecutions(List<TOJobExecution> tOJobExecutions) {
		this.tOJobExecutions = tOJobExecutions;
	}
	
	public TOJob tOJobExecutions(List<TOJobExecution> tOJobExecutions) {
		this.tOJobExecutions = tOJobExecutions;
		return this;
	}
	
	public TOJobExecution addTOJobExecution(TOJobExecution tOJobExecution) {
		getTOJobExecutions().add(tOJobExecution);
		tOJobExecution.setTOJob(this);
		return tOJobExecution;
	}

	public TJobExecution removeTjobExec(TJobExecution tjobExec) {
		getTOJobExecutions().remove(tjobExec);
		tjobExec.setTjob(null);
		return tjobExec;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TOJob toJob = (TOJob) o;
		return Objects.equals(this.id, toJob.id) && Objects.equals(this.name, toJob.name)
				&& Objects.equals(this.tOScript, toJob.tOScript) && Objects.equals(this.project, toJob.project)
				&& Objects.equals(this.tOJobExecutions, toJob.tOJobExecutions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, tOScript, project, tOJobExecutions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class TOJob {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    tOScript: ").append(toIndentedString(tOScript)).append("\n");
		sb.append("    project: ").append(toIndentedString(project)).append("\n");
		sb.append("    tOJobExecutions: ").append(toIndentedString(tOJobExecutions)).append("\n");
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
