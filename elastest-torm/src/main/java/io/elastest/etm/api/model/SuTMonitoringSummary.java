package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * SuTMonitoringSummary
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

public class SuTMonitoringSummary   {
  @JsonProperty("CPU Average")
  private String cpUAverage = null;

  @JsonProperty("CPU temp Average")
  private String cpUTempAverage = null;

  @JsonProperty("CPU cores")
  private String cpUCores = null;

  @JsonProperty("CPU speed")
  private String cpUSpeed = null;

  public SuTMonitoringSummary cpUAverage(String cpUAverage) {
    this.cpUAverage = cpUAverage;
    return this;
  }

   /**
   * Get cpUAverage
   * @return cpUAverage
  **/
  @ApiModelProperty(example = "71%", value = "")


  public String getCpUAverage() {
    return cpUAverage;
  }

  public void setCpUAverage(String cpUAverage) {
    this.cpUAverage = cpUAverage;
  }

  public SuTMonitoringSummary cpUTempAverage(String cpUTempAverage) {
    this.cpUTempAverage = cpUTempAverage;
    return this;
  }

   /**
   * Get cpUTempAverage
   * @return cpUTempAverage
  **/
  @ApiModelProperty(example = "78ºC", value = "")


  public String getCpUTempAverage() {
    return cpUTempAverage;
  }

  public void setCpUTempAverage(String cpUTempAverage) {
    this.cpUTempAverage = cpUTempAverage;
  }

  public SuTMonitoringSummary cpUCores(String cpUCores) {
    this.cpUCores = cpUCores;
    return this;
  }

   /**
   * Get cpUCores
   * @return cpUCores
  **/
  @ApiModelProperty(example = "4", value = "")


  public String getCpUCores() {
    return cpUCores;
  }

  public void setCpUCores(String cpUCores) {
    this.cpUCores = cpUCores;
  }

  public SuTMonitoringSummary cpUSpeed(String cpUSpeed) {
    this.cpUSpeed = cpUSpeed;
    return this;
  }

   /**
   * Get cpUSpeed
   * @return cpUSpeed
  **/
  @ApiModelProperty(example = "3.1 GHz", value = "")


  public String getCpUSpeed() {
    return cpUSpeed;
  }

  public void setCpUSpeed(String cpUSpeed) {
    this.cpUSpeed = cpUSpeed;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuTMonitoringSummary suTMonitoringSummary = (SuTMonitoringSummary) o;
    return Objects.equals(this.cpUAverage, suTMonitoringSummary.cpUAverage) &&
        Objects.equals(this.cpUTempAverage, suTMonitoringSummary.cpUTempAverage) &&
        Objects.equals(this.cpUCores, suTMonitoringSummary.cpUCores) &&
        Objects.equals(this.cpUSpeed, suTMonitoringSummary.cpUSpeed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cpUAverage, cpUTempAverage, cpUCores, cpUSpeed);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuTMonitoringSummary {\n");
    
    sb.append("    cpUAverage: ").append(toIndentedString(cpUAverage)).append("\n");
    sb.append("    cpUTempAverage: ").append(toIndentedString(cpUTempAverage)).append("\n");
    sb.append("    cpUCores: ").append(toIndentedString(cpUCores)).append("\n");
    sb.append("    cpUSpeed: ").append(toIndentedString(cpUSpeed)).append("\n");
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

