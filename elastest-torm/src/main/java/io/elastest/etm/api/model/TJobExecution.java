package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.elastest.etm.api.model.Log;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * TJobExecution
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

public class TJobExecution {
	@JsonProperty("id")
	private Long id = null;

	@JsonProperty("duration")
	private Long duration = null;

	public TJobExecution() {
	}

	public TJobExecution(Long id, Long duration) {
		this.id = id;
		this.duration = duration;
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

	@JsonProperty("result")
	private ResultEnum result = null;

	@JsonProperty("sutExecution")
	private Long sutExecution = null;

	@JsonProperty("error")
	private String error = null;

	@JsonProperty("logs")
	private List<Log> logs = null;

	public TJobExecution id(Long id) {
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

	public TJobExecution duration(Long duration) {
		this.duration = duration;
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
		this.duration = duration;
	}

	public TJobExecution result(ResultEnum result) {
		this.result = result;
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

	public TJobExecution sutExecution(Long sutExecution) {
		this.sutExecution = sutExecution;
		return this;
	}

	/**
	 * Get sutExecution
	 * 
	 * @return sutExecution
	 **/
	@ApiModelProperty(value = "")

	public Long getSutExecution() {
		return sutExecution;
	}

	public void setSutExecution(Long sutExecution) {
		this.sutExecution = sutExecution;
	}

	public TJobExecution error(String error) {
		this.error = error;
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

	public TJobExecution logs(List<Log> logs) {
		this.logs = logs;
		return this;
	}

	public TJobExecution addLogsItem(Log logsItem) {
		if (this.logs == null) {
			this.logs = new ArrayList<Log>();
		}
		this.logs.add(logsItem);
		return this;
	}

	/**
	 * URLs of logs
	 * 
	 * @return logs
	 **/
	@ApiModelProperty(value = "URLs of logs")

	@Valid

	public List<Log> getLogs() {
		return logs;
	}

	public void setLogs(List<Log> logs) {
		this.logs = logs;
	}

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
				&& Objects.equals(this.error, tjobExecution.error) && Objects.equals(this.logs, tjobExecution.logs);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, duration, result, sutExecution, error, logs);
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
		sb.append("    logs: ").append(toIndentedString(logs)).append("\n");
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
