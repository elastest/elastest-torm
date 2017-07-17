package io.elastest.etm.api.model;

import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.api.model.Project.BasicAttProject;
import io.elastest.etm.api.model.TJob.BasicAttTJob;
import io.swagger.annotations.ApiModelProperty;

/**
 * TJobExecution
 */

@Entity
public class TJobExecution {

	public interface BasicAttTJobExec {
	}

	@JsonView({ BasicAttTJobExec.class, BasicAttTJob.class, BasicAttProject.class })
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	@JsonProperty("id")
	private Long id = null;

	@JsonView({ BasicAttTJobExec.class, BasicAttTJob.class, BasicAttProject.class })
	@Column(name = "duration")
	@JsonProperty("duration")
	private Long duration = null;

	@JsonView({ BasicAttTJobExec.class, BasicAttTJob.class, BasicAttProject.class })
	@Column(name = "result")
	@JsonProperty("result")
	private ResultEnum result = null;

	@JsonView({ BasicAttTJobExec.class, BasicAttTJob.class, BasicAttProject.class })
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sut_execution")
	@JsonProperty("sutExecution")
	private SutExecution sutExecution = null;

	@JsonView({ BasicAttTJobExec.class, BasicAttTJob.class, BasicAttProject.class })
	@Column(name = "error")
	@JsonProperty("error")
	private String error = null;

	// @JsonView({ BasicAttTJobExec.class, BasicAttTJob.class,
	// BasicAttProject.class })
	// @OneToMany(mappedBy="tJobExec", cascade = CascadeType.REMOVE)
	// private List<Log> logs = null;

	@JsonView({ BasicAttTJobExec.class, BasicAttTJob.class, BasicAttProject.class })
	@Column(name = "logIndex")
	private String logIndex = null;

	// bi-directional many-to-one association to Tjob
	@JsonView({ BasicAttTJobExec.class })
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tjob")
	private TJob tJob;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "to_job_execution")
	private TOJobExecution tOJobExecution = null;

	// bi-directional many-to-one association to TestSuite
	@JsonView({ BasicAttTJobExec.class, BasicAttTJob.class, BasicAttProject.class })
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "testSuite")
	private TestSuite testSuite;

	// Constructors
	public TJobExecution() {
		this.id = (long) 0;
		this.duration = (long) 0;
		this.result = ResultEnum.IN_PROGRESS;
	}

	public TJobExecution(Long id, Long duration, ResultEnum result) {
		this.id = id == null ? 0 : id;
		this.duration = duration == null ? 0 : duration;
		this.result = result;
	}

	/**
	 * Gets or Sets result
	 */
	public enum ResultEnum {
		SUCCESS("SUCCESS"),

		FAILURE("FAILURE"),

		IN_PROGRESS("IN PROGRESS");

		private String value;

		ResultEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static ResultEnum fromValue(String text) {
			for (ResultEnum b : ResultEnum.values()) {
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
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id == null ? 0 : id;
	}

	public TJobExecution id(Long id) {
		this.id = id;
		return this;
	}

	/**
	 * Get duration
	 * 
	 * @return duration
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration == null ? 0 : duration;
	}

	public TJobExecution duration(Long duration) {
		this.duration = duration;
		return this;
	}

	/**
	 * Get result
	 * 
	 * @return result
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public ResultEnum getResult() {
		return result;
	}

	public void setResult(ResultEnum result) {
		this.result = result;
	}

	public TJobExecution result(ResultEnum result) {
		this.result = result;
		return this;
	}

	/**
	 * Get sutExecution
	 * 
	 * @return sutExecution
	 **/
	@ApiModelProperty(value = "")

	public SutExecution getSutExecution() {
		return sutExecution;
	}

	public void setSutExecution(SutExecution sutExecution) {
		this.sutExecution = sutExecution;
	}

	public TJobExecution sutExecution(SutExecution sutExecution) {
		this.sutExecution = sutExecution;
		return this;
	}

	/**
	 * Get error
	 * 
	 * @return error
	 **/
	@ApiModelProperty(value = "")

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public TJobExecution error(String error) {
		this.error = error;
		return this;
	}

	// /**
	// * URLs of logs
	// *
	// * @return logs
	// **/
	// @ApiModelProperty(value = "URLs of logs")
	//
	// @Valid
	// public List<Log> getLogs() {
	// return logs;
	// }
	//
	// public void setLogs(List<Log> logs) {
	// this.logs = logs;
	// }
	//
	//
	// public TJobExecution logs(List<Log> logs) {
	// this.logs = logs;
	// return this;
	// }
	//
	// public TJobExecution addLogsItem(Log logsItem) {
	// if (this.logs == null) {
	// this.logs = new ArrayList<Log>();
	// }
	// this.logs.add(logsItem);
	// return this;
	// }

	/**
	 * Log table Index
	 * 
	 * @return logIndex
	 */

	public String getLogIndex() {
		return logIndex;
	}

	public void setLogIndex(String logIndex) {
		this.logIndex = logIndex;
	}

	public TJobExecution logs(String logIndex) {
		this.logIndex = logIndex;
		return this;
	}

	/**
	 * tJob
	 * 
	 * 
	 **/
	public TJob getTjob() {
		return this.tJob;
	}

	public void setTjob(TJob tjob) {
		this.tJob = tjob;
	}

	/**
	 * 
	 * TOJobExecution
	 * 
	 * @return tOJobExecution
	 */

	public TOJobExecution getTOJobExecutionb() {
		return tOJobExecution;
	}

	public void setTOJobExecution(TOJobExecution tOJobExecution) {
		this.tOJobExecution = tOJobExecution;
	}

	public TJobExecution tOJobExecution(TOJobExecution tOJobExecution) {
		this.tOJobExecution = tOJobExecution;
		return this;
	}

	/**
	 * testSuite
	 */

	public TestSuite getTestSuite() {
		return testSuite;
	}

	public void settestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
	}

	public TJobExecution testSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
		return this;
	}

	// Others

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TJobExecution tjobExecution = (TJobExecution) o;
		return Objects.equals(this.id, tjobExecution.id) && Objects.equals(this.duration, tjobExecution.duration)
				&& Objects.equals(this.result, tjobExecution.result)
				&& Objects.equals(this.sutExecution, tjobExecution.sutExecution)
				&& Objects.equals(this.error, tjobExecution.error)
				&& Objects.equals(this.logIndex, tjobExecution.logIndex)
				&& Objects.equals(this.tOJobExecution, tjobExecution.tOJobExecution)
				&& Objects.equals(this.testSuite, tjobExecution.testSuite);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, duration, result, sutExecution, error /* , logs */, tOJobExecution, testSuite);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class TJobExecution {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
		sb.append("    result: ").append(toIndentedString(result)).append("\n");
		sb.append("    sutExecution: ").append(toIndentedString(sutExecution)).append("\n");
		sb.append("    error: ").append(toIndentedString(error)).append("\n");
		sb.append("    logIndex: ").append(toIndentedString(logIndex)).append("\n");
		sb.append("    tOJobExecution: ").append(toIndentedString(tOJobExecution)).append("\n");
		sb.append("    testSuite: ").append(toIndentedString(testSuite)).append("\n");;
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
