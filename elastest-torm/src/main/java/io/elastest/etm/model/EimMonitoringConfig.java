package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.EimBeatConfig.EimBeatConfigView;
import io.elastest.etm.model.Project.ProjectMediumView;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.TJob.TJobCompleteView;
import io.elastest.etm.model.TJobExecution.TJobExecCompleteView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalTJob.ExternalTJobView;

/*
 * The Eim monitoring config to deploy the beats into the Sut 
 */

@Entity
public class EimMonitoringConfig {

    public interface EimMonitoringConfigView {
    }

    @Id
    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class,
            ExternalTJobView.class, TJobCompleteView.class,
            TJobExecCompleteView.class })
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class,
            ExternalTJobView.class, TJobCompleteView.class,
            TJobExecCompleteView.class })
    @Column(name = "exec")
    @JsonProperty("exec")
    private String exec = null;

    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class,
            ExternalTJobView.class, TJobCompleteView.class,
            TJobExecCompleteView.class })
    @Column(name = "component")
    @JsonProperty("component")
    private String component = null;

    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class,
            ExternalTJobView.class, TJobCompleteView.class,
            TJobExecCompleteView.class })
    @Column(name = "dockerized")
    @JsonProperty("dockerized")
    private Boolean dockerized;

    @JsonView({ EimMonitoringConfigView.class, SutView.class,
            ExternalProjectView.class, ProjectMediumView.class,
            ExternalTJobView.class, TJobCompleteView.class,
            TJobExecCompleteView.class })
    @OneToMany(mappedBy = "eimMonitoringConfig", cascade = CascadeType.REMOVE)
    @MapKey(name = "name")
    @JsonIgnoreProperties(value = { "eimMonitoringConfig" })
    Map<String, EimBeatConfig> beats;

    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class, })
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "eimMonitoringConfig")
    @JoinColumn(name = "sutSpecification")
    @JsonIgnoreProperties(value = "eimMonitoringConfig", allowSetters = true)
    private SutSpecification sutSpecification;

    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class,
            ExternalTJobView.class, TJobCompleteView.class,
            TJobExecCompleteView.class })
    @Column(name = "beatsStatus")
    @JsonProperty("beatsStatus")
    private BeatsStatusEnum beatsStatus;

    public EimMonitoringConfig() {
    }

    public EimMonitoringConfig(Long id, String exec, String component,
            Boolean dockerized, Map<String, EimBeatConfig> beats) {
        this.id = id == null ? 0 : id;
        this.exec = exec;
        this.component = component;
        this.dockerized = dockerized == null ? false : dockerized;
        this.beats = beats != null ? beats : new HashMap<>();
        this.beatsStatus = BeatsStatusEnum.DEACTIVATED;
    }

    public EimMonitoringConfig(EimMonitoringConfig eimMonitoringConfig) {
        this.setId(null);
        this.exec = null;
        this.beatsStatus = BeatsStatusEnum.DEACTIVATED;

        if (eimMonitoringConfig != null) {
            this.component = eimMonitoringConfig.getComponent();
            this.dockerized = eimMonitoringConfig.isDockerized();

            if (eimMonitoringConfig.beats != null) {
                this.beats = new HashMap<>();
                for (Entry<String, EimBeatConfig> beatConfig : eimMonitoringConfig.beats
                        .entrySet()) {
                    EimBeatConfig newBeatConfig = new EimBeatConfig(
                            beatConfig.getValue());
                    beats.put(beatConfig.getKey(), newBeatConfig);
                }
            }
        }

        this.beats = beats != null ? beats : new HashMap<>();
    }

    public enum BeatsStatusEnum {
        ACTIVATED("ACTIVATED"),

        ACTIVATING("ACTIVATING"),

        DEACTIVATED("DEACTIVATED"),

        DEACTIVATING("DEACTIVATING");

        private String value;

        BeatsStatusEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static BeatsStatusEnum fromValue(String text) {
            for (BeatsStatusEnum b : BeatsStatusEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    /* *** Getters/Setters *** */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id != null ? id : 0;
    }

    public String getExec() {
        return exec;
    }

    public void setExec(String exec) {
        this.exec = exec;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Boolean isDockerized() {
        return dockerized == null ? false : dockerized;
    }

    public void setDockerized(Boolean dockerized) {
        this.dockerized = dockerized == null ? false : dockerized;
    }

    public Map<String, EimBeatConfig> getBeats() {
        return beats;
    }

    public void setBeats(Map<String, EimBeatConfig> beats) {
        this.beats = beats;
    }

    public SutSpecification getSutSpecification() {
        return sutSpecification;
    }

    public void setSutSpecification(SutSpecification sutSpecification) {
        this.sutSpecification = sutSpecification;
    }

    // Others

    public BeatsStatusEnum getBeatsStatus() {
        return beatsStatus;
    }

    public void setBeatsStatus(BeatsStatusEnum beatsStatus) {
        this.beatsStatus = beatsStatus;
    }

    public ApiEimMonitoringConfig getEimMonitoringConfigInApiFormat() {
        ApiEimMonitoringConfig apiEimMonitoringConfig = new ApiEimMonitoringConfig();
        apiEimMonitoringConfig.setExec(this.getExec());
        apiEimMonitoringConfig.setComponent(this.getComponent());
        apiEimMonitoringConfig
                .setDockerized(this.isDockerized() ? "yes" : "no");

        ApiEimBeatConfig packetbeat = new ApiEimBeatConfig();
        packetbeat.setPaths(this.getBeats().get("packetbeat").getPaths());

        if (this.isDockerized()) {
            packetbeat.setDockerized(
                    this.getBeats().get("packetbeat").getDockerized());
        }
        packetbeat.setStream(this.getBeats().get("packetbeat").getStream());

        ApiEimBeatConfig filebeat = new ApiEimBeatConfig();
        filebeat.setPaths(this.getBeats().get("filebeat").getPaths());
        if (this.isDockerized()) {
            filebeat.setDockerized(
                    this.getBeats().get("filebeat").getDockerized());
        }
        filebeat.setStream(this.getBeats().get("filebeat").getStream());

        ApiEimBeatConfig metricbeat = new ApiEimBeatConfig();
        metricbeat.setPaths(this.getBeats().get("metricbeat").getPaths());
        if (this.isDockerized()) {
            metricbeat.setDockerized(
                    this.getBeats().get("metricbeat").getDockerized());
        }
        metricbeat.setStream(this.getBeats().get("metricbeat").getStream());

        apiEimMonitoringConfig.setPacketbeat(packetbeat);
        apiEimMonitoringConfig.setFilebeat(filebeat);
        apiEimMonitoringConfig.setMetricbeat(metricbeat);

        return apiEimMonitoringConfig;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EimMonitoringConfig {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    exec: ").append(toIndentedString(exec)).append("\n");
        sb.append("    component: ").append(toIndentedString(component))
                .append("\n");
        sb.append("    beats: ").append(toIndentedString(beats)).append("\n");
        sb.append("    sutSpecification: ").append(toIndentedString(
                sutSpecification != null ? sutSpecification.getId() : "null"))
                .append("\n");
        sb.append("}");
        return sb.toString();
    }

    public class ApiEimMonitoringConfig {
        String exec;
        String component;
        String dockerized;
        ApiEimBeatConfig packetbeat;
        ApiEimBeatConfig filebeat;
        ApiEimBeatConfig metricbeat;

        public ApiEimMonitoringConfig() {
        }

        public String getExec() {
            return exec;
        }

        public void setExec(String exec) {
            this.exec = exec;
        }

        public String getComponent() {
            return component;
        }

        public void setComponent(String component) {
            this.component = component;
        }

        public String getDockerized() {
            return dockerized;
        }

        public void setDockerized(String dockerized) {
            this.dockerized = dockerized;
        }

        public ApiEimBeatConfig getPacketbeat() {
            return packetbeat;
        }

        public void setPacketbeat(ApiEimBeatConfig packetbeat) {
            this.packetbeat = packetbeat;
        }

        public ApiEimBeatConfig getFilebeat() {
            return filebeat;
        }

        public void setFilebeat(ApiEimBeatConfig filebeat) {
            this.filebeat = filebeat;
        }

        public ApiEimBeatConfig getMetricbeat() {
            return metricbeat;
        }

        public void setMetricbeat(ApiEimBeatConfig metricbeat) {
            this.metricbeat = metricbeat;
        }

        @Override
        public String toString() {
            return "ApiEimMonitoringConfig [exec=" + exec + ", component="
                    + component + ", packetbeat=" + packetbeat + ", filebeat="
                    + filebeat + ", metricbeat=" + metricbeat + "]";
        }

    }

    public class ApiEimBeatConfig {
        String stream;
        List<String> paths;

        @JsonInclude(Include.NON_EMPTY)
        List<String> dockerized;

        public ApiEimBeatConfig() {
        }

        public String getStream() {
            return stream;
        }

        public void setStream(String stream) {
            this.stream = stream;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }

        public List<String> getDockerized() {
            return dockerized;
        }

        public void setDockerized(List<String> dockerized) {
            this.dockerized = dockerized;
        }

        @Override
        public String toString() {
            return "ApiEimBeatConfig [stream=" + stream + ", paths=" + paths
                    + ", dockerized=" + dockerized + "]";
        }

    }
}
