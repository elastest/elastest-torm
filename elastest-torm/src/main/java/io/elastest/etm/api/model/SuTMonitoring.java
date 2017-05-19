package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * SuTMonitoring
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

public class SuTMonitoring   {
  @JsonProperty("CPU")
  private String CPU = null;

  @JsonProperty("CPU temp")
  private String cpUTemp = null;

  @JsonProperty("CPU cores")
  private String cpUCores = null;

  @JsonProperty("CPU speed")
  private String cpUSpeed = null;

  public SuTMonitoring CPU(String CPU) {
    this.CPU = CPU;
    return this;
  }

   /**
   * Get CPU
   * @return CPU
  **/
  @ApiModelProperty(example = "56%", value = "")


  public String getCPU() {
    return CPU;
  }

  public void setCPU(String CPU) {
    this.CPU = CPU;
  }

  public SuTMonitoring cpUTemp(String cpUTemp) {
    this.cpUTemp = cpUTemp;
    return this;
  }

   /**
   * Get cpUTemp
   * @return cpUTemp
  **/
  @ApiModelProperty(example = "66ºC", value = "")


  public String getCpUTemp() {
    return cpUTemp;
  }

  public void setCpUTemp(String cpUTemp) {
    this.cpUTemp = cpUTemp;
  }

  public SuTMonitoring cpUCores(String cpUCores) {
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

  public SuTMonitoring cpUSpeed(String cpUSpeed) {
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
    SuTMonitoring suTMonitoring = (SuTMonitoring) o;
    return Objects.equals(this.CPU, suTMonitoring.CPU) &&
        Objects.equals(this.cpUTemp, suTMonitoring.cpUTemp) &&
        Objects.equals(this.cpUCores, suTMonitoring.cpUCores) &&
        Objects.equals(this.cpUSpeed, suTMonitoring.cpUSpeed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(CPU, cpUTemp, cpUCores, cpUSpeed);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuTMonitoring {\n");
    
    sb.append("    CPU: ").append(toIndentedString(CPU)).append("\n");
    sb.append("    cpUTemp: ").append(toIndentedString(cpUTemp)).append("\n");
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

