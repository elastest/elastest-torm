package io.elastest.etm.api.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.ApiModelProperty;

@Entity
public class Project implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public interface BasicAttProject{
	}
	
	@JsonView(BasicAttProject.class)
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	@JsonProperty("id")
	private Long id = null;
	
	@JsonView(BasicAttProject.class)
	@JsonProperty("name")
	private String name = null;
	
	@JsonProperty("tjobs")
	//bi-directional many-to-one association to TJob
	@OneToMany(mappedBy="project")
	private List<TJob> tJobs;
	
	@JsonProperty("tojobs")
	//bi-directional many-to-one association to ElasEtmTjobexec
	@OneToMany(mappedBy="project")
	private List<TOJob> tOJobs;
	
	@JsonProperty("suts")
	//bi-directional many-to-one association to ElasEtmTjobexec
	@OneToMany(mappedBy="project")
	private List<SutSpecification> suts;
	
	public Project() {}
	
	public Project(Long id, String name, List<TJob> tJobs, List<TOJob> tOJobs, List<SutSpecification> suts){
		this.id = id==null? 0: id;
		this.name = name;
		this.tJobs = tJobs;
		this.tOJobs = tOJobs;
		this.suts = suts;
		
	}

	public Project id(Long id) {
		this.id = id;
		return this;
	}

	/**
	 * Get id
	 * 
	 * @return id
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id==null? 0: id;
	}	

	public Project id(String name) {
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
	
	public Project name(String name) {
		this.name = name;
		return this;
	}	
	
	
	/**
	 * Get tjobs
	 * 
	 * @return tjobs
	 **/
//	@ApiModelProperty(required = true, value = "", example = "", hidden = true)
	public List<TJob> getTJobs() {
		return this.tJobs;
	}

	public void setTJobs(List<TJob> tJobs) {
		this.tJobs = tJobs;
	}
	
	public Project tJobs(List<TJob> tJobs) {
		this.tJobs = tJobs;
		return this;
	}
	
	/**
	 * Get tojobs
	 * 
	 * @return tojobs
	 **/
//	@ApiModelProperty(required = true, value = "", example = "", hidden = true)
	public List<TOJob> getTOJobs() {
		return this.tOJobs;
	}

	public void setTOJobs(List<TOJob> tOJobs) {
		this.tOJobs = tOJobs;
	}
	
	public Project tOJobs(List<TOJob> tOJobs) {
		this.tOJobs = tOJobs;
		return this;
	}
	
	
	/**
	 * Get suts
	 * 
	 * @return suts
	 **/
//	@ApiModelProperty(required = true, value = "", example = "", hidden = true)
	public List<SutSpecification> getSuts() {
		return this.suts;
	}

	public void setSuts(List<SutSpecification> suts) {
		this.suts = suts;
	}
	
	public Project suts(List<SutSpecification> suts) {
		this.suts = suts;
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
		Project project = (Project) o;
		return Objects.equals(this.name, project.name)  && Objects.equals(this.id, project.id) && Objects.equals(this.tJobs, project.tJobs)
				&& Objects.equals(this.suts, project.suts) && Objects.equals(this.tOJobs, project.tOJobs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class DeployConfig {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    tJobs: ").append(toIndentedString(tJobs)).append("\n");
		sb.append("    suts: ").append(toIndentedString(suts)).append("\n");
		sb.append("    tOJobs: ").append(toIndentedString(tOJobs)).append("\n");
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
