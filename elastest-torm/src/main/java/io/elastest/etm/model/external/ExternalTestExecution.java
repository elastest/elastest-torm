package io.elastest.etm.model.external;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalTJob.ExternalTJobView;
import io.elastest.etm.model.external.ExternalTJobExecution.ExternalTJobExecutionView;
import io.elastest.etm.model.external.ExternalTestCase.ExternalTestCaseView;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "externalId", "externalSystemId" }) })
public class ExternalTestExecution implements Serializable {
    private static final long serialVersionUID = 1L;

    public interface ExternalTestExecutionView {
    }

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class, ExternalTestExecutionView.class,
            ExternalTJobExecutionView.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class, ExternalTestExecutionView.class,
            ExternalTJobExecutionView.class })
    @Column(name = "monitoringIndex")
    private String monitoringIndex = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class, ExternalTestExecutionView.class,
            ExternalTJobExecutionView.class })
    @Column(name = "fields", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("fields")
    private String fields = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class, ExternalTestExecutionView.class,
            ExternalTJobExecutionView.class })
    @Column(name = "result")
    @JsonProperty("result")
    private String result = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class, ExternalTestExecutionView.class,
            ExternalTJobExecutionView.class })
    @Column(name = "externalId")
    @JsonProperty("externalId")
    private String externalId;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class, ExternalTestExecutionView.class,
            ExternalTJobExecutionView.class })
    @Column(name = "externalSystemId")
    @JsonProperty("externalSystemId")
    private String externalSystemId;

    @JsonView({ ExternalTestExecutionView.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exTestCase")
    @JsonIgnoreProperties(value = "exTestExecs", allowSetters = true)
    private ExternalTestCase exTestCase;

    @JsonView({ ExternalTestExecutionView.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exTJobExec")
    @JsonIgnoreProperties(value = { "exTestExecs" }, allowSetters = true)
    private ExternalTJobExecution exTJobExec;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class, ExternalTestExecutionView.class,
            ExternalTJobExecutionView.class })
    @Column(name = "startDate")
    private Date startDate = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class, ExternalTestExecutionView.class,
            ExternalTJobExecutionView.class })
    @Column(name = "endDate")
    private Date endDate = null;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalTestExecution() {
    }

    public ExternalTestExecution(Long id) {
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

    public String getMonitoringIndex() {
        return monitoringIndex;
    }

    public String getTestMonitoringIndex() {
        if (monitoringIndex != null) {
            String testMonitoringIndex = monitoringIndex.split(",")[0];
            return testMonitoringIndex != null ? testMonitoringIndex
                    : this.getMonitoringIndex();
        }
        return this.getMonitoringIndex();
    }

    public void setMonitoringIndex(String monitoringIndex) {
        this.monitoringIndex = monitoringIndex;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

    public ExternalTestCase getExTestCase() {
        return exTestCase;
    }

    public void setExTestCase(ExternalTestCase exTestCase) {
        this.exTestCase = exTestCase;
    }

    public ExternalTJobExecution getExTJobExec() {
        return exTJobExec;
    }

    public void setExTJobExec(ExternalTJobExecution exTJobExec) {
        this.exTJobExec = exTJobExec;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
