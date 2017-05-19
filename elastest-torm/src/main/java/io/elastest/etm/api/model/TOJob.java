package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * TOJob
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

public class TOJob   {
  @JsonProperty("id")
  private Long id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("tOScript")
  private String tOScript = null;

  public TOJob id(Long id) {
    this.id = id;
    return this;
  }

   /**
   * Get id
   * @return id
  **/
  @ApiModelProperty(value = "")


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public TOJob name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Get name
   * @return name
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TOJob tOScript(String tOScript) {
    this.tOScript = tOScript;
    return this;
  }

   /**
   * Get tOScript
   * @return tOScript
  **/
  @ApiModelProperty(example = "tjob1 {sut1, browser}, tjob2 {sut1, browser}, tjob3 {sut2, browser2}", required = true, value = "")
  @NotNull


  public String getTOScript() {
    return tOScript;
  }

  public void setTOScript(String tOScript) {
    this.tOScript = tOScript;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TOJob toJob = (TOJob) o;
    return Objects.equals(this.id, toJob.id) &&
        Objects.equals(this.name, toJob.name) &&
        Objects.equals(this.tOScript, toJob.tOScript);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, tOScript);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TOJob {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    tOScript: ").append(toIndentedString(tOScript)).append("\n");
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

