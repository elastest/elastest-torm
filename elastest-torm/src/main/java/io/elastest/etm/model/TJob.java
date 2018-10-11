package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.TJobExecution.BasicAttTJobExec;
import io.elastest.etm.utils.UtilTools;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * TJob
 */

@Entity
@ApiModel(description = "Entity that represents the test to run against a SUT.")
public class TJob {

    public interface BasicAttTJob {
    }

    @JsonView({ BasicAttTJob.class, BasicAttProject.class,
            BasicAttTJobExec.class })
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ BasicAttTJob.class, BasicAttTJobExec.class,
            BasicAttProject.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name = null;

    @JsonView({ BasicAttTJob.class, BasicAttProject.class })
    @Column(name = "image_name")
    @JsonProperty("imageName")
    private String imageName = null;

    @JsonView({ BasicAttTJob.class, BasicAttProject.class,
            BasicAttTJobExec.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sut")
    @JsonProperty("sut")
    private SutSpecification sut = null;

    // bi-directional many-to-one association to TJobExec
    @JsonView({ BasicAttTJob.class, BasicAttProject.class })
    @OneToMany(mappedBy = "tJob", cascade = CascadeType.REMOVE)
    private List<TJobExecution> tjobExecs;

    // bi-directional many-to-one association to Project
    @JsonView({ BasicAttTJob.class, BasicAttTJobExec.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project")
    private Project project;

    @JsonView({ BasicAttTJob.class, BasicAttProject.class })
    @ElementCollection
    @CollectionTable(name = "TJobParameter", joinColumns = @JoinColumn(name = "TJob"))
    private List<Parameter> parameters;

    @JsonView({ BasicAttTJob.class, BasicAttProject.class })
    @Column(name = "commands", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("commands")
    private String commands;

    @JsonView({ BasicAttTJob.class, BasicAttProject.class })
    @Column(name = "resultsPath")
    @JsonProperty("resultsPath")
    private String resultsPath = null;

    @JsonView({ BasicAttTJob.class, BasicAttProject.class,
            BasicAttTJobExec.class })
    @Column(name = "external")
    @JsonProperty("external")
    private boolean external = false;

    @JsonView({ BasicAttTJob.class, BasicAttProject.class,
            BasicAttTJobExec.class })
    @Column(name = "execDashboardConfig", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("execDashboardConfig")
    private String execDashboardConfig = null;

    @JsonView({ BasicAttTJob.class, BasicAttProject.class,
            BasicAttTJobExec.class })
    @Column(name = "selectedServices", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("esmServicesString")
    private String selectedServices;

    @JsonView({ BasicAttTJobExec.class, BasicAttTJob.class,
            BasicAttProject.class })
    @ElementCollection
    @MapKeyColumn(name = "URL_NAME", length = 100)
    @Column(name = "URL_VALUE", length = 400)
    @CollectionTable(name = "TJOB_EXTERNAL_URLS", joinColumns = @JoinColumn(name = "TJOB"))
    private Map<String, String> externalUrls;

    @JsonView({ BasicAttTJob.class, BasicAttProject.class,
            BasicAttTJobExec.class })
    @Column(name = "multi")
    @JsonProperty("multi")
    private Boolean multi = false;

    @JsonView({ BasicAttTJob.class, BasicAttProject.class,
            BasicAttTJobExec.class })
    @ElementCollection
    @CollectionTable(name = "TJobMultiConfiguration", joinColumns = @JoinColumn(name = "TJob"))
    @MapKeyColumn(name = "NAME")
    @Column(name = "VALUES", length = 16777215)
    private List<MultiConfig> multiConfigurations = new ArrayList<MultiConfig>();

    /* ******************** */
    /* *** Constructors *** */
    /* ******************** */

    public TJob() {
    }

    public TJob(Long id, String name, String imageName, SutSpecification sut,
            Project project, boolean external, String execDashboardConfig,
            String selectedServices) {
        this.id = id == null ? 0 : id;
        this.name = name;
        this.imageName = imageName;
        this.sut = sut;
        this.project = project;
        this.external = external;
        this.execDashboardConfig = execDashboardConfig;
        this.selectedServices = selectedServices;
        this.externalUrls = new HashMap<>();
        this.multiConfigurations = new ArrayList<MultiConfig>();
    }

    /* *********************** */
    /* *** Getters/Setters *** */
    /* *********************** */

    /**
     * Get id
     * 
     * @return id
     **/
    @ApiModelProperty(example = "1", value = "Identifies a TJob.")

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id == null ? 0 : id;
    }

    /**
     * Get name
     * 
     * @return name
     **/
    @ApiModelProperty(example = "myFirstTJob", required = true, value = "The name of the TJob.")
    @NotNull

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get imageName
     * 
     * @return imageName
     **/
    @ApiModelProperty(required = true, example = "elastest/test-etm-test1", value = "Name of the docker image that contains the test.")

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * Get sut
     * 
     * @return sut
     **/
    @ApiModelProperty(example = "{ id:\"1\" }", value = "The SUT associated with the TJob.")

    public SutSpecification getSut() {
        return sut;
    }

    public void setSut(SutSpecification sut) {
        this.sut = sut;
    }

    /**
     * Get project
     * 
     * @return project
     **/
    @ApiModelProperty(required = true, value = "Project to which the TJob is associated.", example = "{ id:\"1\" }")

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Get TJobExecutions
     * 
     * @return tjobexecs
     **/

    @ApiModelProperty(value = "List of TJob Executions of a TJob")

    public List<TJobExecution> getTjobExecs() {
        return this.tjobExecs;
    }

    public void setTjobExecs(List<TJobExecution> tjobExec) {
        this.tjobExecs = tjobExec;
    }

    public TJobExecution addTjobExec(TJobExecution tjobExec) {
        getTjobExecs().add(tjobExec);
        tjobExec.setTjob(this);

        return tjobExec;
    }

    public TJobExecution removeTjobExec(TJobExecution tjobExec) {
        getTjobExecs().remove(tjobExec);
        tjobExec.setTjob(null);
        return tjobExec;
    }

    @ApiModelProperty(value = "List of parameters to pass to a docker container as an environment variables")
    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public TJob logs(List<Parameter> parameters) {
        this.parameters = parameters;
        return this;
    }

    public TJob addLogsItem(Parameter parameter) {
        if (this.parameters == null) {
            this.parameters = new ArrayList<Parameter>();
        }
        this.parameters.add(parameter);
        return this;
    }

    @ApiModelProperty(value = "Commands to execute inside a Docker Container")
    public String getCommands() {
        return commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
    }

    public String getExecDashboardConfigPath() {
        return execDashboardConfig;
    }

    public void setExecDashboardConfigPath(String execDashboardConfig) {
        this.execDashboardConfig = execDashboardConfig;
    }

    @ApiModelProperty(required = true, value = "Boolean variable that indicates whether a TJob is really an external Job", example = "false")
    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    @ApiModelProperty(required = true, example = "/app1TestJobsJenkins/target/surefire-reports/TEST-es.tfcfrd.app1TestJobsJenkins.AppTest.xml", value = "absolute path of results file")
    public String getResultsPath() {
        return resultsPath;
    }

    public void setResultsPath(String resultsPath) {
        this.resultsPath = resultsPath;
    }

    public String getSelectedServices() {
        return selectedServices;
    }

    public void setSelectedServices(String selectedServices) {
        this.selectedServices = selectedServices;
    }

    public Map<String, String> getExternalUrls() {
        return externalUrls;
    }

    public void setExternalUrls(Map<String, String> externalUrls) {
        this.externalUrls = externalUrls;
    }

    public Boolean isMulti() {
        return multi != null ? multi : false;
    }

    public void setMulti(Boolean multi) {
        this.multi = multi != null ? multi : false;
    }

    public List<MultiConfig> getMultiConfigurations() {
        return multiConfigurations;
    }

    public void setMultiConfigurations(List<MultiConfig> multiConfigurations) {
        this.multiConfigurations = multiConfigurations;
    }

    /* ******************** */
    /* ****** Others ****** */
    /* ******************** */
    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TJob tjob = (TJob) o;
        return Objects.equals(this.id, tjob.id)
                && Objects.equals(this.name, tjob.name)
                && Objects.equals(this.imageName, tjob.imageName)
                && Objects.equals(this.sut, tjob.sut)
                && Objects.equals(this.project, tjob.project)
                && Objects.equals(this.tjobExecs, tjob.tjobExecs)
                && Objects.equals(this.parameters, tjob.parameters)
                && Objects.equals(this.commands, tjob.commands)
                && Objects.equals(this.resultsPath, tjob.resultsPath)
                && Objects.equals(this.external, tjob.external)
                && Objects.equals(this.execDashboardConfig,
                        tjob.execDashboardConfig)
                && Objects.equals(this.selectedServices, tjob.selectedServices)
                && Objects.equals(this.multi, tjob.multi) && Objects.equals(
                        this.multiConfigurations, tjob.multiConfigurations);

    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, imageName, sut, project, tjobExecs,
                parameters, execDashboardConfig, selectedServices, multi,
                multiConfigurations);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TJob {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    imageName: ").append(toIndentedString(imageName))
                .append("\n");
        sb.append("    sut: ").append(toIndentedString(sut)).append("\n");
        sb.append("    project: ").append(toIndentedString(project))
                .append("\n");
        sb.append("    tjobExecs: ").append(toIndentedString(tjobExecs))
                .append("\n");
        sb.append("    parameters: ").append(toIndentedString(parameters))
                .append("\n");
        sb.append("    commands: ").append(toIndentedString(commands))
                .append("\n");
        sb.append("    resultsPath: ").append(toIndentedString(resultsPath))
                .append("\n");
        sb.append("    external: ").append(toIndentedString(external))
                .append("\n");
        sb.append("    execDashboardConfig: ")
                .append(toIndentedString(execDashboardConfig)).append("\n");
        sb.append("    selectedServices: ")
                .append(toIndentedString(selectedServices)).append("\n");
        sb.append("    multi: ").append(toIndentedString(multi)).append("\n");
        sb.append("    multiConfigurations: ")
                .append(toIndentedString(multiConfigurations)).append("\n");
        sb.append("}");
        return sb.toString();
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
        ObjectMapper mapper = new ObjectMapper();

        return Arrays
                .asList(mapper.readValue(selectedServices, ObjectNode[].class));
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

    public boolean isWithSut() {
        return this.sut != null;
    }

    public boolean isEmsTssSelected()
            throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<TJobSupportService> services = Arrays.asList(mapper.readValue(
                this.getSelectedServices(), TJobSupportService[].class));

        for (TJobSupportService service : services) {
            if (service.getName().toLowerCase().equals("ems")
                    && service.isSelected()) {
                return true;
            }
        }

        return false;
    }

    public Map<String, String> getTJobTSSConfigEnvVars(String tssName)
            throws Exception {
        Map<String, String> tssConfigEnvVars = new HashMap<>();
        List<ObjectNode> tJobTssList = getSupportServicesObj();

        for (ObjectNode tJobSuportService : tJobTssList) {
            JsonNode tJobTssNameObj = tJobSuportService.get("name");
            if (tJobTssNameObj != null && tssName
                    .equals(UtilTools.stringFromJsonNode(tJobTssNameObj))) {
                JsonNode selectedObj = tJobSuportService.get("selected");
                if (selectedObj != null && selectedObj.asBoolean()) {
                    JsonNode manifestObj = tJobSuportService.get("manifest");
                    if (manifestObj != null) {
                        JsonNode configObj = manifestObj.get("config");
                        if (configObj != null) {
                            Iterable<String> singleConfigNamesIterator = () -> configObj
                                    .fieldNames();
                            for (String singleConfigName : singleConfigNamesIterator) {
                                JsonNode singleConfig = configObj
                                        .get(singleConfigName);
                                String envName = singleConfigName.replaceAll(
                                        "([a-z])_?([A-Z])", "$1_$2");
                                envName = "ET_CONFIG_" + envName.toUpperCase();
                                JsonNode valueObj = singleConfig.get("value");
                                if (valueObj != null) {
                                    tssConfigEnvVars.put(envName,
                                            valueObj.toString());
                                }
                            }
                        }
                    }
                }
            }
        }

        return tssConfigEnvVars;
    }
}
