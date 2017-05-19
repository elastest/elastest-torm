package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * DeployedSut
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

public class DeployedSut   {
  @JsonProperty("url")
  private String url = null;

  @JsonProperty("sshKey")
  private String sshKey = null;

  public DeployedSut url(String url) {
    this.url = url;
    return this;
  }

   /**
   * Get url
   * @return url
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public DeployedSut sshKey(String sshKey) {
    this.sshKey = sshKey;
    return this;
  }

   /**
   * Get sshKey
   * @return sshKey
  **/
  @ApiModelProperty(value = "")


  public String getSshKey() {
    return sshKey;
  }

  public void setSshKey(String sshKey) {
    this.sshKey = sshKey;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeployedSut deployedSut = (DeployedSut) o;
    return Objects.equals(this.url, deployedSut.url) &&
        Objects.equals(this.sshKey, deployedSut.sshKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, sshKey);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeployedSut {\n");
    
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    sshKey: ").append(toIndentedString(sshKey)).append("\n");
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

