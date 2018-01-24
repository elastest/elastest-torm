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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.external.ExternalProject.BasicAttExternalProject;

@Entity
public class ExternalTestCase implements Serializable {
    private static final long serialVersionUID = 1L;

    public interface BasicAttExternalTestCase {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class })
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name = null;

    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class })
    @Column(name = "externalId")
    @JsonProperty("externalId")
    private String externalId;

    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class })
    @Column(name = "fields", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("fields")
    private String fields = null;

    // bi-directional many-to-one association to ExternalProject
    @JsonView({ BasicAttExternalTestCase.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exProject")
    private ExternalProject exProject;

    // bi-directional many-to-one association to ExternalTestExecution
    @JsonView({ BasicAttExternalTestCase.class, BasicAttExternalProject.class })
    @OneToMany(mappedBy = "exTestCase", cascade = CascadeType.REMOVE)
    private List<ExternalTestExecution> exTestExecs;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalTestCase() {
    }

    public ExternalTestCase(Long id) {
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

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public ExternalProject getExProject() {
        return exProject;
    }

    public void setExProject(ExternalProject exProject) {
        this.exProject = exProject;
    }

    public List<ExternalTestExecution> getExTestExecs() {
        return exTestExecs;
    }

    public void setExTestExecs(List<ExternalTestExecution> exTestExecs) {
        this.exTestExecs = exTestExecs;
    }

}
