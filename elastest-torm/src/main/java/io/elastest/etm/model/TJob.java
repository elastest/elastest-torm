package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.swagger.annotations.ApiModelProperty;

/**
 * TJob
 */

@Entity
public class TJob {

	public interface BasicAttTJob {
	}

	@JsonView({ BasicAttTJob.class, BasicAttProject.class, BasicAttTJobExec.class })
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	@JsonProperty("id")
	private Long id = null;

	@JsonView({ BasicAttTJob.class, BasicAttProject.class })
	@Column(name = "name")
	@JsonProperty("name")
	private String name = null;

	@JsonView({ BasicAttTJob.class, BasicAttProject.class })
	@Column(name = "image_name")
	@JsonProperty("imageName")
	private String imageName = null;

	@JsonView({ BasicAttTJob.class, BasicAttProject.class, BasicAttTJobExec.class })
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sut")
	@JsonProperty("sut")
	private SutSpecification sut = null;

	// bi-directional many-to-one association to TJobExec
	@JsonView({ BasicAttTJob.class, BasicAttProject.class })
	@OneToMany(mappedBy = "tJob", cascade = CascadeType.REMOVE)
	private List<TJobExecution> tjobExecs;

	// bi-directional many-to-one association to Project
	@JsonView(BasicAttTJob.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project")
	private Project project;

	@JsonView({ BasicAttTJob.class, BasicAttProject.class })
	@ElementCollection
	@CollectionTable(name = "TJobParameter", joinColumns = @JoinColumn(name = "TJob"))
	private List<Parameter> parameters;

	@JsonView({ BasicAttTJob.class, BasicAttProject.class })
	@Column(name = "commands", columnDefinition = "TEXT", length = 65535)
	@JsonProperty("commands")
	private String commands;

	@JsonView({ BasicAttTJob.class, BasicAttProject.class })
	@Column(name = "resultsPath")
	@JsonProperty("resultsPath")
	private String resultsPath = null;

	@Column(name = "external")
	@JsonProperty("external")
	private boolean external = false;

	@JsonView({ BasicAttTJob.class, BasicAttProject.class, BasicAttTJobExec.class })
	@Column(name = "execDashboardConfig", columnDefinition = "TEXT", length = 65535)
	@JsonProperty("execDashboardConfig")
	private String execDashboardConfig = null;

	public TJob() {
	}

	public TJob(Long id, String name, String imageName, SutSpecification sut, Project project, boolean external,
			String execDashboardConfig) {
		this.id = id == null ? 0 : id;
		this.name = name;
		this.imageName = imageName;
		this.sut = sut;
		this.project = project;
		this.external = external;
		this.execDashboardConfig = execDashboardConfig;
	}

	/**
	 * Get id
	 * 
	 * @return id
	 **/
	@ApiModelProperty(example = "", value = "")

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
	@ApiModelProperty(example = "testApp1", required = true, value = "")
	@NotNull

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get imageName
	 * 
	 * @return imageName
	 **/
	@ApiModelProperty(example = "edujgurjc/torm-test-01", value = "")

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	/**
	 * Get sut
	 * 
	 * @return sut
	 **/
	@ApiModelProperty(example = "{ id:\"1\" }", value = "")

	public SutSpecification getSut() {
		return sut;
	}

	public void setSut(SutSpecification sut) {
		this.sut = sut;
	}

	/**
	 * Get project
	 * 
	 * @return project
	 **/
	@ApiModelProperty(required = true, value = "", example = "{ id:\"1\" }")

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Get TJobExecutions
	 * 
	 * @return tjobexecs
	 **/

	// @ApiModelProperty(required = true, value = "", example = "", hidden =
	// true)
	public List<TJobExecution> getTjobExecs() {
		return this.tjobExecs;
	}

	public void setTjobExecs(List<TJobExecution> tjobExec) {
		this.tjobExecs = tjobExec;
	}

	public TJobExecution addTjobExec(TJobExecution tjobExec) {
		getTjobExecs().add(tjobExec);
		tjobExec.setTjob(this);

		return tjobExec;
	}

	public TJobExecution removeTjobExec(TJobExecution tjobExec) {
		getTjobExecs().remove(tjobExec);
		tjobExec.setTjob(null);
		return tjobExec;
	}

	/**
	 * parameters
	 */

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public TJob logs(List<Parameter> parameters) {
		this.parameters = parameters;
		return this;
	}

	public TJob addLogsItem(Parameter parameter) {
		if (this.parameters == null) {
			this.parameters = new ArrayList<Parameter>();
		}
		this.parameters.add(parameter);
		return this;
	}

	/**
	 * commands
	 */

	public String getCommands() {
		return commands;
	}

	public void setCommands(String commands) {
		this.commands = commands;
	}

	/**
	 * execDashboardConfig
	 */

	public String getExecDashboardConfigPath() {
		return execDashboardConfig;
	}

	public void setExecDashboardConfigPath(String execDashboardConfig) {
		this.execDashboardConfig = execDashboardConfig;
	}

	/**
	 * isExternal
	 */

	@ApiModelProperty(required = true, value = "", example = "false")
	public boolean isExternal() {
		return external;
	}

	/**
	 * setExternal
	 */
	public void setExternal(boolean external) {
		this.external = external;
	}

	/**
	 * resultsPath
	 */

	public String getResultsPath() {
		return resultsPath;
	}

	public void setResultsPath(String resultsPath) {
		this.resultsPath = resultsPath;
	}

	/* Others */
	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TJob tjob = (TJob) o;
		return Objects.equals(this.id, tjob.id) && Objects.equals(this.name, tjob.name)
				&& Objects.equals(this.imageName, tjob.imageName) && Objects.equals(this.sut, tjob.sut)
				&& Objects.equals(this.project, tjob.project) && Objects.equals(this.tjobExecs, tjob.tjobExecs)
				&& Objects.equals(this.parameters, tjob.parameters) && Objects.equals(this.commands, tjob.commands)
				&& Objects.equals(this.resultsPath, tjob.resultsPath) && Objects.equals(this.external, tjob.external)
				&& Objects.equals(this.execDashboardConfig, tjob.execDashboardConfig);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, imageName, sut, project, tjobExecs, parameters, execDashboardConfig);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class TJob {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    imageName: ").append(toIndentedString(imageName)).append("\n");
		sb.append("    sut: ").append(toIndentedString(sut)).append("\n");
		sb.append("    project: ").append(toIndentedString(project)).append("\n");
		sb.append("    tjobExecs: ").append(toIndentedString(tjobExecs)).append("\n");
		sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
		sb.append("    commands: ").append(toIndentedString(commands)).append("\n");
		sb.append("    resultsPath: ").append(toIndentedString(resultsPath)).append("\n");
		sb.append("    external: ").append(toIndentedString(external)).append("\n");
		sb.append("    execDashboardConfig: ").append(toIndentedString(execDashboardConfig)).append("\n");
		sb.append("}");
		return sb.toString();
	}

}
