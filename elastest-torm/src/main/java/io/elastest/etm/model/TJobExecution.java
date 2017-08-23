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
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.TJob.BasicAttTJob;
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

	// bi-directional many-to-one association to TestSuite
	@JsonView({ BasicAttTJobExec.class, BasicAttTJob.class, BasicAttProject.class })
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "testSuite")
	private TestSuite testSuite;

	@JsonView({ BasicAttTJobExec.class, BasicAttTJob.class, BasicAttProject.class })
	@ElementCollection
	@CollectionTable(name = "TJobExecParameter", joinColumns = @JoinColumn(name = "TJobExec"))
	private List<Parameter> parameters = new ArrayList<>();

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
		FINISHED("FINISHED"),

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
	 * testSuite
	 */

	public TestSuite getTestSuite() {
		return testSuite;
	}

	public void setTestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
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

	public TJobExecution addLogsItem(Parameter parameter) {
		if (this.parameters == null) {
			this.parameters = new ArrayList<Parameter>();
		}
		this.parameters.add(parameter);
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
				&& Objects.equals(this.testSuite, tjobExecution.testSuite)
				&& Objects.equals(this.parameters, tjobExecution.parameters);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, duration, result, sutExecution, error, testSuite, parameters);
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
		sb.append("    testSuite: ").append(toIndentedString(testSuite)).append("\n");
		sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
		sb.append("}");
		return sb.toString();
	}

}
