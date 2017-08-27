package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.TJob.BasicAttTJob;
import io.swagger.annotations.ApiModelProperty;

@Entity
public class Project implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public interface BasicAttProject{
	}
	
	@JsonView({ BasicAttProject.class, BasicAttTJob.class, SutView.class })
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	@JsonProperty("id")
	private Long id = null;
	
	@JsonView(BasicAttProject.class)
	@JsonProperty("name")
	private String name = null;
	
	@JsonView(BasicAttProject.class)
	@JsonProperty("tjobs")
	//bi-directional many-to-one association to TJob
	@OneToMany(mappedBy="project", fetch=FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<TJob> tJobs;
	
	@JsonView({ BasicAttProject.class, BasicAttTJob.class })
	@JsonProperty("suts")
	//bi-directional many-to-one association to ElasEtmTjobexec
	@OneToMany(mappedBy="project", fetch=FetchType.LAZY, cascade = CascadeType.REMOVE)
	private List<SutSpecification> suts;
	
	public Project() {}
	
	public Project(Long id, String name, List<TJob> tJobs, List<SutSpecification> suts){
		this.id = id == null ? 0 : id;
		this.name = name;
		this.tJobs = tJobs;
		this.suts = suts;
		
	}	

	/**
	 * Get id
	 * 
	 * @return id
	 **/
	@ApiModelProperty(example = "", value = "Id of the Project.")	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id == null? 0: id;
	}	

	/**
	 * Get name
	 * 
	 * @return name
	 **/
	@ApiModelProperty(required = true, value = "The project name.")
	@NotNull

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Get tjobs
	 * 
	 * @return tjobs
	 **/
	@ApiModelProperty(value = "The TJobs list associated with the project.", example = "")
	public List<TJob> getTJobs() {
		return this.tJobs;
	}

	public void setTJobs(List<TJob> tJobs) {
		this.tJobs = tJobs;
	}
	
	/**
	 * Get suts
	 * 
	 * @return suts
	 **/
	@ApiModelProperty(value = "The TSuts list associated with the project.", example = "")
	public List<SutSpecification> getSuts() {
		return this.suts;
	}

	public void setSuts(List<SutSpecification> suts) {
		this.suts = suts;
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
		return Objects.equals(this.name, project.name)  
				&& Objects.equals(this.id, project.id) 
				&& Objects.equals(this.tJobs, project.tJobs)
				&& Objects.equals(this.suts, project.suts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, tJobs, suts);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class DeployConfig {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    tJobs: ").append(toIndentedString(tJobs)).append("\n");
		sb.append("    suts: ").append(toIndentedString(suts)).append("\n");
		sb.append("}");
		return sb.toString();
	}
}
