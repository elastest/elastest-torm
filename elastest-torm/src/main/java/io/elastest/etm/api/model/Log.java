package io.elastest.etm.api.model;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import io.swagger.annotations.ApiModelProperty;

/**
 * Log
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

public class Log {
	/**
	 * Gets or Sets logType
	 */
	public enum LogTypeEnum {
		BROWSERLOG("BrowserLog"),

		TESTLOG("TestLog");

		private String value;

		LogTypeEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static LogTypeEnum fromValue(String text) {
			for (LogTypeEnum b : LogTypeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("logType")
	private LogTypeEnum logType = null;

	@JsonProperty("logUrl")
	private String logUrl = null;

	public Log logType(LogTypeEnum logType) {
		this.logType = logType;
		return this;
	}

	/**
	 * Get logType
	 * 
	 * @return logType
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public LogTypeEnum getLogType() {
		return logType;
	}

	public void setLogType(LogTypeEnum logType) {
		this.logType = logType;
	}

	public Log logUrl(String logUrl) {
		this.logUrl = logUrl;
		return this;
	}

	/**
	 * Get logUrl
	 * 
	 * @return logUrl
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	public String getLogUrl() {
		return logUrl;
	}

	public void setLogUrl(String logUrl) {
		this.logUrl = logUrl;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Log log = (Log) o;
		return Objects.equals(this.logType, log.logType) && Objects.equals(this.logUrl, log.logUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(logType, logUrl);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Log {\n");

		sb.append("    logType: ").append(toIndentedString(logType)).append("\n");
		sb.append("    logUrl: ").append(toIndentedString(logUrl)).append("\n");
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
