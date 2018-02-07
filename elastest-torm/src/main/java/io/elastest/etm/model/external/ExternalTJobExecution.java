package io.elastest.etm.model.external;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
import javax.persistence.MapKeyColumn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.TJobExecution.ResultEnum;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalTJob.ExternalTJobView;
import io.elastest.etm.model.external.ExternalTestCase.ExternalTestCaseView;
import io.elastest.etm.model.external.ExternalTestExecution.ExternalTestExecutionView;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ExternalTJobExecution implements Serializable {
    private static final long serialVersionUID = 1L;

    public interface ExternalTJobExecutionView {
    }

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ ExternalTJobExecutionView.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exTJob")
    @JsonIgnoreProperties(value = "exTJobExecs")
    private ExternalTJob exTJob;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "monitoringIndex")
    private String monitoringIndex = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "result")
    @JsonProperty("result")
    private ResultEnum result = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @ElementCollection
    @MapKeyColumn(name = "VAR_NAME", length = 200)
    @Column(name = "value", length = 400)
    @CollectionTable(name = "ExternalTJobExec_ENV_VARS", joinColumns = @JoinColumn(name = "ExternalTJobExec"))
    private Map<String, String> envVars;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalTJobExecution() {
        this.envVars = new HashMap<>();
        this.result = ResultEnum.NOT_EXECUTED;
    }

    public ExternalTJobExecution(Long id) {
        this.id = id == null ? 0 : id;
        this.envVars = new HashMap<>();
        this.result = ResultEnum.NOT_EXECUTED;
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

    public ExternalTJob getExTJob() {
        return exTJob;
    }

    public void setExTJob(ExternalTJob exTJob) {
        this.exTJob = exTJob;
    }

    public String getMonitoringIndex() {
        return monitoringIndex;
    }

    public void setMonitoringIndex(String monitoringIndex) {
        this.monitoringIndex = monitoringIndex;
    }

    public ResultEnum getResult() {
        return result;
    }

    public void setResult(ResultEnum result) {
        this.result = result;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    public String[] getMonitoringIndicesList() {
        return this.getMonitoringIndex().split(",");
    }

}
