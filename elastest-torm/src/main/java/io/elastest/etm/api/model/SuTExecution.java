package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import io.elastest.etm.api.model.SuTMonitoring;
import io.elastest.etm.api.model.SuTMonitoringSummary;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * SuTExecution
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

public class SuTExecution   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("logs")
  private List<String> logs = null;

  @JsonProperty("monitoringCurrent")
  private SuTMonitoring monitoringCurrent = null;

  @JsonProperty("monitoringSummary")
  private SuTMonitoringSummary monitoringSummary = null;

  /**
   * Gets or Sets deployStatus
   */
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

  @JsonProperty("deployStatus")
  private DeployStatusEnum deployStatus = null;

  public SuTExecution id(Long id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(example = "12345678", value = "")


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SuTExecution logs(List<String> logs) {
    this.logs = logs;
    return this;
  }

  public SuTExecution addLogsItem(String logsItem) {
    if (this.logs == null) {
      this.logs = new ArrayList<String>();
    }
    this.logs.add(logsItem);
    return this;
  }

   /**
   * Get logs
   * @return logs
  **/
  @ApiModelProperty(value = "")


  public List<String> getLogs() {
    return logs;
  }

  public void setLogs(List<String> logs) {
    this.logs = logs;
  }

  public SuTExecution monitoringCurrent(SuTMonitoring monitoringCurrent) {
    this.monitoringCurrent = monitoringCurrent;
    return this;
  }

   /**
   * Get monitoringCurrent
   * @return monitoringCurrent
  **/
  @ApiModelProperty(value = "")

  @Valid

  public SuTMonitoring getMonitoringCurrent() {
    return monitoringCurrent;
  }

  public void setMonitoringCurrent(SuTMonitoring monitoringCurrent) {
    this.monitoringCurrent = monitoringCurrent;
  }

  public SuTExecution monitoringSummary(SuTMonitoringSummary monitoringSummary) {
    this.monitoringSummary = monitoringSummary;
    return this;
  }

   /**
   * Get monitoringSummary
   * @return monitoringSummary
  **/
  @ApiModelProperty(value = "")

  @Valid

  public SuTMonitoringSummary getMonitoringSummary() {
    return monitoringSummary;
  }

  public void setMonitoringSummary(SuTMonitoringSummary monitoringSummary) {
    this.monitoringSummary = monitoringSummary;
  }

  public SuTExecution deployStatus(DeployStatusEnum deployStatus) {
    this.deployStatus = deployStatus;
    return this;
  }

   /**
   * Get deployStatus
   * @return deployStatus
  **/
  @ApiModelProperty(value = "")


  public DeployStatusEnum getDeployStatus() {
    return deployStatus;
  }

  public void setDeployStatus(DeployStatusEnum deployStatus) {
    this.deployStatus = deployStatus;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuTExecution suTExecution = (SuTExecution) o;
    return Objects.equals(this.id, suTExecution.id) &&
        Objects.equals(this.logs, suTExecution.logs) &&
        Objects.equals(this.monitoringCurrent, suTExecution.monitoringCurrent) &&
        Objects.equals(this.monitoringSummary, suTExecution.monitoringSummary) &&
        Objects.equals(this.deployStatus, suTExecution.deployStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, logs, monitoringCurrent, monitoringSummary, deployStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuTExecution {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    logs: ").append(toIndentedString(logs)).append("\n");
    sb.append("    monitoringCurrent: ").append(toIndentedString(monitoringCurrent)).append("\n");
    sb.append("    monitoringSummary: ").append(toIndentedString(monitoringSummary)).append("\n");
    sb.append("    deployStatus: ").append(toIndentedString(deployStatus)).append("\n");
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

