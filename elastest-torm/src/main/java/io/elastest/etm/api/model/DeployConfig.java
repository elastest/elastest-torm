package io.elastest.etm.api.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * DeployConfig
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

public class DeployConfig   {
  @JsonProperty("Shell script")
  private String shellScript = null;

  @JsonProperty("AWS CloudFormation")
  private String awSCloudFormation = null;

  @JsonProperty("Terraform configuration")
  private String terraformConfiguration = null;

  public DeployConfig shellScript(String shellScript) {
    this.shellScript = shellScript;
    return this;
  }

   /**
   * Get shellScript
   * @return shellScript
  **/
  @ApiModelProperty(value = "")


  public String getShellScript() {
    return shellScript;
  }

  public void setShellScript(String shellScript) {
    this.shellScript = shellScript;
  }

  public DeployConfig awSCloudFormation(String awSCloudFormation) {
    this.awSCloudFormation = awSCloudFormation;
    return this;
  }

   /**
   * Get awSCloudFormation
   * @return awSCloudFormation
  **/
  @ApiModelProperty(value = "")


  public String getAwSCloudFormation() {
    return awSCloudFormation;
  }

  public void setAwSCloudFormation(String awSCloudFormation) {
    this.awSCloudFormation = awSCloudFormation;
  }

  public DeployConfig terraformConfiguration(String terraformConfiguration) {
    this.terraformConfiguration = terraformConfiguration;
    return this;
  }

   /**
   * Get terraformConfiguration
   * @return terraformConfiguration
  **/
  @ApiModelProperty(value = "")


  public String getTerraformConfiguration() {
    return terraformConfiguration;
  }

  public void setTerraformConfiguration(String terraformConfiguration) {
    this.terraformConfiguration = terraformConfiguration;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeployConfig deployConfig = (DeployConfig) o;
    return Objects.equals(this.shellScript, deployConfig.shellScript) &&
        Objects.equals(this.awSCloudFormation, deployConfig.awSCloudFormation) &&
        Objects.equals(this.terraformConfiguration, deployConfig.terraformConfiguration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shellScript, awSCloudFormation, terraformConfiguration);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeployConfig {\n");
    
    sb.append("    shellScript: ").append(toIndentedString(shellScript)).append("\n");
    sb.append("    awSCloudFormation: ").append(toIndentedString(awSCloudFormation)).append("\n");
    sb.append("    terraformConfiguration: ").append(toIndentedString(terraformConfiguration)).append("\n");
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

