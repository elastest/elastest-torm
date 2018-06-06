package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.EimBeatConfig.EimBeatConfigView;
import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;

@Entity
public class EimMonitoringConfig {

    public interface EimMonitoringConfigView {
    }

    @Id
    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class,
            SutView.class, ExternalProjectView.class, BasicAttProject.class })
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class,
            SutView.class, ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "exec")
    @JsonProperty("exec")
    private String exec = null;

    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class,
            SutView.class, ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "component")
    @JsonProperty("component")
    private String component = null;

    @JsonView({ EimMonitoringConfigView.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @OneToMany(mappedBy = "eimMonitoringConfig", cascade = CascadeType.REMOVE)
    @MapKey(name = "name")
    Map<String, EimBeatConfig> beats;

    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class, })
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "eimMonitoringConfig")
    @JoinColumn(name = "sutSpecification")
    @JsonIgnoreProperties(value = "eimMonitoringConfig", allowSetters = true)
    private SutSpecification sutSpecification;

    @JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class,
            SutView.class, ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "beatsStatus")
    @JsonProperty("beatsStatus")
    private BeatsStatusEnum beatsStatus;

    public EimMonitoringConfig() {
    }

    public EimMonitoringConfig(Long id, String exec, String component,
            Map<String, EimBeatConfig> beats) {
        this.id = id == null ? 0 : id;
        this.exec = exec;
        this.component = component;
        this.beats = beats != null ? beats : new HashMap<>();
        this.beatsStatus = BeatsStatusEnum.DEACTIVATED;
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
        this.id = id;
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

        ApiEimBeatConfig packetbeat = new ApiEimBeatConfig();
        packetbeat.setPaths(this.getBeats().get("packetbeat").getPaths());
        packetbeat.setDockerized(
                this.getBeats().get("packetbeat").getDockerized());
        packetbeat.setStream(this.getBeats().get("packetbeat").getStream());

        ApiEimBeatConfig filebeat = new ApiEimBeatConfig();
        filebeat.setPaths(this.getBeats().get("filebeat").getPaths());
        filebeat.setDockerized(this.getBeats().get("filebeat").getDockerized());
        filebeat.setStream(this.getBeats().get("filebeat").getStream());

        ApiEimBeatConfig metricbeat = new ApiEimBeatConfig();
        metricbeat.setPaths(this.getBeats().get("metricbeat").getPaths());
        metricbeat.setDockerized(
                this.getBeats().get("metricbeat").getDockerized());
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
        sb.append("    sutSpecification: ")
                .append(toIndentedString(sutSpecification)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    public class ApiEimMonitoringConfig {
        String exec;
        String component;
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
