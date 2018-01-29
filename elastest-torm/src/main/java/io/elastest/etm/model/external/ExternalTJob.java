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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.external.ExternalProject.BasicAttExternalProject;
import io.elastest.etm.model.external.ExternalTestCase.BasicAttExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution.BasicAttExternalTestExecution;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "externalId", "externalSystemId" }) })
public class ExternalTJob implements Serializable {
    private static final long serialVersionUID = 1L;

    public interface BasicAttExternalTJob {
    }

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    Long id = null;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "name")
    @JsonProperty("name")
    String name = null;

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

    @JsonView({ BasicAttExternalTJob.class, BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exProject")
    private ExternalProject exProject;

    @JsonView({ BasicAttExternalTJob.class, BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @JsonProperty("exTJobExec")
    @OneToMany(mappedBy = "exTJob", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ExternalTJobExecution> exTJobExec;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTestExecution.class })
    @JsonProperty("exTestCases")
    @OneToMany(mappedBy = "exTJob", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ExternalTestCase> exTestCases;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTestExecution.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sut")
    @JsonProperty("sut")
    private SutSpecification sut = null;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalTJob() {
    }

    public ExternalTJob(Long id) {
        this.id = id == null ? 0 : id;
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

    public ExternalProject getExProject() {
        return exProject;
    }

    public void setExProject(ExternalProject exProject) {
        this.exProject = exProject;
    }

    public List<ExternalTJobExecution> getExTJobExec() {
        return exTJobExec;
    }

    public void setExTJobExec(List<ExternalTJobExecution> exTJobExec) {
        this.exTJobExec = exTJobExec;
    }

    public List<ExternalTestCase> getExTestCases() {
        return exTestCases;
    }

    public void setExTestCases(List<ExternalTestCase> exTestCases) {
        this.exTestCases = exTestCases;
    }

}
