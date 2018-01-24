package io.elastest.etm.model.external;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.external.ExternalProject.BasicAttExternalProject;
import io.elastest.etm.model.external.ExternalTestCase.BasicAttExternalTestCase;

@Entity
public class ExternalTestExecution implements Serializable {
    private static final long serialVersionUID = 1L;

    public interface BasicAttExternalTestExecution {
    }

    @EmbeddedId
    @JsonView({ BasicAttExternalProject.class })
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private ExternalId id = null;

    @JsonView({ BasicAttExternalTestExecution.class,
            BasicAttExternalTestCase.class })
    @Column(name = "esIndex")
    private String esIndex = null;

    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class })
    @Column(name = "fields", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("fields")
    private String fields = null;

    @JsonView({ BasicAttExternalTestExecution.class,
            BasicAttExternalTestCase.class })
    @Column(name = "result")
    @JsonProperty("result")
    private String result = null;

    // bi-directional many-to-one association to ExternalTestCase
    @JsonView({ BasicAttExternalTestExecution.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "tcExternalId", referencedColumnName = "externalId"),
            @JoinColumn(name = "tcExternalSystemId", referencedColumnName = "externalSystemId") })
    private ExternalTestCase exTestCase;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalTestExecution() {
    }

    public ExternalTestExecution(ExternalId id) {
        this.id = id;
    }

    /* *****************************/
    /* ***** Getters/Setters *******/
    /* *****************************/

    public ExternalId getId() {
        return id;
    }

    public void setId(ExternalId id) {
        this.id = id;
    }

    public String getEsIndex() {
        return esIndex;
    }

    public void setEsIndex(String esIndex) {
        this.esIndex = esIndex;
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

    public ExternalTestCase getExTestCase() {
        return exTestCase;
    }

    public void setExTestCase(ExternalTestCase exTestCase) {
        this.exTestCase = exTestCase;
    }

}
