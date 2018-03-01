package io.elastest.etm.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class EimConfig {
    public interface BasicAttEimConfig {
    }

    @Id
    @JsonView({ BasicAttEimConfig.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ BasicAttEimConfig.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "user")
    @JsonProperty("user")
    private String user = null;

    @JsonView({ BasicAttEimConfig.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "privateKey", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("privateKey")
    private String privateKey = null;

    @JsonView({ BasicAttEimConfig.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "ip")
    @JsonProperty("ip")
    private String ip = null;

    @JsonView({ BasicAttEimConfig.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "agentId")
    @JsonProperty("agentId")
    private String agentId = null;

    @JsonView({ BasicAttEimConfig.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "logstashIp")
    @JsonProperty("logstashIp")
    private String logstashIp = null;

    @JsonView({ BasicAttEimConfig.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "logstashBeatsPort")
    @JsonProperty("logstashBeatsPort")
    private String logstashBeatsPort = null;

    @JsonView({ BasicAttEimConfig.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "logstashHttpPort")
    @JsonProperty("logstashHttpPort")
    private String logstashHttpPort = null;

    @JsonView({ BasicAttEimConfig.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "logstashHttpApiUrl")
    @JsonProperty("logstashHttpApiUrl")
    private String logstashHttpApiUrl = null;

    @JsonView({ BasicAttEimConfig.class })
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "eimConfig")
    @JoinColumn(name = "sutSpecification")
    @JsonIgnoreProperties(value = "eimConfig")
    private SutSpecification sutSpecification;

    // Getters and setters

    public EimConfig(Long id, String user, String privateKey, String ip,
            String agentId, String logstashIp, String logstashBeatsPort,
            String logstashHttpPort, String logstashHttpApiUrl) {
        this.id = id == null ? 0 : id;
        this.user = user;
        this.privateKey = privateKey;
        this.ip = ip;
        this.agentId = agentId;
        this.logstashIp = logstashIp;
        this.logstashBeatsPort = logstashBeatsPort;
        this.logstashHttpPort = logstashHttpPort;
        this.logstashHttpApiUrl = logstashHttpApiUrl;
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

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getLogstashIp() {
        return logstashIp;
    }

    public void setLogstashIp(String logstashIp) {
        this.logstashIp = logstashIp;
    }

    public String getLogstashBeatsPort() {
        return logstashBeatsPort;
    }

    public void setLogstashBeatsPort(String logstashBeatsPort) {
        this.logstashBeatsPort = logstashBeatsPort;
    }

    public String getLogstashHttpPort() {
        return logstashHttpPort;
    }

    public void setLogstashHttpPort(String logstashHttpPort) {
        this.logstashHttpPort = logstashHttpPort;
    }

    public SutSpecification getSutSpecification() {
        return sutSpecification;
    }

    public void setSutSpecification(SutSpecification sutSpecification) {
        this.sutSpecification = sutSpecification;
    }

    @Override
    public String toString() {
        return "EimConfig [id=" + id + ", user=" + user + ", privateKey="
                + privateKey + ", ip=" + ip + ", agentId=" + agentId
                + ", logstashIp=" + logstashIp + ", logstashBeatsPort="
                + logstashBeatsPort + ", logstashHttpPort=" + logstashHttpPort
                + ", sutSpecification=" + sutSpecification + "]";
    }

}
