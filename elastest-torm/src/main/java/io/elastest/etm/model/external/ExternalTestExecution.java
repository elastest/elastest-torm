package io.elastest.etm.model.external;

import java.io.Serializable;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalTJob.ExternalTJobView;
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
            ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "monitoringIndex")
    private String monitoringIndex = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "fields", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("fields")
    private String fields = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "result")
    @JsonProperty("result")
    private String result = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "externalId")
    @JsonProperty("externalId")
    private String externalId;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "externalSystemId")
    @JsonProperty("externalSystemId")
    private String externalSystemId;

    @JsonView({ ExternalTestExecutionView.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exTestCase")
    @JsonIgnoreProperties(value = "exTestExecs")
    private ExternalTestCase exTestCase;

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

}
