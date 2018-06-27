package io.elastest.etm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LogAnalyzerQuery {

    @JsonProperty("indices")
    List<String> indices;

    @JsonProperty("componentsStreams")
    List<AggregationTree> componentsStreams;

    @JsonProperty("levels")
    List<String> levels;

    @JsonProperty("size")
    int size;

    @JsonProperty("rangeLT")
    String rangeLT;

    @JsonProperty("rangeGT")
    String rangeGT;

    @JsonProperty("rangeLTE")
    String rangeLTE;

    @JsonProperty("rangeGTE")
    String rangeGTE;

    @JsonProperty("matchMessage")
    String matchMessage;

    @JsonProperty("searchAfterTrace")
    Map<String, Object> searchAfterTrace;

    public LogAnalyzerQuery() {
        this.indices = new ArrayList<>();
        this.searchAfterTrace = new HashMap<>();
    }

    public List<String> getIndices() {
        return indices;
    }

    public void setIndices(List<String> indices) {
        this.indices = indices;
    }

    public String[] getIndicesAsArray() {
        return indices.toArray(new String[indices.size()]);
    }

    public List<AggregationTree> getComponentsStreams() {
        return componentsStreams;
    }

    public void setComponentsStreams(List<AggregationTree> componentsStreams) {
        this.componentsStreams = componentsStreams;
    }

    public List<String> getLevels() {
        return levels;
    }

    public void setLevels(List<String> levels) {
        this.levels = levels;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getRangeLT() {
        return rangeLT;
    }

    public void setRangeLT(String rangeLT) {
        this.rangeLT = rangeLT;
    }

    public String getRangeGT() {
        return rangeGT;
    }

    public void setRangeGT(String rangeGT) {
        this.rangeGT = rangeGT;
    }

    public String getRangeLTE() {
        return rangeLTE;
    }

    public void setRangeLTE(String rangeLTE) {
        this.rangeLTE = rangeLTE;
    }

    public String getRangeGTE() {
        return rangeGTE;
    }

    public void setRangeGTE(String rangeGTE) {
        this.rangeGTE = rangeGTE;
    }

    public String getMatchMessage() {
        return matchMessage;
    }

    public void setMatchMessage(String matchMessage) {
        this.matchMessage = matchMessage;
    }

    public Map<String, Object> getSearchAfterTrace() {
        return searchAfterTrace;
    }

    public void setSearchAfterTrace(Map<String, Object> searchAfterTrace) {
        this.searchAfterTrace = searchAfterTrace;
    }

}
