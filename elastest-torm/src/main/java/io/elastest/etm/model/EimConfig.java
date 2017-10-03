package io.elastest.etm.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.SutSpecification.SutView;

@Entity
public class EimConfig {
	public interface BasicAttEimConfig {
	}

	@Id
	@JsonView({ BasicAttEimConfig.class, SutView.class, BasicAttProject.class })
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	@JsonProperty("id")
	private Long id = null;

	@JsonView({ BasicAttEimConfig.class, SutView.class, BasicAttProject.class })
	@Column(name = "instrumentalized")
	@JsonProperty("instrumentalized")
	private boolean instrumentalized = false;

	@JsonView({ BasicAttEimConfig.class, SutView.class, BasicAttProject.class })
	@Column(name = "user")
	@JsonProperty("user")
	private String user = null;

	@JsonView({ BasicAttEimConfig.class, SutView.class, BasicAttProject.class })
	@Column(name = "privateKey", columnDefinition = "TEXT", length = 65535)
	@JsonProperty("privateKey")
	private String privateKey = null;

	@JsonView({ BasicAttEimConfig.class, SutView.class, BasicAttProject.class })
	@Column(name = "ip")
	@JsonProperty("ip")
	private String ip = null;

	@JsonView({ BasicAttEimConfig.class, SutView.class, BasicAttProject.class })
	@Column(name = "agentId")
	@JsonProperty("agentId")
	private Long agentId = null;

	@JsonView({ BasicAttEimConfig.class })
	@OneToOne(fetch = FetchType.LAZY, mappedBy = "eimConfig")
	@JoinColumn(name = "sutSpecification")
	private SutSpecification sutSpecification;

	// Getters and setters

	public EimConfig(Long id, boolean instrumentalized, String user, String privateKey, String ip, Long agentId) {
		this.id = id == null ? 0 : id;
		this.instrumentalized = instrumentalized;
		this.user = user;
		this.privateKey = privateKey;
		this.ip = ip;
		this.agentId = agentId;
	}


	public EimConfig() {
	}

	/**
	 * Get id
	 * 
	 * @return id
	 **/
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id == null ? 0 : id;
	}

	public boolean isInstrumentalized() {
		return instrumentalized;
	}

	public void setInstrumentalized(boolean instrumentalized) {
		this.instrumentalized = instrumentalized;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Long getAgentId() {
		return agentId;
	}

	public void setAgentId(Long agentId) {
		this.agentId = agentId;
	}

	public SutSpecification getSutSpecification() {
		return sutSpecification;
	}

	public void setSutSpecification(SutSpecification sutSpecification) {
		this.sutSpecification = sutSpecification;
	}

	@Override
	public String toString() {
		return "instrumentalized=" + instrumentalized + ", user=" + user + ", privateKey=" + privateKey + ", ip=" + ip
				+ ", agentId=" + agentId;
	}

}
