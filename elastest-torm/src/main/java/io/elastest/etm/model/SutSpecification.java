package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.ArrayList;
import java.util.List;
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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Enums.ProtocolEnum;
import io.elastest.etm.model.Project.ProjectMediumView;
import io.elastest.etm.model.TJob.TJobCompleteView;
import io.elastest.etm.model.TJob.TJobMediumView;
import io.elastest.etm.model.TJobExecution.TJobExecCompleteView;
import io.elastest.etm.model.external.ExternalMonitoringDBForLogs;
import io.elastest.etm.model.external.ExternalMonitoringDBForMetrics;
import io.elastest.etm.model.external.ExternalProject;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalTJob;
import io.elastest.etm.model.external.ExternalTJob.ExternalTJobView;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * SutSpecification
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-05-19T13:25:11.074+02:00")

@Entity
@ApiModel(description = "SUT definition.")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class SutSpecification {

    public interface SutView {
    }

    @Id
    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, TJobMediumView.class,
            ExternalTJobView.class, TJobExecCompleteView.class })
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobMediumView.class })
    @JsonProperty("name")
    private String name = null;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "specification", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("specification")
    private String specification = null;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @JsonProperty("description")
    private String description = null;

    @JsonProperty("sutExecution")
    @JsonIgnoreProperties(value = "sutSpecification", allowSetters = true)
    @OneToMany(mappedBy = "sutSpecification", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<SutExecution> sutExecution = null;

    @JsonView({ SutView.class })
    @JoinColumn(name = "project_id", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    private Project project = null;

    @JsonProperty("tjobs")
    @OneToMany(mappedBy = "sut", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<TJob> tJobs;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "sutType", nullable = false)
    @JsonProperty("sutType")
    private SutTypeEnum sutType;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class, TJobExecCompleteView.class })
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "eimConfig")
    @JsonIgnoreProperties(value = "sutSpecification", allowSetters = true)
    private EimConfig eimConfig;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class, TJobExecCompleteView.class })
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "eimMonitoringConfig")
    @JsonIgnoreProperties(value = "sutSpecification", allowSetters = true)
    private EimMonitoringConfig eimMonitoringConfig;

    // Indicates if you want to instrumentalize the sut
    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "instrumentalize")
    @JsonProperty("instrumentalize")
    private boolean instrumentalize = false;

    // Indicates if the Sut is instrumentalized
    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "instrumentalized")
    @JsonProperty("instrumentalized")
    private Boolean instrumentalized = false;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "currentSutExec")
    @JsonProperty("currentSutExec")
    private Long currentSutExec = null;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "instrumentedBy", nullable = false)
    @JsonProperty("instrumentedBy")
    private InstrumentedByEnum instrumentedBy;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "externalMonitoringDBForLogs")
    @JsonIgnoreProperties(value = "sutSpecification", allowSetters = true)
    private ExternalMonitoringDBForLogs externalMonitoringDBForLogs;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "externalMonitoringDBForMetrics")
    @JsonIgnoreProperties(value = "sutSpecification", allowSetters = true)
    private ExternalMonitoringDBForMetrics externalMonitoringDBForMetrics;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "protocol")
    @JsonProperty("protocol")
    private ProtocolEnum protocol = ProtocolEnum.HTTP;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "port")
    @JsonProperty("port")
    private String port = null;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "managedDockerType", nullable = false)
    @JsonProperty("managedDockerType")
    private ManagedDockerType managedDockerType;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @JsonProperty("mainService")
    private String mainService = null;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @ElementCollection
    @CollectionTable(name = "SutParameter", joinColumns = @JoinColumn(name = "SutSpecification"))
    private List<Parameter> parameters;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "commands", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("commands")
    private String commands;

    @JsonView({ SutView.class, ProjectMediumView.class,
            ExternalProjectView.class, ExternalTJobView.class,
            TJobCompleteView.class })
    @Column(name = "commandsOption", nullable = false)
    @JsonProperty("commandsOption")
    private CommandsOptionEnum commandsOption;

    /* ** External ** */

    @JsonView({ SutView.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exProject", nullable = true)
    @JsonIgnoreProperties(value = { "suts", "exTJobs" }, allowSetters = true)
    private ExternalProject exProject = null;

    @JsonView({ SutView.class })
    @JsonProperty("exTJobs")
    @OneToMany(mappedBy = "sut", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JsonIgnoreProperties(value = { "sut", "exProject", "exTJobExecs",
            "exTestCases" }, allowSetters = true)
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<ExternalTJob> exTJobs;

    /* **************************/
    /* ***** Constructors *******/
    /* **************************/

    public SutSpecification() {
        this.id = new Long(0);
        this.commandsOption = CommandsOptionEnum.DEFAULT;
        this.instrumentedBy = InstrumentedByEnum.WITHOUT;
        this.instrumentalized = false;
    }

    public SutSpecification(Long id, String name, String specification,
            String description, List<TJob> tJobs, SutTypeEnum sutType,
            boolean instrumentalize, Boolean instrumentalized,
            Long currentSutExec, InstrumentedByEnum instrumentedBy, String port,
            ManagedDockerType managedDockerType,
            CommandsOptionEnum commandsOption, ProtocolEnum protocol) {
        this.id = id == null ? 0 : id;
        this.name = name;
        this.specification = specification;
        this.description = description;
        this.tJobs = tJobs;
        this.sutType = sutType;
        this.instrumentalize = instrumentalize;
        this.instrumentalized = instrumentalized != null ? instrumentalized
                : false;
        this.currentSutExec = currentSutExec;
        this.instrumentedBy = instrumentedBy;
        this.port = port;
        this.managedDockerType = managedDockerType;
        this.commandsOption = commandsOption;
        this.protocol = protocol;
    }

    // Normal Project
    public SutSpecification(Long id, String name, String specification,
            String description, Project project, List<TJob> tJobs,
            SutTypeEnum sutType, boolean instrumentalize,
            Boolean instrumentalized, Long currentSutExec,
            InstrumentedByEnum instrumentedBy, String port,
            ManagedDockerType managedDockerType,
            CommandsOptionEnum commandsOption, ProtocolEnum protocol) {
        this(id, name, specification, description, tJobs, sutType,
                instrumentalize, instrumentalized, currentSutExec,
                instrumentedBy, port, managedDockerType, commandsOption,
                protocol);
        this.project = project;
    }

    // External Project
    public SutSpecification(Long id, String name, String specification,
            String description, ExternalProject exProject, List<TJob> tJobs,
            SutTypeEnum sutType, boolean instrumentalize,
            Boolean instrumentalized, Long currentSutExec,
            InstrumentedByEnum instrumentedBy, String port,
            ManagedDockerType managedDockerType,
            CommandsOptionEnum commandsOption, ProtocolEnum protocol) {
        this(id, name, specification, description, tJobs, sutType,
                instrumentalize, instrumentalized, currentSutExec,
                instrumentedBy, port, managedDockerType, commandsOption,
                protocol);
        this.exProject = exProject;
    }

    public SutSpecification(SutSpecification sut) {
        this.id = 0l;

        this.name = sut.name;
        this.specification = sut.specification;
        this.description = sut.description;
        this.sutExecution = new ArrayList<>();
        this.project = sut.project;
        this.tJobs = null;
        this.sutType = sut.sutType;
        this.instrumentalize = false;
        this.instrumentalized = false;
        this.currentSutExec = null;
        this.instrumentedBy = sut.instrumentedBy;
        this.externalMonitoringDBForLogs = new ExternalMonitoringDBForLogs(
                sut.externalMonitoringDBForLogs);
        this.externalMonitoringDBForMetrics = new ExternalMonitoringDBForMetrics(
                sut.externalMonitoringDBForMetrics);
        this.protocol = sut.protocol;
        this.port = sut.port;
        this.managedDockerType = sut.managedDockerType;
        this.mainService = sut.mainService;
        this.commands = sut.commands;
        this.commandsOption = sut.commandsOption;
        this.exProject = sut.exProject;
        this.exTJobs = null;

        if (sut.parameters != null) {
            this.parameters = new ArrayList<>();
            for (Parameter param : sut.parameters) {
                Parameter newParam = new Parameter(param);
                this.parameters.add(newParam);
            }
        }

        this.eimConfig = new EimConfig(sut.eimConfig);
        this.eimMonitoringConfig = new EimMonitoringConfig(
                sut.eimMonitoringConfig);
    }

    /* *********************************************** */
    /* ******************** Enums ******************** */
    /* *********************************************** */

    public enum SutTypeEnum {
        MANAGED("MANAGED"),

        REPOSITORY("REPOSITORY"),

        DEPLOYED("DEPLOYED");

        private String value;

        SutTypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static SutTypeEnum fromValue(String text) {
            for (SutTypeEnum b : SutTypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public enum InstrumentedByEnum {
        WITHOUT("WITHOUT"),

        ELASTEST("ELASTEST"),

        ADMIN("ADMIN"),

        EXTERNAL_MONITORING_DB("EXTERNAL_MONITORING_DB");

        private String value;

        InstrumentedByEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static InstrumentedByEnum fromValue(String text) {
            for (InstrumentedByEnum b : InstrumentedByEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public enum ManagedDockerType {
        IMAGE("IMAGE"),

        COMPOSE("COMPOSE"),

        COMMANDS("COMMANDS");

        private String value;

        ManagedDockerType(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static ManagedDockerType fromValue(String text) {
            for (ManagedDockerType b : ManagedDockerType.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    public enum CommandsOptionEnum {
        DEFAULT("DEFAULT"),

        IN_NEW_CONTAINER("IN_NEW_CONTAINER"),

        IN_DOCKER_COMPOSE("IN_DOCKER_COMPOSE");

        private String value;

        CommandsOptionEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static CommandsOptionEnum fromValue(String text) {
            for (CommandsOptionEnum b : CommandsOptionEnum.values()) {
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

    /**
     * Get id
     * 
     * @return id
     **/
    @ApiModelProperty(value = "Value that identifies the SUT Specification.")

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
    @ApiModelProperty(example = "sut definition 1", required = true, value = "Name of the SUT.")
    @NotNull

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Specification
     * 
     * @return specification
     **/
    @ApiModelProperty(example = "https://github.com/elastest/elastest-torm.git", required = true, value = "URL of the GitHub repository where the SUT code is stored (not necessary if the ImageName field is filled)")
    @NotNull
    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    /**
     * Get desc
     * 
     * @return desc
     **/
    @ApiModelProperty(example = "My Web Application", value = "Brief description of a SUT")
    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    /**
     * Get sutExecution
     * 
     * @return sutExecution
     **/
    @ApiModelProperty(value = "List of the SUT Executions of a SUT")
    public List<SutExecution> getSutExecution() {
        return sutExecution;
    }

    public void setSutExecution(List<SutExecution> sutExecution) {
        this.sutExecution = sutExecution;
    }

    public SutExecution addSuTExecution(SutExecution sutExecution) {
        getSutExecution().add(sutExecution);
        sutExecution.setSutSpecification(this);

        return sutExecution;
    }

    public SutExecution removeSuTExecution(SutExecution sutExecution) {
        getSutExecution().remove(sutExecution);
        sutExecution.setSutSpecification(null);

        return sutExecution;
    }

    /**
     * Get project
     * 
     * @return project
     **/
    @ApiModelProperty(required = true, value = "Project to which the SUT is associated", example = "{ id:\"1\" }")
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Get tJobs
     * 
     * @return tJobs
     **/

    @Valid
    @ApiModelProperty(value = "List of TJobs associated to a SUT Specification")
    public List<TJob> getTJobs() {
        return tJobs;
    }

    public void setTJobs(List<TJob> tJobs) {
        this.tJobs = tJobs;
    }

    public TJob addTJob(TJob tJob) {
        getTJobs().add(tJob);
        tJob.setSut(this);

        return tJob;
    }

    public TJob removeTJob(TJob tJob) {
        getTJobs().remove(tJob);
        tJob.setSut(null);

        return tJob;
    }

    /**
     * Get sutType
     * 
     * @return sutType
     **/

    public SutTypeEnum getSutType() {
        return sutType;
    }

    public void setSutType(SutTypeEnum sutType) {
        this.sutType = sutType;
    }

    /**
     * Get eimConfig
     * 
     * @return eimConfig
     **/

    @ApiModelProperty(value = "EIM configuration")
    public EimConfig getEimConfig() {
        return eimConfig;
    }

    public void setEimConfig(EimConfig eimConfig) {
        this.eimConfig = eimConfig;
    }

    /**
     * Get eimMonitoringConfig
     * 
     * @return eimMonitoringConfig
     **/
    public EimMonitoringConfig getEimMonitoringConfig() {
        return eimMonitoringConfig;
    }

    public void setEimMonitoringConfig(
            EimMonitoringConfig eimMonitoringConfig) {
        this.eimMonitoringConfig = eimMonitoringConfig;
    }

    /**
     * Get instrumentalize
     * 
     * @return instrumentalize
     **/

    public boolean isInstrumentalize() {
        return instrumentalize;
    }

    public void setInstrumentalize(boolean instrumentalize) {
        this.instrumentalize = instrumentalize;
    }

    /**
     * Get instrumentalized
     * 
     * @return instrumentalized
     **/

    public Boolean isInstrumentalized() {
        return instrumentalized != null ? instrumentalized : false;
    }

    public void setInstrumentalized(Boolean instrumentalized) {
        this.instrumentalized = instrumentalized != null ? instrumentalized
                : false;
    }

    /**
     * Get externalMonitoringDBForLogs
     * 
     * @return externalMonitoringDBForLogs
     **/

    public ExternalMonitoringDBForLogs getExternalMonitoringDBForLogs() {
        return externalMonitoringDBForLogs;
    }

    public void setExternalMonitoringDBForLogs(
            ExternalMonitoringDBForLogs externalMonitoringDBForLogs) {
        this.externalMonitoringDBForLogs = externalMonitoringDBForLogs;
    }

    /**
     * Get externalMonitoringDBForMetrics
     * 
     * @return externalMonitoringDBForMetrics
     **/

    public ExternalMonitoringDBForMetrics getExternalMonitoringDBForMetrics() {
        return externalMonitoringDBForMetrics;
    }

    public void setExternalMonitoringDBForMetrics(
            ExternalMonitoringDBForMetrics externalMonitoringDBForMetrics) {
        this.externalMonitoringDBForMetrics = externalMonitoringDBForMetrics;
    }

    /**
     * Get currentSutExec
     * 
     * @return currentSutExec
     **/

    public Long getCurrentSutExec() {
        return currentSutExec;
    }

    public void setCurrentSutExec(Long currentSutExec) {
        this.currentSutExec = currentSutExec;
    }

    /**
     * Get instrumentedBy
     * 
     * @return instrumentedBy
     **/

    public InstrumentedByEnum getInstrumentedBy() {
        return instrumentedBy;
    }

    public void setInstrumentedBy(InstrumentedByEnum instrumentedBy) {
        this.instrumentedBy = instrumentedBy;
    }

    /**
     * Get protocol
     * 
     * @return protocol
     **/
    public ProtocolEnum getProtocol() {
        if (protocol == null) {
            return ProtocolEnum.HTTP;
        }
        return protocol;
    }

    public void setProtocol(ProtocolEnum protocol) {
        this.protocol = protocol;
    }

    /**
     * Get port
     * 
     * @return port
     **/

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Get managedDockerType
     * 
     * @return managedDockerType
     **/

    public ManagedDockerType getManagedDockerType() {
        return managedDockerType;
    }

    public void setManagedDockerType(ManagedDockerType managedDockerType) {
        this.managedDockerType = managedDockerType;
    }

    /**
     * Get mainService
     * 
     * @return mainService
     **/

    public String getMainService() {
        return mainService;
    }

    public void setMainService(String mainService) {
        this.mainService = mainService;
    }

    /**
     * Get parameters
     * 
     * @return parameters
     **/

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Get commands
     * 
     * @return commands
     **/

    @ApiModelProperty(value = "Commands to execute inside a Docker Container")
    public String getCommands() {
        return commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
    }

    /**
     * Get exProject
     * 
     * @return exProject
     **/
    public ExternalProject getExProject() {
        return exProject;
    }

    public void setExProject(ExternalProject exProject) {
        this.exProject = exProject;
    }

    /**
     * Get exTJobs
     * 
     * @return exTJobs
     **/
    public List<ExternalTJob> getExTJobs() {
        return exTJobs;
    }

    public void setExTJobs(List<ExternalTJob> exTJobs) {
        this.exTJobs = exTJobs;
    }

    /**
     * Get commandsOption
     * 
     * @return commandsOption
     **/
    public CommandsOptionEnum getCommandsOption() {
        return commandsOption;
    }

    public void setCommandsOption(CommandsOptionEnum commandsOption) {
        this.commandsOption = commandsOption;
    }

    /* ******************************************* */
    /* ************** Other methods ************** */
    /* ******************************************* */

    // TODO tmp
    public boolean isSutInNewContainer() {
        return this.commandsOption != CommandsOptionEnum.DEFAULT;
    }

    public String getSutInContainerAuxLabel() {
        return "aux";
    }

    public boolean isDockerComposeSut() {
        return this.getManagedDockerType() == ManagedDockerType.COMPOSE;
    }

    public boolean isDockerImageSut() {
        return this.getManagedDockerType() == ManagedDockerType.IMAGE;
    }

    public boolean isDockerCommandsSut() {
        return this.getManagedDockerType() == ManagedDockerType.COMMANDS;
    }

    public boolean isDeployedOutside() {
        return this.getSutType() == SutTypeEnum.DEPLOYED;
    }

    public boolean isDeployedByElastest() {
        return this.getSutType() == SutTypeEnum.MANAGED;
    }

    public boolean isInstrumentedByElastest() {
        return this.isDeployedOutside()
                && this.getInstrumentedBy() == InstrumentedByEnum.ELASTEST;
    }

    private boolean isUsingExternalMonitoringDB() {
        return this.isDeployedOutside() && this
                .getInstrumentedBy() == InstrumentedByEnum.EXTERNAL_MONITORING_DB;
    }

    public boolean isUsingExternalElasticsearchForLogs() {
        return isUsingExternalMonitoringDB()
                && this.getExternalMonitoringDBForLogs()
                        .isUsingExternalElasticsearchForLogs();
    }

    public boolean isUsingExternalElasticsearchForMetrics() {
        return isUsingExternalMonitoringDB()
                && this.getExternalMonitoringDBForMetrics()
                        .isUsingExternalElasticsearchForMetrics();
    }

    public boolean isUsingExternalPrometheusForMetrics() {
        return isUsingExternalMonitoringDB()
                && this.getExternalMonitoringDBForMetrics()
                        .isUsingExternalPrometheusForMetrics();
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SutSpecification sutSpecification = (SutSpecification) o;
        return Objects.equals(this.id, sutSpecification.id)
                && Objects.equals(this.name, sutSpecification.name)
                && Objects.equals(this.specification,
                        sutSpecification.specification)
                && Objects.equals(this.description,
                        sutSpecification.description)
                && Objects.equals(this.project, sutSpecification.project)
                && Objects.equals(this.sutType, sutSpecification.sutType)
                && Objects.equals(this.eimConfig, sutSpecification.eimConfig)
                && Objects.equals(this.eimMonitoringConfig,
                        sutSpecification.eimMonitoringConfig)
                && Objects.equals(this.instrumentalize,
                        sutSpecification.instrumentalize)
                && Objects.equals(this.instrumentalized,
                        sutSpecification.instrumentalized)
                && Objects.equals(this.currentSutExec,
                        sutSpecification.currentSutExec)
                && Objects.equals(this.instrumentedBy,
                        sutSpecification.instrumentedBy)
                && Objects.equals(this.protocol, sutSpecification.protocol)
                && Objects.equals(this.port, sutSpecification.port)
                && Objects.equals(this.managedDockerType,
                        sutSpecification.managedDockerType)
                && Objects.equals(this.mainService,
                        sutSpecification.mainService)
                && Objects.equals(this.parameters, sutSpecification.parameters)
                && Objects.equals(this.commands, sutSpecification.commands)
                && Objects.equals(this.exProject, sutSpecification.exProject)
                && Objects.equals(this.exTJobs, sutSpecification.exTJobs)
                && Objects.equals(this.commandsOption,
                        sutSpecification.commandsOption)
                && Objects.equals(this.externalMonitoringDBForLogs,
                        sutSpecification.externalMonitoringDBForLogs)
                && Objects.equals(this.externalMonitoringDBForMetrics,
                        sutSpecification.externalMonitoringDBForMetrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, specification, description, project,
                sutType, eimConfig, eimMonitoringConfig, instrumentedBy, port,
                managedDockerType, mainService, parameters, commands, exProject,
                exTJobs, commandsOption, protocol, externalMonitoringDBForLogs,
                externalMonitoringDBForMetrics);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SutSpecification {\n");
        sb = toStringAppender(sb, "id", id);
        sb = toStringAppender(sb, "name", name);
        sb = toStringAppender(sb, "specification", specification);
        sb = toStringAppender(sb, "description", description);
        sb = toStringAppender(sb, "project id",
                project != null ? project.getId() : "null");
        sb = toStringAppender(sb, "sutType", sutType);
        sb = toStringAppender(sb, "eimConfig", eimConfig);
        sb = toStringAppender(sb, "eimMonitoringConfig", eimMonitoringConfig);
        sb = toStringAppender(sb, "instrumentalize", instrumentalize);
        sb = toStringAppender(sb, "instrumentalized", instrumentalized);
        sb = toStringAppender(sb, "currentSutExec", currentSutExec);
        sb = toStringAppender(sb, "instrumentedBy", instrumentedBy);
        sb = toStringAppender(sb, "protocol", protocol);
        sb = toStringAppender(sb, "port", port);
        sb = toStringAppender(sb, "managedDockerType", managedDockerType);
        sb = toStringAppender(sb, "mainService", mainService);
        sb = toStringAppender(sb, "parameters", parameters);
        sb = toStringAppender(sb, "commands", commands);
        sb = toStringAppender(sb, "exProject",
                exProject != null ? exProject.getId() : "null");
        sb = toStringAppender(sb, "exTJobs", exTJobs);
        sb = toStringAppender(sb, "commandsOption", commandsOption);
        sb = toStringAppender(sb, "externalMonitoringDBForLogs",
                externalMonitoringDBForLogs);
        sb = toStringAppender(sb, "externalMonitoringDBForMetrics",
                externalMonitoringDBForMetrics);
        sb.append("}");

        return sb.toString();
    }

    public StringBuilder toStringAppender(StringBuilder sb, String fieldName,
            Object field) {
        try {
            sb.append("    " + fieldName + ": ").append(toIndentedString(field))
                    .append("\n");
        } catch (Exception e) {
        }
        return sb;
    }

    public String getSutMonitoringIndex() {
        return "s" + this.getId() + "_e" + this.getCurrentSutExec();
    }

    public String getDefaultPortByProtocol() {
        if (this.getProtocol().equals(ProtocolEnum.HTTP)) {
            return "80";
        } else if (this.getProtocol().equals(ProtocolEnum.HTTPS)) {
            return "443";
        }
        return "80";
    }

    public String getSutUrlByGivenIp(String sutIp) {
        String port = getPort() != null && !"".equals(getPort()) ? getPort()
                : getDefaultPortByProtocol();
        String url = getProtocol() + "://" + sutIp;

        // If port 80 and protocol http, don't set port
        if ("80".equals(port) && getProtocol().equals(ProtocolEnum.HTTP)) {
            port = null;
        }

        // If port 443 and protocol https, don't set port
        if ("443".equals(port) && getProtocol().equals(ProtocolEnum.HTTPS)) {
            port = null;
        }

        if (port != null && !"".equals(getPort())) {
            url += ":" + port;
        }

        return url;
    }

    public List<String> getAllMonitoringIndices() {
        List<String> indices = new ArrayList<>();
        if (this.isDeployedOutside() && this.getSutExecution() != null) {
            for (SutExecution sutExec : this.getSutExecution()) {
                if (sutExec != null) {
                    indices.add(sutExec.getSutExecMonitoringIndex());
                }
            }
        }
        return indices;
    }
}
