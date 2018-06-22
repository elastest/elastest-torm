package io.elastest.etm.model;

import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import io.elastest.etm.model.Enums.LevelEnum;
import io.elastest.etm.model.Enums.StreamType;

@Entity
public class Trace {
    public interface TraceView {
    }

    /* *** Common *** */

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    @JsonProperty("id")
    private Long id = null;

    @JsonView({ TraceView.class })
    @Column(name = "exec")
    @JsonProperty("exec")
    String exec;

    @JsonView({ TraceView.class })
    @Column(name = "component")
    @JsonProperty("component")
    String component;

    @JsonView({ TraceView.class })
    @Column(name = "componentService")
    @JsonProperty("componentService")
    String componentService;

    @JsonView({ TraceView.class })
    @Column(name = "etType")
    @JsonProperty("et_type")
    String etType;

    @JsonView({ TraceView.class })
    @Column(name = "timestamp")
    @JsonProperty("timestamp")
    String timestamp;

    @JsonView({ TraceView.class })
    @Column(name = "stream")
    @JsonProperty("stream")
    String stream;

    @JsonView({ TraceView.class })
    @Column(name = "containerName")
    @JsonProperty("containerName")
    String containerName;

    @JsonView({ TraceView.class })
    @Column(name = "streamType", nullable = false)
    @JsonProperty("stream_type")
    StreamType streamType;

    /* *** For Log *** */

    @JsonView({ TraceView.class })
    @Column(name = "message")
    @JsonProperty("message")
    private String message;

    @Column(name = "level")
    @JsonProperty("level")
    @JsonView({ TraceView.class })
    private LevelEnum level;

    /* *** For Metrics *** */

    @JsonView({ TraceView.class })
    @Column(name = "metricName")
    @JsonProperty("metricName")
    private String metricName;

    @JsonView({ TraceView.class })
    @Column(name = "content", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("content")
    private String content;

    @JsonView({ TraceView.class })
    @Column(name = "unit")
    @JsonProperty("unit")
    String unit;

    @JsonView({ TraceView.class })
    @Column(name = "units", columnDefinition = "TEXT", length = 65535)
    @JsonProperty("units")
    String units;

    /* ******************** */
    /* *** Constructors *** */
    /* ******************** */

    public Trace() {
    }

    public Trace(Long id, String exec, String component, String etType,
            String timestamp, String stream, String containerName,
            StreamType streamType, String message, LevelEnum level,
            String metricName, String content, String unit, String units) {
        super();
        this.id = id == null ? 0 : id;
        this.exec = exec;
        this.component = component;
        this.etType = etType;
        this.timestamp = timestamp;
        this.stream = stream;
        this.containerName = containerName;
        this.streamType = streamType;
        this.message = message;
        this.level = level;
        this.metricName = metricName;
        this.content = content;
        this.unit = unit;
        this.units = units;
    }

    /* *********************** */
    /* *** Getters/Setters *** */
    /* *********************** */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id == null ? 0 : id;
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

    public String getComponentService() {
        return componentService;
    }

    public void setComponentService(String componentService) {
        this.componentService = componentService;
    }

    public String getEtType() {
        return etType;
    }

    public void setEtType(String etType) {
        this.etType = etType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public StreamType getStreamType() {
        return streamType;
    }

    public void setStreamType(StreamType streamType) {
        this.streamType = streamType;
    }

    /* ************** */
    /* *** Others *** */
    /* ************** */

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LevelEnum getLevel() {
        return level;
    }

    public void setLevel(LevelEnum level) {
        this.level = level;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContentFromLinkedHashMap(LinkedHashMap<Object, Object> map) {
        String jsonString = new JSONObject(map).toString();
        this.setContent(jsonString);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    @Override
    public String toString() {
        return "Trace [id=" + id + ", exec=" + exec + ", component=" + component
                + ", componentService=" + componentService + ", etType="
                + etType + ", timestamp=" + timestamp + ", stream=" + stream
                + ", containerName=" + containerName + ", streamType="
                + streamType + ", message=" + message + ", level=" + level
                + ", metricName=" + metricName + ", content=" + content
                + ", unit=" + unit + ", units=" + units + "]";
    }

}
