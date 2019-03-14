package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.EimMonitoringConfig.EimMonitoringConfigView;
import io.elastest.etm.model.Project.ProjectView;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.TJob.TJobView;
import io.elastest.etm.model.TJobExecution.TJobExecView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalTJob.ExternalTJobView;

@Entity
public class EimBeatConfig {
    public interface EimBeatConfigView {
    }

    @Id
    @JsonView({ EimBeatConfigView.class, EimMonitoringConfigView.class,
            SutView.class, ExternalProjectView.class, ProjectView.class,
            ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ EimBeatConfigView.class, EimMonitoringConfigView.class,
            SutView.class, ExternalProjectView.class, ProjectView.class,
            ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "name")
    @JsonProperty("name")
    private String name;

    @JsonView({ EimBeatConfigView.class, EimMonitoringConfigView.class,
            SutView.class, ExternalProjectView.class, ProjectView.class,
            ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "stream")
    @JsonProperty("stream")
    private String stream;

    @JsonView({ EimBeatConfigView.class, EimMonitoringConfigView.class,
            SutView.class, ExternalProjectView.class, ProjectView.class,
            ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @ElementCollection
    @CollectionTable(name = "EimBeatPath", joinColumns = @JoinColumn(name = "EimBeatConfig"))
    List<String> paths;

    @JsonView({ EimBeatConfigView.class, EimMonitoringConfigView.class,
            SutView.class, ExternalProjectView.class, ProjectView.class,
            ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @ElementCollection
    @CollectionTable(name = "EimBeatDockerized", joinColumns = @JoinColumn(name = "EimBeatConfig"))
    List<String> dockerized = null;

    @JsonView({ EimBeatConfigView.class })
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "eimMonitoringConfig")
    @JsonIgnoreProperties(value = { "beats" })
    private EimMonitoringConfig eimMonitoringConfig;

    public EimBeatConfig() {
    }

    public EimBeatConfig(Long id) {
        this.id = id == null ? 0 : id;
    }

    public EimBeatConfig(Long id, String stream) {
        this.id = id == null ? 0 : id;
        this.stream = stream;
    }

    public EimBeatConfig(EimBeatConfig beatConfig) {
        this.id = 0l;
        if (beatConfig != null) {
            this.stream = beatConfig.getStream();
            this.name = beatConfig.getName();
            if (beatConfig.getPaths() != null) {
                this.paths = new ArrayList<>(beatConfig.getPaths());
            }
            if (beatConfig.getDockerized() != null) {
                this.dockerized = new ArrayList<>(beatConfig.getDockerized());
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id != null ? id : 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public EimMonitoringConfig getEimMonitoringConfig() {
        return eimMonitoringConfig;
    }

    public void setEimMonitoringConfig(
            EimMonitoringConfig eimMonitoringConfig) {
        this.eimMonitoringConfig = eimMonitoringConfig;
    }

    public Map<String, String> getEimBeatInApiFormat() {
        Map<String, String> body = new HashMap<>();
        body.put("stream", this.getStream());

        if (this.name.equals("filebeat")) {
            body.put("paths", this.getPaths().toString());
        }

        if (this.getDockerized() != null && this.getDockerized().size() > 0) {
            if (this.name.equals("filebeat")
                    || this.name.equals("metricbeat")) {
                body.put("dockerized", this.getDockerized().toString());
            }
        }

        return body;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EimBeatConfig {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    stream: ").append(toIndentedString(stream)).append("\n");
        sb.append("    paths: ").append(toIndentedString(paths)).append("\n");
        sb.append("    dockerized: ").append(toIndentedString(dockerized))
                .append("\n");
        sb.append("    eimMonitoringConfig: ")
                .append(toIndentedString(eimMonitoringConfig != null
                        ? eimMonitoringConfig.getId()
                        : "null"))
                .append("\n");
        sb.append("}");
        return sb.toString();
    }
}