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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.external.ExternalProject.BasicAttExternalProject;
import io.elastest.etm.model.external.ExternalTestCase.BasicAttExternalTestCase;

@Entity
public class ExternalTestExecution implements Serializable {
    private static final long serialVersionUID = 1L;

    public interface BasicAttExternalTestExecution {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView({ BasicAttExternalTestExecution.class,
            BasicAttExternalTestCase.class })
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ BasicAttExternalTestExecution.class,
            BasicAttExternalTestCase.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name = null;

    @JsonView({ BasicAttExternalTestExecution.class,
            BasicAttExternalTestCase.class })
    @Column(name = "externalId")
    @JsonProperty("externalId")
    private String externalId;

    @JsonView({ BasicAttExternalTestExecution.class,
            BasicAttExternalTestCase.class })
    @Column(name = "esIndex")
    private String esIndex = null;

    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class })
    @Column(name = "fields", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("fields")
    private String fields = null;

    // bi-directional many-to-one association to ExternalTestCase
    @JsonView({ BasicAttExternalTestExecution.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exTestCase")
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

    public ExternalTestCase getExTestCase() {
        return exTestCase;
    }

    public void setExTestCase(ExternalTestCase exTestCase) {
        this.exTestCase = exTestCase;
    }

}
