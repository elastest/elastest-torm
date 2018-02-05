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
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalTJobExecution.ExternalTJobExecutionView;
import io.elastest.etm.model.external.ExternalTestCase.ExternalTestCaseView;
import io.elastest.etm.model.external.ExternalTestExecution.ExternalTestExecutionView;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "externalId", "externalSystemId" }) })
public class ExternalTJob implements Serializable {
    private static final long serialVersionUID = 1L;

    public interface ExternalTJobView {
    }

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    Long id = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "name")
    @JsonProperty("name")
    String name = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "externalId")
    @JsonProperty("externalId")
    private String externalId;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "externalSystemId")
    @JsonProperty("externalSystemId")
    private String externalSystemId;

    @JsonView({ ExternalTJobView.class, ExternalTJobExecutionView.class,
            ExternalTestCaseView.class, ExternalTestExecutionView.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exProject")
    @JsonIgnoreProperties(value = "exTJobs")
    private ExternalProject exProject;

    @JsonView({ ExternalTJobView.class, ExternalProjectView.class })
    @JsonProperty("exTJobExecs")
    @OneToMany(mappedBy = "exTJob", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = "exTJob")
    private List<ExternalTJobExecution> exTJobExecs;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestExecutionView.class, ExternalTJobExecutionView.class })
    @JsonProperty("exTestCases")
    @OneToMany(mappedBy = "exTJob", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = "exTJob")
    private List<ExternalTestCase> exTestCases;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sut")
    @JsonProperty("sut")
    private SutSpecification sut = null;

    @Column(name = "execDashboardConfig", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("execDashboardConfig")
    private String execDashboardConfig = null;
    
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

    public List<ExternalTJobExecution> getExTJobExecs() {
        return exTJobExecs;
    }

    public void setExTJobExecs(List<ExternalTJobExecution> exTJobExecs) {
        this.exTJobExecs = exTJobExecs;
    }

    public List<ExternalTestCase> getExTestCases() {
        return exTestCases;
    }

    public void setExTestCases(List<ExternalTestCase> exTestCases) {
        this.exTestCases = exTestCases;
    }

    public SutSpecification getSut() {
        return sut;
    }

    public void setSut(SutSpecification sut) {
        this.sut = sut;
    }

    public String getExecDashboardConfig() {
        return execDashboardConfig;
    }

    public void setExecDashboardConfig(String execDashboardConfig) {
        this.execDashboardConfig = execDashboardConfig;
    }
    
}
