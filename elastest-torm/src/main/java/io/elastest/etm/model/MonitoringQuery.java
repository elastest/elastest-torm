package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;

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

    @JsonProperty("selectedTerms")
    List<String> selectedTerms;

    @JsonProperty("message")
    String message;

    public MonitoringQuery() {
        this.indices = new ArrayList<>();
        this.selectedTerms = new ArrayList<>();
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

    public List<String> getSelectedTerms() {
        return selectedTerms;
    }

    public void setSelectedTerms(List<String> selectedTerms) {
        this.selectedTerms = selectedTerms;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TermQueryBuilder getAttributeTermByGivenName(String attrName) {
        switch (attrName) {
        case "component":
            return QueryBuilders.termQuery("component", component);

        case "componentService":
            return QueryBuilders.termQuery("componentService",
                    componentService);

        case "etType":
            return QueryBuilders.termQuery("et_type", etType);

        case "timestamp":
            return QueryBuilders.termQuery("@timestamp", timestamp);

        case "stream":
            return QueryBuilders.termQuery("stream", stream);

        case "containerName":
            return QueryBuilders.termQuery("containerName", containerName);

        case "streamType":
            return QueryBuilders.termQuery("stream_type", streamType);

        case "message":
            return QueryBuilders.termQuery("message", message);

        default:
            return null;
        }

    }

    public Object getAttributeValueByGivenName(String attrName) {
        switch (attrName) {
        case "component":
            return component;

        case "componentService":
            return componentService;

        case "etType":
            return etType;

        case "timestamp":
            return timestamp;

        case "stream":
            return stream;

        case "containerName":
            return containerName;

        case "streamType":
            return streamType;

        case "message":
            return message;

        default:
            return null;
        }
    }
}
