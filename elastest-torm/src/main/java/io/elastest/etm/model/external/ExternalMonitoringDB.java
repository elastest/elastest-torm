package io.elastest.etm.model.external;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Enums.ProtocolEnum;
import io.elastest.etm.model.Project.ProjectMediumView;
import io.elastest.etm.model.SutSpecification.SutView;
import io.elastest.etm.model.external.ExternalElasticsearch.ExternalElasticsearchView;
import io.elastest.etm.model.external.ExternalMonitoringDBForLogs.ExternalMonitoringDBForLogsView;
import io.elastest.etm.model.external.ExternalMonitoringDBForMetrics.ExternalMonitoringDBForMetricsView;
import io.elastest.etm.model.external.ExternalProject.ExternalProjectView;
import io.elastest.etm.model.external.ExternalPrometheus.ExternalPrometheusView;

@JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "monitoringType", visible = true)
@JsonSubTypes({
        @Type(value = ExternalElasticsearch.class, name = "ELASTICSEARCH"),
        @Type(value = ExternalPrometheus.class, name = "PROMETHEUS"),
        @Type(value = ExternalMonitoringDB.class, name = "NONE") })

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ExternalMonitoringDB {

    public interface ExternalMonitoringDBView {
    }

    @Id
    @JsonView({ ExternalMonitoringDBView.class,
            ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalElasticsearchView.class, ExternalPrometheusView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @GeneratedValue(strategy = GenerationType.TABLE)
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ ExternalMonitoringDBView.class,
            ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalElasticsearchView.class, ExternalPrometheusView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "ip")
    @JsonProperty("ip")
    private String ip;

    @JsonView({ ExternalMonitoringDBView.class,
            ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalElasticsearchView.class, ExternalPrometheusView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "port")
    @JsonProperty("port")
    private String port;

    @JsonView({ ExternalMonitoringDBView.class,
            ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalElasticsearchView.class, ExternalPrometheusView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "path")
    @JsonProperty("path")
    private String path;

    @JsonView({ ExternalMonitoringDBView.class,
            ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalElasticsearchView.class, ExternalPrometheusView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "protocol")
    @JsonProperty("protocol")
    private ProtocolEnum protocol = ProtocolEnum.HTTP;

    @JsonView({ ExternalMonitoringDBView.class,
            ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalElasticsearchView.class, ExternalPrometheusView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "user")
    @JsonProperty("user")
    private String user;

    @JsonView({ ExternalMonitoringDBView.class,
            ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalElasticsearchView.class, ExternalPrometheusView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "pass")
    @JsonProperty("pass")
    private String pass;

    @JsonView({ ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            ExternalPrometheusView.class, SutView.class,
            ExternalProjectView.class, ProjectMediumView.class })
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "externalMonitoringDB")
    @JoinColumn(name = "externalMonitoringDBForLogs")
    @JsonIgnoreProperties(value = "externalMonitoringDB")
    private ExternalMonitoringDBForLogs externalMonitoringDBForLogs;

    @JsonView({ ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            ExternalPrometheusView.class, SutView.class,
            ExternalProjectView.class, ProjectMediumView.class })
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "externalMonitoringDB")
    @JoinColumn(name = "externalMonitoringDBForMetrics")
    @JsonIgnoreProperties(value = "externalMonitoringDB")
    private ExternalMonitoringDBForMetrics externalMonitoringDBForMetrics;

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "streamFields")
    @JsonProperty("streamFields")
    private String streamFields;

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "contentFieldName")
    @JsonProperty("contentFieldName")
    private String contentFieldName;

    @JsonView({ ExternalMonitoringDBForLogsView.class,
            ExternalMonitoringDBForMetricsView.class,
            ExternalMonitoringDBView.class, ExternalElasticsearchView.class,
            SutView.class, ExternalProjectView.class, ProjectMediumView.class })
    @Column(name = "traceNameField")
    @JsonProperty("traceNameField")
    private String traceNameField;

    /* ************************** */
    /* ****** Constructors ****** */
    /* ************************** */

    public ExternalMonitoringDB() {
    }

    public ExternalMonitoringDB(Long id, String ip, String port, String path,
            ProtocolEnum protocol, String user, String pass,
            String streamFields, String contentFieldName,
            String traceNameField) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.path = path;
        this.protocol = protocol;
        this.user = user;
        this.pass = pass;
        this.streamFields = streamFields;
        this.contentFieldName = contentFieldName;
        this.traceNameField = traceNameField;
    }

    public ExternalMonitoringDB(ExternalMonitoringDB externalMonitoringDB) {
        this.setId(null);
        if (externalMonitoringDB != null) {
            this.ip = externalMonitoringDB.getIp();
            this.port = externalMonitoringDB.getPort();
            this.path = externalMonitoringDB.getPath();
            this.user = externalMonitoringDB.getUser();
            this.pass = externalMonitoringDB.getPass();
            this.protocol = externalMonitoringDB.getProtocol();
            this.streamFields = externalMonitoringDB.getStreamFields();
            this.contentFieldName = externalMonitoringDB.getContentFieldName();
            this.traceNameField = externalMonitoringDB.getTraceNameField();
        }
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

    public ExternalMonitoringDBForLogs getExternalMonitoringDBForLogs() {
        return externalMonitoringDBForLogs;
    }

    public void setExternalMonitoringDBForLogs(
            ExternalMonitoringDBForLogs externalMonitoringDBForLogs) {
        this.externalMonitoringDBForLogs = externalMonitoringDBForLogs;
    }

    public ExternalMonitoringDBForMetrics getExternalMonitoringDBForMetrics() {
        return externalMonitoringDBForMetrics;
    }

    public void setExternalMonitoringDBForMetrics(
            ExternalMonitoringDBForMetrics externalMonitoringDBForMetrics) {
        this.externalMonitoringDBForMetrics = externalMonitoringDBForMetrics;
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

    public String getContentFieldName() {
        return contentFieldName;
    }

    public void setContentFieldName(String contentFieldName) {
        this.contentFieldName = contentFieldName;
    }

    public String getTraceNameField() {
        return traceNameField;
    }

    public void setTraceNameField(String traceNameField) {
        this.traceNameField = traceNameField;
    }

    /* *************************** */
    /* ****** Other methods ****** */
    /* *************************** */

    @Override
    public String toString() {
        return "ExternalMonitoringDB [id=" + id + ", ip=" + ip + ", port="
                + port + ", path=" + path + ", protocol=" + protocol + ", user="
                + user + ", pass=" + pass + ", streamFields=" + streamFields
                + ", contentFieldName=" + contentFieldName + "]";
    }

}
