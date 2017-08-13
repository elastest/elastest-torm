package io.elastest.etm.model;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * SuTMonitoring
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

public class SuTMonitoring   {
  @JsonProperty("CPU Use")
  private List<String> cpu = null;

  @JsonProperty("Mem Use")
  private List<String> cpUTempAverage = null;

  @JsonProperty("CPU cores")
  private String cpUCores = null;
  

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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SuTMonitoring SuTMonitoring = (SuTMonitoring) o;
    return Objects.equals(this.cpUTempAverage, SuTMonitoring.cpUTempAverage) &&
        Objects.equals(this.cpUCores, SuTMonitoring.cpUCores);
        
  }

  @Override
  public int hashCode() {
    return Objects.hash(cpUTempAverage, cpUCores);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SuTMonitoring {\n");
        
    sb.append("    cpUTempAverage: ").append(toIndentedString(cpUTempAverage)).append("\n");
    sb.append("    cpUCores: ").append(toIndentedString(cpUCores)).append("\n");    
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

