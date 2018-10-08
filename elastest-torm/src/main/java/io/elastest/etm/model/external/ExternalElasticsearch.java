package io.elastest.etm.model.external;

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

import io.elastest.etm.model.Project.BasicAttProject;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ExternalElasticsearch {

    public interface BasicAttExternalElasticsearch {
    }

    @Id
    @JsonView({ BasicAttExternalElasticsearch.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ BasicAttExternalElasticsearch.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "ip")
    @JsonProperty("ip")
    private String ip;

    @JsonView({ BasicAttExternalElasticsearch.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "port")
    @JsonProperty("port")
    private String port;

    @JsonView({ BasicAttExternalElasticsearch.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "user")
    @JsonProperty("user")
    private String user;

    @JsonView({ BasicAttExternalElasticsearch.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "pass")
    @JsonProperty("pass")
    private String pass;

    @JsonView({ BasicAttExternalElasticsearch.class, SutView.class,
            ExternalProjectView.class, BasicAttProject.class })
    @Column(name = "indices")
    @JsonProperty("indices")
    private String indices;

    @JsonView({ BasicAttExternalElasticsearch.class })
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "externalElasticsearch")
    @JoinColumn(name = "sutSpecification")
    @JsonIgnoreProperties(value = "externalElasticsearch")
    private SutSpecification sutSpecification;

    /* ************************** */
    /* ****** Constructors ****** */
    /* ************************** */

    public ExternalElasticsearch() {
    }

    public ExternalElasticsearch(Long id, String ip, String port, String user,
            String pass, String indices, SutSpecification sutSpecification) {
        this.id = id == null ? 0 : id;
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.indices = indices;
        this.sutSpecification = sutSpecification;
    }

    /* *************************** */
    /* *** Getters and setters *** */
    /* *************************** */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id == null ? 0 : id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getIndices() {
        return indices;
    }

    public void setIndices(String indices) {
        this.indices = indices;
    }

    public SutSpecification getSutSpecification() {
        return sutSpecification;
    }

    public void setSutSpecification(SutSpecification sutSpecification) {
        this.sutSpecification = sutSpecification;
    }

    @Override
    public String toString() {
        return "ExternalElasticsearch [id=" + id + ", ip=" + ip + ", port="
                + port + ", user=" + user + ", pass=" + pass + ", indices="
                + indices + ", sutSpecification=" + sutSpecification + "]";
    }

}
