package io.elastest.etm.model.external;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.external.ExternalTJob.BasicAttExternalTJob;
import io.elastest.etm.model.external.ExternalTestCase.BasicAttExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution.BasicAttExternalTestExecution;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "externalId", "externalSystemId" }) })
public class ExternalProject implements Serializable {
    private static final long serialVersionUID = 1L;

    public interface BasicAttExternalProject {
    }

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name = null;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "type")
    @JsonProperty("type")
    private TypeEnum type = null;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "externalId")
    @JsonProperty("externalId")
    private String externalId;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "externalSystemId")
    @JsonProperty("externalSystemId")
    private String externalSystemId;

    @JsonView({ BasicAttExternalProject.class })
    @JsonProperty("exTJob")
    @OneToMany(mappedBy = "exProject", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ExternalTJob> exTJob;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @JsonProperty("suts")
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<SutSpecification> suts;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalProject() {
    }

    public ExternalProject(Long id) {
        this.id = id == null ? 0 : id;
    }

    public enum TypeEnum {
        TESTLINK("TESTLINK");

        private String value;

        TypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static TypeEnum fromValue(String text) {
            for (TypeEnum b : TypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    /* *****************************/
    /* ***** Getters/Setters *******/
    /* *****************************/

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id == null ? 0 : id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getExternalSystemId() {
        return externalSystemId;
    }

    public void setExternalSystemId(String externalSystemId) {
        this.externalSystemId = externalSystemId;
    }

    public List<ExternalTJob> getExTJob() {
        return exTJob;
    }

    public void setExTJob(List<ExternalTJob> exTJob) {
        this.exTJob = exTJob;
    }

    public List<SutSpecification> getSuts() {
        return suts;
    }

    public void setSuts(List<SutSpecification> suts) {
        this.suts = suts;
    }
}
