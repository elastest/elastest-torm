package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.querydsl.core.types.Path;

import io.elastest.etm.model.Enums.StreamType;

public class MonitoringQuery {
    @JsonProperty("indices")
    List<String> indices;

    @JsonProperty("component")
    String component;

    @JsonProperty("components")
    List<String> components;

    @JsonProperty("componentService")
    String componentService;

    @JsonProperty("etType")
    String etType;

    @JsonProperty("timestamp")
    String timestamp;

    @JsonProperty("stream")
    String stream;

    @JsonProperty("streams")
    List<String> streams;

    @JsonProperty("streamType")
    StreamType streamType;

    @JsonProperty("containerName")
    String containerName;

    @JsonProperty("selectedTerms")
    List<String> selectedTerms;

    @JsonProperty("message")
    String message;

    @JsonProperty("rawData")
    StreamType rawData;

    @JsonProperty("timeRange")
    TimeRange timeRange;

    // Optional
    @JsonProperty("execsIds")
    List<Long> execsIds;

    public MonitoringQuery() {
        this.indices = new ArrayList<>();
        this.selectedTerms = new ArrayList<>();
        this.execsIds = new ArrayList<>();
    }

    public MonitoringQuery(MonitoringQuery monitoringQuery) {
        this.indices = monitoringQuery.indices != null
                ? new ArrayList<>(monitoringQuery.indices)
                : new ArrayList<>();

        this.component = monitoringQuery.component;
        this.components = monitoringQuery.components != null
                ? new ArrayList<>(monitoringQuery.components)
                : new ArrayList<>();
        this.componentService = monitoringQuery.componentService;
        this.etType = monitoringQuery.etType;
        this.timestamp = monitoringQuery.timestamp;
        this.stream = monitoringQuery.stream;
        this.streams = monitoringQuery.streams != null
                ? new ArrayList<>(monitoringQuery.streams)
                : new ArrayList<>();
        this.streamType = monitoringQuery.streamType;
        this.containerName = monitoringQuery.containerName;
        this.selectedTerms = monitoringQuery.selectedTerms;
        this.message = monitoringQuery.message;
        this.rawData = monitoringQuery.rawData;
        this.timeRange = new TimeRange(monitoringQuery.timeRange);

        this.execsIds = monitoringQuery.execsIds != null
                ? new ArrayList<>(monitoringQuery.execsIds)
                : new ArrayList<>();

    }

    public List<String> getIndices() {
        return indices;
    }

    public boolean isPairOfIndices() {
        return getIndices() != null && getIndices().size() == 2;
    }

    // Indices can be comma separated
    // ["1068,s61_e425", "1067,s61_e425"]
    public List<String> getIndicesSplitted() {
        try {
            List<String> indices = new ArrayList<>();
            if (getIndices() != null) {
                for (String index : getIndices()) {
                    indices.addAll(Arrays.asList(index.split(",")));
                }
            }
            return indices;
        } catch (Exception e) {
            return getIndices();
        }
    }

    public String[] getIndicesAsArray() {
        return indices.toArray(new String[indices.size()]);
    }

    public String getIndicesAsString() {
        return String.join(",", indices);
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

    public List<String> getComponents() {
        return components;
    }

    public void setComponents(List<String> components) {
        this.components = components;
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

    public List<String> getStreams() {
        return streams;
    }

    public void setStreams(List<String> streams) {
        this.streams = streams;
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

    public StreamType getRawData() {
        return rawData;
    }

    public void setRawData(StreamType rawData) {
        this.rawData = rawData;
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(TimeRange timeRange) {
        this.timeRange = timeRange;
    }

    public List<Long> getExecsIds() {
        return execsIds;
    }

    public void setExecsIds(List<Long> execsIds) {
        this.execsIds = execsIds;
    }

    // For Elasticsearch
    public TermQueryBuilder getAttributeTermByGivenName(String attrName) {
        switch (attrName) {

        case "indices":
            return QueryBuilders.termQuery("indices", indices);

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

        case "rawData":
            return QueryBuilders.termQuery("raw_data", rawData);

        default:
            return null;
        }

    }

    public Path<?> getAttributeBooleanExpressionByGivenName(String attrName) {
        switch (attrName) {
        case "indices":
        case "exec":
            return QTrace.trace.exec;
        case "component":
            return QTrace.trace.component;

        case "componentService":
            return QTrace.trace.componentService;

        case "etType":
        case "et_type":
            return QTrace.trace.etType;

        case "timestamp":
            return QTrace.trace.timestamp;

        case "stream":
            return QTrace.trace.stream;

        case "containerName":
            return QTrace.trace.containerName;

        case "streamType":
        case "stream_type":
            return QTrace.trace.streamType;

        case "message":
            return QTrace.trace.message;

        case "level":
            return QTrace.trace.level;

        case "metricName":
            return QTrace.trace.metricName;

        case "content":
            return QTrace.trace.content;

        case "unit":
            return QTrace.trace.unit;

        case "units":
            return QTrace.trace.units;

        case "rawData":
        case "raw_data":
            return QTrace.trace.rawData;

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

        case "rawData":
            return rawData;

        case "timeRange":
            return timeRange;

        default:
            return null;
        }
    }
}
