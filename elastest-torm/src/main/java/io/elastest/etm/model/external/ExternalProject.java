package io.elastest.etm.model.external;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.elastest.etm.model.external.ExternalTestCase.BasicAttExternalTestCase;
import io.elastest.etm.model.external.ExternalTestExecution.BasicAttExternalTestExecution;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ExternalProject {

    public interface BasicAttExternalProject {
    }

    @EmbeddedId
    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private ExternalId id = null;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name = null;

    @JsonView({ BasicAttExternalProject.class, BasicAttExternalTestCase.class,
            BasicAttExternalTestExecution.class })
    @Column(name = "type")
    @JsonProperty("type")
    private TypeEnum type = null;

    @JsonView(BasicAttExternalProject.class)
    @JsonProperty("exTestCases")
    // bi-directional many-to-one association to ExternalTestCase
    @OneToMany(mappedBy = "exProject", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<ExternalTestCase> exTestCases;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalProject() {
    }

    public ExternalProject(ExternalId id) {
        this.id = id;
    }

    public enum TypeEnum {
        TESTLINK("TESTLINK");

        private String value;

        TypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static TypeEnum fromValue(String text) {
            for (TypeEnum b : TypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    /* *****************************/
    /* ***** Getters/Setters *******/
    /* *****************************/

    public String getName() {
        return name;
    }

    public ExternalId getId() {
        return id;
    }

    public void setId(ExternalId id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }

    public List<ExternalTestCase> getExTestCases() {
        return exTestCases;
    }

    public void setExTestCases(List<ExternalTestCase> exTestCases) {
        this.exTestCases = exTestCases;
    }

}
