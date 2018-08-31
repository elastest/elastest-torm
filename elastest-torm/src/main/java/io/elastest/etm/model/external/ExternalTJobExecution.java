package io.elastest.etm.model.external;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.SutTypeEnum;
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
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ ExternalTJobExecutionView.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exTJob")
    @JsonIgnoreProperties(value = "exTJobExecs", allowSetters = true)
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
    @LazyCollection(LazyCollectionOption.FALSE)
    private ResultEnum result = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "resultMsg")
    private String resultMsg = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @ElementCollection
    @MapKeyColumn(name = "VAR_NAME", length = 200)
    @Column(name = "value", length = 400)
    @CollectionTable(name = "ExternalTJobExec_ENV_VARS", joinColumns = @JoinColumn(name = "ExternalTJobExec"))
    private Map<String, String> envVars;

    @JsonView({ ExternalTJobExecutionView.class })
    @OneToMany(mappedBy = "exTJobExec", cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = { "exTJobExec" }, allowSetters = true)
    private List<ExternalTestExecution> exTestExecs;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "startDate")
    private Date startDate = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "endDate")
    private Date endDate = null;

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

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    public String getExternalTJobExecMonitoringIndex() {
        return "ext" + getExTJob().getId() + "_e" + getId();
    }

    public void generateMonitoringIndex() {
        SutSpecification sut = this.getExTJob().getSut();
        String monitoringIndex = this.getExternalTJobExecMonitoringIndex();
        if (sut != null && sut.getSutType() == SutTypeEnum.DEPLOYED) {
            monitoringIndex += ",s" + sut.getId() + "_e"
                    + sut.getCurrentSutExec();
        }
        this.setMonitoringIndex(monitoringIndex);
    }

    public String[] getMonitoringIndicesList() {
        return this.getMonitoringIndex().split(",");
    }

    public List<ExternalTestExecution> getExTestExecs() {
        return exTestExecs;
    }

    public void setExTestExecs(List<ExternalTestExecution> exTestExecs) {
        this.exTestExecs = exTestExecs;
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

    public boolean isWithSut() {
        return this.exTJob != null && this.exTJob.isWithSut();
    }

    @Override
    public String toString() {
        return "ExternalTJobExecution [id=" + id + ", exTJob=" + exTJob
                + ", monitoringIndex=" + monitoringIndex + ", result=" + result
                + ", resultMsg=" + resultMsg + ", envVars=" + envVars
                + ", exTestExecs=" + exTestExecs + ", startDate=" + startDate
                + ", endDate=" + endDate + "]";
    }

}
