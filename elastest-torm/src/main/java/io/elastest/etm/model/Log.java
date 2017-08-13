package io.elastest.etm.model;

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
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.TJob.BasicAttTJob;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.swagger.annotations.ApiModelProperty;

/**
 * Log
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Entity
public class Log {
	
	public interface BasicAttLog{
	}
	
	@JsonView(BasicAttProject.class)
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="id")
	@JsonProperty("id")
	private Long id = null;
	
	@JsonView(BasicAttLog.class)
	@JsonProperty("logType")
	private LogTypeEnum logType = null;

	@JsonView({BasicAttLog.class, BasicAttTJob.class, BasicAttTJobExec.class})
	@JsonProperty("logUrl")
	private String logUrl = null;
	
	@ManyToOne(fetch=FetchType.LAZY, cascade = {CascadeType.ALL})
	@JoinColumn(name="tJobExec")
	private TJobExecution tJobExec;
	
	
	/* Constructors */
	public Log(){}	
	
	public Log(Long id, LogTypeEnum logType, String logUrl, TJobExecution tJobExec) {
		this.id = id==null? 0: id;
		this.logType = logType;
		this.logUrl = logUrl;
		this.tJobExec = tJobExec;
	}
	
	

	/**
	 * Gets or Sets Id
	 */
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id==null? 0: id;
	}
	
	public Log id(Long id) {
		this.id = id;
		return this;
	}
	
	/**
	 * Gets or Sets tJobExec
	 */

	public TJobExecution gettJobExec() {
		return tJobExec;
	}

	public void settJobExec(TJobExecution tJobExec) {
		this.tJobExec = tJobExec;
	}

	public Log tJobExec(TJobExecution tJobExec) {
		this.tJobExec = tJobExec;
		return this;
	}


	/**
	 * Gets or Sets logType
	 */
	public enum LogTypeEnum {
		SUTLOG("SutLog"),

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
	
	public Log logType(LogTypeEnum logType) {
		this.logType = logType;
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
	
	public Log logUrl(String logUrl) {
		this.logUrl = logUrl;
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
