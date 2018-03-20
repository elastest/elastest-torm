package io.elastest.etm.model;

import static io.elastest.etm.utils.ToStringUtils.toIndentedString;

import java.util.HashMap;
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
	@JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class, SutView.class, ExternalProjectView.class,
			BasicAttProject.class })
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	@JsonProperty("id")
	private Long id = null;

	@JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class, SutView.class, ExternalProjectView.class,
			BasicAttProject.class })
	@Column(name = "exec")
	@JsonProperty("exec")
	private String exec = null;

	@JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class, SutView.class, ExternalProjectView.class,
			BasicAttProject.class })
	@Column(name = "component")
	@JsonProperty("component")
	private String component = null;

	@JsonView({ EimMonitoringConfigView.class, SutView.class, ExternalProjectView.class, BasicAttProject.class })
	@OneToMany(mappedBy = "eimMonitoringConfig", cascade = CascadeType.REMOVE)
	@MapKey(name = "name")
	Map<String, EimBeatConfig> beats;

	@JsonView({ EimMonitoringConfigView.class, EimBeatConfigView.class, })
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "eimMonitoringConfig")
	@JoinColumn(name = "sutSpecification")
	@JsonIgnoreProperties(value = "eimMonitoringConfig", allowSetters = true)
	private SutSpecification sutSpecification;

	public EimMonitoringConfig() {
	}

	public EimMonitoringConfig(Long id, String exec, String component, Map<String, EimBeatConfig> beats) {
		this.id = id == null ? 0 : id;
		this.exec = exec;
		this.component = component;
		this.beats = beats != null ? beats : new HashMap<>();
	}

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

	public Map<String, String> getEimMonitoringConfigInApiFormat() {
		Map<String, String> body = new HashMap<>();
		body.put("exec", this.getExec());
		body.put("component", this.getComponent());
		body.put("packetbeat", this.getBeats().get("packetbeat").getEimBeatInApiFormat().toString());
		body.put("filebeat", this.getBeats().get("filebeat").getEimBeatInApiFormat().toString());
		body.put("topbeat", this.getBeats().get("topbeat").getEimBeatInApiFormat().toString());

		return body;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class EimMonitoringConfig {\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    exec: ").append(toIndentedString(exec)).append("\n");
		sb.append("    component: ").append(toIndentedString(component)).append("\n");
		sb.append("    beats: ").append(toIndentedString(beats)).append("\n");
		sb.append("    sutSpecification: ").append(toIndentedString(sutSpecification)).append("\n");
		sb.append("}");
		return sb.toString();
	}

}
