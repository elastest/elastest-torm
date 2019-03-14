package io.elastest.etm.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Project.MediumProjectView;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.TJob.TJobView;
import io.elastest.etm.model.TJobExecution.TJobExecView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalTJob.ExternalTJobView;

/*
 * The EIM configuration for instrumentalize sut (register in EIM)
 */

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class EimConfig {
    public interface EimConfigView {
    }

    @Id
    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "user")
    @JsonProperty("user")
    private String user = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "password")
    @JsonProperty("password")
    private String password = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "privateKey", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("privateKey")
    private String privateKey = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "ip")
    @JsonProperty("ip")
    private String ip = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "agentId")
    @JsonProperty("agentId")
    private String agentId = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashIp")
    @JsonProperty("logstashIp")
    private String logstashIp = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashTcpHost")
    @JsonProperty("logstashTcpHost")
    private String logstashTcpHost = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashTcpPort")
    @JsonProperty("logstashTcpPort")
    private String logstashTcpPort = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashBeatsHost")
    @JsonProperty("logstashBeatsHost")
    private String logstashBeatsHost = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashBeatsPort")
    @JsonProperty("logstashBeatsPort")
    private String logstashBeatsPort = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashBindedTcpHost")
    @JsonProperty("logstashBindedTcpHost")
    private String logstashBindedTcpHost = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashBindedTcpPort")
    @JsonProperty("logstashBindedTcpPort")
    private String logstashBindedTcpPort = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashBindedBeatsHost")
    @JsonProperty("logstashBindedBeatsHost")
    private String logstashBindedBeatsHost = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashBindedBeatsPort")
    @JsonProperty("logstashBindedBeatsPort")
    private String logstashBindedBeatsPort = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashHttpPort")
    @JsonProperty("logstashHttpPort")
    private String logstashHttpPort = null;

    @JsonView({ EimConfigView.class, SutView.class, ExternalProjectView.class,
            MediumProjectView.class, ExternalTJobView.class, TJobView.class,
            TJobExecView.class })
    @Column(name = "logstashHttpApiUrl")
    @JsonProperty("logstashHttpApiUrl")
    private String logstashHttpApiUrl = null;

    @JsonView({ EimConfigView.class })
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "eimConfig")
    @JoinColumn(name = "sutSpecification")
    @JsonIgnoreProperties(value = "eimConfig")
    private SutSpecification sutSpecification;

    public EimConfig() {
    }

    public EimConfig(Long id, String user, String password, String privateKey,
            String ip, String agentId, String logstashIp,
            String logstashTcpHost, String logstashTcpPort,
            String logstashBeatsHost, String logstashBeatsPort,
            String logstashBindedTcpHost, String logstashBindedTcpPort,
            String logstashBindedBeatsHost, String logstashBindedBeatsPort,
            String logstashHttpPort, String logstashHttpApiUrl) {
        this.id = id == null ? 0 : id;
        this.user = user;
        this.password = password;
        this.privateKey = privateKey;
        this.ip = ip;
        this.agentId = agentId;
        this.logstashIp = logstashIp;
        this.logstashTcpHost = logstashTcpHost;
        this.logstashTcpPort = logstashTcpPort;
        this.logstashBeatsHost = logstashBeatsHost;
        this.logstashBeatsPort = logstashBeatsPort;
        this.logstashBindedTcpHost = logstashBindedTcpHost;
        this.logstashBindedTcpPort = logstashBindedTcpPort;
        this.logstashBindedBeatsHost = logstashBindedBeatsHost;
        this.logstashBindedBeatsPort = logstashBindedBeatsPort;
        this.logstashHttpPort = logstashHttpPort;
        this.logstashHttpApiUrl = logstashHttpApiUrl;
    }

    public EimConfig(EimConfig eimConfig) {
        this.setId(null);
        if (eimConfig != null) {
            this.user = eimConfig.getUser();
            this.password = eimConfig.getPassword();
            this.privateKey = eimConfig.getPrivateKey();
            this.ip = eimConfig.getIp();
            this.agentId = eimConfig.getAgentId();
            this.logstashIp = eimConfig.getLogstashIp();
            this.logstashTcpHost = eimConfig.getLogstashTcpHost();
            this.logstashTcpPort = eimConfig.getLogstashTcpPort();
            this.logstashBeatsHost = eimConfig.getLogstashBeatsHost();
            this.logstashBeatsPort = eimConfig.getLogstashBeatsPort();
            this.logstashBindedTcpHost = eimConfig.getLogstashBindedTcpHost();
            this.logstashBindedTcpPort = eimConfig.getLogstashBindedTcpPort();
            this.logstashBindedBeatsHost = eimConfig
                    .getLogstashBindedBeatsHost();
            this.logstashBindedBeatsPort = eimConfig
                    .getLogstashBindedBeatsPort();
            this.logstashHttpPort = eimConfig.getLogstashHttpPort();
            this.logstashHttpApiUrl = eimConfig.getLogstashHttpApiUrl();
        }
    }

    /* *************************** */
    /* *** Getters and setters *** */
    /* *************************** */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id != null ? id : 0;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getLogstashTcpHost() {
        return logstashTcpHost;
    }

    public void setLogstashTcpHost(String logstashTcpHost) {
        this.logstashTcpHost = logstashTcpHost;
    }

    public String getLogstashTcpPort() {
        return logstashTcpPort;
    }

    public void setLogstashTcpPort(String logstashTcpPort) {
        this.logstashTcpPort = logstashTcpPort;
    }

    public String getLogstashBeatsHost() {
        return logstashBeatsHost;
    }

    public void setLogstashBeatsHost(String logstashBeatsHost) {
        this.logstashBeatsHost = logstashBeatsHost;
    }

    public String getLogstashBeatsPort() {
        return logstashBeatsPort;
    }

    public void setLogstashBeatsPort(String logstashBeatsPort) {
        this.logstashBeatsPort = logstashBeatsPort;
    }

    public String getLogstashBindedTcpHost() {
        return logstashBindedTcpHost;
    }

    public void setLogstashBindedTcpHost(String logstashBindedTcpHost) {
        this.logstashBindedTcpHost = logstashBindedTcpHost;
    }

    public String getLogstashBindedTcpPort() {
        return logstashBindedTcpPort;
    }

    public void setLogstashBindedTcpPort(String logstashBindedTcpPort) {
        this.logstashBindedTcpPort = logstashBindedTcpPort;
    }

    public String getLogstashBindedBeatsHost() {
        return logstashBindedBeatsHost;
    }

    public void setLogstashBindedBeatsHost(String logstashBindedBeatsHost) {
        this.logstashBindedBeatsHost = logstashBindedBeatsHost;
    }

    public String getLogstashBindedBeatsPort() {
        return logstashBindedBeatsPort;
    }

    public void setLogstashBindedBeatsPort(String logstashBindedBeatsPort) {
        this.logstashBindedBeatsPort = logstashBindedBeatsPort;
    }

    public String getLogstashHttpPort() {
        return logstashHttpPort;
    }

    public void setLogstashHttpPort(String logstashHttpPort) {
        this.logstashHttpPort = logstashHttpPort;
    }

    public String getLogstashHttpApiUrl() {
        return logstashHttpApiUrl;
    }

    public void setLogstashHttpApiUrl(String logstashHttpApiUrl) {
        this.logstashHttpApiUrl = logstashHttpApiUrl;
    }

    public SutSpecification getSutSpecification() {
        return sutSpecification;
    }

    public void setSutSpecification(SutSpecification sutSpecification) {
        this.sutSpecification = sutSpecification;
    }

    @Override
    public String toString() {
        return "EimConfig [id=" + id + ", user=" + user + ", password="
                + password + ", privateKey=" + privateKey + ", ip=" + ip
                + ", agentId=" + agentId + ", logstashIp=" + logstashIp
                + ", logstashTcpHost=" + logstashTcpHost + ", logstashTcpPort="
                + logstashTcpPort + ", logstashBeatsHost=" + logstashBeatsHost
                + ", logstashBeatsPort=" + logstashBeatsPort
                + ", logstashBindedTcpHost=" + logstashBindedTcpHost
                + ", logstashBindedTcpPort=" + logstashBindedTcpPort
                + ", logstashBindedBeatsHost=" + logstashBindedBeatsHost
                + ", logstashBindedBeatsPort=" + logstashBindedBeatsPort
                + ", logstashHttpPort=" + logstashHttpPort
                + ", logstashHttpApiUrl=" + logstashHttpApiUrl + ", sutId="
                + (sutSpecification != null ? sutSpecification.getId() : "null")
                + "]";
    }

}
