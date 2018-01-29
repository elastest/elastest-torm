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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.external.ExternalProject.BasicAttExternalProject;
import io.elastest.etm.model.external.ExternalTJob.BasicAttExternalTJob;
import io.elastest.etm.model.external.ExternalTestCase.BasicAttExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution.BasicAttExternalTestExecution;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ExternalTJobExecution implements Serializable {
    private static final long serialVersionUID = 1L;

    public interface BasicAttExternalTJobExecution {
    }

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTJobExecution.class, BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ BasicAttExternalProject.class,
            BasicAttExternalTJobExecution.class, BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exTJob")
    private ExternalTJob exTJob;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTJob.class,
            BasicAttExternalTJobExecution.class, BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "esIndex")
    private String esIndex = null;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalTJobExecution() {
    }

    public ExternalTJobExecution(Long id) {
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

    public ExternalTJob getExTJob() {
        return exTJob;
    }

    public void setExTJob(ExternalTJob exTJob) {
        this.exTJob = exTJob;
    }

    public String getEsIndex() {
        return esIndex;
    }

    public void setEsIndex(String esIndex) {
        this.esIndex = esIndex;
    }

}
