package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.api.model.TJobExecution;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * TOJobExecution
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Entity
public class TOJobExecution {

	public interface TOJobExecView{		
	}
		
	@Id
	@JsonView(TOJobExecView.class)
	@JsonProperty("id")
	private Long id = null;

	/**
	 * Gets or Sets status
	 */
	public enum StatusEnum {
		SUCCESS("SUCCESS"),

		FAILURE("FAILURE"),

		IN_PROGRESS("IN PROGRESS");

		private String value;

		StatusEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static StatusEnum fromValue(String text) {
			for (StatusEnum b : StatusEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonView(TOJobExecView.class)
	@JsonProperty("status")
	private StatusEnum status = null;

	@JsonView(TOJobExecView.class)
	@JsonProperty("tJobsExecutions")
	@OneToMany(mappedBy="tOJobExecution")
	private List<TJobExecution> tJobsExecutions = null;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JsonProperty("tOJob")
	private TOJob tOJob = null;
	
	public TOJobExecution id(Long id) {
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
		this.id = id;
	}

	public TOJobExecution status(StatusEnum status) {
		this.status = status;
		return this;
	}

	/**
	 * Get status
	 * 
	 * @return status
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public TOJobExecution tJobsExecutions(List<TJobExecution> tJobsExecutions) {
		this.tJobsExecutions = tJobsExecutions;
		return this;
	}

	public TOJobExecution addTJobsExecutionsItem(TJobExecution tJobsExecutionsItem) {
		if (this.tJobsExecutions == null) {
			this.tJobsExecutions = new ArrayList<TJobExecution>();
		}
		this.tJobsExecutions.add(tJobsExecutionsItem);
		return this;
	}

	/**
	 * Get tJobsExecutions
	 * 
	 * @return tJobsExecutions
	 **/
	@ApiModelProperty(value = "")

	@Valid

	public List<TJobExecution> getTJobsExecutions() {
		return tJobsExecutions;
	}

	public void setTJobsExecutions(List<TJobExecution> tJobsExecutions) {
		this.tJobsExecutions = tJobsExecutions;
	}
	
	/**
	 * Get tOJob
	 * 
	 * @return tOJob
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull
	public TOJob getTOJob() {
		return tOJob;
	}

	public void setTOJob(TOJob tOJob) {
		this.tOJob = tOJob;
	}
	
	public TOJobExecution tOJob(TOJob tOJob){
		this.tOJob = tOJob;
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
		TOJobExecution toJobExecution = (TOJobExecution) o;
		return Objects.equals(this.id, toJobExecution.id) && Objects.equals(this.status, toJobExecution.status)
				&& Objects.equals(this.tJobsExecutions, toJobExecution.tJobsExecutions)
				&& Objects.equals(this.tOJob, toJobExecution.tOJob);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, status, tJobsExecutions, tOJob);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class TOJobExecution {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    tJobsExecutions: ").append(toIndentedString(tJobsExecutions)).append("\n");
		sb.append("    tOJob: ").append(toIndentedString(tOJob)).append("\n");
		
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
