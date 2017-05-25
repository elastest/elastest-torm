package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonCreator;

import io.elastest.etm.api.model.SutSpecification.SutView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * DeployedSut
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Entity
public class DeployedSut {
	
	public interface DeploSutView{		
	}	
	
	@Id
	@JsonView(DeploSutView.class)
	@JsonProperty("url")
	private String url = null;

	@JsonView(DeploSutView.class)
	@JsonProperty("sshKey")
	private String sshKey = null;

	@ManyToOne(fetch = FetchType.LAZY)	
	private SutSpecification sutSpecification = null;

	@JsonView(DeploSutView.class)
	@JsonProperty("url")
	public DeployedSut url(String url) {
		this.url = url;
		return this;
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
		this.url = url;
	}

	public DeployedSut sshKey(String sshKey) {
		this.sshKey = sshKey;
		return this;
	}

	/**
	 * Get sshKey
	 * 
	 * @return sshKey
	 **/
	@ApiModelProperty(value = "")

	public String getSshKey() {
		return sshKey;
	}

	public void setSshKey(String sshKey) {
		this.sshKey = sshKey;
	}

	public SutSpecification getSutSpecification() {
		return sutSpecification;
	}
	
	public DeployedSut sutSpecification(SutSpecification sutSpecification) {
		this.sutSpecification = sutSpecification;
		return this;
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
		DeployedSut deployedSut = (DeployedSut) o;
		return Objects.equals(this.url, deployedSut.url) && Objects.equals(this.sshKey, deployedSut.sshKey) 
				&& Objects.equals(this.sutSpecification, deployedSut.sutSpecification);
	}

	@Override
	public int hashCode() {
		return Objects.hash(url, sshKey, sutSpecification);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class DeployedSut {\n");

		sb.append("    url: ").append(toIndentedString(url)).append("\n");
		sb.append("    sshKey: ").append(toIndentedString(sshKey)).append("\n");
		sb.append("    sutSpecification: ").append(toIndentedString(sutSpecification)).append("\n");
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
