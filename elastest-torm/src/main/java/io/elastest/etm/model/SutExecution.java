package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.TJob.BasicAttTJob;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.swagger.annotations.ApiModelProperty;

/**
 * SuTExecution
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Entity
public class SutExecution {

	public interface SutExecView {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonView({ SutExecView.class, BasicAttTJob.class, BasicAttTJobExec.class })
	@JsonProperty("id")
	private Long id = null;
	
	@JsonView(SutExecView.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JsonProperty("sutSpecification")
	private SutSpecification sutSpecification = null;
	
	@JsonView(SutExecView.class)
	@JsonProperty("url")
	@Column(name="url")
	private String url = null;

	public enum DeployStatusEnum {
		DEPLOYING("deploying"),

		DEPLOYED("deployed"),

		UNDEPLOYING("undeploying"),

		UNDEPLOYED("undeployed"),

		ERROR("error");

		private String value;

		DeployStatusEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static DeployStatusEnum fromValue(String text) {
			for (DeployStatusEnum b : DeployStatusEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}
	
	@JsonView(SutExecView.class)
	@JsonProperty("deployStatus")
	private DeployStatusEnum deployStatus = null;

	
	/* Constructors */
	public SutExecution() {	}

	public SutExecution(Long id, SutSpecification sutSpecification, String url, DeployStatusEnum deployStatus) {
		this.id = id==null? 0: id;
		this.sutSpecification = sutSpecification;
		this.url = url==null? "": url;
		this.deployStatus = deployStatus;
	}


	/**
	 * Get id
	 * 
	 * @return id
	 **/
	@ApiModelProperty(example = "12345678", value = "")

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id==null? 0: id;
	}

	/**
	 * Get url
	 * 
	 * @return url
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url==null? "": url;
	}
	
	/**
	 * Get deployStatus
	 * 
	 * @return deployStatus
	 **/
	@ApiModelProperty(value = "")

	public DeployStatusEnum getDeployStatus() {
		return deployStatus;
	}

	public void setDeployStatus(DeployStatusEnum deployStatus) {
		this.deployStatus = deployStatus;
	}

	/**
	 * Get sutSpecification
	 * 
	 * @return sutSpecification
	 **/
	@ApiModelProperty(value = "")

	public SutSpecification getSutSpecification() {
		return sutSpecification;
	}

	public void setSutSpecification(SutSpecification sutSpecification) {
		this.sutSpecification = sutSpecification;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		SutExecution suTExecution = (SutExecution) o;
		return Objects.equals(this.id, suTExecution.id) 
				&& Objects.equals(this.url, suTExecution.url)
				&& Objects.equals(this.deployStatus, suTExecution.deployStatus)
				&& Objects.equals(this.sutSpecification, suTExecution.sutSpecification);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, url, deployStatus, sutSpecification);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class SuTExecution {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    url: ").append(toIndentedString(url)).append("\n");
		sb.append("    deployStatus: ").append(toIndentedString(deployStatus)).append("\n");
		sb.append("    sutSpecification: ").append(toIndentedString(sutSpecification)).append("\n");
		sb.append("}");
		return sb.toString();
	}

}
