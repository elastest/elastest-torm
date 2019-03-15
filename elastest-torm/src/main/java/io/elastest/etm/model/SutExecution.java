package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.TJob.TJobCompleteView;
import io.elastest.etm.model.TJobExecution.TJobExecCompleteView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * SuTExecution
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Entity
@ApiModel(description = "Data generated by the execution of a SUT.")
public class SutExecution {

    public interface SutExecView {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @JsonView({ SutExecView.class, TJobCompleteView.class, TJobExecCompleteView.class })
    @JsonProperty("id")
    private Long id = null;

    @JsonView(SutExecView.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonProperty("sutSpecification")
    @JsonIgnoreProperties(value = "sutExecution")
    private SutSpecification sutSpecification = null;

    @JsonView(SutExecView.class)
    @JsonProperty("url")
    @Column(name = "url")
    private String url = null;

    @JsonView(SutExecView.class)
    @ElementCollection
    @CollectionTable(name = "SutExecParameter", joinColumns = @JoinColumn(name = "SutExec"))
    private List<Parameter> parameters = new ArrayList<>();

    @JsonView({ SutExecView.class })
    @OneToMany(mappedBy = "sutExecution", cascade = CascadeType.REMOVE)
    private List<TJobExecution> tjobExecs;

    @JsonView(SutExecView.class)
    private Long publicPort = null;

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

    @JsonView(SutExecView.class)
    @JsonProperty("deployStatus")
    private DeployStatusEnum deployStatus = null;

    private String ip;

    /* Constructors */
    public SutExecution() {
    }

    public SutExecution(Long id, SutSpecification sutSpecification, String url,
            DeployStatusEnum deployStatus) {
        this.id = id == null ? 0 : id;
        this.sutSpecification = sutSpecification;
        this.url = url == null ? "" : url;
        this.deployStatus = deployStatus;
    }

    /**
     * Get id
     * 
     * @return id
     **/
    @ApiModelProperty(example = "12345678", value = "Value that identifies a SUT Execution.")

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id == null ? 0 : id;
    }

    /**
     * Get url
     * 
     * @return url
     **/
    @ApiModelProperty(example = "http://www.myapp.io:8090", value = "URL to access the SUT.")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? "" : url;
    }

    @ApiModelProperty(example = "192.168.0.1", value = "SUT IP.")
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? "" : ip;
    }

    /**
     * Get deployStatus
     * 
     * @return deployStatus
     **/
    @ApiModelProperty(example = "deploying", value = "Status of the SUT (deploying, deployed, undeploying, undeployed and error)")

    public DeployStatusEnum getDeployStatus() {
        return deployStatus;
    }

    public void setDeployStatus(DeployStatusEnum deployStatus) {
        this.deployStatus = deployStatus;
    }

    /**
     * Get sutSpecification
     * 
     * @return sutSpecification
     **/
    @ApiModelProperty(example = "MySut", value = "SUT deployed")
    public SutSpecification getSutSpecification() {
        return sutSpecification;
    }

    public void setSutSpecification(SutSpecification sutSpecification) {
        this.sutSpecification = sutSpecification;
    }

    /**
     * Get parameters
     * 
     * @return parameters
     **/

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Get tjobExecs
     * 
     * @return tjobExecs
     **/

    public List<TJobExecution> getTjobExecs() {
        return tjobExecs;
    }

    public void setTjobExecs(List<TJobExecution> tjobExecs) {
        this.tjobExecs = tjobExecs;
    }

    public Long getPublicPort() {
        return publicPort;
    }

    public void setPublicPort(Long publicPort) {
        this.publicPort = publicPort;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SutExecution suTExecution = (SutExecution) o;
        return Objects.equals(this.id, suTExecution.id)
                && Objects.equals(this.url, suTExecution.url)
                && Objects.equals(this.deployStatus, suTExecution.deployStatus)
                && Objects.equals(this.sutSpecification,
                        suTExecution.sutSpecification)
                && Objects.equals(this.parameters, suTExecution.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, deployStatus, sutSpecification);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SuTExecution {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    deployStatus: ").append(toIndentedString(deployStatus))
                .append("\n");
        sb.append("    sutSpecification: ").append(toIndentedString(
                sutSpecification != null ? sutSpecification.getId() : "null"))
                .append("\n");
        sb.append("    parameters: ").append(toIndentedString(parameters))
                .append("\n");
        sb.append("}");
        return sb.toString();
    }

    public String getSutExecMonitoringIndex() {
        return "s" + this.getSutSpecification().getId() + "_e" + this.getId();
    }

}
