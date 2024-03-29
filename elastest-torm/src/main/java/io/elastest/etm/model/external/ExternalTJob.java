package io.elastest.etm.model.external;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
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
    @JsonIgnoreProperties(value = "exTJobs", allowSetters = true)
    private ExternalProject exProject;

    @JsonView({ ExternalTJobView.class, ExternalProjectView.class })
    @JsonProperty("exTJobExecs")
    @OneToMany(mappedBy = "exTJob", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = { "exTJob",
            "exTestExecs" }, allowSetters = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ExternalTJobExecution> exTJobExecs;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTestExecutionView.class, ExternalTJobExecutionView.class })
    @JsonProperty("exTestCases")
    @ManyToMany(cascade = { CascadeType.PERSIST }, fetch = FetchType.LAZY)
    @JoinTable(name = "exTJob_exTestCase", joinColumns = @JoinColumn(name = "exTJob_id"), inverseJoinColumns = @JoinColumn(name = "exTestCase_id"))
    @JsonIgnoreProperties(value = "exTJob", allowSetters = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ExternalTestCase> exTestCases = new ArrayList<ExternalTestCase>();

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    // MANDATORY add/remove methods for manyToOne below
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sut")
    @JsonProperty("sut")
    @JsonIgnoreProperties(value = "exTJobs", allowSetters = true)
    private SutSpecification sut = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "execDashboardConfig", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("execDashboardConfig")
    private String execDashboardConfig = null;

    @JsonView({ ExternalProjectView.class, ExternalTJobView.class,
            ExternalTJobExecutionView.class, ExternalTestCaseView.class,
            ExternalTestExecutionView.class })
    @Column(name = "selectedServices", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("esmServicesString")
    private String selectedServices;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public ExternalTJob() {
        exTestCases = new ArrayList<ExternalTestCase>();
    }

    public ExternalTJob(Long id) {
        exTestCases = new ArrayList<ExternalTestCase>();
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

    // MANDATORY add/remove methods for manyToOne
    public void addExTestCase(ExternalTestCase exTestCase) {
        if (exTestCases == null) {
            exTestCases = new ArrayList<>();
        }
        exTestCases.add(exTestCase);
        exTestCase.getExTJobs().add(this);
    }

    public void removeExTestCase(ExternalTestCase exTestCase) {
        if (exTestCases == null) {
            exTestCases = new ArrayList<>();
        }
        exTestCases.remove(exTestCase);
        exTestCase.getExTJobs().remove(this);
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

    public String getSelectedServices() {
        return selectedServices;
    }

    public void setSelectedServices(String selectedServices) {
        this.selectedServices = selectedServices;
    }

    /* ************************************** */
    /* *************** Others *************** */
    /* ************************************** */

    public boolean isWithSut() {
        return this.sut != null;
    }

    public boolean isSelectedService(String serviceName) {
        try {
            List<ObjectNode> services = getSelectedServicesObj();
            for (ObjectNode service : services) {
                String currentServiceName = service.get("name").asText();
                if (serviceName.toLowerCase()
                        .equals(currentServiceName.toLowerCase())) {
                    return (service.get("selected").asBoolean());
                }
            }
        } catch (Exception e) {
        }

        return false;
    }

    public List<ObjectNode> getSelectedServicesObj()
            throws JsonParseException, JsonMappingException, IOException {
        if (selectedServices != null) {
            ObjectMapper mapper = new ObjectMapper();

            return Arrays.asList(
                    mapper.readValue(selectedServices, ObjectNode[].class));
        } else {
            return new ArrayList<>();
        }
    }

    public List<ObjectNode> getSupportServicesObj() {
        List<ObjectNode> services = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            services = Arrays.asList(mapper
                    .readValue(this.getSelectedServices(), ObjectNode[].class));
        } catch (Exception e) {
        }
        return services;
    }

    @Override
    public String toString() {
        return "ExternalTJob [id=" + id + ", name=" + name + ", externalId="
                + externalId + ", externalSystemId=" + externalSystemId
                + ", exProject="
                + (exProject != null ? exProject.getId() : "null")
                + ", exTJobExecs=" + exTJobExecs + ", exTestCases="
                + exTestCases + ", sut=" + (sut != null ? sut.getId() : "null")
                + ", execDashboardConfig=" + execDashboardConfig + "]";
    }

}
