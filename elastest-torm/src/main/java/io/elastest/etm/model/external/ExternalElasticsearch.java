package io.elastest.etm.model.external;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Enums.ProtocolEnum;
import io.elastest.etm.model.MultiConfig;
import io.elastest.etm.model.Project.ProjectView;
import io.elastest.etm.model.SutSpecification;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ExternalElasticsearch {

    public interface ExternalElasticsearchView {
    }

    @Id
    @JsonView({ ExternalElasticsearchView.class, SutView.class,
            ExternalProjectView.class, ProjectView.class })
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ ExternalElasticsearchView.class, SutView.class,
            ExternalProjectView.class, ProjectView.class })
    @Column(name = "ip")
    @JsonProperty("ip")
    private String ip;

    @JsonView({ ExternalElasticsearchView.class, SutView.class,
            ExternalProjectView.class, ProjectView.class })
    @Column(name = "port")
    @JsonProperty("port")
    private String port;

    @JsonView({ ExternalElasticsearchView.class, SutView.class,
            ExternalProjectView.class, ProjectView.class })
    @Column(name = "path")
    @JsonProperty("path")
    private String path;

    @JsonView({ ExternalElasticsearchView.class, SutView.class,
            ExternalProjectView.class, ProjectView.class })
    @Column(name = "protocol")
    @JsonProperty("protocol")
    private ProtocolEnum protocol = ProtocolEnum.HTTP;

    @JsonView({ ExternalElasticsearchView.class, SutView.class,
            ExternalProjectView.class, ProjectView.class })
    @Column(name = "user")
    @JsonProperty("user")
    private String user;

    @JsonView({ ExternalElasticsearchView.class, SutView.class,
            ExternalProjectView.class, ProjectView.class })
    @Column(name = "pass")
    @JsonProperty("pass")
    private String pass;

    @JsonView({ ExternalElasticsearchView.class, SutView.class,
            ExternalProjectView.class, ProjectView.class })
    @Column(name = "indices")
    @JsonProperty("indices")
    private String indices;

    @JsonView({ ExternalElasticsearchView.class, SutView.class,
            ExternalProjectView.class, ProjectView.class })
    @Column(name = "streamFields")
    @JsonProperty("streamFields")
    private String streamFields;

    @JsonView({ ExternalElasticsearchView.class, SutView.class,
            ExternalProjectView.class, ProjectView.class })
    @ElementCollection
    @CollectionTable(name = "ExternalElasticsearchFieldFilters", joinColumns = @JoinColumn(name = "ExternalElasticsearch"))
    @MapKeyColumn(name = "NAME")
    @Column(name = "VALUES", length = 16777215)
    private List<MultiConfig> fieldFilters = new ArrayList<MultiConfig>();

    @JsonView({ ExternalElasticsearchView.class })
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "externalElasticsearch")
    @JoinColumn(name = "sutSpecification")
    @JsonIgnoreProperties(value = "externalElasticsearch")
    private SutSpecification sutSpecification;

    /* ************************** */
    /* ****** Constructors ****** */
    /* ************************** */

    public ExternalElasticsearch() {
    }

    public ExternalElasticsearch(Long id, String ip, String port, String path,
            String user, String pass, String indices,
            SutSpecification sutSpecification, ProtocolEnum protocol,
            String streamFields) {
        super();
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.path = path;
        this.protocol = protocol;
        this.user = user;
        this.pass = pass;
        this.indices = indices;
        this.streamFields = streamFields;
        this.fieldFilters = new ArrayList<>();
        this.sutSpecification = sutSpecification;
    }

    public ExternalElasticsearch(ExternalElasticsearch externalElasticsearch) {
        this.setId(null);
        this.ip = externalElasticsearch.getIp();
        this.port = externalElasticsearch.getPort();
        this.path = externalElasticsearch.getPath();
        this.user = externalElasticsearch.getUser();
        this.pass = externalElasticsearch.getPass();
        this.indices = externalElasticsearch.getIndices();
        this.protocol = externalElasticsearch.getProtocol();
        this.streamFields = externalElasticsearch.getStreamFields();
        this.fieldFilters = externalElasticsearch.getFieldFilters();
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

    public ProtocolEnum getProtocol() {
        return protocol;
    }

    public void setProtocol(ProtocolEnum protocol) {
        this.protocol = protocol;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public String getStreamFields() {
        return streamFields;
    }

    public List<String> getStreamFieldsAsList() {
        List<String> streamFieldsList = new ArrayList<>();

        if (streamFields != null && !streamFields.isEmpty()) {
            streamFieldsList = Arrays.asList(streamFields.split(","));
        }

        return streamFieldsList;
    }

    public void setStreamFields(String streamFields) {
        this.streamFields = streamFields;
    }

    public List<MultiConfig> getFieldFilters() {
        return fieldFilters;
    }

    public void setFieldFilters(List<MultiConfig> fieldFilters) {
        this.fieldFilters = fieldFilters;
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
                + port + ", path=" + path + ", protocol=" + protocol + ", user="
                + user + ", pass=" + pass + ", indices=" + indices
                + ", streamFields=" + streamFields + ", sutSpecification="
                + (sutSpecification != null ? sutSpecification.getId() : "null")
                + "]";
    }

}
