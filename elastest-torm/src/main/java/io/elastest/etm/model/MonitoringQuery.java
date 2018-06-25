package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.elastest.etm.model.Enums.StreamType;

public class MonitoringQuery {
    @JsonProperty("indices")
    List<String> indices;

    @JsonProperty("component")
    String component;

    @JsonProperty("componentService")
    String componentService;

    @JsonProperty("etType")
    String etType;

    @JsonProperty("timestamp")
    String timestamp;

    @JsonProperty("stream")
    String stream;

    @JsonProperty("streamType")
    StreamType streamType;

    @JsonProperty("containerName")
    String containerName;

    public MonitoringQuery() {
        this.indices = new ArrayList<>();
    }

    public List<String> getIndices() {
        return indices;
    }

    public String[] getIndicesAsArray() {
        return indices.toArray(new String[indices.size()]);
    }

    public void setIndices(List<String> indices) {
        this.indices = indices;
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

}
